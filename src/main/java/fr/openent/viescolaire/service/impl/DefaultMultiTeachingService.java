package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.db.DBService;
import fr.openent.viescolaire.helper.FutureHelper;
import fr.openent.viescolaire.helper.MultiTeachingHelper;
import fr.openent.viescolaire.model.MultiTeaching;
import fr.openent.viescolaire.service.CommonCoursService;
import fr.openent.viescolaire.service.MultiTeachingService;
import fr.openent.viescolaire.service.UtilsService;
import fr.openent.viescolaire.utils.DateHelper;
import fr.wseduc.webutils.Either;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.sql.Sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static fr.openent.Viescolaire.VSCO_SCHEMA;
import static fr.wseduc.webutils.Utils.handlerToAsyncHandler;
import static org.entcore.common.sql.SqlResult.validResultHandler;
import static org.entcore.common.sql.SqlResult.validRowsResultHandler;

public class DefaultMultiTeachingService extends DBService implements MultiTeachingService {

    private final UtilsService utilsService;
    private final CommonCoursService commonCoursService;
    private final DateHelper dateHelper = new DateHelper();
    private final EventBus eb;

    protected static final Logger log = LoggerFactory.getLogger(DefaultMultiTeachingService.class);

    /**
     * Old constructor with event bus to null. Can throw {@link NullPointerException} when calling some methode
     *
     * @deprecated use {@link DefaultMultiTeachingService#DefaultMultiTeachingService(EventBus)} instead.
     */
    @Deprecated
    public DefaultMultiTeachingService() {
        this.utilsService = new DefaultUtilsService();
        this.commonCoursService = null;
        this.eb = null;
    }

    public DefaultMultiTeachingService(EventBus eb) {
        this.utilsService = new DefaultUtilsService();
        this.commonCoursService = new DefaultCommonCoursService();
        this.eb = eb;
    }

    @Override
    public void createMultiTeaching(String structureId, String mainTeacherId, JsonArray secondTeacherIds,
                                    String subjectId, JsonArray classOrGroupIds, String startDate, String endDate,
                                    String enteredEndDate, Boolean coTeaching, Handler<Either<String, JsonArray>> handler,
                                    boolean hasCompetences) {


        String query;
        query = "WITH insert AS ( INSERT INTO " + VSCO_SCHEMA + "." + Viescolaire.VSCO_MULTI_TEACHING_TABLE +
                " (structure_id, main_teacher_id,  second_teacher_id, subject_id, class_or_group_id, is_coteaching, " +
                "start_date, end_date, entered_end_date)" +
                " VALUES ";

        JsonArray values = new JsonArray();

        for (int i = 0; i < secondTeacherIds.size(); i++) {
            String secondTeacherId = secondTeacherIds.getString(i);
            for (int j = 0; j < classOrGroupIds.size(); j++) {
                String classOrGroupId = classOrGroupIds.getString(j);
                query += "( ?, ?, ?, ?, ?, ?,";

                values.add(structureId)
                        .add(mainTeacherId)
                        .add(secondTeacherId)
                        .add(subjectId)
                        .add(classOrGroupId)
                        .add(coTeaching);

                if (startDate != null && endDate != null && enteredEndDate != null) {
                    query += " to_timestamp( ?, 'YYYY-MM-DD'), to_timestamp( ?, 'YYYY-MM-DD'), to_timestamp( ?, 'YYYY-MM-DD') ),";
                    values.add(startDate)
                            .add(endDate)
                            .add(enteredEndDate);
                } else {
                    query += " NULL, NULL, NULL ),";
                }

            }
        }

        query = query.substring(0, query.length() - 1);//remove useless ','

        query += "RETURNING * ) SELECT second_teacher_id FROM insert " +
                "UNION SELECT DISTINCT second_teacher_id FROM " + VSCO_SCHEMA + "." + Viescolaire.VSCO_MULTI_TEACHING_TABLE +
                " WHERE structure_id = ? AND main_teacher_id = ? AND subject_id = ? " +
                "AND class_or_group_id IN " + Sql.listPrepared(classOrGroupIds.getList()) +
                " AND deleted_date IS NULL ";

        values.add(structureId).add(mainTeacherId).add(subjectId);

        for (Object o : classOrGroupIds) {
            values.add(o);
        }

        for (int i = 0; i < classOrGroupIds.size(); i++) {
            String classId = classOrGroupIds.getString(i);
            JsonObject classAction = new JsonObject()
                    .put("action", "manual-add-users")
                    .put("classId", classId)
                    .put("userIds", secondTeacherIds);

            this.eb.request("entcore.feeder", classAction, event -> {
                if (event.failed()) {
                    String message = String.format("[Viescolaire@%s::handleCreationSqlResponse] Error when call entcore.feeder:%s %s",
                            this.getClass().getSimpleName(), classAction.toString(), event.cause().getMessage());
                    log.error(message);
                }
            });

            for (int j = 0; j < secondTeacherIds.size(); j++) {
                //manual-add-user-group
                JsonObject groupAction = new JsonObject()
                        .put("action", "manual-add-user-group")
                        .put("groupId", classId)
                        .put("userId", secondTeacherIds.getString(j));

                this.eb.request("entcore.feeder", groupAction, event -> {
                    if (event.failed()) {
                        String message = String.format("[Viescolaire@%s::handleCreationSqlResponse] Error when call entcore.feeder:%s %s",
                                this.getClass().getSimpleName(), groupAction.toString(), event.cause().getMessage());
                        log.error(message);
                    }
                });
            }
        }

        MultiTeaching multiTeaching = new MultiTeaching();
        multiTeaching.setStructureId(structureId);
        multiTeaching.setMainTeacherId(mainTeacherId);
        multiTeaching.setSubjectId(subjectId);
        multiTeaching.setStartDate(startDate);
        multiTeaching.setEndDate(endDate);

        handleCreationSqlResponse(multiTeaching, secondTeacherIds, classOrGroupIds, handler, hasCompetences, query, values);
    }

