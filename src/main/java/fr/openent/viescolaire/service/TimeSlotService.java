package fr.openent.viescolaire.service;

import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public interface TimeSlotService {

    void getSlotProfiles(String id_structure, Handler<Either<String, JsonArray>> handler);

    /**
     * Retrieve ONLY slots defined by your default time slots. Returns slots
     * @param structureId   Structure identifier
     * @param handler       Function handler returning data
     */
    void getDefaultSlots(String structureId, Handler<Either<String, JsonArray>> handler);

    void saveTimeProfil(JsonObject timeSlot, Handler<Either<String, JsonArray>> handler);

    /**
     * Defining the end of the half day for the current timeslots
     * @param id            time slot identifier
     * @param time          time defined as the end of the half day
     * @param structureId   structure identifier
     * @param handler       Function handler returning data
     */
    void updateEndOfHalfDay(String id, String time, String structureId, Handler<Either<String, JsonObject>> handler);

    /**
     * Retrieve default structure timeslot. Returns slots sort by hour
     * @param id Structure identifier
     * @param handler Function handler returning data
     */
    void getDefaultTimeSlot(String id, Handler<Either<String, JsonObject>> handler);

}
