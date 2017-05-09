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
import fr.wseduc.webutils.Either;
import fr.openent.viescolaire.service.ClasseService;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.entcore.common.sql.SqlResult;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by ledunoiss on 19/02/2016.
 */
public class DefaultClasseService extends SqlCrudService implements ClasseService {

    private final Neo4j neo4j = Neo4j.getInstance();

    private static final String mParameterIdClasse = "idClasse";
    
    public DefaultClasseService() {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.VSCO_CLASSE_TABLE);
    }

    @Override
    public void getEleveClasse(  String idClasse, Handler<Either<String, JsonArray>> handler){
        StringBuilder query = new StringBuilder();
        query.append("Match (c:Class{id: {idClasse} }) with c ")
                .append( "MATCH (u:User{profiles :['Student']}) where c.externalId IN u.classes  ")
                .append( "RETURN u.id as id, u.firstName as firstName, u.lastName as lastName,  u.level as level, u.classes as classes ORDER BY lastName");

        neo4j.execute(query.toString(), new JsonObject().putString(mParameterIdClasse, idClasse), Neo4jResult.validResultHandler(handler));

    }

    @Override
    public void getNbElevesGroupe(JsonArray idGroupes, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject values = new JsonObject();

        query.append("MATCH (u:User)-[:IN]-(:ProfileGroup)--(f:Class) WHERE u.profiles=[\"Student\"] AND f.id IN {idClasse} " +
                "RETURN f.id as id_groupe, count(distinct u) as nb " +
                        "UNION ALL MATCH (u:User)--(f:FunctionalGroup) WHERE u.profiles=[\"Student\"] AND f.id IN {idGroupe} " +
                "RETURN f.id as id_groupe, count(distinct u) as nb");

        values.putArray(mParameterIdClasse, idGroupes);
        values.putArray("idGroupe", idGroupes);

        neo4j.execute(query.toString(), values, Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void getEleveClasses(String idEtablissement, JsonArray idClasse,Boolean isTeacher, Handler<Either<String, JsonArray>> handler){
        StringBuilder query = new StringBuilder();
        JsonObject params =  new JsonObject();
        query.append("MATCH (s:Structure {id:'"+idEtablissement+"'})<-[BELONGS]-(c:Class)<-[DEPENDS]")
                .append("-(:ProfileGroup)<-[IN]-(u:User {profiles: ['Student']}) ");
        if(isTeacher){
            query.append(" WHERE c.id IN {idClasse}");
            params.putArray(mParameterIdClasse, idClasse);
        }
        query.append("RETURN distinct(u.id) as id, u.displayName as displayName, u.firstName as firstName, u.lastName as lastName, c.id as idClasse ORDER BY displayName");

        neo4j.execute(query.toString(),params, Neo4jResult.validResultHandler(handler));

    }

    /**
     * Récupère la liste des classes de l'utilisateur
     * @param user
     * @param handler handler portant le résultat de la requête
     */
    @Override
    public void listClasses(String idEtablissement, UserInfos user, Handler<Either<String, JsonArray>> handler) {
        String query;
        JsonObject params = new JsonObject();
        // Dans le cas du chef d'établissement, on récupère toutes les classes
        if("Personnel".equals(user.getType())  && user.getFunctions().containsKey("DIR")){
            query = "MATCH (g:Class)-[b:BELONGS]->(s:Structure) WHERE s.id = {idEtablissement} return g " +
                    "UNION ALL " +
                    "MATCH (g:FunctionalGroup)-[d:DEPENDS]->(s:Structure) where s.id = {idEtablissement} return g";
            params.putString("idEtablissement", idEtablissement);
        }
        else {
            query = "MATCH (g:Class)-[b:BELONGS]->(s:Structure) WHERE g.id IN {classes} AND s.id = {idEtablissement} return g " +
                    "UNION ALL " +
                    "MATCH (g:FunctionalGroup)-[d:DEPENDS]->(s:Structure) WHERE g.id IN {groups} AND s.id = {idEtablissement} return g";
            params = new JsonObject();
            params.putArray("classes", new JsonArray(user.getClasses().toArray()))
                    .putArray("groups", new JsonArray(user.getGroupsIds().toArray()))
                    .putString("idEtablissement", idEtablissement);
        }
        neo4j.execute(query, params, Neo4jResult.validResultHandler(handler));
    }
}