    /**
     * Handle multiTeaching creation (+ add substitutes to courses related to multiTeachings)
     *
     * @param multiTeaching    multiTeaching object
     * @param secondTeacherIds list of substitute teacher identifiers
     * @param classOrGroupIds  list of class/group identifiers
     * @param handler          Function handler returning data
     * @param hasCompetences   is Competences module installed
     * @param query            creation query string
     * @param values           creation query parameters
     */
    private void handleCreationSqlResponse(MultiTeaching multiTeaching, JsonArray secondTeacherIds, JsonArray classOrGroupIds,
                                           Handler<Either<String, JsonArray>> handler,
                                           boolean hasCompetences, String query, JsonArray values) {

        sql.prepared(query, values, res -> {

            if (res.body().getString(Field.STATUS).equals(Field.OK)) {
                List<Future> futureList = new ArrayList<>();

                Future<JsonArray> futureResult = hasCompetences ?
                        prepareSendIdsToShare(multiTeaching, classOrGroupIds, res.body().getJsonArray(Field.RESULTS)) :
                        Future.succeededFuture(res.body().getJsonArray(Field.RESULTS));

                futureList.add(futureResult);

                if (multiTeaching.getStartDate() != null && multiTeaching.getEndDate() != null) {
                    futureList.add(addCourseSubstitutes(multiTeaching, secondTeacherIds, classOrGroupIds));
                }

                CompositeFuture.all(futureList)
                        .onSuccess(event -> handler.handle(new Either.Right<>(futureResult.result())))
                        .onFailure(error -> {
                            String message = String.format("[Viescolaire@%s::handleCreationSqlResponse] Error creating multiteaching : %s",
                                    this.getClass().getSimpleName(), error.getMessage());
                            log.error(message);
                            handler.handle(new Either.Left<>(error.getMessage()));
                        });
            } else {
                String message = String.format("[Viescolaire@%s::handleCreationSqlResponse] Error creating multiteaching : %s",
                        this.getClass().getSimpleName(), res.body().getString(Field.STATUS));
                handler.handle(new Either.Left<>(message));
            }
        });
    }

