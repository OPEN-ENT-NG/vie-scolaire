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
import fr.openent.viescolaire.service.GroupeService;
import fr.openent.viescolaire.service.UtilsService;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.Utils;
import io.vertx.core.eventbus.Message;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.utils.StringUtils;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Arrays;
import java.util.List;

/**
 * Created by vogelmt on 13/02/2017.
 */
public class DefaultGroupeService extends SqlCrudService implements GroupeService {

    private final Neo4j neo4j = Neo4j.getInstance();
    private UtilsService utilsService;
    public DefaultGroupeService () {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.VSCO_MATIERE_TABLE);
        utilsService = new DefaultUtilsService();
    }


    @Override
    public void listGroupesEnseignementsByUserId(final String userId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject values = new JsonObject();
        query.append("MATCH (u:User {id :{userId}}),(g:Group)")
                .append(" WHERE g.externalId IN users.groups return g ");
        values.put("userId", userId);

        neo4j.execute(query.toString(), values, new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> event) {
                if ("ok".equals(((JsonObject) event.body()).getString("status"))) {

                    JsonArray rNeo = ((JsonObject) event.body()).getJsonArray("result",
                            new fr.wseduc.webutils.collections.JsonArray());
                    // Si l'utilisateur est présent dans l'annuaire on renvoit le résultat
                    if (rNeo.size() > 0) {
                        handler.handle(new Either.Right(event));
                    } else {
                        // Sinon, ça peut être un élève supprimé, on va chercher s'il est enregistré dans les
                        // la base de donnée de viescolaire
                        String[] idEleves = new String[1];
                        idEleves[0] = userId;
                        new DefaultEleveService().getStoredDeletedStudent(null, null,
                                idEleves,rNeo,
                                new Handler<Either<String, JsonArray>>() {
                                    public void handle(Either<String, JsonArray> event) {
                                        if (event.isRight()) {
                                            UtilsService utilsService = new DefaultUtilsService();
                                            JsonArray rPostgres = event.right().getValue();

                                            if (rPostgres.size() > 0) {
                                                String idGroupe = rPostgres.getJsonObject(0)
                                                        .getString("idClasse");

                                                StringBuilder queryGroup = new StringBuilder();
                                                JsonObject valuesGroup = new JsonObject();
                                                queryGroup.append("MATCH (g:Group {id :{idGroupe}})")
                                                        .append(" WHERE g.externalId IN users.groups return g ");
                                                valuesGroup.put("idGroupe", idGroupe);

                                                // Avec l'id du groupe de l'utilisateur stocké dans la base de viesco
                                                // On refait une requête NEo pour renvoyer les informations du groupe
                                                neo4j.execute(queryGroup.toString(), valuesGroup,
                                                        Neo4jResult.validResultHandler(handler));
                                            }

                                        } else {
                                            handler.handle(new Either.Right(rNeo));
                                        }
                                    }
                                });

                    }
                } else {
                    handler.handle(new Either.Left<>("Error While Check groupeENseignement ID in Neo4J "));
                }
            }
        });
    }

    @Override
    public void getClasseGroupe(String[] idGroupe, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        query.append(" MATCH (n:FunctionalGroup)-[:IN]-(u:User{profiles:['Student']}) ")
                .append(" WHERE n.id IN {idGroupe} WITH  n, u ")
                .append(" MATCH (c:Class) WHERE c.externalId IN u.classes RETURN n.id as id_groupe, ")
                .append(" COLLECT(DISTINCT c.id) AS id_classes ")
                .append(" UNION ")
                .append(" MATCH (n:ManualGroup)-[:IN]-(u:User{profiles:['Student']}) ")
                .append(" WHERE n.id IN {idGroupe} WITH  n, u ")
                .append(" MATCH (c:Class) WHERE c.externalId IN u.classes RETURN n.id as id_groupe, ")
                .append(" COLLECT(DISTINCT c.id) AS id_classes ")
        ;
        params.put("idGroupe", new fr.wseduc.webutils.collections.JsonArray(Arrays.asList(idGroupe)))
                .put("idGroupe", new fr.wseduc.webutils.collections.JsonArray(Arrays.asList(idGroupe)));

        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void listUsersByGroupeEnseignementId(String groupeEnseignementId,String profile,
                                                Long idPeriode,
                                                Handler<Either<String, JsonArray>> handler) {


        JsonObject values = new JsonObject();
        String PROFILFILTER = " true" ;

        // Format de retour des données
        StringBuilder RETURNING = new StringBuilder().append( " RETURN DISTINCT users.lastName as lastName, ")
                .append(" users.firstName as firstName, users.id as id, users.deleteDate as deleteDate, ")
                .append(" users.login as login, users.activationCode as activationCode, users.birthDate as birthDate, ")
                .append(" users.blocked as blocked, users.source as source, c.name as className, c.id as classId ORDER")
                .append(" BY lastName, firstName ");

        if(!StringUtils.isEmpty(profile)){
            PROFILFILTER = " users.profiles =[{profile}] ";
            values.put("profile", profile);
        }

        // Si l'id en entrée est celui d'un groupe d'enseignement ou un groupe manuel
        StringBuilder query = new StringBuilder();
        query.append("MATCH (g:Group {id : {groupeEnseignementId}})<-[:IN]-(users)-[:IN]->(:ProfileGroup)-")
                .append("[:DEPENDS]->(c:Class) WHERE ")
                .append(PROFILFILTER)
                .append(RETURNING.toString());

        // Si l'id en entrée est celui d'une classe
        StringBuilder queryGetClass = new StringBuilder();
        queryGetClass.append("MATCH (users)-[:IN]->(:ProfileGroup)-")
                .append("[:DEPENDS]->(c:Class {id : {groupeEnseignementId}})  WHERE ")
                .append(PROFILFILTER)
                .append(RETURNING.toString());

        // Récupération des utilisateurs en instance de suppression (plus liés aux classes ni aux groupes)
        // Mais toujours présents dans l'annuaire
        StringBuilder queryGetDeleteUsers = new StringBuilder();
        queryGetDeleteUsers.append("MATCH (:DeleteGroup)<-[:IN]-(users:User)-[:HAS_RELATIONSHIPS]->(b:Backup),")
                .append(" (fgroup:Group) ")
                .append(" WHERE HAS(users.deleteDate) ")
                .append(" AND fgroup.id = {groupeEnseignementId} ")
                .append(" AND (fgroup.externalId IN users.groups  OR fgroup.id IN b.IN_OUTGOING) ")
                .append(" AND " + PROFILFILTER)
                .append(" OPTIONAL MATCH (c:Class) WHERE c.externalId IN users.classes ")
                .append(RETURNING.toString());


        query.append(" UNION ")
                .append(queryGetClass.toString())
                .append(" UNION ")
                .append(queryGetDeleteUsers.toString());

        values.put("groupeEnseignementId", groupeEnseignementId);

        // Rajout des élèves supprimés de l'annuaire qui sont stockés dans la base viescolaire
        String [] sortedField = new  String[2];
        sortedField[0] = "lastName";
        sortedField[1] = "firstName";
        neo4j.execute(query.toString(), values,
                utilsService.addStoredDeletedStudent(new JsonArray().add(groupeEnseignementId),
                        null,null, sortedField, idPeriode, handler));
    }

    @Override
    public void getNameOfGroupeClasse(String idGroupe, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject values = new JsonObject();
        query.append("MATCH (c:`Class` {id: {groupeId} }) RETURN c.id as id,  c.name as name ");
        query.append("UNION ");
        query.append("MATCH (g:`FunctionalGroup` {id: {groupeId}}) return g.id as id, g.name as name ");
        query.append("UNION ");
        query.append("MATCH (g:`ManualGroup` {id: {groupeId}}) return g.id as id, g.name as name ");
        values.put("groupeId", idGroupe);

        neo4j.execute(query.toString(), values, Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void search(String structure_id, String query, List<String> fields, Handler<Either<String, JsonArray>> handler) {
        String filter = "";

        for (int i = 0; i < fields.size(); i++) {
            String field = fields.get(i);
            if (i > 0) {
                filter += "OR ";
            }

            filter += "toLower(g." + field + ") CONTAINS '" + query.toLowerCase() + "' ";
        }

        String neo4jquery = "MATCH (g)-[:BELONGS|:DEPENDS]->(s:Structure {id:{structureId}}) WHERE " + filter +
                "AND (g:Class OR g:FunctionalGroup) " +
                "RETURN g.id as id, g.name as name " +
                "ORDER BY g.name";

        JsonObject params = new JsonObject()
                .put("structureId", structure_id);

        neo4j.execute(neo4jquery, params, Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void getTypesOfGroup(JsonArray groupsIds, Handler<Either<String, JsonArray>> handler) {
        String neo4jQuery="MATCH (c:FunctionalGroup) WHERE c.id IN {ids}"+
                "RETURN c.id as id,\"FunctionalGroup\" as type  " +
                "UNION " +
                "MATCH(c:Class)   WHERE c.id IN {ids}"+
                "RETURN c.id as id,\"Class\" as type  " +
                "UNION " +
                "Match(c:ManualGroup) WHERE c.id IN {ids}"+
                "RETURN c.id as id,\"ManualGroup\" as type" ;


        JsonObject params = new JsonObject()
                .put("ids", groupsIds);
        neo4j.execute(neo4jQuery, params, Neo4jResult.validResultHandler(handler));
    }

}
