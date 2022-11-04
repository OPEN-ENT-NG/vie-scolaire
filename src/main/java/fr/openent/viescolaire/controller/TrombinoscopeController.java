package fr.openent.viescolaire.controller;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.core.constants.Actions;
import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.core.enums.TrombinoscopeError;
import fr.openent.viescolaire.model.Structure;
import fr.openent.viescolaire.model.Trombinoscope.TrombinoscopeFailure;
import fr.openent.viescolaire.model.Trombinoscope.TrombinoscopeReport;
import fr.openent.viescolaire.security.Trombinoscope.AccessTrombinoscope;
import fr.openent.viescolaire.security.Trombinoscope.ManageTrombinoscope;
import fr.openent.viescolaire.service.TrombinoscopeFailureService;
import fr.openent.viescolaire.service.TrombinoscopeReportService;
import fr.openent.viescolaire.service.TrombinoscopeService;
import fr.openent.viescolaire.service.impl.DefaultTrombinoscopeFailureService;
import fr.openent.viescolaire.service.impl.DefaultTrombinoscopeReportService;
import fr.openent.viescolaire.service.impl.DefaultTrombinoscopeService;
import fr.openent.viescolaire.service.impl.StructureService;
import fr.openent.viescolaire.utils.FileHelper;
import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.I18n;
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.request.RequestUtils;
import io.vertx.core.*;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.http.filter.Trace;
import org.entcore.common.http.response.DefaultResponseHandler;
import org.entcore.common.storage.Storage;

import java.io.File;
import java.util.Map;
import java.util.UUID;

/**
 * Vert.x backend controller for the application using Mongodb.
 */
public class TrombinoscopeController extends ControllerHelper {

    private static final Logger log = LoggerFactory.getLogger(TrombinoscopeController.class);

    private final StructureService structureService = new StructureService();
    private final Storage storage;
    private final Vertx vertx;
    private final TrombinoscopeFailureService failureService;
    private final TrombinoscopeService trombinoscopeService;
    private final TrombinoscopeReportService reportService;
    private final Map<String, String> skins;
    private static final String ASSET_THEME = "/assets/themes/";
    private static final String IMG_ILLUSTRATION = "img/illustrations";
    private static final String NO_AVATAR = "no-avatar.svg";

    public TrombinoscopeController(Vertx vertx, Storage storage) {
        this.vertx = vertx;
        this.storage = storage;
        this.failureService = new DefaultTrombinoscopeFailureService(storage);
        this.trombinoscopeService = new DefaultTrombinoscopeService(vertx.fileSystem(), storage, failureService);
        this.reportService = new DefaultTrombinoscopeReportService();
        this.skins = vertx.sharedData().getLocalMap("skins");
    }

    @SecuredAction(value = Viescolaire.MANAGE_TROMBINOSCOPE)
    public void manageTrombinoscope(final HttpServerRequest request) {
    }

    /* SETTINGS */

    @Get("/structures/:structureId/trombinoscope/setting")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(ManageTrombinoscope.class)
    @ApiDoc("Determine structure trombinoscope if active or disable")
    public void getTrombinoscopeSetting(HttpServerRequest request) {
        String structureId = request.getParam(Field.STRUCTUREID);
        trombinoscopeService.getSetting(structureId, settingAsync -> {
            if (settingAsync.failed()) {
                renderError(request, new JsonObject().put(Field.ERROR, settingAsync.cause().getMessage()));
            } else {
                renderJson(request, new JsonObject().put(Field.ACTIVE,  settingAsync.result()));
            }
        });
    }

    @Post("/structures/:structureId/trombinoscope/setting")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(ManageTrombinoscope.class)
    @Trace(value = Actions.VIESCOLAIRE_SETTINGS_MANAGE)
    @ApiDoc("Toggle structure trombinoscope settings enable/disable")
    public void setTrombinoscopeSetting(final HttpServerRequest request) {
        String structureId = request.getParam(Field.STRUCTUREID);
        RequestUtils.bodyToJson(request, pathPrefix + "trombinoscope_setting", activeJson -> {
            Boolean isActive = activeJson.getBoolean(Field.ACTIVE, false);
            trombinoscopeService.setSetting(structureId, isActive, DefaultResponseHandler.asyncDefaultResponseHandler(request));
        });
    }

    /* TROMBINOSCOPE */

