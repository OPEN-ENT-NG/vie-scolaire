package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.helper.PromiseHelper;
import fr.openent.viescolaire.service.GroupingService;
import fr.openent.viescolaire.service.ServiceFactory;
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
    private static final Logger log = LoggerFactory.getLogger(DefaultGroupingService.class);
    private final DefaultGroupeService groupService;
    private final String tableGrouping = Viescolaire.VSCO_SCHEMA + "." + Viescolaire.GROUPING_TABLE;
    private final String tableRel = Viescolaire.VSCO_SCHEMA + "." + Viescolaire.REL_GROUPING_CLASS_TABLE;
    public DefaultGroupingService(ServiceFactory serviceFactory) {
        groupService = (DefaultGroupeService) serviceFactory.groupeService();
    }

    /**
     * Create a new grouping
     * @param name          Name of the new grouping
     * @param structureId   Identifier of the structure to which the grouping belongs
     * @return              Promise with the status of the grouping creation.
     */
    public Future<JsonObject> createGrouping(String name, String structureId) {
        Promise<JsonObject> promise = Promise.promise();
        if (name == null || structureId == null || name.isEmpty() || structureId.isEmpty()) {
            String messageToFormat = "[vie-scolaire@%s::createGrouping] Error while creating grouping : %s";
            PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception("error.parameters"), promise);
            return promise.future();
        }
        String uuid = UUID.randomUUID().toString();
        JsonArray values = new JsonArray();
        String query = "INSERT INTO " + tableGrouping + "(id, name, structure_id) VALUES(?, ?, ?)";
        values.add(uuid);
        values.add(name);
        values.add(structureId);
        Sql.getInstance().prepared(query, values, SqlResult.validUniqueResultHandler(res -> {
            if(res.isRight())
                promise.complete(new JsonObject().put(Field.STATUS, Field.OK));
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
        if (name == null || groupingId == null || groupingId.isEmpty() || name.isEmpty()) {
            String messageToFormat = "[vie-scolaire@%s::updateGrouping] Error while updating grouping : %s";
            PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception("error.parameters"), promise);
            return promise.future();
        }
        JsonArray values = new JsonArray();
        String query = "UPDATE " + tableGrouping + " SET name = ?, updated_at = ? WHERE id = ?";
        values.add(name);
        values.add(DateHelper.getCurrentDate(DateHelper.MONGO_FORMAT));
        values.add(groupingId);
        Sql.getInstance().prepared(query, values, SqlResult.validUniqueResultHandler(res -> {
            if (res.isRight())
                promise.complete(new JsonObject().put(Field.STATUS, Field.OK));
            else {
                String messageToFormat = "[vie-scolaire@%s::updateGrouping] Error while updating grouping : %s";
                PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception(res.left().getValue()), promise);
            }
        }));
        return promise.future();
    }


    /**
     * Add classes and groups to the grouping
     * @param groupingId           Identifier of the grouping
     * @param studentDivisionId    Class or group identifier
     * @return                     Promise with the status of the operation.
     */
    @Override
    public Future<JsonObject> addToGrouping(String groupingId, String studentDivisionId) {
        Promise<JsonObject> promise = Promise.promise();
        if (studentDivisionId == null || groupingId == null || studentDivisionId.isEmpty() || groupingId.isEmpty()) {
            String messageToFormat = "[vie-scolaire@%s::addToGrouping] Error while adding to grouping : %s";
            PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception("error.parameters"), promise);
            return promise.future();
        }
        JsonArray values = new JsonArray();
        groupOrClassExist(studentDivisionId)
                .onSuccess(res -> {
                    if (Boolean.TRUE.equals(res)) {
                        String query = "INSERT INTO " + tableRel +"(grouping_id, student_division_id)  VALUES(?, ?)";
                        values.add(groupingId);
                        values.add(studentDivisionId);
                        Sql.getInstance().prepared(query, values, SqlResult.validUniqueResultHandler(queryRes -> {
                            if(queryRes.isRight())
                                promise.complete(new JsonObject().put(Field.STATUS, Field.OK));
                            else {
                                String messageToFormat = "[vie-scolaire@%s::addGrouping] Error while adding classes or groups to grouping : %s";
                                PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception(queryRes.left().getValue()), promise);
                            }
                        }));
                    } else {
                        String messageToFormat = "[vie-scolaire@%s::addToGrouping] Class or group does not exist : %s";
                        PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception("no.records.founded"), promise);
                    }

                })
                .onFailure(err -> {
                    String messageToFormat = "[vie-scolaire@%s::addToGrouping] Error while checking group or class existence : %s";
                    PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), err, promise);
                });

        return promise.future();
    }

    @Override
    public Future<Boolean> groupOrClassExist(String studentDivisionId) {
        Promise<Boolean> promise = Promise.promise();
        groupService.getNameOfGroupeClasse(studentDivisionId, divisionName -> {
            if (divisionName.isRight()) {
                promise.complete(divisionName.right().getValue().size() > 0);
            } else {
                String messageToFormat = "[vie-scolaire@%s::groupAndClassExist] Error while checking group or class existence : %s";
                PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception("error.retrieving.classes.groups"), promise);
            }
        });
        return promise.future();
    }
}
