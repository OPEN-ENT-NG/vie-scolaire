package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.core.enums.TrombinoscopeError;
import fr.openent.viescolaire.db.DBService;
import fr.openent.viescolaire.helper.FutureHelper;
import fr.openent.viescolaire.helper.UserHelper;
import fr.openent.viescolaire.model.Person.Person;
import fr.openent.viescolaire.model.Person.Student;
import fr.openent.viescolaire.model.Trombinoscope.ReportException;
import fr.openent.viescolaire.model.Trombinoscope.TrombinoscopeReport;
import fr.openent.viescolaire.service.EleveService;
import fr.openent.viescolaire.service.TrombinoscopeFailureService;
import fr.openent.viescolaire.service.TrombinoscopeService;
import fr.openent.viescolaire.utils.FileHelper;
import fr.wseduc.webutils.I18n;
import io.vertx.core.*;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import org.entcore.common.sql.SqlResult;
import org.entcore.common.storage.Storage;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

import static fr.wseduc.webutils.http.Renders.getHost;

public class DefaultTrombinoscopeService extends DBService implements TrombinoscopeService {

    private static final Logger log = LoggerFactory.getLogger(DefaultTrombinoscopeService.class);

    private final FileSystem fileSystem;
    private final Storage storage;
    private final TrombinoscopeFailureService failureService;
    private final EleveService studentService;

    private static final String[] EXTENSIONS_ALLOWED = {".jpg", ".JPG", ".png", ".PNG", ".jpeg", ".JPEG", ".bmp", ".BMP"};

    public DefaultTrombinoscopeService(FileSystem fileSystem, Storage storage, TrombinoscopeFailureService failureService) {
        this.fileSystem = fileSystem;
        this.storage = storage;
        this.failureService = failureService;
        this.studentService = new DefaultEleveService();
    }

    @Override
    public void getSetting(String structureId, Handler<AsyncResult<Boolean>> handler) {
        String query = " SELECT * FROM " + Viescolaire.VSCO_SCHEMA + ".trombinoscope_settings WHERE structure_id = ?";

        JsonArray params = new JsonArray().add(structureId);

        sql.prepared(query, params, SqlResult.validUniqueResultHandler(result -> {
            if (result.isLeft()) {
                String messageError = "[Viescolaire@DefaultTrombinoscopeService::getSetting] Failed to get trombinoscope setting";
                log.error(messageError, result.left().getValue());
                handler.handle(Future.failedFuture(result.left().getValue()));
                return;
            }
            handler.handle(Future.succeededFuture(result.right().getValue().getBoolean("active", false)));
        }));
    }

    @Override
    public void setSetting(String structureId, Boolean active, Handler<AsyncResult<JsonObject>> handler) {
        String query = " INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".trombinoscope_settings " +
                " (structure_id, active) " +
                " VALUES (?, ?) " +
                " ON CONFLICT (structure_id) DO UPDATE " +
                " SET active = ?";

        JsonArray params = new JsonArray().add(structureId).add(active).add(active);

        sql.prepared(query, params, SqlResult.validUniqueResultHandler(result -> {
            if (result.isLeft()) {
                String messageError = "[Viescolaire@DefaultTrombinoscopeService::setSetting] Failed to toggle trombinoscope setting";
                log.error(messageError, result.left().getValue());
                handler.handle(Future.failedFuture(result.left().getValue()));
                return;
            }
            handler.handle(Future.succeededFuture(result.right().getValue()));
        }));
    }

    @Override
    public void get(String structureId, String studentId, final Handler<AsyncResult<JsonObject>> handler) {
        String query = " SELECT * FROM " + Viescolaire.VSCO_SCHEMA + ".trombinoscope "
                + " WHERE structure_id = ? AND student_id = ?";

        JsonArray params = new JsonArray()
                .add(structureId)
                .add(studentId);

        sql.prepared(query, params, SqlResult.validUniqueResultHandler(result -> {
            if (result.isLeft()) {
                String messageError = "[Viescolaire@DefaultTrombinoscopeFailureService::getFailures] Failed to get trombinoscope "
                        + "of student " + studentId + " from structure " + structureId + ".";
                log.error(messageError, result.left().getValue());
                handler.handle(Future.failedFuture(messageError));
                return;
            }
            handler.handle(Future.succeededFuture(result.right().getValue()));
        }));
    }