    private Future<JsonArray> prepareSendIdsToShare(MultiTeaching multiTeaching, JsonArray classOrGroupIds, JsonArray otherTeacher) {
        Promise<JsonArray> promise = Promise.promise();

        JsonArray idsToSend = new JsonArray();
        JsonArray teacherIds = new JsonArray();
        teacherIds.add(multiTeaching.getMainTeacherId());
        for (Object teacher : otherTeacher) {
            JsonArray teacherArray = (JsonArray) teacher;
            teacherIds.add(teacherArray.getString(0));
        }
        for (int i = 0; i < teacherIds.size(); i++) {
            String firstSecondId = teacherIds.getString(i);
            for (int j = i; j < teacherIds.size(); j++) {
                String secondSecondId = teacherIds.getString(j);
                if (!secondSecondId.equals(firstSecondId))
                    for (int k = 0; k < classOrGroupIds.size(); k++) {
                        JsonArray ids = new JsonArray();
                        String groupId = classOrGroupIds.getString(k);

                        ids.add(secondSecondId)
                                .add(firstSecondId)
                                .add(multiTeaching.getSubjectId())
                                .add(groupId)
                                .add(multiTeaching.getStructureId());
                        idsToSend.add(ids);
                    }
            }
        }

        sendIdsToShare(idsToSend, FutureHelper.handlerEitherPromise(promise));

        return promise.future();
    }


    /**
     * Add substitute teachers to courses related to a multiTeaching
     *
     * @param multiTeaching    multiTeaching object
     * @param secondTeacherIds list of substitute teacher identifiers
     * @param classOrGroupIds  list of class/group identifiers
     * @return Future
     */
    private Future<JsonObject> addCourseSubstitutes(MultiTeaching multiTeaching, JsonArray secondTeacherIds,
                                                    JsonArray classOrGroupIds) {

        Promise<JsonObject> promise = Promise.promise();
        fetchGroupClassesInfos(classOrGroupIds, multiTeaching.getStartDate())
                .compose(infos -> fetchCoursesIds(multiTeaching,
                        infos.getJsonArray(Field.CLASSGROUPEXTERNALIDS), infos.getJsonArray(Field.MANUALGROUPNAMES),
                        infos.getString(Field.STARTDATE), multiTeaching.getEndDate(),
                        infos.getString(Field.STARTTIME, null)))
                .compose(courseIds -> addCourseTeachersFromIds(courseIds, multiTeaching.getMainTeacherId(), secondTeacherIds))
                .onFailure(fail -> promise.fail(fail.getMessage()))
                .onSuccess(promise::complete);

        return promise.future();
    }

    /**
     * Remove substitute teachers from courses related to a multiTeaching
     *
     * @param multiTeaching   multiTeaching object
     * @param classOrGroupIds list of class/group identifiers
     * @return Future
     */
    private Future<Void> removeCourseSubstitutes(MultiTeaching multiTeaching, JsonArray classOrGroupIds) {

        Promise<Void> promise = Promise.promise();

        String endDateString = (multiTeaching.getEndDate() != null) ?
                DateHelper.getDateString(dateHelper.getDate(multiTeaching.getEndDate(), DateHelper.DATE_FORMATTER),
                        DateHelper.YEAR_MONTH_DAY) : null;

        fetchGroupClassesInfos(classOrGroupIds, multiTeaching.getStartDate())
                .compose(infos -> fetchCoursesIds(multiTeaching,
                        infos.getJsonArray(Field.CLASSGROUPEXTERNALIDS),
                        infos.getJsonArray(Field.MANUALGROUPNAMES), infos.getString(Field.STARTDATE),
                        endDateString, infos.getString(Field.STARTTIME, null)))
                .compose(courseIds -> removeCourseTeachersFromIds(courseIds, multiTeaching.getSecondTeacherId()))
                .onFailure(fail -> promise.fail(fail.getMessage()))
                .onSuccess(ar -> promise.complete());

        return promise.future();
    }

    /**
     * Fetch group/classes information from their ids (name, externalIds, dates)
     *
     * @param classOrGroupIds list of class and group identifiers
     * @param startDate       start date
     * @return Future containing information about group/classes
     */
    private Future<JsonObject> fetchGroupClassesInfos(JsonArray classOrGroupIds, String startDate) {
        Promise<JsonObject> promise = Promise.promise();
        Promise<JsonArray> externalIdsFuture = Promise.promise();
        Promise<JsonArray> manualNamesFuture = Promise.promise();

        List<String> classGroupIdList = classOrGroupIds
                .stream().filter(Objects::nonNull).map(String.class::cast)
                .collect(Collectors.toList());

        CompositeFuture.all(externalIdsFuture.future(), manualNamesFuture.future())
                .onFailure(fail -> promise.fail(fail.getMessage()))
                .onSuccess(res -> {
                    JsonObject responseArrays = new JsonObject();
                    responseArrays.put(Field.MANUALGROUPNAMES, manualNamesFuture.future().result())
                            .put(Field.CLASSGROUPEXTERNALIDS, externalIdsFuture.future().result());


                    String currentDate = DateHelper.getCurrentDate(DateHelper.YEAR_MONTH_DAY);

                    boolean isPastCourse = dateHelper.isBeforeOrEquals(startDate, currentDate, dateHelper.SIMPLE_DATE_FORMATTER);

                    responseArrays.put(Field.STARTDATE, isPastCourse ? currentDate : startDate)
                            .put(Field.STARTTIME, isPastCourse ? DateHelper.getCurrentDate(DateHelper.HOUR_MINUTES_SECONDS) : null);

                    promise.complete(responseArrays);
                });

        utilsService.getClassGroupExternalIdsFromIds(classGroupIdList, externalIdsFuture);
        utilsService.getManualGroupNameById(classGroupIdList, manualNamesFuture);

        return promise.future();
    }