    @Get("/structures/:structureId/students/:studentId/picture")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AccessTrombinoscope.class)
    @ApiDoc("Retrieve structure trombinoscope or avatar picture")
    public void getTrombinoscopePicture(HttpServerRequest request) {
        String structureId = request.getParam(Field.STRUCTUREID);
        String studentId = request.getParam(Field.STUDENTID);

        String skin = this.skins.get(Renders.getHost(request));

        trombinoscopeService.getSetting(structureId, isActive -> {
            if (isActive.result().equals(true)) {
                trombinoscopeService.get(structureId, studentId, resultFile -> {
                    if (resultFile.failed()) {
                        notFound(request);
                        return;
                    }
                    JsonObject result = resultFile.result();

                    if (result.getString(Field.PICTURE_ID) != null) {
                        FileHelper.exist(storage, result.getString(Field.PICTURE_ID), existAsync -> {
                            // send default picture no avatar if no file found
                            if (Boolean.FALSE.equals(existAsync.result())) {
                                redirect(request, ASSET_THEME + skin + "/" + IMG_ILLUSTRATION + "/" + NO_AVATAR);
                            } else {
                                storage.sendFile(result.getString(Field.PICTURE_ID), null, request, true, new JsonObject());
                            }
                        });
                    } else {
                        // send default picture no avatar if no picture_id or field found on the JsonObject
                        redirect(request, ASSET_THEME + skin + "/" + IMG_ILLUSTRATION + "/" + NO_AVATAR);
                    }
                });
            } else {
                redirect(request, "/userbook/avatar/" + studentId);
            }
        });
    }

    @Post("/structures/:structureId/trombinoscope")
    @ResourceFilter(ManageTrombinoscope.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @Trace(value = Actions.VIESCOLAIRE_IMPORT_CREATION, body = false)
    @ApiDoc("Import trombinoscope for classes in structure")
    public void importTrombinoscope(final HttpServerRequest request) {
        String structureId = request.getParam(Field.STRUCTUREID);
        final String importId = UUID.randomUUID().toString();
        final String path = config.getString("import-folder", "/tmp") + File.separator + importId;
        FileHelper fileHelper = new FileHelper(vertx, path);
        TrombinoscopeReport report = new TrombinoscopeReport(vertx, "fr");
        request.pause();
        report.start();
        fetchStructureInfo(structureId, report, request)
                .compose((structure) -> clearOldFailures(structure, report, request))
                .compose((clearOldFailureResult) -> uploadFile(request, report, fileHelper))
                .compose((zipFilename) -> unzipFile(zipFilename, report, request))
                .compose((uploadPathFromUnzip) -> processImportTrombinoscope(request, structureId, report, uploadPathFromUnzip))
                .setHandler(ar -> {
                    if (ar.failed()) {
                        log.error("[Viescolaire@TrombinoscopeController::importTrombinoscope] Importing trombinoscope failed." +
                                " See previous logs", ar.cause());
                        report.generate(rep -> {
                            if (rep.failed()) {
                                renderError(request, new JsonObject().put(Field.ERROR, ar.cause().getMessage()).put(Field.ERRORREPORT, rep.cause()));
                            } else {
                                report.save(s -> {
                                    if (s.failed()) {
                                        renderError(request, new JsonObject().put(Field.ERROR, ar.cause().getMessage()).put(Field.ERRORREPORT, s.cause()));
                                        return;
                                    }
                                    renderError(request, new JsonObject().put(Field.ERROR, ar.cause().getMessage()).put(Field.ERRORREPORT, "generated"));
                                });
                            }
                        });
                    } else {
                        renderJson(request, new JsonObject().put(Field.SUCCESS, Field.OK));
                    }
                    deleteVertxDirectoryFile(path);
                });
    }

    /**
     * fetch Structure info
     *
     * @param structureId            structure Identifier
     * @return Future<Structure>     return Structure model
     */
    private Future<Structure> fetchStructureInfo(String structureId, TrombinoscopeReport report, HttpServerRequest request) {
        Promise<Structure> promise = Promise.promise();

        structureService.retrieveStructureInfo(structureId, either -> {
            if (either.isLeft()) {
                String message = I18n.getInstance().translate(TrombinoscopeError.STRUCTURE_FAILURE.key(), getHost(request), I18n.acceptLanguage(request));
                report.setFileRecordMessage(message, false);
                promise.fail(either.left().getValue());
            } else {
                Structure structure = new Structure(either.right().getValue());
                report.setUai(structure.getUAI());
                report.setStructureId(structure.getId());
                promise.complete(structure);
            }
        });

        return promise.future();
    }

    /**
     * Clear trombinoscope failure history
     *
     * @param structure         structure
     * @return Future<Void>     return void
     */
    private Future<Void> clearOldFailures(Structure structure, TrombinoscopeReport report, HttpServerRequest request) {
        Promise<Void> promise = Promise.promise();

        failureService.delete(structure.getId(), deleteFailureResult -> {
            if (deleteFailureResult.failed()) {
                String message = I18n.getInstance().translate(TrombinoscopeError.TROMBINOSCOPE_CLEAR_FAILURES_HISTORY_FAIL.key(),
                        getHost(request), I18n.acceptLanguage(request));
                report.setFileRecordMessage(message, false);
                promise.fail(deleteFailureResult.cause());
            } else {
                promise.complete();
            }
        });
        return promise.future();
    }

    /**
     * Upload file (folder.zip in our case) in targeted path in vertx (will mkdir if no exist)
     *
     * @param request    HttpServerRequest
     * @param fileHelper FileHelper instantiated with targeted path
     * @return Future<String>   Return the zipped file as vertx path
     */
    private Future<String> uploadFile(final HttpServerRequest request, TrombinoscopeReport report, FileHelper fileHelper) {
        Promise<String> promise = Promise.promise();
        fileHelper.upload(request, event -> {
            if (event.failed()) {
                log.error("[Viescolaire@TrombinoscopeController::uploadFile] Failed to upload file", event.cause());
                String message = I18n.getInstance().translate(TrombinoscopeError.UPLOAD_FILE_FAILURE.key(),
                        getHost(request), I18n.acceptLanguage(request));
                report.setFileRecordMessage(message, false);
                promise.fail(event.cause());
            } else {
                promise.complete(event.result());
            }
        });
        return promise.future();
    }

    /**
     * Unzip file we previously uploaded (folder.unzip in our case) in targeted path in vertx
     *
     * @param zipFilename ZipFileName we will unzip
     * @return Future<String>   Return the new file unzipped file as vertx path
     */
    private Future<String> unzipFile(String zipFilename, TrombinoscopeReport report, HttpServerRequest request) {
        Promise<String> promise = Promise.promise();

        String uploadPath = zipFilename
                .replace("'", "")
                .replace(" ", "_")
                .replace(".zip", "")
                + "_unzip";

        FileHelper.unzip(zipFilename, uploadPath, unzip -> {
            if (unzip.failed()) {
                String message = "[Viescolaire@TrombinoscopeController::unzipFile] Failed to unzip file at path: " + uploadPath + ".";
                log.error(message, unzip.cause());
                String reportMessage = I18n.getInstance().translate(TrombinoscopeError.UNZIP_FILE_FAILURE.key(),
                        getHost(request), I18n.acceptLanguage(request));
                report.setFileRecordMessage(reportMessage, false);
                promise.fail(unzip.cause());
            } else {
                promise.complete(uploadPath);
            }
        });

        return promise.future();
    }

    /**
     * Start processing import trombinoscope
     *
     * @param request               HttpServerRequest
     * @param structureId           structure identifier
     * @param report                report file
     * @param uploadPathFromUnzip   unzipped file path vertx
     * @return Future<Void>         Return void
     */
    private Future<Void> processImportTrombinoscope(final HttpServerRequest request, String structureId, TrombinoscopeReport report,
                                                    String uploadPathFromUnzip) {
        Promise<Void> promise = Promise.promise();
        report.setFileRecordMessage(true);

        trombinoscopeService.process(structureId, uploadPathFromUnzip, report, request, event -> {
            if (event.failed()) {
                String message = "[Viescolaire@TrombinoscopeController::processImportTrombinoscope] Failed to import trombinoscope.";
                log.error(message, event.cause());
                promise.fail(event.cause());
            } else {
                report.generate(rep -> {
                    if (rep.failed()) {
                        promise.fail(rep.cause());
                        return;
                    }

                    report.save(s -> {
                        if (s.failed()) {
                            promise.fail(s.cause());
                            return;
                        }
                        promise.complete();
                    });
                });
            }
        });

        return promise.future();
    }

    /**
     * delete the directory where we stored our file for processing
     *
     * @param path path created that we will delete
     */
    private void deleteVertxDirectoryFile(String path) {
        vertx.fileSystem().deleteRecursive(path, true, deleteZipResult -> {
            if (deleteZipResult.failed()) {
                log.error("[Viescolaire@TrombinoscopeController::importTrombinoscope] " +
                        "Failed to remove zip and unzipped directory", deleteZipResult.cause());
            }
        });
    }

    @Post("/structures/:structureId/students/:studentId/trombinoscope")
    @ResourceFilter(ManageTrombinoscope.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @Trace(value = Actions.VIESCOLAIRE_IMPORT_LINK)
    @ApiDoc("Link an existing picture (often time from failure) to a student trombinoscope")
    public void linkTrombinoscope(final HttpServerRequest request) {
        String structureId = request.getParam(Field.STRUCTUREID);
        String studentId = request.getParam(Field.STUDENTID);
        RequestUtils.bodyToJson(request, pathPrefix + "trombinoscope_link", pictureJson -> {
            String pictureId = pictureJson.getString(Field.PICTUREID);
            FileHelper.exist(storage, pictureId, exist -> {
                if (!exist.result()) {
                    String message = "[Viescolaire@TrombinoscopeController::linkTrombinoscopee] " +
                            "File does not exists.";
                    log.error(message);
                    renderError(request);
                    return;
                }

                trombinoscopeService.create(structureId, studentId, pictureId, result -> {
                    if (result.failed()) {
                        renderError(request);
                        return;
                    }

                    renderJson(request, result.result());
                });
            });
        });
    }

    @Put("/structures/:structureId/students/:studentId/trombinoscope")
    @ResourceFilter(ManageTrombinoscope.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @Trace(value = Actions.VIESCOLAIRE_IMPORT_UPDATE, body = false)
    @ApiDoc("update trombinoscope")
    public void updateTrombinoscope(final HttpServerRequest request) {
        String structureId = request.getParam(Field.STRUCTUREID);
        String studentId = request.getParam(Field.STUDENTID);

        storage.writeUploadFile(request, resultUpload -> {
            if (!Field.OK.equals(resultUpload.getString(Field.STATUS))) {
                String message = "[Viescolaire@Trombinoscope::updateTrombinoscope] Failed to save file.";
                log.error(message, resultUpload.getString(Field.MESSAGE));
                renderError(request);
                return;
            }

            String pictureId = resultUpload.getString(Field._ID);

            trombinoscopeService.create(structureId, studentId, pictureId, result -> {
                if (result.failed()) {
                    renderError(request);
                    return;
                }

                renderJson(request, result.result());
            });
        });
    }

    @Delete("/structures/:structureId/students/:studentId/trombinoscope")
    @ResourceFilter(ManageTrombinoscope.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @Trace(value = Actions.VIESCOLAIRE_IMPORT_UPDATE, body = false)
    @ApiDoc("update trombinoscope")
    public void deletePicture(final HttpServerRequest request) {
        String structureId = request.getParam(Field.STRUCTUREID);
        String studentId = request.getParam(Field.STUDENTID);

        trombinoscopeService.deletePicture(structureId, studentId)
                .onSuccess(res -> renderJson(request, res))
                .onFailure(unused -> renderError(request));
    }

    /* FAILURE */

    @Get("/structures/:structureId/trombinoscope/failures")
    @ResourceFilter(ManageTrombinoscope.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ApiDoc("Retrieve structure trombinoscope failures")
    public void getTrombinoscopeFailures(HttpServerRequest request) {
        String structureId = request.getParam(Field.STRUCTUREID);
        failureService.get(structureId, result -> {
            if (result.failed()) {
                renderError(request);
                return;
            }

            renderJson(request, new JsonObject().put(Field.ALL, result.result()));
        });
    }

    @Get("/structures/:structureId/trombinoscope/failures/:failureId")
    @ResourceFilter(ManageTrombinoscope.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ApiDoc("Retrieve structure trombinoscope failure picture")
    public void getTrombinoscopeFailurePicture(HttpServerRequest request) {
        String structureId = request.getParam(Field.STRUCTUREID);
        String failureId = request.getParam(Field.FAILUREID);
        failureService.get(structureId, failureId, resultFile -> {
            if (resultFile.failed()) {
                notFound(request);
                return;
            }
            TrombinoscopeFailure result = resultFile.result();

            storage.sendFile(result.getPictureId(),null, request, true, new JsonObject());
        });
    }

    @Delete("/structures/:structureId/trombinoscope/failures")
    @ApiDoc("Clear failures history")
    @ResourceFilter(ManageTrombinoscope.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    public void clearFailures(final HttpServerRequest request) {
        String structureId = request.getParam(Field.STRUCTUREID);
        failureService.delete(structureId, DefaultResponseHandler.asyncVoidResponseHandler(request));
    }

    /* REPORT */

    @Get("/structures/:structureId/trombinoscope/reports")
    @ResourceFilter(ManageTrombinoscope.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ApiDoc("Retrieve structure trombinoscope reports")
    public void getTrombinoscopeReports(HttpServerRequest request) {
        String structureId = request.getParam(Field.STRUCTUREID);
        String limitString = request.getParam(Field.LIMIT);
        String offsetString = request.getParam(Field.OFFSET);
        Integer limit =  limitString != null && !limitString.equals("") ? Integer.parseInt(limitString) : -1;
        Integer offset = offsetString != null && !offsetString.equals("") ? Integer.parseInt(request.getParam(Field.OFFSET)): 0;
        reportService.get(structureId, limit, offset, result -> {
            if (result.isLeft()) {
                notFound(request);
                return;
            }

            renderJson(request, new JsonObject().put(Field.ALL, result.right().getValue()));
        });
    }
}
