package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.helper.PromiseHelper;
import fr.openent.viescolaire.service.GroupeService;
import fr.openent.viescolaire.service.GroupingService;
import fr.openent.viescolaire.service.ServiceFactory;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class DefaultGroupingService implements GroupingService {
    private static final Logger log = LoggerFactory.getLogger(DefaultGroupingService.class);
    private final GroupeService groupService;
    private static final String TABLE_GROUPING = Viescolaire.VSCO_SCHEMA + "." + Viescolaire.GROUPING_TABLE;
    private static final String TABLE_REL = Viescolaire.VSCO_SCHEMA + "." + Viescolaire.REL_GROUPING_CLASS_TABLE;

    public DefaultGroupingService(ServiceFactory serviceFactory) {
        groupService = serviceFactory.groupeService();
    }

    private Future<JsonArray> getGroupingsByStructure(String structureId) {
        Promise<JsonArray> promise = Promise.promise();
        if (structureId == null || structureId.isEmpty()) {
            String messageToFormat = "[Viescolaire@%s::getGroupingsByStructure] Error while listing grouping : %s";
            PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception("error.parameters"), promise);
            return promise.future();
        }
        JsonArray values = new JsonArray().add(structureId);
        String query = "SELECT GRP.id, name, structure_id, array_to_json(ARRAY_AGG(REL.student_division_id)) AS student_divisions FROM " + DefaultGroupingService.TABLE_GROUPING + " AS GRP LEFT JOIN " + DefaultGroupingService.TABLE_REL
                + " AS REL ON REL.grouping_id = GRP.id WHERE structure_id = ? GROUP BY GRP.id";
        Sql.getInstance().prepared(query, values, SqlResult.validResultHandler(res -> {
            if (res.isRight()) {
                promise.complete(res.right().getValue());
            } else {
                String messageToFormat = "[Viescolaire@%s::getGroupingsByStructure] Error while retrieving groupings : %s";
                PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception(res.left().getValue()), promise);
            }
        }));
        return promise.future();
    }

    @SuppressWarnings("unchecked")
    private Future<JsonArray> generateGroupingsList(JsonArray groupingSQLResults) {
        Promise<JsonArray> promise = Promise.promise();

        List<String> listAudienceId = groupingSQLResults.stream()
                .map(JsonObject.class::cast)
                .map(grouping -> ((List<String>) new JsonArray(grouping.getString(Field.STUDENT_DIVISIONS)).getList()))
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

        groupService.getNameOfGroupClass(listAudienceId)
                .onSuccess(audienceList -> {
                    //Audience map with id as key and name as value
                    Map<String, String> audienceMap = audienceList.stream().map(JsonObject.class::cast)
                            .collect(Collectors.toMap(audience -> audience.getString(Field.ID), audience -> audience.getString(Field.NAME)));
                    ((List<JsonObject>) groupingSQLResults.getList()).forEach(grouping -> {
                        List<String> studentDivisionsIdList = new JsonArray(grouping.getString(Field.STUDENT_DIVISIONS)).getList();
                        List<JsonObject> studentDivisionsList = studentDivisionsIdList.stream()
                                .map(studentDivisionId -> new JsonObject()
                                        .put(Field.ID, studentDivisionId)
                                        .put(Field.NAME, audienceMap.getOrDefault(studentDivisionId, "")))
                                .collect(Collectors.toList());
                        grouping.put(Field.STUDENT_DIVISIONS, studentDivisionsList);
                    });
                    promise.complete(groupingSQLResults);
                })
                .onFailure(err -> {
                    String messageToFormat = "[Viescolaire@%s::generateGroupingsList] Error while retrieving class or group name : %s";
                    PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), err, promise);
                });

        return promise.future();
    }


    @Override
    public Future<JsonArray> listGrouping(String structureId) {
        Promise<JsonArray> promise = Promise.promise();
        getGroupingsByStructure(structureId)
                .compose(this::generateGroupingsList)
                .onSuccess(promise::complete)
                .onFailure(err -> {
                    String messageToFormat = "[Viescolaire@%s::listGrouping] Error while generating grouping list : %s";
                    PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), err, promise);
                });
        return promise.future();
    }

    /**
     * Create a new grouping
     *
     * @param name        Name of the new grouping
     * @param structureId Identifier of the structure to which the grouping belongs
     * @return Promise with the status of the grouping creation.
     */
    public Future<JsonObject> createGrouping(String name, String structureId) {
        Promise<JsonObject> promise = Promise.promise();
        if (name == null || structureId == null || name.isEmpty() || structureId.isEmpty()) {
            String messageToFormat = "[Viescolaire@%s::createGrouping] Error while creating grouping : %s";
            PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception("error.parameters"), promise);
            return promise.future();
        }
        String uuid = UUID.randomUUID().toString();
        JsonArray values = new JsonArray();
        String query = "INSERT INTO " + TABLE_GROUPING + "(id, name, structure_id) VALUES(?, ?, ?)";
        values.add(uuid);
        values.add(name);
        values.add(structureId);
        Sql.getInstance().prepared(query, values, SqlResult.validUniqueResultHandler(res -> {
            if (res.isRight())
                promise.complete(new JsonObject()
                        .put(Field.STATUS, Field.OK)
                        .put(Field.ID, uuid));
            else {
                String messageToFormat = "[Viescolaire@%s::createGrouping] Error while creating grouping : %s";
                PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception(res.left().getValue()), promise);
            }
        }));
        return promise.future();
    }

    /**
     * Update the grouping
     *
     * @param groupingId Identifier of the grouping
     * @param name       New grouping name
     * @return Promise with the status of the update
     */
    @Override
    public Future<JsonObject> updateGrouping(String groupingId, String name) {
        Promise<JsonObject> promise = Promise.promise();
        if (name == null || groupingId == null || groupingId.isEmpty() || name.isEmpty()) {
            String messageToFormat = "[Viescolaire@%s::updateGrouping] Error while updating grouping : %s";
            PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception("error.parameters"), promise);
            return promise.future();
        }
        JsonArray values = new JsonArray();
        String query = "UPDATE " + TABLE_GROUPING + " SET name = ? WHERE id = ?";
        values.add(name);
        values.add(groupingId);
        Sql.getInstance().prepared(query, values, SqlResult.validUniqueResultHandler(res -> {
            if (res.isRight())
                promise.complete(new JsonObject().put(Field.STATUS, Field.OK));
            else {
                String messageToFormat = "[Viescolaire@%s::updateGrouping] Error while updating grouping : %s";
                PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception(res.left().getValue()), promise);
            }
        }));
        return promise.future();
    }


    /**
     * Add classes and groups to the grouping
     *
     * @param groupingId        Identifier of the grouping
     * @param studentDivisionId Class or group identifier
     * @return Promise with the status of the operation.
     */
    @Override
    public Future<JsonObject> addToGrouping(String groupingId, String studentDivisionId) {
        Promise<JsonObject> promise = Promise.promise();
        if (studentDivisionId == null || groupingId == null || studentDivisionId.isEmpty() || groupingId.isEmpty()) {
            String messageToFormat = "[Viescolaire@%s::addToGrouping] Error while adding to grouping : %s";
            PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception("error.parameters"), promise);
            return promise.future();
        }
        JsonArray values = new JsonArray();
        groupOrClassExist(studentDivisionId)
                .onSuccess(res -> {
                    if (Boolean.TRUE.equals(res)) {
                        String query = "INSERT INTO " + TABLE_REL + "(grouping_id, student_division_id)  VALUES(?, ?)";
                        values.add(groupingId);
                        values.add(studentDivisionId);
                        Sql.getInstance().prepared(query, values, SqlResult.validUniqueResultHandler(queryRes -> {
                            if (queryRes.isRight())
                                promise.complete(new JsonObject().put(Field.STATUS, Field.OK));
                            else {
                                String messageToFormat = "[Viescolaire@%s::addGrouping] Error while adding classes or groups to grouping : %s";
                                PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception(queryRes.left().getValue()), promise);
                            }
                        }));
                    } else {
                        String messageToFormat = "[Viescolaire@%s::addToGrouping] Class or group does not exist : %s";
                        PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception("no.records.founded"), promise);
                    }

                })
                .onFailure(err -> {
                    String messageToFormat = "[Viescolaire@%s::addToGrouping] Error while checking group or class existence : %s";
                    PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), err, promise);
                });

        return promise.future();
    }

    @Override
    public Future<Boolean> groupOrClassExist(String studentDivisionId) {
        Promise<Boolean> promise = Promise.promise();
        groupService.getNameOfGroupeClasse(studentDivisionId, divisionName -> {
            if (divisionName.isRight()) {
                promise.complete(!divisionName.right().getValue().isEmpty());
            } else {
                String messageToFormat = "[Viescolaire@%s::groupOrClassExist] Error while checking group or class existence : %s";
                PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception("error.retrieving.classes.groups"), promise);
            }
        });
        return promise.future();
    }

    @Override
    public Future<JsonObject> deleteGrouping(String groupingId) {
        Promise<JsonObject> promise = Promise.promise();
        JsonArray values = new JsonArray();
        String query = "DELETE FROM " + TABLE_GROUPING + " WHERE " + TABLE_GROUPING + ".id = ?";
        values.add(groupingId);
        Sql.getInstance().prepared(query, values, SqlResult.validUniqueResultHandler(res -> {
            if (res.isRight())
                promise.complete(new JsonObject().put(Field.STATUS, Field.OK));
            else {
                String messageToFormat = "[Viescolaire@%s::deleteGrouping] Error while deleting grouping : %s";
                PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception(res.left().getValue()), promise);
            }
        }));
        return promise.future();
    }

    @Override
    public Future<JsonObject> deleteGroupingAudience(String groupingId, String studentDivisionId) {
        Promise<JsonObject> promise = Promise.promise();
        JsonArray values = new JsonArray();
        String query = "DELETE FROM " + TABLE_REL + " WHERE " + "grouping_id = ? AND " + "student_division_id = ?";
        values.add(groupingId);
        values.add(studentDivisionId);
        Sql.getInstance().prepared(query, values, SqlResult.validUniqueResultHandler(res -> {
            if (res.isRight())
                promise.complete(new JsonObject().put(Field.STATUS, Field.OK));
            else {
                String messageToFormat = "[Viescolaire@%s::deleteGroupingAudience] Error while deleting class or group to the grouping : %s";
                PromiseHelper.reject(log, messageToFormat, this.getClass().getSimpleName(), new Exception(res.left().getValue()), promise);
            }
        }));
        return promise.future();
    }
}
