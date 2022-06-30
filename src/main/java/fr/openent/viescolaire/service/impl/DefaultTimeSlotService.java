package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.helper.FutureHelper;
import fr.openent.viescolaire.service.ServiceFactory;
import fr.openent.viescolaire.service.TimeSlotService;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.webutils.Either;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.mongodb.MongoDbResult;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;

import java.util.List;

public class DefaultTimeSlotService implements TimeSlotService {

    private String COLLECTION = "slotprofile";
    private Logger LOGGER = LoggerFactory.getLogger(TimeSlotService.class);
    private ServiceFactory serviceFactory;

    public DefaultTimeSlotService(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    public DefaultTimeSlotService() {
    }

    @Override
    public void getSlotProfiles(String id_structure, Handler<Either<String, JsonArray>> handler) {
        String query = "SELECT * FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_TIME_SLOTS +
                " WHERE id_structure = ?";
        JsonArray params = new fr.wseduc.webutils.collections.JsonArray().add(id_structure);

        Sql.getInstance().prepared(query, params, SqlResult.validResultHandler(handler));
    }

    @Override
    public Future<JsonObject> getSlotProfiles(String structureId) {
        Promise<JsonObject> promise = Promise.promise();

        this.getSlotProfiles(structureId, slotProfiles -> {
            if (slotProfiles.isLeft()) {
                promise.fail(slotProfiles.left().getValue());
            } else {
                JsonArray res = slotProfiles.right().getValue();
                promise.complete(res.isEmpty() ? new JsonObject() : res.getJsonObject(0));
            }
        });

        return promise.future();
    }

    @Override
    public Future<String> getSlotProfilesFromClasse(String idClass) {
        Promise<String> promise = Promise.promise();
        String query = "SELECT * FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_REL_TIME_SLOT_CLASS +
                " WHERE id_class = ?";
        JsonArray params = new JsonArray().add(idClass);

        Sql.getInstance().prepared(query, params, SqlResult.validUniqueResultHandler(event -> {
            if (event.isLeft()) {
                promise.fail(event.left().getValue());
            } else {
                promise.complete(event.right().getValue().getString(Field.ID_TIME_SLOT, ""));
            }
        }));

        return promise.future();
    }

    @Override
    public Future<JsonArray> getSlotProfilesFromClasses(List<String> idsClass) {
        if (idsClass == null || idsClass.isEmpty()) {
            return Future.succeededFuture(new JsonArray());
        }
        Promise<JsonArray> promise = Promise.promise();
        String query = "SELECT * FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_REL_TIME_SLOT_CLASS +
                " WHERE id_class IN " + Sql.listPrepared(idsClass);
        JsonArray params = new JsonArray(idsClass);

        Sql.getInstance().prepared(query, params, SqlResult.validResultHandler(FutureHelper.handlerJsonArray(promise)));

        return promise.future();
    }

    @Override
    public void getSlotProfileSetting(String id_structure, Handler<Either<String, JsonObject>> handler) {
        String query = "SELECT * FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_TIME_SLOTS +
                " WHERE id_structure = ?";
        JsonArray params = new fr.wseduc.webutils.collections.JsonArray().add(id_structure);

        Sql.getInstance().prepared(query, params, SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void getDefaultSlots(String structureId, Handler<Either<String, JsonArray>> handler) {
        String query = "SELECT * FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_SLOTS +
                " WHERE structure_id = ? ORDER BY start_hour";
        JsonArray params = new JsonArray().add(structureId);
        Sql.getInstance().prepared(query, params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void saveTimeProfil(JsonObject timeSlot, Handler<Either<String, JsonArray>> handler) {
        JsonArray statements = new JsonArray();
        getTimeSlot(timeSlot.getString("id"), slotResult -> {
            statements.add(setTimeSlotDefault(timeSlot));
            if (slotResult.right().getValue().getJsonArray("slots").size() > 0) {
                addStructureToSlots(timeSlot.getString("id_structure"), slotResult.right().getValue().getJsonArray("slots"));
                statements.add(insertSlot(slotResult.right().getValue().getJsonArray("slots")));
            }
            Sql.getInstance().transaction(statements, statementResults -> {
                Either<String, JsonArray> either = SqlResult.validResult(0, statementResults);
                if (either.isLeft()) {
                    String err = "[Viescolaire@DefaultTimeSlotService] Failed to update timeSlot by default or to update slots";
                    LOGGER.error(err, either.left().getValue());
                }
                handler.handle(either);
            });
        });
    }

    private JsonObject setTimeSlotDefault(JsonObject timeSlot) {
        String query = "INSERT INTO " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_TIME_SLOTS +
                "(id_structure, id) " + "VALUES (?, ?)" + "ON CONFLICT (id_structure) DO UPDATE SET id = ? RETURNING *;";

        JsonArray params = new fr.wseduc.webutils.collections.JsonArray()
                .add(timeSlot.getString("id_structure"))
                .add(timeSlot.getString("id"))
                .add(timeSlot.getString("id"));

        return new JsonObject()
                .put("action", "prepared")
                .put("statement", query)
                .put("values", params);
    }

    @Override
    public void updateEndOfHalfDay(String id, String time, String structureId, Handler<Either<String, JsonObject>> handler) {
        String query = "UPDATE " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_TIME_SLOTS +
                " SET end_of_half_day = ? WHERE id = ? returning id";
        JsonArray params = new JsonArray().add(time).add(id);
        Sql.getInstance().prepared(query, params, SqlResult.validUniqueResultHandler(handler));
    }

    private void addStructureToSlots(String structureId, JsonArray slots) {
        for (int i = 0; i < slots.size(); i++) {
            JsonObject slot = slots.getJsonObject(i);
            slot.put("structureId", structureId);
        }
    }

    private JsonObject insertSlot(JsonArray slots) {
        JsonArray params = new JsonArray();
        StringBuilder query = new StringBuilder("INSERT INTO " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_SLOTS +
                "(id, structure_id, name, start_hour, end_hour) " + "VALUES ");
        for (int i = 0; i < slots.size(); i++) {
            query.append(addSlot(slots.getJsonObject(i), params)).append((i == (slots.size() - 1) ? "" : ","));
        }
        return new JsonObject()
                .put("action", "prepared")
                .put("statement", query)
                .put("values", params);
    }

    private String addSlot(JsonObject slot, JsonArray params) {
        params.add(slot.getString("id"))
                .add(slot.getString("structureId"))
                .add(slot.getString("name"))
                .add(slot.getString("startHour"))
                .add(slot.getString("endHour"));
        return "(?, ?, ?, ?, ?)";
    }

    @Override
    public void getDefaultTimeSlot(String id, Handler<Either<String, JsonObject>> handler) {
        Future<JsonArray> slotsFuture = Future.future();
        Future<JsonObject> profileFuture = Future.future();

        CompositeFuture.all(slotsFuture, profileFuture).setHandler(event -> {
            if (event.failed()) {
                LOGGER.error("[Viescolaire@DefaultTimeSlotService] Failed retrieve default structure timeslot. One future failed.", event.cause());
                handler.handle(new Either.Left<>(event.cause().toString()));
            }

            JsonObject profile = profileFuture.result();
            JsonArray slots = slotsFuture.result();

            handler.handle(new Either.Right<>(profile.put("slots", slots)));
        });

        getSortedSlots(id, slotsFuture);
        getTimeSlot(id, profileFuture);
    }

    @Override
    public Future<JsonObject> getDefaultTimeSlot(String slotId) {
        Promise<JsonObject> promise = Promise.promise();

        this.getDefaultTimeSlot(slotId, FutureHelper.handlerJsonObject(promise));

        return promise.future();
    }

    @Override
    public Future<JsonObject> getTimeSlot(String audienceId, String structureId) {
        Promise<JsonObject> promise = Promise.promise();
        serviceFactory.classeService().getClasseIdFromAudience(audienceId)
                .compose(classId -> this.getTimeSlotFromClass(classId, structureId))
                .onSuccess(promise::complete)
                .onFailure(promise::fail);
        return promise.future();
    }

    @Override
    public Future<JsonObject> setTimeSlotFromAudience(String audienceId, String slotProfileId) {
        Promise<JsonObject> promise = Promise.promise();
        String query = "INSERT INTO " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_REL_TIME_SLOT_CLASS +
                " VALUES(?, ?) " +
                " ON CONFLICT ON CONSTRAINT comments_pkey" +
                " DO UPDATE SET id_time_slot = ? WHERE " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_REL_TIME_SLOT_CLASS + ".id_class = ?";
        JsonArray params = new JsonArray().add(audienceId).add(slotProfileId).add(slotProfileId).add(audienceId);
        Sql.getInstance().prepared(query, params, SqlResult.validUniqueResultHandler(FutureHelper.handlerJsonObject(promise)));
        return promise.future();
    }

    @Override
    public Future<String> getStructureFromTimeSlot(String timeslotId) {
        Promise<String> promise = Promise.promise();
        this.getTimeSlot(timeslotId, timeSlot -> {
            if (timeSlot.isLeft()) {
                promise.fail(timeSlot.left().getValue());
            } else {
                promise.complete(timeSlot.right().getValue().getString("schoolId", ""));
            }
        });
        return promise.future();
    }

    @Override
    public Future<JsonObject> deleteTimeSlotFromClass(String classId) {
        Promise<JsonObject> promise = Promise.promise();
        String query = "DELETE FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_REL_TIME_SLOT_CLASS +
                " WHERE " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_REL_TIME_SLOT_CLASS + ".id_class = ?";
        JsonArray params = new JsonArray().add(classId);
        Sql.getInstance().prepared(query, params, SqlResult.validUniqueResultHandler(FutureHelper.handlerJsonObject(promise)));
        return promise.future();
    }

    @Override
    public Future<JsonObject> deleteTimeSlotFromTimeslot(String timeslotId) {
        Promise<JsonObject> promise = Promise.promise();
        String query = "DELETE FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_REL_TIME_SLOT_CLASS +
                " WHERE " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_REL_TIME_SLOT_CLASS + ".id_time_slot = ?";
        JsonArray params = new JsonArray().add(timeslotId);
        Sql.getInstance().prepared(query, params, SqlResult.validUniqueResultHandler(FutureHelper.handlerJsonObject(promise)));
        return promise.future();
    }

    @Override
    public Future<JsonObject> getTimeSlotFromClass(String classId, String structureId) {
        return this.getSlotProfilesFromClasse(classId)
                .compose(slotProfileId -> {
                    if (!slotProfileId.isEmpty()) {
                        return this.getDefaultTimeSlot(slotProfileId);
                    } else {
                        return this.getTimeSlotFromStructure(structureId);
                    }
                })
                .compose(jsonObject -> {
                    if (jsonObject.isEmpty()) {
                        return this.getTimeSlotFromStructure(structureId);
                    }
                    Promise<JsonObject> promiseJsonObject = Promise.promise();
                    promiseJsonObject.complete(jsonObject);
                    return promiseJsonObject.future();
                });
    }

    @Override
    public Future<JsonObject> getTimeSlotFromStructure(String structureId) {
        Promise<JsonObject> promise = Promise.promise();

        this.getSlotProfiles(structureId).compose(slot -> this.getDefaultTimeSlot(slot.getString(Field.ID, "")))
                .onSuccess(promise::complete)
                .onFailure(promise::fail);

        return promise.future();
    }

    @Override
    public Future<JsonObject> getTimeSlotFromClass(String classId) {
        return serviceFactory.timeSlotService().getSlotProfilesFromClasse(classId)
                .compose(serviceFactory.timeSlotService()::getDefaultTimeSlot);
    }

    private void getTimeSlot(String slotId, Future<JsonObject> future) {
        MongoDb.getInstance().findOne(COLLECTION, new JsonObject().put("_id", slotId), message -> {
            Either<String, JsonObject> either = MongoDbResult.validResult(message);
            if (either.isLeft()) {
                LOGGER.error("[Viescolaire@DefaultTimeSlotService] Failed to fetch given slot", either.left().getValue());
                future.fail(either.left().getValue());
            } else {
                future.complete(either.right().getValue());
            }
        });
    }

    @Override
    public Future<JsonArray> getMultipleTimeSlot(List<String> slotIds) {
        Promise<JsonArray> promise = Promise.promise();
        JsonArray ids = new JsonArray(slotIds);
        JsonObject in = new JsonObject().put("$in", ids);
        JsonObject filterId = new JsonObject().put("_id", in);
        MongoDb.getInstance().find(COLLECTION, filterId, MongoDbResult.validResultsHandler(event -> {
            if (event.isLeft()) {
                String message = String.format("[Viescolaire@%s::getMultipleTimeSlot] an error has occuring during finding time slot: %s",
                        this.getClass().getSimpleName(), event.left().getValue());
                LOGGER.error(message);
                promise.fail(event.left().getValue());
            } else {
                promise.complete(event.right().getValue());
            }
        }));

        return promise.future();
    }

    private void getTimeSlot(String slotId, Handler<Either<String, JsonObject>> handler) {
        Future<JsonObject> future = Future.future();
        getTimeSlot(slotId, future.setHandler(event -> {
            if (event.failed()) {
                handler.handle(new Either.Left<>(event.cause().toString()));
            } else {
                handler.handle(new Either.Right<>(event.result()));
            }
        }));
    }

    private void getSortedSlots(String slotId, Future<JsonArray> future) {
        JsonArray pipeline = new JsonArray();
        JsonObject command = new JsonObject()
                .put("aggregate", COLLECTION)
                .put("allowDiskUse", true)
                .put("cursor", new JsonObject())
                .put("pipeline", pipeline);

        JsonObject matcher = new JsonObject().put("$match", new JsonObject().put("_id", slotId));
        JsonObject unwind = new JsonObject().put("$unwind", "$slots");
        JsonObject projection = new JsonObject()
                .put("_id", "$slots.id")
                .put("name", "$slots.name")
                .put("startHour", "$slots.startHour")
                .put("endHour", "$slots.endHour");
        JsonObject sort = new JsonObject().put("$sort", new JsonObject().put("startHour", 1));

        pipeline.add(matcher)
                .add(unwind)
                .add(new JsonObject().put("$project", projection))
                .add(sort);

        MongoDb.getInstance().command(command.toString(), message -> {
            JsonObject body = message.body();
            if ("ok".equals(body.getString("status"))) {
                future.complete(body.getJsonObject("result").getJsonObject("cursor").getJsonArray("firstBatch"));
            } else {
                String error = "[Viescolaire@DefaultTimeSlotsService] Failed to retrieve sorted slots";
                LOGGER.error(error);
                future.fail(error);
            }

        });
    }
}