    /**
     * Fetch list of course identifiers related to multiTeaching
     *
     * @param multiTeaching         multi teaching object
     * @param classGroupExternalIds list of class and groups external ids
     * @param manualGroupNames      list of manual group names
     * @param startDate             start date filter
     * @param endDate               end date filter
     * @param startTime             start time filter (ignored if null)
     * @return Future containing list of course ids
     */
    private Future<List<String>> fetchCoursesIds(MultiTeaching multiTeaching, JsonArray classGroupExternalIds,
                                                 JsonArray manualGroupNames, String startDate, String endDate, String startTime) {
        Promise<List<String>> promise = Promise.promise();

        List<String> manualGroupNamesList = manualGroupNames
                .stream().map(name -> ((JsonObject) name).getString(Field.NAME))
                .collect(Collectors.toList());

        List<String> classGroupExternalIdsList = classGroupExternalIds
                .stream().map(id -> ((JsonObject) id).getString(Field.EXTERNALID))
                .collect(Collectors.toList());


        List<String> teacherIdArray = new ArrayList<>(Collections.singletonList(multiTeaching.getMainTeacherId()));
        List<String> subjectIdArray = new ArrayList<>(Collections.singletonList(multiTeaching.getSubjectId()));

        if (!(manualGroupNamesList.isEmpty() && classGroupExternalIdsList.isEmpty()) && commonCoursService != null) {
            commonCoursService.listCoursesBetweenTwoDates(multiTeaching.getStructureId(), teacherIdArray, null,
                    classGroupExternalIdsList, manualGroupNamesList, subjectIdArray, startDate,
                    endDate, startTime, null, false, false, null,
                    null, false, null, event -> {

                        if (event.isLeft()) {
                            promise.fail(event.left().getValue());
                        } else {
                            List<String> courseIds = event.right().getValue().stream()
                                    .map(c -> ((JsonObject) c).getString(Field._ID)).collect(Collectors.toList());

                            promise.complete(courseIds);
                        }
                    });
        } else {
            promise.fail(String.format("[Viescolaire@%s::fetchCoursesIds] Group name/externalId array is empty", this.getClass().getSimpleName()));
        }
        return promise.future();
    }

    /**
     * Add new substitutes teachers to courses in identifier list
     *
     * @param courseIds        course identifier list
     * @param mainTeacherId    main teacher identifier
     * @param secondTeacherIds substitute teacher identifier list
     * @return Future
     */
    private Future<JsonObject> addCourseTeachersFromIds(List<String> courseIds, String mainTeacherId, JsonArray secondTeacherIds) {

        Promise<JsonObject> promise = Promise.promise();

        List<String> teacherIds = secondTeacherIds
                .stream().map(String.class::cast).filter(id -> !id.equals(mainTeacherId))
                .collect(Collectors.toList());


        if (commonCoursService != null) {
            commonCoursService.addCoursesTeachers(courseIds, teacherIds, event -> {
                if (event.isLeft()) {
                    String message = String.format("[Viescolaire@%s::addCourseTeachersFromIds] " +
                                    "Error adding courses teachers : %s",
                            this.getClass().getSimpleName(), event.left().getValue());

                    promise.fail(message);
                } else {
                    promise.complete(event.right().getValue());
                }
            });
        } else {
            promise.fail(String.format("[Viescolaire@%s::addCourseTeachersFromIds] CommonCourseService is not defined",
                    this.getClass().getSimpleName()));
        }

        return promise.future();
    }

