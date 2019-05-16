package fr.openent.viescolaire.service;

import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public interface TimeSlotService {

    void getSlotProfiles(String id_structure, Handler<Either<String, JsonArray>> handler);

    void saveTimeProfil(JsonObject timeSlot, Handler<Either<String, JsonArray>> handler);

}
