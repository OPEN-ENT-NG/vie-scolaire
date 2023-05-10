package fr.openent.viescolaire.service.impl;

import fr.openent.viescolaire.core.constants.*;
import fr.openent.viescolaire.helper.*;
import fr.openent.viescolaire.model.*;
import fr.openent.viescolaire.model.InitForm.*;
import fr.openent.viescolaire.model.Person.*;
import fr.openent.viescolaire.model.SlotProfile.*;
import fr.openent.viescolaire.service.*;
import fr.openent.viescolaire.worker.*;
import fr.wseduc.mongodb.*;
import fr.wseduc.webutils.*;
import io.vertx.core.*;
import io.vertx.core.eventbus.*;
import io.vertx.core.json.*;
import io.vertx.core.logging.*;
import org.entcore.common.mongodb.*;
import org.entcore.common.neo4j.*;
import org.entcore.common.sql.*;
import org.entcore.common.user.*;

import java.util.*;
import java.util.stream.*;

import static fr.openent.Viescolaire.*;

public class DefaultInitService implements InitService {

    private final Neo4j neo4j;
    private final Sql sql;
    private final MongoDb mongoDb;
    private final EventBus eb;
    private final JsonObject config;
    private static final String SLOTPROFILE_COLLECTION = "slotprofile";