    /**
     * Remove subtitute teachers on all courses from course identifier list
     *
     * @param courseIds course identifier list
     * @param teacherId teacher identifier
     * @return Future
     */
    private Future<JsonObject> removeCourseTeachersFromIds(List<String> courseIds, String teacherId) {

        Promise<JsonObject> promise = Promise.promise();

        if (commonCoursService != null) {
            commonCoursService.removeCoursesTeachers(courseIds, teacherId, event -> {
                if (event.isLeft()) {
                    String message = String.format("[Viescolaire@%s::removeCourseTeachersFromIds] " +
                                    "Error removing courses teachers : %s",
                            this.getClass().getSimpleName(), event.left().getValue());

                    promise.fail(message);
                } else {
                    promise.complete(event.right().getValue());
                }
            });
        } else {
            promise.fail(String.format("[Viescolaire@%s::removeCourseTeachersFromIds] CommonCourseService is not defined",
                    this.getClass().getSimpleName()));
        }

        return promise.future();
    }

    private void sendIdsToShare(JsonArray idsToSend, Handler<Either<String, JsonArray>> requestHandler) {
        JsonObject action = new JsonObject()
                .put("action", "homeworks.setShare")
                .put("ids", idsToSend);
        this.eb.request(Viescolaire.COMPETENCES_BUS_ADDRESS, action, handlerToAsyncHandler(event -> {
            if (event.body().getString(Field.STATUS).equals(Field.OK)) {
                requestHandler.handle(new Either.Right<>(new JsonArray().add(event.body().getJsonArray(Field.RESULTS))));
            } else {
                log.error(String.format("[Viescolaire@%s::removeCourseTeachersFromIds] Fail to send ids to share",
                        this.getClass().getSimpleName()));
                requestHandler.handle(new Either.Left<>(event.body().getString("message")));
            }
        }));
    }

    private void sendIdsToDelete(JsonArray idsToSend, Handler<Either<String, JsonObject>> requestHandler) {
        JsonObject action = new JsonObject()
                .put("action", "homeworks.removeShare")
                .put("ids", idsToSend);
        this.eb.request(Viescolaire.COMPETENCES_BUS_ADDRESS, action, handlerToAsyncHandler(event -> {
            if (event.body().getString(Field.STATUS).equals(Field.OK)) {
                requestHandler.handle(new Either.Right<>(new JsonObject().put(Field.RESULTS, event.body().getJsonArray(Field.RESULTS))));
            } else {
                log.error(String.format("[Viescolaire@%s::removeCourseTeachersFromIds] Fail to send ids to delete",
                        this.getClass().getSimpleName()));
                requestHandler.handle(new Either.Left<>(event.body().getString("message")));
            }
        }));
    }

    @Override
    public void deleteMultiTeaching(JsonArray multiTeachingIds, boolean hasCompetences,
                                    Handler<Either<String, JsonObject>> handler) {

        removeSubstitutesFromCourseList(multiTeachingIds)
                .onFailure(fail -> handler.handle(new Either.Left<>(fail.getMessage())))
                .onSuccess(evt -> {
                    String deleteQuery = "UPDATE " + VSCO_SCHEMA + "." + Viescolaire.VSCO_MULTI_TEACHING_TABLE +
                            " SET deleted_date = NOW() " +
                            "WHERE id IN " + Sql.listPrepared(multiTeachingIds.getList());
                    String selectQuery = "SELECT second_teacher_id, main_teacher_id,subject_id,class_or_group_id " +
                            "FROM " + VSCO_SCHEMA + "." + Viescolaire.VSCO_MULTI_TEACHING_TABLE + " WHERE id IN "
                            + Sql.listPrepared(multiTeachingIds.getList())
                            + "UNION "
                            + "SELECT mtt.second_teacher_id, mtt.main_teacher_id,mtt.subject_id,mtt.class_or_group_id "
                            + "FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_MULTI_TEACHING_TABLE + " mt "
                            + "INNER JOIN " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_MULTI_TEACHING_TABLE + " mtt "
                            + "ON  mtt.main_teacher_id = mt.main_teacher_id AND mt.subject_id = mtt.subject_id " +
                            " AND mt.second_teacher_id != mtt.second_teacher_id AND  mt.class_or_group_id = mtt.class_or_group_id "
                            + "WHERE mt.id IN "
                            + Sql.listPrepared(multiTeachingIds.getList());
                    JsonArray paramsSelectQuery = new JsonArray().addAll(multiTeachingIds).addAll(multiTeachingIds);

                    if (hasCompetences) {
                        sql.prepared(selectQuery, paramsSelectQuery, event -> {
                            if (event.body().getString(Field.STATUS).equals(Field.OK) && event.body().getJsonArray(Field.RESULTS).size() > 0) {
                                JsonArray idsToSend = event.body().getJsonArray(Field.RESULTS);

                                sql.prepared(deleteQuery, multiTeachingIds, deleteEvent -> sendIdsToDelete(idsToSend, handler));
                            }
                        });
                    } else {
                        sql.prepared(deleteQuery, multiTeachingIds, validRowsResultHandler(handler));
                    }
                });
    }