    @Override
    public void process(String structureId, String path, TrombinoscopeReport report, final HttpServerRequest request,
                        Handler<AsyncResult<Void>> handler) {

        fetchDirectoriesContentName(path, report, request, handler)
                .compose((directoriesAudienceNamesResult) -> getStudentsByFetchedAudiences(structureId, directoriesAudienceNamesResult, path, report, request))
                .compose((audienceStudentMap) -> startImportTrombinoscope(structureId, audienceStudentMap, path, report, request, handler))
                .setHandler(ar -> {
                    if (ar.failed()) {
                        log.error("[Viescolaire@DefaultTrombinoscopeService::process] An error has occured during processus." +
                                " See previous logs", ar.cause());
                        handler.handle(Future.failedFuture(ar.cause()));
                    } else {
                        report.end();
                        handler.handle(Future.succeededFuture());
                    }
                });
    }

    private Future<List<String>> fetchDirectoriesContentName(String path, TrombinoscopeReport report, final HttpServerRequest request,
                                                             Handler<AsyncResult<Void>> requestHandler) {
        Future<List<String>> future = Future.future();

        fileSystem.readDir(path, audiencesPath -> {
            if (audiencesPath.failed()) {
                log.error("[Viescolaire@DefaultTrombinoscopeService::fetchDirectoriesContentName] " +
                        "An error has occured while reading the whole directory", audiencesPath.cause());
                String message = getTranslated(TrombinoscopeError.DIRECTORY_READ_FAILURE.key(), request);
                report.addReport(new ReportException(message, FileHelper.getAbsolutePath(path)));
                future.fail(audiencesPath.cause());
            } else {
                List<String> audienceDirectories = FileHelper.readDirectoryHandler(path, audiencesPath, requestHandler);
                if (audienceDirectories == null) {
                    log.error("[Viescolaire@DefaultTrombinoscopeService::fetchDirectoriesContentName] Read directory but contain no folder");
                    String message = getTranslated(TrombinoscopeError.DIRECTORY_READ_FAILURE.key(), request);
                    report.addReport(new ReportException(message, FileHelper.getAbsolutePath(path)));
                    future.fail(message);
                } else {
                    future.complete(getContentNames(audienceDirectories));
                }
            }
        });
        return future;
    }

    private Future<HashMap<String, List<Student>>> getStudentsByFetchedAudiences(String structureId, List<String> audienceNames,
                                                                                 String path, TrombinoscopeReport report, final HttpServerRequest request) {
        Future<HashMap<String, List<Student>>> future = Future.future();

        studentService.getStudentsFromStructure(structureId, null, null, audienceNames, false, audiencesStudentsResult -> {
            if (audiencesStudentsResult.isLeft()) {
                log.error("[Viescolaire@DefaultTrombinoscopeService::fetchDirectoriesContentName] " +
                        "An error has occured while reading the whole directory", audiencesStudentsResult.left().getValue());
                String message = getTranslated(TrombinoscopeError.FETCH_STUDENT_BY_NAME_FAILURE.key(), request);
                report.addReport(new ReportException(message, FileHelper.getAbsolutePath(path), audienceNames, null));
                future.fail(audiencesStudentsResult.left().getValue());
            } else {
                List<Student> students = UserHelper.toStudentList(audiencesStudentsResult.right().getValue());
                HashMap<String, List<Student>> audienceHashMap = new HashMap<>();

                for (Student student : students) {
                    if (!audienceHashMap.containsKey(student.getAudienceName())) {
                        audienceHashMap.put(student.getAudienceName(), new ArrayList<>());
                    }
                    audienceHashMap.get(student.getAudienceName()).add(student);
                }

                for (String name : audienceNames) {
                    if (!audienceHashMap.containsKey(name)) {
                        String message = getTranslated(TrombinoscopeError.RETRIEVE_LINKED_STUDENT_AUDIENCE_FAILURE.key(), request);
                        report.addReport(new ReportException(message, FileHelper.getAbsolutePath(path), Collections.singletonList(name), null));
                    }
                }

                future.complete(audienceHashMap);

            }
        });
        return future;
    }

