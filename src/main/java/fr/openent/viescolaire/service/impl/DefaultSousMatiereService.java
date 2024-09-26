/*
 * Copyright (c) Région Hauts-de-France, Département de la Seine-et-Marne, Région Nouvelle Aquitaine, Mairie de Paris, CGI, 2016.
 * This file is part of OPEN ENT NG. OPEN ENT NG is a versatile ENT Project based on the JVM and ENT Core Project.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 * For the sake of explanation, any module that communicate over native
 * Web protocols, such as HTTP, with OPEN ENT NG is outside the scope of this
 * license and could be license under its own terms. This is merely considered
 * normal use of OPEN ENT NG, and does not fall under the heading of "covered work".
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.SousMatiereService;
import fr.wseduc.webutils.Either;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;

import static org.entcore.common.sql.SqlResult.validResultHandler;

/**
 * Created by ledunoiss on 18/10/2016.
 */
public class DefaultSousMatiereService extends SqlCrudService implements SousMatiereService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSousMatiereService.class);
    public DefaultSousMatiereService() {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.VSCO_SOUSMATIERE_TABLE);
    }

    @Override
    public void listSousMatieres(String id, String idStructure, Handler<Either<String, JsonArray>> handler) {
        JsonArray values = new JsonArray().add(id).add(idStructure);

        String query = "SELECT sousmatiere.id ,type_sousmatiere.libelle " +
                "FROM " + Viescolaire.VSCO_SCHEMA + ".sousmatiere, " + Viescolaire.VSCO_SCHEMA + ".type_sousmatiere " +
                "WHERE sousmatiere.id_type_sousmatiere = type_sousmatiere.id " +
                "AND sousmatiere.id_matiere = ? AND id_structure = ?";

        Sql.getInstance().prepared(query, values, validResultHandler(handler));
    }

    @Override
    public void getSousMatiereById(String[] ids, String idStructure, Handler<Either<String, JsonArray>> handler) {
        JsonArray params = new JsonArray().add(idStructure);
        for (String id : ids) {
            params.add(id);
        }

        String query = "SELECT sousmatiere.*, type_sousmatiere.libelle FROM " + Viescolaire.VSCO_SCHEMA +
                ".sousmatiere INNER JOIN " + Viescolaire.VSCO_SCHEMA +
                ".type_sousmatiere on sousmatiere.id_type_sousmatiere = type_sousmatiere.id" +
                " WHERE id_structure = ? AND sousmatiere.id_matiere IN " + Sql.listPrepared(ids);

        Sql.getInstance().prepared(query, params, SqlResult.validResultHandler(handler));
    }

    public void listTypeSousMatieres(String idStructure, Handler<Either<String, JsonArray>> handler) {
        JsonArray params = new JsonArray().add(idStructure);

        String query = "SELECT * FROM " + Viescolaire.VSCO_SCHEMA + ".type_sousmatiere " +
                "WHERE id_structure = ? ORDER BY id ";

        Sql.getInstance().prepared(query, params, validResultHandler(handler));
    }

    public void duplicateDefaultSousMatieres(String idStructure, Handler<Either<String, JsonArray>> handler) {
        JsonArray params = new JsonArray().add(idStructure);

        String query = "INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".type_sousmatiere (id, libelle, id_structure) " +
                "SELECT nextval('" + Viescolaire.VSCO_SCHEMA + ".type_sousmatiere_id_seq'), libelle, ? " +
                "FROM " + Viescolaire.VSCO_SCHEMA + ".type_sousmatiere WHERE id_structure = 'DEFAULT' ORDER BY id";

        Sql.getInstance().prepared(query, params, validResultHandler(handler));
    }

    @Override
    public void create(Handler<Either<String, JsonObject>> handler, JsonObject event) {
        JsonArray params = new JsonArray().add(event.getString("libelle"))
                .add(event.getString("id_structure"));

        String query = "INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".type_sousmatiere (id, libelle, id_structure) " +
                "VALUES (nextval('" + Viescolaire.VSCO_SCHEMA + " .type_sousmatiere_id_seq'), ?, ?) RETURNING id";

        Sql.getInstance().prepared(query, params, SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void update(Handler<Either<String, JsonObject>> handler, int id, JsonObject event) {
        JsonArray params = new JsonArray().add(event.getString("libelle")).add(id);

        String query = "UPDATE " + Viescolaire.VSCO_SCHEMA + ".type_sousmatiere " +
                "SET libelle = ? " +
                "WHERE id = ? " +
                "RETURNING id ";

        Sql.getInstance().prepared(query, params, SqlResult.validUniqueResultHandler(handler));
    }

    private JsonObject updateEvaluationQuery(String id_topic, Integer id_sub_topic) {
        String query = "UPDATE " + Viescolaire.EVAL_SCHEMA + ".devoirs SET id_sousmatiere = ? " +
                "WHERE id_matiere = ? AND ( " +
                "id_sousmatiere IS NULL " +
                "OR id_sousmatiere NOT IN( " +
                "SELECT id_type_sousmatiere " +
                "FROM " + Viescolaire.VSCO_SCHEMA + ".sousmatiere " +
                "INNER JOIN " + Viescolaire.EVAL_SCHEMA + ".devoirs " +
                "ON id_type_sousmatiere = devoirs.id_sousmatiere AND sousmatiere.id_matiere = ?))";

        JsonArray params = new JsonArray()
                .add(id_sub_topic).add(id_topic).add(id_topic);

        return new JsonObject()
                .put("statement", query)
                .put("values", params)
                .put("action", "prepared");
    }

    private JsonObject resetEvaluationQuery(String id_topic) {
        String query = "UPDATE " + Viescolaire.EVAL_SCHEMA + ".devoirs SET id_sousmatiere = null " +
                "WHERE  id_matiere = ? AND id_sousmatiere NOT IN( " +
                "SELECT id_type_sousmatiere " +
                "FROM " + Viescolaire.VSCO_SCHEMA + ".sousmatiere " +
                "INNER JOIN " + Viescolaire.EVAL_SCHEMA + ".devoirs " +
                "ON id_type_sousmatiere = devoirs.id_sousmatiere AND sousmatiere.id_matiere = ?)";

        JsonArray params = new JsonArray()
                .add(id_topic).add(id_topic);

        return new JsonObject()
                .put("statement", query)
                .put("values", params)
                .put("action", "prepared");
    }
    private JsonObject insertQuery(String id_topic, Integer id_sub_topic){
        String query = "INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".sousmatiere (id_matiere, id_type_sousmatiere) " +
                " VALUES (? , ? )";

        JsonArray params = new JsonArray()
                .add(id_topic).add(id_sub_topic);

        return new JsonObject()
                .put("statement", query)
                .put("values", params)
                .put("action", "prepared");
    }

    @Override
    public void updateMatiereRelation(JsonArray topics, JsonArray subTopics, Handler<Either<String, JsonArray>> handler) {
        JsonArray params = new JsonArray();
        for (int i = 0 ; i < topics.size();i++){
            params.add(topics.getValue(i));
        }

        JsonArray statements = new JsonArray();

        String query = "DELETE FROM " + Viescolaire.VSCO_SCHEMA + ".sousmatiere " +
                "WHERE id_matiere IN " + Sql.listPrepared(topics.getList());

        Sql.getInstance().prepared(query, params, createStatements(topics, subTopics, handler, statements));
    }

    private Handler<Message<JsonObject>> createStatements(JsonArray topics, JsonArray subTopics,
                                                          Handler<Either<String, JsonArray>> handler,
                                                          JsonArray statements) {
        return new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> event) {
                if (event.body().getString("status").equals("ok")) {
                    String id_topic;
                    Integer id_sub_topic;
                    if (subTopics.size() > 0) {
                        for (int i = 0; i < topics.size(); i++) {
                            id_topic = topics.getString(i);
                            for (int j = 0; j < subTopics.size(); j++) {
                                id_sub_topic = subTopics.getInteger(j);
                                statements.add(insertQuery(id_topic, id_sub_topic));
                            }
                            statements.add(updateEvaluationQuery(id_topic, subTopics.getInteger(0)));

                        }
                        handleStatementsTransaction(statements, handler);
                    } else {
                        for (int i = 0; i < topics.size(); i++) {
                            id_topic = topics.getString(i);
                            statements.add(resetEvaluationQuery(id_topic));
                        }
                        handleStatementsTransaction(statements, handler);
                    }
                } else {
                    handler.handle(new Either.Left<>("[Viescolaire@DefaultSousMatiereService] Failed to Delete previous relations"));
                    LOGGER.error("[Viescolaire@DefaultSousMatiereService] Failed to Delete previous relations");
                }
            }
        };
    }

    private void handleStatementsTransaction(JsonArray statements, Handler<Either<String, JsonArray>> handler) {
        Sql.getInstance().transaction(statements, statementResults -> {
            Either<String, JsonArray> either = SqlResult.validResult(0, statementResults);
            if (either.isLeft()) {
                String err = "[Viescolaire@DefaultSousMatiereService] Failed to insert New Relations";
                LOGGER.error(err, either.left().getValue());
            }
            handler.handle(either);
        });
    }
}
