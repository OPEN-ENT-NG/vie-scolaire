package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.TimeSlotService;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.webutils.Either;
import io.vertx.core.AsyncResult;
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

    @Override
    public void saveTimeProfil(JsonObject timeSlot, Handler<Either<String, JsonArray>> handler) {
        String query = "INSERT INTO " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_TIME_SLOTS +
                "(id_structure, id) " +  "VALUES (?, ?)" + "ON CONFLICT (id_structure) DO UPDATE SET id = ? RETURNING *;";
        JsonArray params = new fr.wseduc.webutils.collections.JsonArray()
                .add(timeSlot.getString("id_structure"))
                .add(timeSlot.getString("id"))
                .add(timeSlot.getString("id"));

        Sql.getInstance().prepared(query, params, SqlResult.validResultHandler(handler));
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
