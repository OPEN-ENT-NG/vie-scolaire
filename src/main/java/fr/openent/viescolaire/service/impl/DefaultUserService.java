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
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.sql.SqlStatementsBuilder;
import org.entcore.common.user.UserInfos;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static fr.wseduc.webutils.Utils.handlerToAsyncHandler;

/**
 * Created by ledunoiss on 08/11/2016.
 */
public class DefaultUserService implements UserService {

    private EventBus eb;
    private final Neo4j neo4j = Neo4j.getInstance();

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

        Sql.getInstance().prepared(query.toString(), new fr.wseduc.webutils.collections.JsonArray().add(user.getUserId()),
                SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void getStructures(UserInfos user, Handler<Either<String, JsonArray>> handler) {
        List<String> structures = user.getStructures();
        StringBuilder query = new StringBuilder()
                .append("SELECT * FROM " + Viescolaire.VSCO_SCHEMA + ".structure " +
                        "WHERE structure.fk4j_structure_id IN " + Sql.listPrepared(structures.toArray()));
        JsonArray params = new fr.wseduc.webutils.collections.JsonArray();
        for (int i = 0; i < structures.size(); i++) {
            params.add(structures.get(i));
        }
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getClasses(UserInfos user, Handler<Either<String, JsonArray>> handler) {
        List<String> classes = user.getClasses();
        JsonArray params = new fr.wseduc.webutils.collections.JsonArray();
        StringBuilder query = new StringBuilder()
                .append("SELECT * FROM " + Viescolaire.VSCO_SCHEMA + ".classe " +
                        "WHERE classe.fk4j_classe_id IN " + Sql.listPrepared(classes.toArray()));
        for (int i = 0; i < classes.size(); i++) {
            params.add(classes.get(i));
        }
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getMatiere(UserInfos user, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray params = new fr.wseduc.webutils.collections.JsonArray();
        switch (user.getType()) {
            case "Teacher" : {
                query.append("SELECT matiere.* " +
                        "FROM " + Viescolaire.VSCO_SCHEMA + ".matiere " +
                        "INNER JOIN " + Viescolaire.VSCO_SCHEMA + ".personnel ON (personnel.id = matiere.id_professeur)" +
                        "WHERE personnel.fk4j_user_id = ?");
                params.add(user.getUserId());
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
                    params.add(classes.get(i));
                }
            }
            break;
            default : {
                handler.handle(new Either.Right<String, JsonArray>(new fr.wseduc.webutils.collections.JsonArray()));
            }
        }
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getMoyenne(String idEleve, Long[] idDevoirs, final Handler<Either<String, JsonObject>> handler) {
        JsonObject action = new JsonObject()
                .put("action", "note.getNotesParElevesParDevoirs")
                .put("idEleves", new fr.wseduc.webutils.collections.JsonArray().add(idEleve))
                .put("idDevoirs", new fr.wseduc.webutils.collections.JsonArray(Arrays.asList(idDevoirs)));

        eb.send(Viescolaire.COMPETENCES_BUS_ADDRESS, action, handlerToAsyncHandler(new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> message) {
                if ("ok".equals(message.body().getString("status"))) {
                    JsonArray notes = new fr.wseduc.webutils.collections.JsonArray();
                    JsonArray listNotes = message.body().getJsonArray("results");

                    for (int i = 0; i < listNotes.size(); i++) {

                        JsonObject note = listNotes.getJsonObject(i);

                        JsonObject noteDevoir = new JsonObject()
                                .put("valeur", Double.valueOf(note.getString("valeur")))
                                .put("diviseur", Double.valueOf(note.getString("diviseur")))
                                .put("ramenerSur", note.getBoolean("ramener_sur"))
                                .put("coefficient", Double.valueOf(note.getString("coefficient")));

                        notes.add(noteDevoir);
                    }

                    Either<String, JsonObject> result = null;

                    if (notes.size() > 0) {
                        JsonObject action = new JsonObject()
                                .put("action", "note.calculMoyenne")
                                .put("listeNoteDevoirs", notes)
                                .put("statistiques", false)
                                .put("diviseurM", 20);

                        eb.send(Viescolaire.COMPETENCES_BUS_ADDRESS, action, handlerToAsyncHandler(new Handler<Message<JsonObject>>() {
                            @Override
                            public void handle(Message<JsonObject> message) {
                                Either<String, JsonObject> result = null;
                                if ("ok".equals(message.body().getString("status"))) {
                                    result = new Either.Right<String, JsonObject>(message.body().getJsonObject("result"));
                                    handler.handle(result);
                                } else {
                                    result = new Either.Left<>(message.body().getString("message"));
                                    handler.handle(result);
                                }
                            }
                        }));
                    } else {
                        result = new Either.Right<>(new JsonObject());
                    }
                    handler.handle(result);
                } else {
                    handler.handle(new Either.Left<String, JsonObject>(message.body().getString("message")));
                }
            }
        }));
    }

    @Override
    public void createPersonnesSupp(JsonArray users, Handler<Either<String, JsonObject>> handler) {
        SqlStatementsBuilder statements = new SqlStatementsBuilder();
        for (Object u : users) {
            if (!(u instanceof JsonObject) || !validProfile((JsonObject) u)) {
                continue;
            }
            final JsonObject user = (JsonObject) u;
            // Insert user in the right table
            String uQuery =
                    "INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".personnes_supp(id_user, display_name, user_type, first_name, last_name) " +
                    "VALUES (?, ?, ?, ?, ?);";
            JsonArray uParams = new fr.wseduc.webutils.collections.JsonArray()
                    .add(user.getString("id"))
                    .add(user.getString("displayName"))
                    .add(user.getString("type"))
					.add(user.getString("firstName"))
					.add(user.getString("lastName"));

            statements.prepared(uQuery, uParams);

            if (user.containsKey("classIds") && user.getJsonArray("classIds").size() > 0) {
               formatGroups(user.getJsonArray("classIds"), user.getString("id"), statements, Viescolaire.CLASSE_TYPE);
            }

            if (user.containsKey("groupIds") && user.getJsonArray("groupIds").size() > 0) {
                formatGroups(user.getJsonArray("groupIds"), user.getString("id"), statements, Viescolaire.GROUPE_TYPE);
            }

            if (user.containsKey("structureIds") && user.getJsonArray("structureIds").size() > 0) {
                formatStructure(user.getJsonArray("structureIds"), user.getString("id"), statements);
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
            JsonArray params = new fr.wseduc.webutils.collections.JsonArray()
                    .add(ids.getString(i))
                    .add(userId)
                    .add(type);
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
            JsonArray params = new fr.wseduc.webutils.collections.JsonArray()
                    .add(ids.getString(i))
                    .add(userId);
            statements.prepared(query, params);
        }
    }

    /**
     * Recupere les établissements inactifs de l'utilisateur connecté
     * @param userInfos : utilisateur connecté
     * @param handler handler comportant le resultat
     */
    @Override
    public void getActivesIDsStructures(UserInfos userInfos, String module,
                                        Handler<Either<String, JsonArray>> handler) {
        StringBuilder query =new StringBuilder();
        JsonArray params = new fr.wseduc.webutils.collections.JsonArray();

        query.append("SELECT id_etablissement ")
                .append("FROM "+ module +".etablissements_actifs  ")
                .append("WHERE id_etablissement IN " + Sql.listPrepared(userInfos.getStructures().toArray()))
                .append(" AND actif = TRUE");

        for(String idStructure :  userInfos.getStructures()){
            params.add(idStructure);
        }
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));
    }