    private Future<Void> startImportTrombinoscope(String structureId, HashMap<String, List<Student>> audienceStudentMap, String path,
                                                  TrombinoscopeReport report, final HttpServerRequest request, Handler<AsyncResult<Void>> handler) {
        Future<Void> processStudentTrombinoscopeFuture = Future.future();

        List<Future<Void>> processImportTrombinoscopeFutures = new ArrayList<>();

        for (String audienceName : audienceStudentMap.keySet()) {
            processImportTrombinoscopeFutures.add(processImportTrombinoscope(structureId, audienceStudentMap, path, report, request, handler, audienceName));
        }

        FutureHelper.join(processImportTrombinoscopeFutures).setHandler(event -> {
            if (event.failed()) {
                log.error("[Viescolaire@DefaultTrombinoscopeService::startImportTrombinoscope]" +
                        " Some audience folder import trombinoscope failed during their processes", event.cause());
                processStudentTrombinoscopeFuture.fail(event.cause());
            } else {
                processStudentTrombinoscopeFuture.complete();
            }
        });

        return processStudentTrombinoscopeFuture;
    }

    private Future<Void> processImportTrombinoscope(String structureId, HashMap<String, List<Student>> audienceStudentMap,
                                                    String path, TrombinoscopeReport report, final HttpServerRequest request, Handler<AsyncResult<Void>> handler, String audienceName) {
        String audiencePath = path + "/" + audienceName;
        List<Student> students = getStudentsFromAudienceNames(audiencePath, audienceName, report, request, audienceStudentMap);
        Future<Void> future = Future.future();

        if (students == null) {
            future.handle(Future.failedFuture("[Viescolaire@DefaultTrombinoscopeService::processImportTrombinoscope]: " +
                    " No Students fetched for this audience"));
        } else {
            getPicturesNamesFromAudienceDirectories(audiencePath, handler, pictureNamesResult -> {
                if (pictureNamesResult.failed()) {
                    String message = getTranslated(TrombinoscopeError.RETRIEVE_LINKED_STUDENT_AUDIENCE_FAILURE.key(), request);
                    report.addReport(new ReportException(message, FileHelper.getAbsolutePath(path), Collections.singletonList(audienceName), null));
                    future.handle(Future.failedFuture(pictureNamesResult.cause()));
                    return;
                }
                List<String> pictureNames = pictureNamesResult.result();
                List<Future<JsonObject>> picturesFuture = new ArrayList<>();

                for (String pictureName : pictureNames) {
                    String picturePath = audiencePath + '/' + pictureName;
                    String extension = getAllowedExtension(pictureName);
                    Future<JsonObject> pictureFuture = Future.future();
                    picturesFuture.add(pictureFuture);
                    if (extension == null) {
                        String message = getTranslated(TrombinoscopeError.EXTENSION_FILE_FAILURE.key(), request);
                        report.addReport(new ReportException(message, FileHelper.getAbsolutePath(picturePath)));
                        pictureFuture.fail("[Viescolaire@DefaultTrombinoscopeService::processImportTrombinoscope] extension fetched was null");
                    } else {
                        saveTrombinoscope(structureId, picturePath, pictureName, extension, students, report, request,
                                FutureHelper.futureJsonObject(pictureFuture));
                    }
                }
                FutureHelper.join(picturesFuture).setHandler(ar -> {
                    if (ar.failed()) {
                        future.handle(Future.failedFuture(ar.cause()));
                    } else {
                        future.handle(Future.succeededFuture());
                    }
                });
            });
        }

        return future;
    }