    private Future<List<MultiTeaching>> removeSubstitutesFromCourseList(JsonArray multiTeachingIds) {

        Promise<List<MultiTeaching>> promise = Promise.promise();

        getMultiTeachings(multiTeachingIds, res -> {
            if (res.isLeft()) {
                String message = String.format("[Viescolaire@%s::removeSubstitutesFromCourseList] " +
                                "Error fetching multi teachings from ids : %s",
                        this.getClass().getSimpleName(), res.left().getValue());
                promise.fail(message);
            } else {
                List<MultiTeaching> multiTeachings = MultiTeachingHelper.toMultiTeachingList(res.right().getValue());

                List<Future<Void>> futures = new ArrayList<>();

                for (MultiTeaching m : multiTeachings) {

                    if (!m.isCoteaching() && !(m.getMainTeacherId().equals(m.getSecondTeacherId()))) {
                        JsonArray classOrGroupIds = new JsonArray();
                        classOrGroupIds.add(m.getClassOrGroupId());
                        futures.add(removeCourseSubstitutes(m, classOrGroupIds));
                    }
                }

                Future.all(futures)
                        .onFailure(fail -> {
                            String message = String.format("[Viescolaire@%s::removeSubstitutesFromCourseList] " +
                                            "Error deleting course teachers : %s",
                                    this.getClass().getSimpleName(), fail.getMessage());
                            log.error(message);
                            promise.fail(message);
                        })
                        .onSuccess(ar -> promise.complete(multiTeachings));
            }
        });
        return promise.future();
    }

    @Override
    public void updateMultiteaching(JsonArray idsMultiTeachingToUpdate, String secondTeacher, String startDate,
                                    String endDate, String enteredEndDate, Boolean isVisible, boolean hasCompetences,
                                    Handler<Either<String, JsonArray>> handler) {

        removeSubstitutesFromCourseList(idsMultiTeachingToUpdate)
                .onFailure(fail -> handler.handle(new Either.Left<>(fail.getMessage())))
                .onSuccess(multiTeachings -> {

                    JsonArray secondTeacherIds = new JsonArray();
                    secondTeacherIds.add(secondTeacher);

                    List<Future<JsonObject>> futures = new ArrayList<>();

                    for (MultiTeaching m : multiTeachings) {
                        JsonArray classOrGroupIds = new JsonArray();
                        classOrGroupIds.add(m.getClassOrGroupId());
                        m.setStartDate(startDate);
                        m.setEndDate(endDate);
                        futures.add(addCourseSubstitutes(m, secondTeacherIds, classOrGroupIds));
                    }

                    Future.all(futures)
                            .onFailure(fail -> handler.handle(new Either.Left<>(fail.getMessage())))
                            .onSuccess(ar -> {
                                JsonArray values = new JsonArray();
                                String query = "UPDATE " + VSCO_SCHEMA + "." + Viescolaire.VSCO_MULTI_TEACHING_TABLE +
                                        " SET second_teacher_id = ? , start_date = to_timestamp( ?, 'YYYY-MM-DD')," +
                                        " end_date = to_timestamp( ?, 'YYYY-MM-DD'), entered_end_date = to_timestamp( ?, 'YYYY-MM-DD'), is_visible = ?" +
                                        " WHERE id IN " + Sql.listPrepared(idsMultiTeachingToUpdate.getList()) +
                                        " RETURNING second_teacher_id, main_teacher_id,subject_id,class_or_group_id;";
                                values.add(secondTeacher).add(startDate).add(endDate).add(enteredEndDate).add(isVisible);

                                for (Object o : idsMultiTeachingToUpdate) {
                                    values.add(o);
                                }

                                if (hasCompetences) {
                                    sql.prepared(query, values, event -> {
                                        if (event.body().getString(Field.STATUS).equals(Field.OK)) {
                                            JsonArray idsToSend = event.body().getJsonArray(Field.RESULTS);
                                            sendIdsToShare(idsToSend, handler);
                                        }
                                    });
                                } else {
                                    sql.prepared(query, values, validResultHandler(handler));
                                }
                            });
                });
    }

