package fr.openent.viescolaire.service;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public interface GroupingService {
    /**
     * Create a new grouping
     * @param name          Name of the new grouping
     * @param structureId   Identifier of the structure to which the grouping belongs
     * @return              Promise with the status of the grouping creation.
     */
    Future<JsonObject> createGrouping(String name, String structureId);

    /**
     * Update the grouping
     * @param groupingId    Identifier of the grouping
     * @param name          New grouping name
     * @return              Promise with the status of the update
     */
    Future<JsonObject> updateGrouping(String groupingId, String name);

    /**
     * Add classes and groups to the grouping
     * @param groupingId            Identifier of the grouping
     * @param studentDivisionId     Class or group identifier.
     * @return                      Promise with the status of the operation.
     */
    Future<JsonObject> addToGrouping(String groupingId, String studentDivisionId);

    /**
     * Check if both class identifier and group identifiers exist in database.
     * @param studentDivisionId     Identifier of the student division (group or class).
     * @return                      A promise with the result of the check.
     */
    Future<Boolean> groupOrClassExist(String studentDivisionId);

    /**
     * Delete the grouping.
     * @param groupingId     Identifier of the grouping.
     * @return               A promise with the status of the deletion.
     */
    Future<JsonObject> deleteGrouping(String groupingId);

}
