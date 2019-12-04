package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.TimeSlotService;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.webutils.Either;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.mongodb.MongoDbResult;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;

public class DefaultTimeSlotService implements TimeSlotService {

    private String COLLECTION = "slotprofile";
    private Logger LOGGER = LoggerFactory.getLogger(TimeSlotService.class);

    @Override
    public void getSlotProfiles(String id_structure, Handler<Either<String, JsonArray>> handler) {
        String query = "SELECT * FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_TIME_SLOTS +
                " WHERE id_structure = ?";
        JsonArray params = new fr.wseduc.webutils.collections.JsonArray().add(id_structure);

        Sql.getInstance().prepared(query, params, SqlResult.validResultHandler(handler));
    }

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
        "(id_structure, id) " +  "VALUES (?, ?)" + "ON CONFLICT (id_structure) DO UPDATE SET id = ? RETURNING *;";

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
        StringBuilder query = new StringBuilder("INSERT INTO " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_SLOTS +
                "(id, structure_id, name, start_hour, end_hour) " + "VALUES ");
        for (int i = 0; i < slots.size(); i++) {
           query.append(addSlot(slots.getJsonObject(i))).append((i == (slots.size() - 1) ? "" : ","));
        }
        return new JsonObject()
                .put("action", "raw")
                .put("command", query.toString());
    }

    private String addSlot(JsonObject slot) {
        return  "('" + slot.getString("id") + "','" +
                slot.getString("structureId") + "','" +
                slot.getString("name") + "','" +
                slot.getString("startHour") + "','" +
                slot.getString("endHour") + "')";
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