    @Override
    public void updateMultiTeachingVisibility(JsonArray groupsId, String structureId, String mainTeacherId,
                                              String secondTeacherId, String subjectId,
                                              Boolean isVisible, Handler<Either<String, JsonArray>> handler) {
        JsonArray values = new JsonArray();
        String query = "UPDATE " + VSCO_SCHEMA + "." + Viescolaire.VSCO_MULTI_TEACHING_TABLE +
                " SET is_visible = ? WHERE structure_id = ? AND subject_id = ? AND main_teacher_id = ? AND " +
                "second_teacher_id = ? AND class_or_group_id IN " + Sql.listPrepared(groupsId.getList());
        values.add(isVisible).add(structureId).add(subjectId).add(mainTeacherId).add(secondTeacherId);

        for (Object o : groupsId) {
            values.add(o);
        }

        sql.prepared(query, values, validResultHandler(handler));
    }


    @Override
    public void getMultiTeaching(String structureId, Handler<Either<String, JsonArray>> handler) {
        String query = "SELECT * FROM " + VSCO_SCHEMA + "." + Viescolaire.VSCO_MULTI_TEACHING_TABLE + " " +
                "WHERE structure_id = ? AND deleted_date IS NULL;";

        JsonArray values = new JsonArray().add(structureId);
        sql.prepared(query, values, validResultHandler(handler));
    }

    @Override
    public void getMultiTeachings(JsonArray ids, Handler<Either<String, JsonArray>> handler) {
        String query = "SELECT * FROM " + VSCO_SCHEMA + "." + Viescolaire.VSCO_MULTI_TEACHING_TABLE + " " +
                "WHERE id IN " + Sql.listPrepared(ids.getList()) + " AND deleted_date IS NULL;";

        sql.prepared(query, ids, validResultHandler(handler));
    }

