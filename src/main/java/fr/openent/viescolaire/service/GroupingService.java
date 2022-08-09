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
     * @param groupingId    Identifier of the grouping
     * @param groupId       Identifier of the group
     * @param classId       Identifier of the class
     * @return              Promise with the status of the operation.
     */
    Future<JsonObject> addToGrouping(String groupingId, String groupId, String classId);

    /**
     * Check if both class identifier and group identifiers exist in database.
     * @param classId   Identifier of the class.
     * @param groupId   Identifier of the group.
     * @return          A promise with the result of the check.
     */
    Future<Boolean> groupAndClassExist(String classId, String groupId);
}
