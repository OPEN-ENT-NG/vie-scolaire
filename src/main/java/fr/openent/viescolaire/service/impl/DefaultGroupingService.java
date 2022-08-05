package fr.openent.viescolaire.service.impl;

import fr.openent.viescolaire.helper.PromiseHelper;
import fr.openent.viescolaire.service.GroupingService;
import fr.openent.viescolaire.utils.DateHelper;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;

import java.util.UUID;

public class DefaultGroupingService implements GroupingService {
    protected static final Logger log = LoggerFactory.getLogger(DefaultGroupingService.class);

    /**
     * Create a new grouping
     * @param name          Name of the new grouping
     * @param structureId   Identifier of the structure to which the grouping belongs
     * @return              Promise with the status of the grouping creation.
     */
    public Future<JsonObject> createGrouping(String name, String structureId) {
        Promise<JsonObject> promise = Promise.promise();
        String uuid = UUID.randomUUID().toString();
        JsonArray values = new JsonArray();
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO grouping(id, name, structure_id, created_at, updated_at) ")
                .append("VALUES(?, ?, ?, ?, ?) ON CONFLICT DO NOTHING");
        values.add(uuid);
        values.add(name);
        values.add(structureId);
        values.add(DateHelper.getCurrentDate(DateHelper.HOUR_MINUTES_SECONDS));
        values.add(DateHelper.getCurrentDate(DateHelper.HOUR_MINUTES_SECONDS));
        Sql.getInstance().prepared(query.toString(), values, SqlResult.validUniqueResultHandler(res -> {
            if(res.isRight())
                promise.complete(res.right().getValue());
            else {
                String messageToFormat = "[vie-scolaire@%s::createGrouping] Error while creating grouping : %s";
                PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception(res.left().getValue()), promise);

            }
        }));
    return promise.future();
    }

    /**
     * Update the grouping
     * @param groupingId    Identifier of the grouping
     * @param name          New grouping name
     * @return              Promise with the status of the update
     */
    @Override
    public Future<JsonObject> updateGrouping(String groupingId ,String name) {
        Promise<JsonObject> promise = Promise.promise();
        JsonArray values = new JsonArray();
        StringBuilder query = new StringBuilder();
        query.append("UPDATE grouping")
                .append("SET name = ? WHERE id = ?");
        values.add(name);
        values.add(groupingId);
        Sql.getInstance().prepared(query.toString(), values, SqlResult.validUniqueResultHandler(res -> {
            if (res.isRight())
                promise.complete(res.right().getValue());
            else {
                String messageToFormat = "[vie-scolaire@%s::updateGrouping] Error while updating grouping : %s";
                PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception(res.left().getValue()), promise);
            }
        }));
        return promise.future();
    }

    /**
     * Add classes and groups to the grouping
     * @param groupingId    Identifier of the grouping
     * @param groupId       Identifier of the group
     * @param classId       Identifier of the class
     * @return              Promise with the status of the operation.
     */
    @Override
    public Future<JsonObject> addToGrouping(String groupingId, String groupId, String classId) {
        Promise<JsonObject> promise = Promise.promise();
        JsonArray values = new JsonArray();
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO rel_grouping_class(grouping_id, class_id, group_id, created_at, updated_at) ")
                .append("VALUES(?, ?, ?, ?, ?)");
        values.add(groupingId);
        values.add(classId);
        values.add(groupId);
        values.add(DateHelper.getCurrentDate(DateHelper.HOUR_MINUTES_SECONDS));
        values.add(DateHelper.getCurrentDate(DateHelper.HOUR_MINUTES_SECONDS));
        Sql.getInstance().prepared(query.toString(), values, SqlResult.validUniqueResultHandler(res -> {
            if(res.isRight())
                promise.complete(res.right().getValue());
            else {
                String messageToFormat = "[vie-scolaire@%s::addGrouping] Error while adding classes or groups to grouping : %s";
                PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception(res.left().getValue()), promise);

            }
        }));
        return promise.future();
    }


}