    private final TimeSlotService timeSlotService;
    private final UserService userService;
    private final ServicesService servicesService;
    private final MatiereService matiereService;
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultInitService.class);
    public DefaultInitService(ServiceFactory serviceFactory) {
        this.neo4j = serviceFactory.neo4j();
        this.sql = serviceFactory.sql();
        this.mongoDb = serviceFactory.mongoDb();
        this.eb = serviceFactory.getEventbus();
        this.config = serviceFactory.config();
        this.timeSlotService = serviceFactory.timeSlotService();
        this.userService = serviceFactory.userService();
        this.servicesService = serviceFactory.servicesService();
        this.matiereService = serviceFactory.matiereService();
    }

    @Override
    public Future<Void> launchInitWorker(UserInfos user, String structureId, InitFormModel form, JsonObject i18nParams) {
        Promise<Void> promise = Promise.promise();

        JsonObject params = new JsonObject()
                .put(Field.STRUCTUREID, structureId)
                .put(Field.STRUCTURENAME, user.getStructureNames().get(user.getStructures().indexOf(structureId)))
                .put(Field.OWNER, new JsonObject().put(Field.ID, user.getUserId()).put(Field.DISPLAYNAME, user.getUsername()))
                .put(Field.I18N_PARAMS, i18nParams)
                .put(Field.PARAMS, form.toJson());

        this.eb.send(InitWorker1D.class.getName(), params, new DeliveryOptions().setSendTimeout(1000 * 1000L));

        promise.complete();
        return promise.future();
    }

    @Override
    public Future<InitTeachers> getTeachersStatus(String structureId) {
        Promise<InitTeachers> promise = Promise.promise();

        String query = "MATCH (u:User)-[:IN]->(pg:ProfileGroup)-[:DEPENDS]->(s:Structure {id: {structureId}}) " +
                "WHERE 'Teacher' IN u.profiles " +
                "AND NOT (u)-[:IN]->(:Group)-[:DEPENDS]->(:Class)-[:BELONGS]->(s) " +
                "RETURN u.id AS id, u.displayName AS displayName " +
                "ORDER BY u.displayName";
        JsonObject params = new JsonObject()
                .put(Field.STRUCTUREID, structureId);

        neo4j.execute(query, params, Neo4jResult.validResultHandler(res -> {
            if (res.isRight()) {
                JsonArray teachers = res.right().getValue();

                InitTeachers initTeachers = new InitTeachers();
                initTeachers.setCount(teachers.size());
                initTeachers.setTeachers(teachers.stream().map(o -> new User((JsonObject) o)).collect(Collectors.toList()));
                promise.complete(initTeachers);
            } else {
                LOGGER.error(String.format("[Viescolaire@%s::getTeachersStatus] Failed to retrieve teachers status",
                        this.getClass().getSimpleName()), res.left().getValue());
                promise.fail(res.left().getValue());
            }
        }));
        return promise.future();
    }

    @Override
    public Future<Boolean> getInitializationStatus(String structureId) {
        Promise<Boolean> promise = Promise.promise();

        String query = "SELECT initialized FROM " + VSCO_SCHEMA + ".settings WHERE structure_id = ?";
        JsonArray params = new JsonArray().add(structureId);

        sql.prepared(query, params, SqlResult.validUniqueResultHandler(res -> {
            if (res.isRight()) {
                promise.complete(res.right().getValue().getBoolean(Field.INITIALIZED));
            } else {
                LOGGER.error(String.format("[Viescolaire@%s::getInitializationStatus] Failed to retrieve initialization status",
                        this.getClass().getSimpleName()), res.left().getValue());
                promise.fail(res.left().getValue());
            }
        }));

        return promise.future();
    }

    @Override
    public Future<Void> setInitializationStatus(String structureId, boolean status) {
        Promise<Void> promise = Promise.promise();

        String query = "UPDATE " + VSCO_SCHEMA + ".settings SET initialized = ? WHERE structure_id = ?";
        JsonArray params = new JsonArray().add(status).add(structureId);

        sql.prepared(query, params, SqlResult.validUniqueResultHandler(res -> {
            if (res.isRight()) {
                promise.complete();
            } else {
                LOGGER.error(String.format("[Viescolaire@%s::setInitializationStatus] Failed to set initialization status : %s",
                        this.getClass().getSimpleName(), res.left().getValue()), res.left().getValue());
                promise.fail(res.left().getValue());
            }
        }));
        return promise.future();
    }

    @Override
    public Future<SlotProfile> initTimeSlots(String structureId, String structureName, User owner, InitFormTimetable timetable,
                                             String locale, String acceptLanguage) {
        Promise<SlotProfile> promise = Promise.promise();

        SlotProfile slotProfile = new SlotProfile();

        slotProfile.setId(UUID.randomUUID().toString());
        slotProfile.setName(String.format("%s %s", I18n.getInstance().translate("viescolaire.time.grid",
                locale, acceptLanguage), structureName));
        slotProfile.setSchoolId(structureId);
        slotProfile.setSlots(timetable.getSlots(
                I18n.getInstance().translate("viescolaire.time.grid.morning.prefix", locale, acceptLanguage),
                I18n.getInstance().translate("viescolaire.time.grid.afternoon.prefix", locale, acceptLanguage),
                I18n.getInstance().translate("viescolaire.time.grid.lunch", locale, acceptLanguage)));
        slotProfile.setCreated(MongoDb.now());
        slotProfile.setModified(MongoDb.now());
        slotProfile.setOwner(owner);

        this.getSlotProfile(structureId, slotProfile)
                .compose(res -> {
                    if (res != null) {
                        return Future.succeededFuture(res);
                    } else {
                        return this.createSlotProfile(structureId, slotProfile, timetable);
                    }
                });

        return promise.future();
    }

    private Future<SlotProfile> getSlotProfile(String structureId, SlotProfile slotProfile) {
        Promise<SlotProfile> promise = Promise.promise();
        mongoDb.findOne(SLOTPROFILE_COLLECTION, new JsonObject()
                        .put(Field.NAME, slotProfile.getName())
                        .put(Field.SCHOOLID, structureId),
                MongoDbResult.validResultHandler(res -> {
                    if (res.isRight() && res.right().getValue() != null &&
                            !res.right().getValue().isEmpty() && new SlotProfile(res.right().getValue()).isEquals(slotProfile)) {
                        promise.complete(new SlotProfile(res.right().getValue()));
                    } else if (res.isLeft()){
                        String message = String.format("[Viescolaire@%s::initTimeSlots] Failed to retrieve slot profile : %s",
                                this.getClass().getSimpleName(), res.left().getValue());
                        LOGGER.error(message);
                        promise.fail(res.left().getValue());
                    }
                }));

        return promise.future();
    }

    private Future<SlotProfile> createSlotProfile(String structureId, SlotProfile slotProfile, InitFormTimetable timetable) {
        Promise<SlotProfile> promise = Promise.promise();
        mongoDb.insert(SLOTPROFILE_COLLECTION, slotProfile.toJson(), MongoDbResult.validResultHandler(results -> {
            if (results.isLeft()) {
                String message = String.format("[Viescolaire@%s::initTimeSlots] Failed to create slot profile",
                        this.getClass().getSimpleName());
                LOGGER.error(message, results.left().getValue());
                promise.fail(results.left().getValue());
            } else {
                this.timeSlotService.saveTimeProfil(
                                new JsonObject()
                                        .put(Field.ID, slotProfile.getId())
                                        .put(Field.ID_STRUCTURE, structureId))
                        .compose(v -> this.timeSlotService.updateEndOfHalfDay(slotProfile.getId(),
                                timetable.getMorning().getString(Field.ENDHOUR), structureId))
                        .onFailure(fail -> {
                            LOGGER.error(String.format("[Viescolaire@%s::createSlotProfile] Failed to save and update " +
                                            "end of half day : %s",
                                    this.getClass().getSimpleName(), fail.getMessage()), fail.getMessage());
                            promise.fail(fail);
                        })
                        .onSuccess(success -> promise.complete(slotProfile));
            }
        }));
        return promise.future();
    }

    @Override
    public Future<SubjectModel> initSubject(String structureId, SubjectModel subject) {
        Promise<SubjectModel> promise = Promise.promise();

        JsonObject subjectParams = new JsonObject()
                .put(Field.STRUCTUREID, structureId)
                .put(Field.LABEL, subject.getLabel())
                .put(Field.CODE, subject.getCode());

        this.matiereService.getSubjectByCode(structureId, subject.getCode())
                .onFailure(promise::fail)
                .onSuccess(sub -> {
                    boolean isSubjectExist = sub != null;
                    JsonObject action = new JsonObject()
                            .put(Field.ACTION, isSubjectExist ? "manual-update-subject" : "manual-add-subject")
                            .put(Field.SUBJECT, isSubjectExist ? subjectParams.put(Field.ID, sub.getId()) : subjectParams);

                    eb.request(FEEDER_ADDRESS, action, MessageResponseHandler.messageJsonObjectHandler(res -> {
                        JsonArray resArray = res.isRight() ? res.right().getValue()
                                .getJsonArray(Field.RESULTS, new JsonArray()) : new JsonArray();

                        if (res.isLeft() || resArray.isEmpty() || resArray.getJsonArray(0).isEmpty() ||
                                resArray.getJsonArray(0).getJsonObject(0).isEmpty()) {
                            LOGGER.error(String.format("[Viescolaire@%s::initSubjects] Failed to create subject",
                                    this.getClass().getSimpleName()), res.isLeft() ? res.left().getValue() : resArray);
                            promise.fail(res.left().getValue());
                        } else {
                            promise.complete(new SubjectModel(resArray.getJsonArray(0).getJsonObject(0)));
                        }
                    }));
                });

        return promise.future();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Future<JsonObject> initServices(String structureId, SubjectModel subject) {
        Promise<JsonObject> promise = Promise.promise();
        this.userService.getTeachersWithClassGroupIds(structureId)
                .onFailure(fail -> {
                    LOGGER.error(String.format("[Viescolaire@%s::initServices] Failed to retrieve teachers with classes/groups",
                            this.getClass().getSimpleName()), fail);
                    promise.fail(fail);
                })
                .onSuccess(teachers -> {
                    List<Future<JsonObject>> createServiceFutures = new ArrayList<>();

                    teachers.stream()
                            .filter(JsonObject.class::isInstance)
                            .map(JsonObject.class::cast)
                            .collect(Collectors.toList()).forEach(teacher -> {
                                String teacherId = teacher.getString(Field.ID);
                                List<String> classIds = teacher.getJsonArray(Field.CLASSIDS, new JsonArray())
                                        .stream()
                                        .filter(String.class::isInstance)
                                        .map(String.class::cast)
                                        .collect(Collectors.toList());
                                List<String> groupIds = teacher.getJsonArray(Field.GROUPIDS, new JsonArray())
                                        .stream()
                                        .filter(String.class::isInstance)
                                        .map(String.class::cast)
                                        .collect(Collectors.toList());
                                classIds.addAll(groupIds);

                                if (!classIds.isEmpty()) {
                                    InitServiceModel service = new InitServiceModel(
                                            new JsonObject()
                                                    .put(Field.ID_MATIERE, subject.getId())
                                                    .put(Field.ID_GROUPES, classIds)
                                                    .put(Field.ID_ENSEIGNANT, teacherId)
                                                    .put(Field.ID_ETABLISSEMENT, structureId));

                                    createServiceFutures.add(this.servicesService.createService(service));
                                }
                            });

                    FutureHelper.all(createServiceFutures)
                            .onFailure(fail -> {
                                LOGGER.error(String.format("[Viescolaire@%s::initServices] Failed to create services: %s",
                                        this.getClass().getSimpleName(), fail.getMessage()));
                                promise.fail(fail.getMessage());
                            })
                            .onSuccess(res -> promise.complete());
                });

        return promise.future();
    }

    @Override
    public Future<JsonObject> initExclusionPeriod(String structureId, String zone) {
        Promise<JsonObject> promise = Promise.promise();

        if (!"A".equals(zone) && !"B".equals(zone) && !"C".equals(zone)) {
           promise.complete();
        } else {
            JsonObject action = new JsonObject()
                    .put(Field.ACTION, "init")
                    .put(Field.STRUCTUREID, structureId)
                    .put(Field.ZONE, zone)
                    .put(Field.INITSCHOOLYEAR, false);

            eb.request(EDT_ADDRESS, action, res -> {
                if (res.failed()) {
                    LOGGER.error(String.format("[Viescolaire@%s::initExclusionPeriod] Failed to init exclusion period : %s",
                            this.getClass().getSimpleName(), res.cause()), res.cause());
                    promise.fail(res.cause());
                } else {
                    promise.complete((JsonObject) res.result().body());
                }
            });
        }

        return promise.future();
    }

    @Override
    public Future<JsonObject> initCourses(String structureId, String subjectId,
                                          String startDate, String endDate, InitFormTimetable timetable,
                                          List<Timeslot> timeslots, String userId) {

        Promise<JsonObject> promise = Promise.promise();

        JsonObject action = new JsonObject()
                .put(Field.ACTION, "init-courses")
                .put(Field.STRUCTUREID, structureId)
                .put(Field.SUBJECTID, subjectId)
                .put(Field.STARTDATE, startDate)
                .put(Field.ENDDATE, endDate)
                .put(Field.TIMETABLE, timetable.toJson())
                .put(Field.TIMESLOTS, timeslots.stream().map(Timeslot::toJson).collect(Collectors.toList()))
                .put(Field.USERID, userId);

        eb.request(EDT_ADDRESS, action, MessageResponseHandler.messageJsonObjectHandler(FutureHelper.handlerEitherPromise(promise)));


        return promise.future();
    }

    @Override
    public Future<Void> resetInit(String structureId) {
        Promise<Void> promise = Promise.promise();
        this.matiereService.getSubjectByCode(structureId, "999999")
                .onFailure(promise::fail)
                .onSuccess(subject -> this.servicesService.deleteServiceBySubjectId(structureId, subject.getId())
                        .compose(res -> this.resetCourses(structureId, subject.getId()))
                        .compose(res -> this.setInitializationStatus(structureId, false))
                        .onSuccess(res -> promise.complete())
                        .onFailure(fail -> {
                            LOGGER.error(String.format("[Viescolaire@%s::resetInit] Failed to reset init : %s",
                                    this.getClass().getSimpleName(), fail.getMessage()), fail);
                            promise.fail(fail);
                        }));

        return promise.future();
    }


    private Future<JsonObject> resetCourses(String structureId, String subjectId) {
        Promise<JsonObject> promise = Promise.promise();

        JsonObject action = new JsonObject()
                .put(Field.ACTION, "delete-courses-subject")
                .put(Field.STRUCTUREID, structureId)
                .put(Field.SUBJECTID, subjectId);

        eb.request(EDT_ADDRESS, action, MessageResponseHandler.messageJsonObjectHandler(FutureHelper.handlerEitherPromise(promise)));

        return promise.future();
    }
}
