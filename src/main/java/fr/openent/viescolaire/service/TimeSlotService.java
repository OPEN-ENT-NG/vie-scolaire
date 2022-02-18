package fr.openent.viescolaire.service;

import fr.wseduc.webutils.Either;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public interface TimeSlotService {

    /**
     * Get the slotProfiles of a class
     *
     * @param id_structure structure identifier
     * @param handler Function handler returning data
     */
    void getSlotProfiles(String id_structure, Handler<Either<String, JsonArray>> handler);

    /**
     * Get the slotProfiles of a structure
     *
     * @param structureId structure identifier
     * @return {@link Future} of {@link JsonObject} completed or failure
     */
    Future<JsonObject> getSlotProfiles(String structureId);

    /**
     * Get the slotProfiles of a list of classId
     *
     * @param idsClass list of classId
     * @return {@link Future} of {@link JsonArray} completed or failure
     */
    Future<JsonArray> getSlotProfilesFromClasses(List<String> idsClass);

    /**
     * Get the slotProfiles of a class
     *
     * @param idClass class identifier
     * @return
     */
    Future<String> getSlotProfilesFromClasse(String idClass);

    void getSlotProfileSetting(String id_structure, Handler<Either<String, JsonObject>> handler);

    /**
     * Retrieve ONLY slots defined by your default time slots. Returns slots
     *
     * @param structureId Structure identifier
     * @param handler     Function handler returning data
     */
    void getDefaultSlots(String structureId, Handler<Either<String, JsonArray>> handler);

    void saveTimeProfil(JsonObject timeSlot, Handler<Either<String, JsonArray>> handler);

    /**
     * Defining the end of the half day for the current timeslots
     *
     * @param id          time slot identifier
     * @param time        time defined as the end of the half day
     * @param structureId structure identifier
     * @param handler     Function handler returning data
     */
    void updateEndOfHalfDay(String id, String time, String structureId, Handler<Either<String, JsonObject>> handler);

    /**
     * Retrieve default structure timeslot. Returns slots sort by hour
     *
     * @param id      Structure identifier
     * @param handler Function handler returning data
     */
    void getDefaultTimeSlot(String id, Handler<Either<String, JsonObject>> handler);

    /**
     * Retrieve default structure timeslot. Returns slots sort by hour
     *
     * @param slotId      Structure identifier
     */
    Future<JsonObject> getDefaultTimeSlot(String slotId);

    /**
     * Retrieve the timeslot of an audience. If the audience has no timeslot, we return that of the establishment.
     *
     * @param audienceId  audience identifier
     * @param structureId audience structure identifier
     * @return {@link Future} of {@link JsonObject} completed or failure
     */
    Future<JsonObject> getTimeSlot(String audienceId, String structureId);

    /**
     * Set or update an audience's timeslot
     *
     * @param audienceId  audience identifier
     * @param slotProfileId slot profile identifier
     * @return {@link Future} of {@link JsonObject} completed or failure
     */
    Future<JsonObject> setTimeSlotFromAudience(String audienceId, String slotProfileId);

    /**
     * Delete a class's timeslot
     *
     * @param audienceId  audience identifier
     * @return {@link Future} of {@link JsonObject} completed or failure
     */
    Future<JsonObject> deleteTimeSlotFromClass(String audienceId);

    /**
     * Delete all class's timeslot for a selected timeslot
     *
     * @param timeslotId timeslot identifier
     * @return {@link Future} of {@link JsonObject} completed or failure
     */
    Future<JsonObject> deleteTimeSlotFromTimeslot(String timeslotId);


    /**
     * Get the structure of a timeslot
     *
     * @param timeslotId timeslot identifier
     * @return {@link Future} of {@link String} completed or failure
     */
    Future<String> getStructureFromTimeSlot(String timeslotId);

    /**
     * Retrieve the timeslot of a class. If the class has no timeslot, we return that of the establishment.
     *
     * @param classId     class identifier
     * @param structureId class structure identifier
     * @return {@link Future} of {@link JsonObject} completed or failure
     */
    Future<JsonObject> getTimeSlotFromClass(String classId, String structureId);

    /**
     * Retrieve the timeslot of a class.
     *
     * @param classId     class identifier
     * @return {@link Future} of {@link JsonObject} completed or failure
     */
    Future<JsonObject> getTimeSlotFromClass(String classId);

    /**
     * Get multiple time slot
     *
     * @param slotIds list of timeslotId
     * @return {@link Future} of {@link JsonArray} completed or failure
     */
    Future<JsonArray> getMultipleTimeSlot(List<String> slotIds);
}
