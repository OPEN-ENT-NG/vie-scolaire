/*
 * Copyright (c) Région Hauts-de-France, Département 77, CGI, 2016.
 *
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
 *
 */

package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.UserService;
import fr.wseduc.webutils.Either;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.sql.SqlStatementsBuilder;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.List;

/**
 * Created by ledunoiss on 08/11/2016.
 */
public class DefaultUserService implements UserService {

    private EventBus eb;

    public DefaultUserService(EventBus eb) {
        this.eb = eb;
    }

    @Override
    public void getUserId(UserInfos user, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder()
                .append("SELECT id FROM " + Viescolaire.VSCO_SCHEMA);
        switch (user.getType()) {
            case "Teacher":
            case "Personnel": {
                query.append(".personnel");
            }
            break;
            case "Relative": {
                query.append(".parent");
            }
            break;
            case "Student": {
                query.append(".eleve");
            }
            break;
        }
        query.append(" WHERE fk4j_user_id = ?;");

        Sql.getInstance().prepared(query.toString(), new JsonArray().addString(user.getUserId()), SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void getStructures(UserInfos user, Handler<Either<String, JsonArray>> handler) {
        List<String> structures = user.getStructures();
        StringBuilder query = new StringBuilder()
                .append("SELECT * FROM " + Viescolaire.VSCO_SCHEMA + ".structure " +
                        "WHERE structure.fk4j_structure_id IN " + Sql.listPrepared(structures.toArray()));
        JsonArray params = new JsonArray();
        for (int i = 0; i < structures.size(); i++) {
            params.addString(structures.get(i));
        }
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getClasses(UserInfos user, Handler<Either<String, JsonArray>> handler) {
        List<String> classes = user.getClasses();
        JsonArray params = new JsonArray();
        StringBuilder query = new StringBuilder()
                .append("SELECT * FROM " + Viescolaire.VSCO_SCHEMA + ".classe " +
                        "WHERE classe.fk4j_classe_id IN " + Sql.listPrepared(classes.toArray()));
        for (int i = 0; i < classes.size(); i++) {
            params.addString(classes.get(i));
        }
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getMatiere(UserInfos user, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray params = new JsonArray();
        switch (user.getType()) {
            case "Teacher" : {
                query.append("SELECT matiere.* " +
                        "FROM " + Viescolaire.VSCO_SCHEMA + ".matiere " +
                        "INNER JOIN " + Viescolaire.VSCO_SCHEMA + ".personnel ON (personnel.id = matiere.id_professeur)" +
                        "WHERE personnel.fk4j_user_id = ?");
                params.addString(user.getUserId());
            }
            break;
            case "Eleve" : {
                List<String> classes = user.getClasses();
                query.append("SELECT matiere.* " +
                        "FROM " + Viescolaire.VSCO_SCHEMA + ".matiere INNER JOIN " + Viescolaire.VSCO_SCHEMA + ".personnel ON (matiere.id_professeur = personnel.id) " +
                        "INNER JOIN " + Viescolaire.VSCO_SCHEMA + ".rel_personnel_classe ON (personnel.id = rel_personnel_classe.id_personnel) " +
                        "INNER JOIN " + Viescolaire.VSCO_SCHEMA + ".classe ON (rel_personnel_classe.id_classe = classe.id) " +
                        "WHERE classe.externalid IN " + Sql.listPrepared(classes.toArray()));
                for (int i = 0; i < classes.size(); i++) {
                    params.addString(classes.get(i));
                }
            }
            break;
            default : {
                handler.handle(new Either.Right<String, JsonArray>(new JsonArray()));
            }
        }
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getMoyenne(String idEleve, Long[] idDevoirs, final Handler<Either<String, JsonObject>> handler) {
        JsonObject action = new JsonObject()
                .putString("action", "note.getNotesParElevesParDevoirs")
                .putArray("idEleves", new JsonArray().addString(idEleve))
                .putArray("idDevoirs", new JsonArray(idDevoirs));

        eb.send(Viescolaire.COMPETENCES_BUS_ADDRESS, action, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> message) {
                if ("ok".equals(message.body().getString("status"))) {
                    JsonArray notes = new JsonArray();
                    JsonArray listNotes = message.body().getArray("results");

                    for (int i = 0; i < listNotes.size(); i++) {

                        JsonObject note = listNotes.get(i);

                        JsonObject noteDevoir = new JsonObject()
                                .putNumber("valeur", Double.valueOf(note.getString("valeur")))
                                .putNumber("diviseur", Double.valueOf(note.getString("diviseur")))
                                .putBoolean("ramenerSur", note.getBoolean("ramener_sur"))
                                .putNumber("coefficient", Double.valueOf(note.getString("coefficient")));

                        notes.add(noteDevoir);
                    }

                    Either<String, JsonObject> result = null;

                    if (notes.size() > 0) {
                        JsonObject action = new JsonObject()
                                .putString("action", "note.calculMoyenne")
                                .putArray("listeNoteDevoirs", notes)
                                .putBoolean("statistiques", false)
                                .putNumber("diviseurM", 20);

                        eb.send(Viescolaire.COMPETENCES_BUS_ADDRESS, action, new Handler<Message<JsonObject>>() {
                            @Override
                            public void handle(Message<JsonObject> message) {
                                Either<String, JsonObject> result = null;
                                if ("ok".equals(message.body().getString("status"))) {
                                    result = new Either.Right<String, JsonObject>(message.body().getObject("result"));
                                    handler.handle(result);
                                } else {
                                    result = new Either.Left<>(message.body().getString("message"));
                                    handler.handle(result);
                                }
                            }
                        });
                    } else result = new Either.Right<>(new JsonObject());
                    handler.handle(result);
                } else {
                    handler.handle(new Either.Left<String, JsonObject>(message.body().getString("message")));
                }
            }
        });
    }

    @Override
    public void createPersonnesSupp(JsonArray users, Handler<Either<String, JsonObject>> handler) {
        SqlStatementsBuilder statements = new SqlStatementsBuilder();
        for (Object u : users) {
            if (!(u instanceof JsonObject) || !validProfile((JsonObject) u)) continue;
            final JsonObject user = (JsonObject) u;
            // Insert user in the right table
            String uQuery =
                    "INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".personnes_supp(id_user, display_name, user_type, first_name, last_name) " +
                    "VALUES (?, ?, ?, ?, ?);";
            JsonArray uParams = new JsonArray()
                    .addString(user.getString("id"))
                    .addString(user.getString("displayName"))
                    .addString(user.getString("type"))
					.addString(user.getString("firstName"))
					.addString(user.getString("lastName"));

            statements.prepared(uQuery, uParams);

            if (user.containsField("classIds") && user.getArray("classIds").size() > 0) {
               formatGroups(user.getArray("classIds"), user.getString("id"), statements, Viescolaire.CLASSE_TYPE);
            }

            if (user.containsField("groupIds") && user.getArray("groupIds").size() > 0) {
                formatGroups(user.getArray("groupIds"), user.getString("id"), statements, Viescolaire.GROUPE_TYPE);
            }

            if (user.containsField("structureIds") && user.getArray("structureIds").size() > 0) {
                formatStructure(user.getArray("structureIds"), user.getString("id"), statements);
            }
        }
        Sql.getInstance().transaction(statements.build(), SqlResult.validUniqueResultHandler(handler));
    }

    /**
     * Inject creation request in SqlStatementBuilder for every class in ids
     * @param ids class ids list
     * @param userId user id
     * @param statements Sql statement builder
     * @param type Group type
     */
    private static void formatGroups (JsonArray ids, String userId, SqlStatementsBuilder statements, Integer type) {
        for (int i = 0; i < ids.size(); i++) {
            String query =
                    "INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".rel_groupes_personne_supp(id_groupe, id_user, type_groupe) " +
                    "VALUES (?, ?, ?);";
            JsonArray params = new JsonArray()
                    .addString(ids.get(i).toString())
                    .addString(userId)
                    .addNumber(type);
            statements.prepared(query, params);
        }
    }

    /**
     * Return if the user is a valid profile user
     * @param user user object
     * @return true | false if the profile is a valid profile
     */
    private static boolean validProfile (JsonObject user) {
        return "Teacher".equals(user.getString("type")) || "Student".equals(user.getString("type"));
    }

    /**
     * Inject creation request in SqlStatementBuilder for every stucture in ids
     * @param ids structure ids list
     * @param userId user id
     * @param statements Sql statement builder
     */
    private static void formatStructure (JsonArray ids, String userId, SqlStatementsBuilder statements) {
        for (int i = 0; i < ids.size(); i++) {
            String query =
                    "INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".rel_structures_personne_supp(id_structure, id_user) " +
                            "VALUES (?, ?);";
            JsonArray params = new JsonArray()
                    .addString(ids.get(i).toString())
                    .addString(userId);
            statements.prepared(query, params);
        }
    }

    /**
     * Recupere les établissements inactifs de l'utilisateur connecté
     * @param userInfos : utilisateur connecté
     * @param handler handler comportant le resultat
     */
    @Override
    public void getActivesIDsStructures(UserInfos userInfos, String module, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query =new StringBuilder();
        JsonArray params = new JsonArray();

        query.append("SELECT id_etablissement ")
                .append("FROM "+ module +".etablissements_actifs  ")
                .append("WHERE id_etablissement IN " + Sql.listPrepared(userInfos.getStructures().toArray()))
                .append(" AND actif = TRUE");

        for(String idStructure :  userInfos.getStructures()){
            params.addString(idStructure);
        }
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));
    }

    /**
     * Active un établissement
     * @param id : établissement
     * @param handler handler comportant le resultat
     */
    @Override
    public void createActiveStructure (String id, String module, UserInfos user,Handler<Either<String, JsonArray>> handler) {
        SqlStatementsBuilder s = new SqlStatementsBuilder();
        JsonObject data = new JsonObject();
        String userQuery = "SELECT " + module + ".merge_users(?,?)";
        s.prepared(userQuery, (new JsonArray()).add(user.getUserId()).add(user.getUsername()));
        data.putString("id_etablissement", id);
        data.putBoolean("actif", true);
        s.insert(module + ".etablissements_actifs ", data, "id_etablissement");
        Sql.getInstance().transaction(s.build(), SqlResult.validResultHandler(handler));

    }

    /**
     * Supprime un étbalissement actif
     * @param id : utilisateur connecté
     * @param handler handler comportant le resultat
     */
    @Override
    public void deleteActiveStructure(String id, String module, Handler<Either<String, JsonArray>> handler) {
        String query = "DELETE FROM " + module + ".etablissements_actifs WHERE id_etablissement = ?";
        Sql.getInstance().prepared(query, (new JsonArray()).add(Sql.parseId(id)), SqlResult.validResultHandler(handler));
    }

    @Override
    public void getUAI(String idEtabl, Handler<Either<String,JsonObject>> handler){
        StringBuilder query= new StringBuilder();
        query.append("MATCH(s:Structure) WHERE s.id={id} RETURN s.UAI as uai");
        Neo4j.getInstance().execute(query.toString(), new JsonObject().putString("id", idEtabl), Neo4jResult.validUniqueResultHandler(handler));
    }

    @Override
    public void getResponsablesEtabl(List<String> idsResponsable, Handler<Either<String,JsonArray>> handler){
        StringBuilder query=new StringBuilder();
        query.append("MATCH (u:User) WHERE u.id IN {id} RETURN u.externalId as externalId, u.displayName as displayName");
        Neo4j.getInstance().execute(query.toString(), new JsonObject().putArray("id",new JsonArray(idsResponsable.toArray())), Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void getElevesRelatives(List<String> idsClass,Handler<Either<String,JsonArray>> handler){
        StringBuilder query = new StringBuilder();
        JsonObject param = new JsonObject();
        query.append("MATCH (c:Class)<-[:DEPENDS]-(:ProfileGroup)<-[:IN]-(u:User {profiles:['Student']})-[:RELATED]-(r:User{profiles:['Relative']}) WHERE c.id IN {idClass}");
        query.append(" RETURN u.id as idNeo4j, u.externalId as externalId,u.attachmentId as attachmentId,u.lastName as lastName,u.level as level,u.firstName as firstName,u.relative as relative,");
        query.append("r.externalId as externalIdRelative, r.lastName as lastNameRelative, r.firstName as firstNameRelative, r.address as address, r.zipCode as zipCode, r.city as city,");
        query.append("c.id as idClass, c.name as nameClass ORDER BY nameClass, lastName");
        param.putArray("idClass", new JsonArray(idsClass.toArray()));
        Neo4j.getInstance().execute(query.toString(), param, Neo4jResult.validResultHandler(handler));
    }


    @Override
    public void getCodeDomaine(String idClass,Handler<Either<String,JsonArray>> handler){
        StringBuilder query = new StringBuilder();
          query.append("SELECT id_groupe,id as id_domaine, code_domaine as code_domaine ");
          query.append("FROM notes.domaines INNER JOIN notes.rel_groupe_cycle ");
          query.append("ON notes.domaines.id_cycle= notes.rel_groupe_cycle.id_cycle ");
          query.append("WHERE notes.rel_groupe_cycle.id_groupe = ? AND code_domaine IS NOT NULL");
        JsonArray params = new JsonArray();

        params.addString(idClass);
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));

    }

    @Override
    public void getResponsablesDirection(String idStructure, Handler<Either<String, JsonArray>> handler) {
        String query = "MATCH (u:User{profiles:['Personnel']})-[ADMINISTRATIVE_ATTACHMENT]->(s:Structure {id:{structureId}}) " +
                "WHERE ANY(function IN u.functions WHERE function =~ '(?i).*\\\\$DIR\\\\$.*')" +
                " RETURN u.id as id, u.displayName as displayName, u.externalId as externalId";
        JsonObject param = new JsonObject();
        param.putString("structureId",idStructure);
        Neo4j.getInstance().execute(query,param,Neo4jResult.validResultHandler(handler));
    }


}