    private List<Student> getStudentsFromAudienceNames(String audiencePath, String audienceName,
                                                       TrombinoscopeReport report, final HttpServerRequest request,
                                                       HashMap<String, List<Student>> audienceStudentMap) {
        List<Student> students = audienceStudentMap.getOrDefault(audienceName, new ArrayList<>());
        if (students.isEmpty()) {
            log.error("[Viescolaire@DefaultTrombinoscopeService::getStudentsFromAudienceNames] Failed to retrieve students from directory audience name");
            String message = getTranslated(TrombinoscopeError.RETRIEVE_LINKED_STUDENT_AUDIENCE_FAILURE.key(), request);
            report.addReport(new ReportException(message, FileHelper.getAbsolutePath(audiencePath), Collections.singletonList(audienceName), null));
            return null;
        }
        return students;
    }

    private void getPicturesNamesFromAudienceDirectories(String audiencePath, Handler<AsyncResult<Void>> requestHandler,
                                                         Handler<AsyncResult<List<String>>> handler) {
        fileSystem.readDir(audiencePath, picturesPath -> {
            List<String> pictureFiles = FileHelper.readDirectoryHandler(audiencePath, picturesPath, requestHandler);
            if (pictureFiles == null) {
                handler.handle(Future.failedFuture(picturesPath.cause()));
                return;
            }

            handler.handle(Future.succeededFuture(getContentNames(pictureFiles)));
        });
    }

    private String getAllowedExtension(String pictureName) {
        return Arrays.stream(EXTENSIONS_ALLOWED)
                .filter(pictureName::endsWith)
                .findFirst()
                .orElse(null);
    }

    private void saveTrombinoscope(String structureId, String picturePath, String pictureName, String extension,
                                   List<Student> students, TrombinoscopeReport report, final HttpServerRequest request,
                                   Handler<AsyncResult<JsonObject>> handler) {

        final String fileId = UUID.randomUUID().toString();
        storage.writeFsFile(fileId, picturePath, file -> {
            if (!"ok".equals(file.getString("status"))) {
                String message = getTranslated(TrombinoscopeError.WRITING_FILE_FAILURE.key(), request);
                report.addReport(new ReportException(message, FileHelper.getAbsolutePath(picturePath)));
                return;
            }

            Student student = findStudentFromFileName(picturePath, pictureName.replace(extension, ""),
                    students, report, request);

            // if student is not found, we save this data into trombinoscope failure hoping we will find its matching student
            if (student == null) {
                failureService.create(structureId, FileHelper.getAbsolutePath(picturePath), fileId, handler);
                return;
            }

            // if student is found, we create its trombinoscope
            create(structureId, student.getId(), fileId, result -> {
                if (result.failed()) {
                    String message = getTranslated(TrombinoscopeError.TROMBINOSCOPE_CREATE_FAILURE.key(), request);
                    report.addReport(new ReportException(message, FileHelper.getAbsolutePath(picturePath), null, Collections.singletonList(student.getName())));
                    return;
                }
                handler.handle(Future.succeededFuture(new JsonObject().put("status", "ok")));
            });
        });
    }

    private String getTranslated(String i18n, final HttpServerRequest request) {
        return I18n.getInstance().translate(i18n, getHost(request), I18n.acceptLanguage(request));
    }

    private List<String> getContentNames(List<String> directories) {
        return directories.stream().map(path -> {
            String[] pathTable = path.split("/");
            return pathTable[pathTable.length - 1];
        }).collect(Collectors.toList());
    }