    /**
     * Recupere les établissements inactifs de l'utilisateur connecté
     * @param handler handler comportant le resultat
     */
    @Override
    public void getActivesIDsStructures( String module,
                                        Handler<Either<String, JsonArray>> handler) {
        StringBuilder query =new StringBuilder();
        JsonArray params = new fr.wseduc.webutils.collections.JsonArray();

        query.append("SELECT id_etablissement ")
                .append("FROM "+ module +".etablissements_actifs  ")
                .append("WHERE actif = TRUE");

        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));
    }

    /**
     * Active un établissement
     * @param id : établissement
     * @param handler handler comportant le resultat
     */
    @Override
    public void createActiveStructure (String id, String module, UserInfos user,
                                       Handler<Either<String, JsonArray>> handler) {
        SqlStatementsBuilder s = new SqlStatementsBuilder();
        JsonObject data = new JsonObject();
        String userQuery = "SELECT " + module + ".merge_users(?,?)";
        s.prepared(userQuery, (new fr.wseduc.webutils.collections.JsonArray()).add(user.getUserId()).add(user.getUsername()));
        data.put("id_etablissement", id);
        data.put("actif", true);
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
        Sql.getInstance().prepared(query, (new fr.wseduc.webutils.collections.JsonArray()).add(Sql.parseId(id)), SqlResult.validResultHandler(handler));
    }

    @Override
    public void getUAI(String idEtabl, Handler<Either<String,JsonObject>> handler){
        StringBuilder query= new StringBuilder();
        query.append("MATCH(s:Structure) WHERE s.id={id} RETURN s.UAI as uai");
        Neo4j.getInstance().execute(query.toString(), new JsonObject().put("id", idEtabl), Neo4jResult.validUniqueResultHandler(handler));
    }

    @Override
    public void getResponsablesEtabl(List<String> idsResponsable, Handler<Either<String,JsonArray>> handler){
        StringBuilder query=new StringBuilder();
        query.append("MATCH (u:User) WHERE u.id IN {id} RETURN u.externalId as externalId, u.displayName as displayName");
        Neo4j.getInstance().execute(query.toString(), new JsonObject().put("id",new fr.wseduc.webutils.collections.JsonArray(idsResponsable)), Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void getElevesRelatives(List<String> idsClass,Handler<Either<String,JsonArray>> handler){
        StringBuilder query = new StringBuilder();
        JsonObject param = new JsonObject();
        query.append("MATCH (c:Class)<-[:DEPENDS]-(:ProfileGroup)<-[:IN]-(u:User {profiles:['Student']})-[:RELATED]-(r:User{profiles:['Relative']}) WHERE c.id IN {idClass}");
        query.append(" RETURN u.id as idNeo4j, u.externalId as externalId,u.attachmentId as attachmentId,u.lastName as lastName,u.level as level,u.firstName as firstName,u.relative as relative,");
        query.append("r.externalId as externalIdRelative, r.title as civilite, r.lastName as lastNameRelative, r.firstName as firstNameRelative, r.address as address, r.zipCode as zipCode, r.city as city,");
        query.append("c.id as idClass, c.name as nameClass, c.externalId as externalIdClass ORDER BY nameClass, lastName");
        param.put("idClass", new fr.wseduc.webutils.collections.JsonArray(idsClass));
        Neo4j.getInstance().execute(query.toString(), param, Neo4jResult.validResultHandler(handler));
    }


    @Override
    public void getCodeDomaine(String idClass,Handler<Either<String,JsonArray>> handler){
        StringBuilder query = new StringBuilder();
          query.append("SELECT id_groupe,id as id_domaine, code_domaine as code_domaine ");
          query.append("FROM notes.domaines INNER JOIN notes.rel_groupe_cycle ");
          query.append("ON notes.domaines.id_cycle= notes.rel_groupe_cycle.id_cycle ");
          query.append("WHERE notes.rel_groupe_cycle.id_groupe = ? AND code_domaine IS NOT NULL");
        JsonArray params = new fr.wseduc.webutils.collections.JsonArray();

        params.add(idClass);
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));

    }

    @Override
    public void getResponsablesDirection(String idStructure, Handler<Either<String, JsonArray>> handler) {
        String query = "MATCH (u:User{profiles:['Personnel']})-[ADMINISTRATIVE_ATTACHMENT]->(s:Structure {id:{structureId}}) " +
                "WHERE ANY(function IN u.functions WHERE function =~ '(?i).*\\\\$DIR\\\\$.*')" +
                " RETURN u.id as id, u.displayName as displayName, u.externalId as externalId";
        JsonObject param = new JsonObject();
        param.put("structureId",idStructure);
        Neo4j.getInstance().execute(query,param,Neo4jResult.validResultHandler(handler));
    }


    /**
     * Retourne la liste des enfants pour un utilisateur donné
     * @param idUser    Id de l'utilisateur
     * @param handler   Handler comportant le resultat de la requete
     */
    @Override
    public void getEnfants(String idUser, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        query.append("MATCH (m:User {id: {id}})<-[:RELATED]-(n:User)")
                .append("RETURN n.id as id, n.firstName as firstName, n.lastName as lastName,  n.level as level, n.classes as classes, n.birthDate as birthDate ORDER BY lastName");
        neo4j.execute(query.toString(), new JsonObject().put("id", idUser), Neo4jResult.validResultHandler(handler));
    }

    /**
     * Retourne la liste des personnels pour une liste d'id donnée
     *
     * @param idPersonnels ids des personnels
     * @param handler      Handler comportant le resultat de la requete
     */
    public void getPersonnels(List<String> idPersonnels, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        query.append("MATCH (u:User)")
                .append("WHERE ANY (x IN u.profiles WHERE x IN ['Teacher', 'Personnel']) AND u.id IN {idPersonnel}")
                .append("RETURN u.id as id, u.lastName as lastName, u.firstName as firstName, u.emailAcademy as emailAcademy");
        neo4j.execute(query.toString(), new JsonObject().put("idPersonnel", new fr.wseduc.webutils.collections.JsonArray(idPersonnels)), Neo4jResult.validResultHandler(handler));

    }

    @Override
    public void list(String structureId, String classId, String groupId,
                     JsonArray expectedProfiles, String filterActivated, String nameFilter,
                     UserInfos userInfos, Handler<Either<String, JsonArray>> results) {
        JsonObject params = new JsonObject();
        String filter = "";
        String filterProfile = "WHERE 1=1 ";
        String optionalMatch =
                "OPTIONAL MATCH u-[:IN]->(:ProfileGroup)-[:DEPENDS]->(class:Class)-[:BELONGS]->(s) " +
                        "OPTIONAL MATCH u-[:RELATED]->(parent: User) " +
                        "OPTIONAL MATCH (child: User)-[:RELATED]->u " +
                        "OPTIONAL MATCH u-[rf:HAS_FUNCTION]->fg-[:CONTAINS_FUNCTION*0..1]->(f:Function) ";
        if (expectedProfiles != null && expectedProfiles.size() > 0) {
            filterProfile += "AND p.name IN {expectedProfiles} ";
            params.put("expectedProfiles", expectedProfiles);
        }
        if (classId != null && !classId.trim().isEmpty()) {
            filter = "(n:Class {id : {classId}})<-[:DEPENDS]-(g:ProfileGroup)<-[:IN]-";
            params.put("classId", classId);
        } else if (structureId != null && !structureId.trim().isEmpty()) {
            filter = "(n:Structure {id : {structureId}})<-[:DEPENDS]-(g:ProfileGroup)<-[:IN]-";
            params.put("structureId", structureId);
        } else if (groupId != null && !groupId.trim().isEmpty()) {
            filter = "(n:Group {id : {groupId}})<-[:IN]-";
            params.put("groupId", groupId);
        }
        String condition = "";
        String functionMatch = "WITH u MATCH (s:Structure)<-[:DEPENDS]-(pg:ProfileGroup)-[:HAS_PROFILE]->(p:Profile), u-[:IN]->pg ";

        if(nameFilter != null && !nameFilter.trim().isEmpty()){
            condition += "AND u.displayName =~ {regex}  ";
            params.put("regex", "(?i)^.*?" + Pattern.quote(nameFilter.trim()) + ".*?$");
        }
        if(filterActivated != null){
            if("inactive".equals(filterActivated)){
                condition += "AND NOT(u.activationCode IS NULL)  ";
            } else if("active".equals(filterActivated)){
                condition += "AND u.activationCode IS NULL ";
            }
        }

        String query =
                "MATCH " + filter + "(u:User) " +
                        functionMatch + filterProfile + condition + optionalMatch +
                        "RETURN DISTINCT u.id as id, p.name as type, u.externalId as externalId, " +
                        "u.activationCode as code, u.login as login, u.firstName as firstName, " +
                        "u.lastName as lastName, u.displayName as displayName, u.source as source, u.attachmentId as attachmentId, " +
                        "u.birthDate as birthDate, " +
                        "extract(function IN u.functions | last(split(function, \"$\"))) as aafFunctions, " +
                        "collect(distinct {id: s.id, name: s.name}) as structures, " +
                        "collect(distinct {id: class.id, name: class.name}) as allClasses, " +
                        "collect(distinct [f.externalId, rf.scope]) as functions, " +
                        "CASE WHEN parent IS NULL THEN [] ELSE collect(distinct {id: parent.id, firstName: parent.firstName, lastName: parent.lastName}) END as parents, " +
                        "CASE WHEN child IS NULL THEN [] ELSE collect(distinct {id: child.id, firstName: child.firstName, lastName: child.lastName, attachmentId : child.attachmentId }) END as children, " +
                        "HEAD(COLLECT(distinct parent.externalId)) as parent1ExternalId, " + // Hack for GEPI export
                        "HEAD(TAIL(COLLECT(distinct parent.externalId))) as parent2ExternalId " + // Hack for GEPI export
                        "ORDER BY type DESC, displayName ASC ";
        neo4j.execute(query, params,  Neo4jResult.validResultHandler(results));
    }

}