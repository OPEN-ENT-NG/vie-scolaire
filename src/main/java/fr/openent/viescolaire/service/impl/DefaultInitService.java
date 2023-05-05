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
    private static final String SLOTPROFILE_COLLECTION = "slotprofile";

    private final TimeSlotService timeSlotService;
    private final UserService userService;

    private final ServicesService servicesService;
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultInitService.class);
    public DefaultInitService(ServiceFactory serviceFactory) {
        this.neo4j = serviceFactory.neo4j();
        this.sql = serviceFactory.sql();
        this.mongoDb = serviceFactory.mongoDb();
        this.eb = serviceFactory.getEventbus();
        this.timeSlotService = serviceFactory.timeSlotService();
        this.userService = serviceFactory.userService();
        this.servicesService = serviceFactory.servicesService();
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
                            LOGGER.error(String.format("[Viescolaire@%s::initTimeSlots] Failed to save and update end of half day",
                                    this.getClass().getSimpleName()), fail);
                            promise.fail(fail);
                        })
                        .onSuccess(success -> promise.complete(slotProfile));
            }
        }));

        return promise.future();
    }

    @Override
    public Future<JsonObject> initSubjects(String structureId, String label, String code) {
        Promise<JsonObject> promise = Promise.promise();

        JsonObject subject = new JsonObject()
                .put(Field.STRUCTUREID, structureId)
                .put(Field.LABEL, label)
                .put(Field.CODE, code);

        JsonObject action = new JsonObject()
                .put(Field.ACTION, "manual-add-subject")
                .put(Field.SUBJECT, subject);

        eb.request(FEEDER_ADDRESS, action, MessageResponseHandler.messageJsonObjectHandler(FutureHelper.handlerEitherPromise(promise)));

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
}