    private Student findStudentFromFileName(String picturePath,
                                            String pictureName, List<Student> students, TrombinoscopeReport report,
                                            final HttpServerRequest request) {

        String[] pictureNamePart = splitName(pictureName);

        // if we don't fetch correct picture name format (firstName and lastName ONLY) in order to have 2 elements
        // return NULL
        if (pictureNamePart.length < 2) {
            String message = getTranslated(TrombinoscopeError.FILE_FORMAT_FAILURE.key(), request);
            report.addReport(new ReportException(message, FileHelper.getAbsolutePath(picturePath)));
            return null;
        }

        // Here, key correspond to firstName and value correspond to lastName
        Map<String, String> firstAndLastNames = new HashMap<>();

        for (int i = 1; i < pictureNamePart.length; i++) {
            String[] lastNamePart = Arrays.copyOfRange(pictureNamePart, 0, i);
            String[] firstNamePart = Arrays.copyOfRange(pictureNamePart, i, pictureNamePart.length);

            String lastName = String.join(" ", lastNamePart);
            String firstName = String.join(" ", firstNamePart);

            firstAndLastNames.put(formatStudentName(firstName), formatStudentName(lastName));
        }

        List<Student> studentsMatchingFileName = students.stream().filter(student -> {
            String studentFirstName = formatStudentName(student.getFirstName());
            String studentLastName = formatStudentName(student.getLastName());

            return firstAndLastNames.entrySet().stream().anyMatch((name) ->
                    name.getKey().equals(studentFirstName) && name.getValue().equals(studentLastName)
            );

        }).collect(Collectors.toList());

        if (studentsMatchingFileName.size() < 1) {
            String message = getTranslated(TrombinoscopeError.MATCH_STUDENT_FAILURE.key(), request);
            report.addReport(new ReportException(message, FileHelper.getAbsolutePath(picturePath)));
            return null;
        } else if (studentsMatchingFileName.size() > 1) {
            String message = getTranslated(TrombinoscopeError.TOO_MANY_STUDENTS_FOUND_FAILURE.key(), request);
            List<String> names = studentsMatchingFileName.stream().map(Person::getName).collect(Collectors.toList());
            report.addReport(new ReportException(message, FileHelper.getAbsolutePath(picturePath), null, names));
            return null;
        }

        return studentsMatchingFileName.get(0);
    }

    private String[] splitName(String pictureName) {
        String[] pictureNamePart = pictureName.split("\\.");

        if (pictureNamePart.length == 1) {
            pictureNamePart = pictureName.split(" ");
        }
        return pictureNamePart;
    }