    @Override
    public void getMultiTeachers(String structureId, JsonArray groupIds, String periodId, Boolean onlyVisible,
                                 Handler<Either<String, JsonArray>> handler) {
        JsonArray values = new JsonArray().add(structureId);
        for (int i = 0; i < groupIds.size(); i++) {
            values.add(groupIds.getString(i));
        }
        values.add(onlyVisible);

        StringBuffer query = new StringBuffer();
        query.append("SELECT * FROM " + VSCO_SCHEMA + "." + Viescolaire.VSCO_MULTI_TEACHING_TABLE)
                .append(" JOIN " + VSCO_SCHEMA + "." + Viescolaire.VSCO_PERIODE_TABLE + " on class_or_group_id = id_classe ")
                .append("WHERE structure_id = ? AND class_or_group_id IN " + Sql.listPrepared(groupIds))
                .append("AND is_visible = ? ");

        if (periodId != null) {
            query.append("AND id_type = ? AND deleted_date IS NULL  AND (is_coteaching = TRUE OR (is_coteaching = FALSE AND (")
                    .append("(timestamp_dt <= start_date AND start_date <= timestamp_fn) OR ")
                    .append("(timestamp_dt <= end_date AND end_date <= timestamp_fn) OR ")
                    .append("(start_date <= timestamp_dt AND timestamp_dt <= end_date) OR ")
                    .append("(start_date <= timestamp_fn AND timestamp_fn <= end_date) )))");

            values.add(periodId);
        }

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void getAllMultiTeachers(String structureId, JsonArray groupIds, Handler<Either<String, JsonArray>> handler) {
        JsonArray values = new JsonArray().add(structureId);

        StringBuffer query = new StringBuffer();
        query.append("SELECT * FROM " + VSCO_SCHEMA + "." + Viescolaire.VSCO_MULTI_TEACHING_TABLE)
                .append(" WHERE structure_id = ? AND class_or_group_id IN " + Sql.listPrepared(groupIds));
        for (int i = 0; i < groupIds.size(); i++) {
            values.add(groupIds.getString(i));
        }

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void getSubTeachersandCoTeachers(String userId, String idStructure, String subjectId,
                                            String groupId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();
        query.append("SELECT DISTINCT main_teacher_id  as teacher_id ")
                .append("FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_MULTI_TEACHING_TABLE)
                .append(" WHERE second_teacher_id = ? ")
                .append("AND structure_id = ? ")
                .append("AND subject_id = ? ")
                .append("AND class_or_group_id = ? ")
                .append("AND (start_date <= current_date OR start_date IS NULL) ")
                .append("AND (current_date <= entered_end_date OR entered_end_date IS NULL) ")
                .append("AND is_coteaching IS NOT NULL ")

                .append("UNION ")

                .append("SELECT DISTINCT second_teacher_id  as teacher_id ")
                .append("FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_MULTI_TEACHING_TABLE)
                .append(" WHERE main_teacher_id = ? ")
                .append("AND structure_id = ? ")
                .append("AND subject_id = ? ")
                .append("AND  class_or_group_id = ? ")
                .append("AND (start_date <= current_date OR start_date IS NULL) ")
                .append("AND (current_date <= entered_end_date OR entered_end_date IS NULL) ")
                .append("AND is_coteaching IS NOT NULL ")

                .append("UNION ")

                .append("SELECT DISTINCT  mtt.second_teacher_id  as teacher_id ")
                .append("FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_MULTI_TEACHING_TABLE + " mt ")
                .append("INNER JOIN " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_MULTI_TEACHING_TABLE + " mtt ")
                .append("ON  mtt.main_teacher_id = mt.main_teacher_id AND mt.subject_id = mtt.subject_id ")
                .append("WHERE mtt.second_teacher_id != ? ")
                .append("AND mt.second_teacher_id = ? ")
                .append("AND mt.is_coteaching IS NOT NULL ")
                .append("AND  mtt.structure_id = ? ")
                .append("AND  mtt.subject_id = ? ")
                .append("AND  mtt.class_or_group_id = ? ")
                .append("AND (mtt.start_date <= current_date OR mtt.start_date IS NULL) ")
                .append("AND (current_date <= mtt.entered_end_date OR mtt.entered_end_date IS NULL) ")
                .append("AND mtt.is_coteaching IS NOT NULL ");

        values.add(userId);
        values.add(idStructure);
        values.add(subjectId);
        values.add(groupId);

        values.add(userId);
        values.add(idStructure);
        values.add(subjectId);
        values.add(groupId);

        values.add(userId);
        values.add(userId);
        values.add(idStructure);
        values.add(subjectId);
        values.add(groupId);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    public void getOtherSubTeachersandCoTeachers(String userId, String idStructure, String subjectId, Handler<Either<String, JsonArray>> handler) {
        //TODO PASSER PAR LE MAIN TEACHER
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT DISTINCT main_teacher_id  as teacher_id ")
                .append("FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_MULTI_TEACHING_TABLE)
                .append(" WHERE second_teacher_id = ? ")
                .append("AND structure_id = ? ")
                .append("AND subject_id = ? ")
                .append("AND start_date <= current_date ")
                .append("AND current_date <= entered_end_date ")
                .append("AND is_coteaching IS NOT NULL ")
                .append("UNION ")
                .append("SELECT DISTINCT second_teacher_id  as teacher_id ")
                .append("FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_MULTI_TEACHING_TABLE)
                .append(" WHERE main_teacher_id = ? ")
                .append("AND structure_id = ? ")
                .append("AND subject_id = ? ")
                .append("AND start_date <= current_date ")
                .append("AND current_date <= entered_end_date ")
                .append("AND is_coteaching IS NOT NULL ");

        values.add(userId);
        values.add(idStructure);
        values.add(subjectId);
        values.add(userId);
        values.add(idStructure);
        values.add(subjectId);
        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void getCoTeachers(String userId, String idStructure, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT DISTINCT main_teacher_id ")
                .append("FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_MULTI_TEACHING_TABLE)
                .append(" WHERE second_teacher_id = ? ")
                .append("AND structure_id = ? ")
                .append("AND is_coteaching is TRUE ");

        values.add(userId);
        values.add(idStructure);
        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void getIdGroupsMutliTeaching(String userId, String idStructure, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT class_or_group_id  as group_id ")
                .append("FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_MULTI_TEACHING_TABLE)
                .append(" WHERE second_teacher_id = ? ")
                .append("AND structure_id = ? ")
                .append("AND start_date <= current_date ")
                .append("AND current_date <= entered_end_date ")
                .append("AND is_coteaching IS NOT NULL ");

        values.add(userId);
        values.add(idStructure);
        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }
}

