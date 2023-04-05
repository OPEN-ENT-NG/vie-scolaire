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

import static fr.openent.Viescolaire.VSCO_SCHEMA;

public class DefaultInitService implements InitService {

    private final Neo4j neo4j;
    private final Sql sql;
    private final MongoDb mongoDb;
    private final EventBus eb;
    private static final String SLOTPROFILE_COLLECTION = "slotprofile";

    private final TimeSlotService timeSlotService;
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultInitService.class);
    public DefaultInitService(ServiceFactory serviceFactory) {
        this.neo4j = serviceFactory.neo4j();
        this.sql = serviceFactory.sql();
        this.mongoDb = serviceFactory.mongoDb();
        this.eb = serviceFactory.getEventbus();
        this.timeSlotService = serviceFactory.timeSlotService();
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
    public Future<Void> initTimeSlots(String structureId, String structureName, User owner, InitFormTimetable timetable,
                                      String locale, String acceptLanguage) {
        Promise<Void> promise = Promise.promise();

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
                        .onSuccess(success -> promise.complete());
            }
        }));

        return promise.future();
    }


}
