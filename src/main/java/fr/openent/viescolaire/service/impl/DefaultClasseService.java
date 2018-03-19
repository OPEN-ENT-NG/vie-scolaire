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
import fr.openent.viescolaire.service.ClasseService;
import fr.wseduc.webutils.Either;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
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
    public void getClasseEtablissement(String idEtablissement, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        query.append("MATCH (c:Class)-[BELONGS]->(s:Structure) WHERE s.id = {idEtablissement} RETURN c.id as idClasse ORDER BY c.name");
        params.putString("idEtablissement", idEtablissement);

        neo4j.execute(query.toString(),params, Neo4jResult.validResultHandler(handler));
    }

    //TODO Revoir avec getEleveClasses
    @Override
    public void getEleveClasse(  String idClasse, Handler<Either<String, JsonArray>> handler){
		String RETURN_VALUES = "RETURN u.id as id, u.firstName as firstName, u.lastName as lastName,"
                            + " u.level as level, u.classes as classes, " +
                " CASE WHEN u.birthDate IS NULL THEN 'undefined' ELSE u.birthDate END AS birthDate " +
                " ORDER BY lastName, firstName ";
		StringBuilder query = new StringBuilder();
        query.append("MATCH (u:User {profiles: ['Student']})-[:IN]-(:ProfileGroup)-[:DEPENDS]-(c:Class {id: {idClasse}}) ")
				.append(RETURN_VALUES)
				.append("UNION MATCH (u:User {profiles: ['Student']})-[:IN]-(c:FunctionalGroup {id: {idClasse}}) ")
				.append(RETURN_VALUES)
                .append("UNION MATCH (u:User {profiles: ['Student']})-[:IN]-(c:ManualGroup {id: {idClasse}}) ")
                .append(RETURN_VALUES);
        neo4j.execute(query.toString(), new JsonObject().putString(mParameterIdClasse, idClasse), Neo4jResult.validResultHandler(handler));

    }

    @Override
    public void getNbElevesGroupe(JsonArray idGroupes, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject values = new JsonObject();

        query.append("MATCH (u:User)-[:IN]-(:ProfileGroup)--(f:Class) WHERE u.profiles=[\"Student\"] " +
                " AND f.id IN {idClasse} RETURN f.id as id_groupe, count(distinct u) as nb " +
                " UNION ALL MATCH (u:User)--(f:FunctionalGroup) WHERE u.profiles=[\"Student\"] " +
                " AND f.id IN {idGroupe} RETURN f.id as id_groupe, count(distinct u) as nb " +
                " UNION ALL MATCH (u:User)--(f:ManualGroup) WHERE u.profiles=[\"Student\"] " +
                " AND f.id IN {idGroupe} RETURN f.id as id_groupe, count(distinct u) as nb ");

        values.putArray(mParameterIdClasse, idGroupes);
        values.putArray("idGroupe", idGroupes);

        neo4j.execute(query.toString(), values, Neo4jResult.validResultHandler(handler));
    }

    //TODO Revoir avec getEleveClasse
    @Override
    public void getEleveClasses(String idEtablissement, JsonArray idClasse,Boolean isTeacher, Handler<Either<String, JsonArray>> handler){
        StringBuilder query = new StringBuilder();
        JsonObject params =  new JsonObject();
        query.append("MATCH (s:Structure)<-[BELONGS]-(c:Class)<-[DEPENDS]")
                .append("-(:ProfileGroup)<-[IN]-(u:User {profiles: ['Student']}) WHERE s.id = {idEtablissement} ");
        params.putString("idEtablissement", idEtablissement);
        if(isTeacher){
            query.append(" AND c.id IN {idClasse}");
            params.putArray(mParameterIdClasse, idClasse);
        }
        query.append("RETURN distinct(u.id) as id, u.displayName as displayName, u.firstName as firstName, u.lastName as lastName, c.id as idClasse ORDER BY displayName");

        neo4j.execute(query.toString(),params, Neo4jResult.validResultHandler(handler));

    }

    @Override
    public void getElevesGroupesClasses(String[] idClasses, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        query.append("MATCH (u:User {profiles: ['Student']})-[:IN]-(:ProfileGroup)-[:DEPENDS]-(c:Class)")
                .append(" WHERE c.id IN {idClasses}")
                .append(" RETURN distinct(u.id) as id, u.displayName as displayName, u.firstName as firstName,")
                .append(" u.lastName as lastName, c.id as idClasse ORDER BY displayName")
                .append(" UNION MATCH (u:User {profiles: ['Student']})-[:IN]-(c:FunctionalGroup) ")
                .append(" WHERE c.id IN {idClasses}")
                .append(" RETURN distinct(u.id) as id, u.displayName as displayName, u.firstName as firstName, ")
                .append(" u.lastName as lastName, c.id as idClasse ORDER BY displayName")
                .append(" UNION MATCH (u:User {profiles: ['Student']})-[:IN]-(c:ManualGroup) ")
                .append(" WHERE c.id IN {idClasses}")
                .append(" RETURN distinct(u.id) as id, u.displayName as displayName, u.firstName as firstName, ")
                .append(" u.lastName as lastName, c.id as idClasse ORDER BY displayName");
        params.putArray("idClasses", new JsonArray(idClasses));

        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(handler));
    }

    /**
     * Récupère la liste des classes de l'utilisateur
     * @param user
     * @param handler handler portant le résultat de la requête
     */
    @Override
    public void listClasses(String idEtablissement, Boolean classOnly, UserInfos user, Handler<Either<String, JsonArray>> handler) {
        String query;
        JsonObject params = new JsonObject();
        // Dans le cas du chef d'établissement, on récupère toutes les classes

        String queryClass = "MATCH (m:Class)-[b:BELONGS]->(s:Structure) ";
        String queryGroup = "MATCH (m:FunctionalGroup)-[d:DEPENDS]->(s:Structure) ";
        String paramEtab = "s.id = {idEtablissement} ";
        String paramClass = "m.id IN {classes} ";
        String paramGroup = "m.id IN {groups} ";
        String paramGroupManuel = ("Personnel".equals(user.getType()))? paramEtab : paramGroup + " AND " + paramEtab;

        String queryGroupManuel = " MATCH (u:User{profiles :['Student']})-[i:IN]->(m:ManualGroup)-[r:DEPENDS]->"
                                        +"(s:Structure)" +
                                " WITH m, s" +
                                " MATCH (u:User{profiles :['Teacher']})-[i:IN]->(m2:ManualGroup)" +
                                " WHERE m2.id = m.id AND " + paramGroupManuel +
                                " RETURN m " +
                                " UNION " +
                                " MATCH (u:User{profiles :['Student']})-[i:IN]->(m:ManualGroup)-[r:DEPENDS]->(c:Class)-"
                                        +"[BELONGS]->(s:Structure) " +
                                " WITH m, s " +
                                " MATCH (u:User{profiles :['Teacher']})-[i:IN]->(m2:ManualGroup) " +
                                " WHERE m2.id = m.id AND " + paramGroupManuel +
                                " RETURN distinct(m) ";
        String param1;
        String param2;

        if ("Personnel".equals(user.getType())) {
            param1 = "WHERE " + paramEtab + "RETURN m ";
            param2 = param1;
            params.putString("idEtablissement", idEtablissement)
                    .putString("idEtablissement", idEtablissement)
                    .putString("idEtablissement", idEtablissement);
        } else {
            param1 = "WHERE " + paramClass + "AND " + paramEtab + "RETURN m ";
            param2 = "WHERE " + paramGroup + "AND " + paramEtab + "RETURN m ";
            params.putArray("classes", new JsonArray(user.getClasses().toArray()))
                    .putArray("groups", new JsonArray(user.getGroupsIds().toArray()))
                    .putString("idEtablissement", idEtablissement)
                    .putArray("groups", new JsonArray(user.getGroupsIds().toArray()))
                    .putString("idEtablissement", idEtablissement)
                    .putArray("groups", new JsonArray(user.getGroupsIds().toArray()))
                    .putString("idEtablissement", idEtablissement);
        }

        if(classOnly == null){
            query = queryClass + param1 + " UNION " + queryGroup + param2;
            query = query + " UNION " +  queryGroupManuel ;
        } else if (classOnly){
            query = queryClass + param1;
        } else {
            query = queryGroup + param2;
        }
        neo4j.execute(query, params, Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void getElevesClasses(String[] idClasses, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        query.append("MATCH (u:User {profiles: ['Student']})-[:IN]-(:ProfileGroup)-[:DEPENDS]-(c:Class) ")
                .append("WHERE c.id IN {idClasses} ")
                .append("RETURN c.id as idClasse, u.id as idEleve ORDER BY c.name, u.lastName, u.firstName ")
                .append("UNION MATCH (u:User {profiles: ['Student']})-[:IN]-(c:FunctionalGroup) ")
                .append("WHERE c.id IN {idClasses} ")
                .append("RETURN c.id as idClasse, u.id as idEleve ORDER BY c.name, u.lastName, u.firstName ")
                .append("UNION MATCH (u:User {profiles: ['Student']})-[:IN]-(c:ManualGroup) ")
                .append("WHERE c.id IN {idClasses} ")
                .append("RETURN c.id as idClasse, u.id as idEleve ORDER BY c.name, u.lastName, u.firstName ");
        params.putArray("idClasses", new JsonArray(idClasses));

        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void getEtabClasses(String[] idClasses, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();
        query.append("MATCH (g:Class)-[:BELONGS]->(s:Structure) WHERE g.id IN {idClasses} ")
                .append(" return DISTINCT(s.id) AS idStructure, COLLECT(g.id) AS idClasses ")
                .append(" UNION MATCH (g:FunctionalGroup)-[:DEPENDS]->(s:Structure) WHERE g.id IN {idClasses} ")
                .append(" return DISTINCT(s.id) AS idStructure, COLLECT(g.id) AS idClasses ")
                .append(" UNION MATCH (g:ManualGroup)-[:DEPENDS]->(s:Structure) WHERE g.id IN {idClasses} ")
                .append(" return DISTINCT(s.id) AS idStructure, COLLECT(g.id) AS idClasses")
                .append(" UNION MATCH (g:ManualGroup)-[:DEPENDS]->(:Class)-[BELONGS]->(s:Structure) ")
                .append(" WHERE g.id IN {idClasses} ")
                .append(" return DISTINCT(s.id) AS idStructure, COLLECT(g.id) AS idClasses");
        params.putArray("idClasses", new JsonArray(idClasses));

        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void getClasseEleve(String idEtablissement, String eleveId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();
        query.append(" MATCH (u:User)-[r:ADMINISTRATIVE_ATTACHMENT]->(s:Structure) where u.profiles= [\"Student\"] ")
                .append(" and u.id={idEleve}  and s.id={idEtablissement} ")
                .append(" with u  ")
                .append(" Match  (c:Class) where  c.externalId IN u.classes ")
                .append(" with   c, u ")
                .append(" OPTIONAL Match (u2:User)-[i:IN]->(f:ManualGroup) where u.id = u2.id  ")
                .append(" with  collect(DISTINCT c.id) as C,  u2 ")
                .append(" OPTIONAL Match (u3:User)-[i:IN]->(f:FunctionalGroup) where u2.id = u3.id  ")
                .append(" Return C + collect(f.id)  as Classes ");
        params.putString("idEtablissement", idEtablissement);
        params.putString("idEleve", eleveId);
        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(handler));
    }


    @Override
    public void getClasseInfo(String idClasse, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();
        query.append("MATCH (c {id: {idClasse}}) return c");

        params.putString("idClasse", idClasse);
        neo4j.execute(query.toString(), params, Neo4jResult.validUniqueResultHandler(handler));
    }
    @Override
    public void getGroupeClasse(String[] idClasses, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        query.append("MATCH (g:FunctionalGroup)--(u:User {profiles:['Student']})--(:profileGroup)--(c:Class) ")
                .append("WHERE c.id IN {idClasses} ")
                .append("RETURN c.id as id_classe, COLLECT(DISTINCT g.id) AS id_groupes");
        params.putArray("idClasses", new JsonArray(idClasses));

        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(handler));
    }

    /**
     * get idClasse by idEleve
     * @param idEleve
     * @param handler id_classe
     */
    @Override
    public void getClasseByEleve(String idEleve, Handler<Either<String, JsonObject>> handler) {
      StringBuilder query = new StringBuilder()
              .append("MATCH (u:User {id :{id_eleve}}) with u MATCH (c:Class) ")
              .append("WHERE c.externalId IN u.classes return c");
      neo4j.execute(query.toString(),new JsonObject().putString("id_eleve", idEleve),Neo4jResult.validUniqueResultHandler(handler));
    }
}