    private String formatStudentName(String name) {
        return Normalizer
                .normalize(name, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("-", "")
                .toLowerCase()
                .trim();
    }

    @Override
    public void create(String structureId, String studentId, String pictureId, Handler<AsyncResult<JsonObject>> handler) {
        get(structureId, studentId, existsEither -> {
            if (existsEither.failed()) {
                handler.handle(Future.failedFuture(existsEither.cause()));
            } else {
                JsonObject existingTrombinoscope = existsEither.result();
                saveTrombinoscope(structureId, studentId, pictureId, save -> {
                    if (save.failed()) {
                        handler.handle(Future.failedFuture(save.cause()));
                        return;
                    }
                    removePictureFile(existingTrombinoscope, handler);
                });
            }
        });
    }

    private void removePictureFile(JsonObject existingTrombinoscope, Handler<AsyncResult<JsonObject>> handler) {
        if (!existingTrombinoscope.isEmpty() && !existingTrombinoscope.fieldNames().isEmpty()) {
            List<String> pictureIds = new ArrayList<>(Collections.singletonList(existingTrombinoscope.getString("picture_id")));
            FileHelper.removeFiles(storage, pictureIds, handler);
        } else {
            handler.handle(Future.succeededFuture());
        }
    }

    private void saveTrombinoscope(String structureId, String studentId, String pictureId, Handler<AsyncResult<JsonObject>> handler) {
        String query = " INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".trombinoscope " +
                " (structure_id, student_id, picture_id) " +
                " VALUES (?, ?, ?) " +
                " ON CONFLICT (structure_id, student_id) DO UPDATE " +
                " SET picture_id = ?";

        JsonArray params = new JsonArray()
                .add(structureId)
                .add(studentId)
                .add(pictureId)
                .add(pictureId);

        sql.prepared(query, params, SqlResult.validUniqueResultHandler(result -> {
            if (result.isLeft()) {
                String message = "[Viescolaire@DefaultTrombinoscopeService::create] Failed to create trombinoscope: ";
                log.error(message, result.left().getValue());
                handler.handle(Future.failedFuture(message));
                return;
            }
            handler.handle(Future.succeededFuture(result.right().getValue()));
        }));
    }

    /**
     * delete picture from a studentId
     *
     * @param structureId   Structure Identifier {@link String}
     * @param studentId     Student identifier {@link String}
     * @return Future       {@link Future} of {@link JsonObject} completed or failure
     */
    @Override
    public Future<JsonObject> deletePicture(String structureId, String studentId) {
        Promise<JsonObject> promise = Promise.promise();

        get(structureId, studentId, existsEither -> {
            if (existsEither.failed()) {
                promise.fail(existsEither.cause());
            } else {
                JsonObject existingTrombinoscope = existsEither.result();
                proceedOnDeleteTrombinosope(structureId, studentId, existingTrombinoscope, promise);
            }
        });

        return promise.future();
    }

    /**
     * delete picture from a studentId
     *
     * @param structureId               Structure Identifier {@link String}
     * @param studentId                 Student identifier {@link String}
     * @param existingTrombinoscope     existingTrombinoscope Object {@link JsonObject}
     * @param promise                   promise to handle {@link Promise<JsonObject>}
     */
    private void proceedOnDeleteTrombinosope(String structureId, String studentId, JsonObject existingTrombinoscope,
                                             Promise<JsonObject> promise) {
        if (existingTrombinoscope.getString("picture_id") != null) {
            FileHelper.exist(storage, existingTrombinoscope.getString("picture_id"), existAsync -> {
                // if no exist, directly delete the student's trombinoscope
                if (Boolean.FALSE.equals(existAsync.result())) {
                    deleteTrombinoscope(structureId, studentId).onSuccess(promise::complete).onFailure(promise::fail);
                } else {
                    // if exists, delete file from storage and then delete the student's trombinoscope
                    removePictureFile(existingTrombinoscope, removeAsync -> {
                        if (removeAsync.failed()) {
                            promise.fail(removeAsync.cause());
                        } else {
                            deleteTrombinoscope(structureId, studentId).onSuccess(promise::complete).onFailure(promise::fail);                                }
                    });
                }
            });
        } else {
            // delete the student's trombinoscope since no picture id is found
            deleteTrombinoscope(structureId, studentId).onSuccess(promise::complete).onFailure(promise::fail);
        }
    }

    /**
     * delete trombinoscope by its student info
     *
     * @param structureId   Trombinoscope's structure Identifier {@link String}
     * @param studentId     Trombinoscope's student identifier {@link String}
     *
     * @return Future       {@link Future} of {@link JsonObject} completed or failure
     */
    private Future<JsonObject> deleteTrombinoscope(String structureId, String studentId) {
        Promise<JsonObject> promise = Promise.promise();

        String query = "DELETE FROM " + Viescolaire.VSCO_SCHEMA + ".trombinoscope " +
                " WHERE structure_id = ? AND student_id = ? ";

        JsonArray params = new JsonArray()
                .add(structureId)
                .add(studentId);

        sql.prepared(query, params, SqlResult.validUniqueResultHandler(result -> {
            if (result.isLeft()) {
                String message = "[Viescolaire@DefaultTrombinoscopeService::deleteTrombinoscope] Failed to delete trombinoscope: ";
                log.error(message, result.left().getValue());
                promise.fail(result.left().getValue());
                return;
            }
            promise.complete(result.right().getValue());
        }));

        return promise.future();
    }
}
