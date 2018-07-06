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
import fr.openent.viescolaire.service.UtilsService;
import fr.wseduc.webutils.Either;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.user.UserInfos;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by ledunoiss on 19/02/2016.
 */
public class DefaultClasseService extends SqlCrudService implements ClasseService {

    private final Neo4j neo4j = Neo4j.getInstance();
    protected static final Logger log = LoggerFactory.getLogger(DefaultClasseService.class);

    private static final String mParameterIdClasse = "idClasse";

    private UtilsService utilsService;

    public DefaultClasseService() {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.VSCO_CLASSE_TABLE);
        utilsService = new DefaultUtilsService();
    }

    @Override
    public void getClasseEtablissement(String idEtablissement, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        query.append("MATCH (c:Class)-[BELONGS]->(s:Structure) WHERE s.id = {idEtablissement} RETURN c.id as idClasse ORDER BY c.name");
        params.put("idEtablissement", idEtablissement);

        neo4j.execute(query.toString(),params, Neo4jResult.validResultHandler(handler));
    }

    //TODO Revoir avec getEleveClasses
    @Override
    public void getEleveClasse(String idClasse, Long idPeriode,Handler<Either<String, JsonArray>> handler){

        StringBuilder query = new StringBuilder();

        String RETURNING = " RETURN DISTINCT u.id as id, u.firstName as firstName, u.lastName as lastName," +
                " u.level as level, u.deleteDate as deleteDate, u.classes as classes, " +
                " CASE WHEN u.birthDate IS NULL THEN 'undefined' ELSE u.birthDate END AS birthDate " +
                " ORDER BY lastName, firstName ";

        query.append("MATCH (u:User {profiles: ['Student']})-[:IN]-(:ProfileGroup)-[:DEPENDS]-(c:Class {id :{idClasse}}) ")
                .append(RETURNING)
                .append(" UNION MATCH (u:User {profiles: ['Student']})-[:IN]-(c:FunctionalGroup {id :{idClasse}}) ")
                .append(RETURNING)
                .append(" UNION MATCH (u:User {profiles: ['Student']})-[:IN]-(c:ManualGroup {id :{idClasse}}) ")
                .append(RETURNING)
                .append(" UNION MATCH (dg:DeleteGroup)<-[:IN]-(u:User {profiles: ['Student']})-[:HAS_RELATIONSHIPS]->(b:Backup) ,")
                .append(" (g:Group), (c:Class) ")
                .append(" WHERE HAS(u.deleteDate) ")
                .append(" AND  c.externalId IN u.classes ")
                .append(" AND (c.id = {idClasse} ")
                .append("      OR (g.id = {idClasse}  AND (g.externalId IN u.groups OR g.id IN b.IN_OUTGOING) )) ")
                .append(RETURNING);

        String [] sortedField = new  String[2];
        sortedField[0] = "lastName";
        sortedField[1] = "firstName";

        neo4j.execute(query.toString(), new JsonObject().put(mParameterIdClasse, idClasse),
                utilsService.addStoredDeletedStudent(new JsonArray().add(idClasse), null,
                        null, sortedField, idPeriode, handler));

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

        values.put(mParameterIdClasse, idGroupes);
        values.put("idGroupe", idGroupes);

        neo4j.execute(query.toString(), values, Neo4jResult.validResultHandler(handler));
    }

    //TODO Revoir avec getEleveClasse
    @Override
    public void getEleveClasses(String idEtablissement, JsonArray idClasse, Boolean isTeacher,
                                Handler<Either<String, JsonArray>> handler){

        StringBuilder query = new StringBuilder();
        JsonObject params =  new JsonObject();

        // Rajout de filtre pour les enseignants
        String FILTER;
        if(isTeacher) {
            FILTER = " AND c.id IN {idClasse} ";
            params.put(mParameterIdClasse, idClasse);
        }
        else {
            FILTER = " ";
        }

        // Requête Néo
        query.append(" MATCH (u:User {profiles: ['Student']}),(s:Structure),(c:Class) ")
                .append(" WHERE  c.externalId IN u.classes ")
                .append(" AND s.externalId IN u.structures ")
                .append(" AND s.id = {idEtablissement} ")
                .append(FILTER)

                // Format de retour des données
                .append(" RETURN distinct(u.id) as id, u.displayName as ")
                .append(" displayName, u.firstName as firstName, u.lastName as lastName, ")
                .append(" c.id as idClasse, u.deleteDate ")
                .append(" ORDER BY displayName ");

        params.put("idEtablissement", idEtablissement);

        // Rajout des élèves supprimés et absent de l'annuaire
        String [] sortedField = new  String[1];
        sortedField[0]= "displayName";
        neo4j.execute(query.toString(),params,
                utilsService.addStoredDeletedStudent(isTeacher? idClasse : null,
                        !isTeacher? idEtablissement :null,
                        null,
                        sortedField, null,
                        handler));

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
        String paramGroupManuel = "";
        if(null == user || "Personnel".equals(user.getType())){
            paramGroupManuel =  paramEtab;
        } else {
            paramGroupManuel =  paramGroup + " AND " + paramEtab;
        }

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

        if (null == user || "Personnel".equals(user.getType())) {
            param1 = "WHERE " + paramEtab + "RETURN m ";
            param2 = param1;
            params.put("idEtablissement", idEtablissement)
                    .put("idEtablissement", idEtablissement)
                    .put("idEtablissement", idEtablissement);
        } else {
            param1 = "WHERE " + paramClass + "AND " + paramEtab + "RETURN m ";
            param2 = "WHERE " + paramGroup + "AND " + paramEtab + "RETURN m ";
            params.put("classes", new fr.wseduc.webutils.collections.JsonArray(user.getClasses()))
                    .put("groups", new fr.wseduc.webutils.collections.JsonArray(user.getGroupsIds()))
                    .put("idEtablissement", idEtablissement)
                    .put("groups", new fr.wseduc.webutils.collections.JsonArray(user.getGroupsIds()))
                    .put("idEtablissement", idEtablissement)
                    .put("groups", new fr.wseduc.webutils.collections.JsonArray(user.getGroupsIds()))
                    .put("idEtablissement", idEtablissement);
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
    public void getElevesClasses(String[] idClasses,
                                 Long idPeriode,
                                 Handler<Either<String, JsonArray>> handler) {

        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();
        String RETURNING = " RETURN c.id as idClasse, " +
                "u.id as idEleve, c.name as name , u.lastName as lastName, u.firstName as firstName, " +
                "u.deleteDate as deleteDate ,u.displayName as displayName" +
                " ORDER BY  " +
                " u.lastName, u.firstName ";

        query.append("MATCH (u:User {profiles: ['Student']})-[:IN]-(:ProfileGroup)-[:DEPENDS]-(c:Class) ")
                .append(" WHERE c.id IN {idClasses} ")
                .append(RETURNING)
                .append(" UNION MATCH (u:User {profiles: ['Student']})-[:IN]-(c:FunctionalGroup) ")
                .append(" WHERE c.id IN {idClasses} ")
                .append(RETURNING)
                .append(" UNION MATCH (u:User {profiles: ['Student']})-[:IN]-(c:ManualGroup) ")
                .append(" WHERE c.id IN {idClasses} ")
                .append(RETURNING)
                .append(" UNION MATCH (dg:DeleteGroup)<-[:IN]-(u:User{profiles: ['Student']})-[:HAS_RELATIONSHIPS]->(b:Backup),")
                .append(" (g:Group), (c:Class) ")
                .append(" WHERE HAS(u.deleteDate) ")
                .append(" AND c.externalId IN u.classes ")
                .append(" AND (c.id IN {idClasses} ")
                .append("      OR (g.id IN {idClasses} AND (g.externalId IN u.groups  OR g.id IN b.IN_OUTGOING ) ) ) ")
                .append(RETURNING);

        params.put("idClasses", new fr.wseduc.webutils.collections.JsonArray(Arrays.asList(idClasses)));

        neo4j.execute(query.toString(), params, new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> eventNeo) {
                if ("ok".equals(((JsonObject) eventNeo.body()).getString("status"))) {
                    // Récupération des élèves supprimés et stockés dans postgres
                    JsonArray rNeo = eventNeo.body().getJsonArray("result");
                    new DefaultEleveService().getStoredDeletedStudent(new JsonArray(Arrays.asList(idClasses)),
                            null, null,
                            new Handler<Either<String, JsonArray>>() {
                                public void handle(Either<String, JsonArray> eventPostgres) {
                                    if (eventPostgres.isLeft()) {
                                        // Si on a un problème lors de la récupération postgres
                                        // On retourne le résultat de Neo4J
                                        handler.handle(new Either.Right<>(rNeo));
                                    }

                                    else {
                                        DefaultUtilsService utilsService = new DefaultUtilsService();
                                        JsonArray rPostgres = eventPostgres.right().getValue();


                                        // On récupère les noms des Classes des élèves Stockés dans Postgres
                                        String[] idClassePostgresStudents = new String[rPostgres.size()];
                                        for (int i=0; i< rPostgres.size(); i++) {
                                            idClassePostgresStudents[i] = rPostgres.getJsonObject(i)
                                                    .getString("idClasse");
                                        }
                                        getClassesName(idClassePostgresStudents,
                                                new Handler<Either<String, JsonArray>>() {
                                                    public void handle(Either<String, JsonArray> classeNamesEvent) {
                                                        if (classeNamesEvent.isLeft()) {
                                                            handler.handle(new Either.Left<>("PB While getting Classe Name"));
                                                        }
                                                        else  {
                                                            JsonArray classesNames = classeNamesEvent.right().getValue();
                                                            HashMap<String,String>  mapClasseName = new LinkedHashMap<>();

                                                            // On stocke les noms des classes dans une map
                                                            for (int i= 0; i < classesNames.size(); i++) {
                                                                if(mapClasseName.get(classesNames.getJsonObject(i)
                                                                        .getString("idClasse")) == null){
                                                                    mapClasseName.put(classesNames.getJsonObject(i)
                                                                                    .getString("idClasse"),
                                                                            classesNames.getJsonObject(i)
                                                                                    .getString("name"));
                                                                }
                                                            }
                                                            // On construit la liste des élèves stocké dans postgres
                                                            JsonArray studentPostgres = new JsonArray();
                                                            for(int i=0; i< rPostgres.size(); i++) {
                                                                JsonObject o = new JsonObject();
                                                                JsonObject student = rPostgres.getJsonObject(i);
                                                                String studentIdClasse =  rPostgres.getJsonObject(i)
                                                                        .getString("idClasse");

                                                                // Formatage des données pour union
                                                                o.put("name", mapClasseName.get(studentIdClasse));
                                                                o.put("idClasse", studentIdClasse);
                                                                o.put("idEleve", student.getString("idEleve"));
                                                                o.put("lastName", student.getString("lastName"));
                                                                o.put("firstName", student.getString("firstName"));
                                                                o.put("deleteDate", student.getString("deleteDate"));
                                                                o.put("displayName", student.getString("displayName"));

                                                                studentPostgres.add(o);
                                                            }
                                                            DefaultUtilsService utilsService = new DefaultUtilsService();



                                                            JsonArray result =  utilsService.saUnion(rNeo, studentPostgres);
                                                            String [] sortedField = new  String[2];
                                                            sortedField[0] = "lastName";
                                                            sortedField[1] = "firstName";
                                                            if (null == idPeriode) {
                                                                handler.handle(new Either.Right(
                                                                        utilsService.sortArray(result, sortedField)));
                                                            }
                                                            else {
                                                                // Si on veut filtrer sur la période
                                                                // On récupère la période
                                                                new DefaultPeriodeService().getPeriodes(null,
                                                                        idClasses,
                                                                        new  Handler<Either<String, JsonArray>>() {
                                                                            @Override
                                                                            public void handle(Either<String, JsonArray>
                                                                                                       message) {

                                                                                if (message.isRight()) {
                                                                                    JsonArray periodes = message.right()
                                                                                            .getValue();
                                                                                    JsonArray elevesAvailable = new JsonArray();

                                                                                    // On récupére la période de la classe
                                                                                    JsonObject periode = null;
                                                                                    for (int i = 0; i < periodes.size(); i++) {
                                                                                        if (idPeriode.intValue()
                                                                                                == ((JsonObject) periodes.getJsonObject(i))
                                                                                                .getInteger("id_type").intValue()) {
                                                                                            periode = (JsonObject) periodes.getJsonObject(i);
                                                                                            break;
                                                                                        }
                                                                                    }
                                                                                    if (periode != null) {
                                                                                        String debutPeriode = periode.getString("timestamp_dt")
                                                                                                .split("T")[0];
                                                                                        String finPeriode = periode.getString("timestamp_fn")
                                                                                                .split("T")[0];

                                                                                        DateFormat formatter =
                                                                                                new SimpleDateFormat("yy-MM-dd");
                                                                                        try {
                                                                                            final Date dateDebutPeriode =
                                                                                                    formatter.parse(debutPeriode);
                                                                                            final Date dateFinPeriode =
                                                                                                    formatter.parse(finPeriode);

                                                                                            utilsService.getAvailableStudent(
                                                                                                    result, idPeriode,
                                                                                                    dateDebutPeriode,
                                                                                                    dateFinPeriode,
                                                                                                    sortedField,handler);

                                                                                        } catch (ParseException e) {
                                                                                            String messageLog = "Error :can not"
                                                                                                    +
                                                                                                    "calcul students " +
                                                                                                    "of groupe : " + idClasses[0];
                                                                                            log.error(message,e);
                                                                                            handler.handle(new Either.Left<>(messageLog));

                                                                                        }
                                                                                    } else {
                                                                                        handler.handle(
                                                                                                new Either.Right<>(
                                                                                                        utilsService.sortArray(result,
                                                                                                                sortedField)));
                                                                                    }
                                                                                }

                                                                            }
                                                                        });
                                                            }

                                                        }

                                                    }
                                                });


                                    }
                                }
                            });


                } else {
                    handler.handle(new Either.Left<>("Pb While  getting Student in Neo"));
                }
            }
        });
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
        params.put("idClasses", new fr.wseduc.webutils.collections.JsonArray(Arrays.asList(idClasses)));

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
        params.put("idEtablissement", idEtablissement);
        params.put("idEleve", eleveId);
        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(handler));
    }


    @Override
    public void getClasseInfo(String idClasse, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();
        query.append("MATCH (c {id: {idClasse}}) return c");

        params.put("idClasse", idClasse);
        neo4j.execute(query.toString(), params, Neo4jResult.validUniqueResultHandler(handler));
    }
    @Override
    public void getGroupeClasse(String[] idClasses, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        query.append("MATCH (g:FunctionalGroup)--(u:User {profiles:['Student']})--(:profileGroup)--(c:Class) ")
                .append("WHERE c.id IN {idClasses} ")
                .append("RETURN c.id as id_classe, COLLECT(DISTINCT g.id) AS id_groupes");
        params.put("idClasses", new fr.wseduc.webutils.collections.JsonArray(Arrays.asList(idClasses)));

        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(handler));
    }
    /**
     * get idClasse by idEleve
     * @param idEleve
     * @param handler id_classe
     */

    @Override
    public void getClasseIdByEleve(String idEleve, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder()
                .append("MATCH (u:User {id :{id_eleve}}) with u MATCH (c:Class) ")
                .append("WHERE c.externalId IN u.classes return c");


        neo4j.execute(query.toString(),new JsonObject().put("id_eleve", idEleve),
                new Handler<Message<JsonObject>>() {
                    public void handle(Message<JsonObject> event) {
                        if ("ok".equals(((JsonObject) event.body()).getString("status"))) {

                            // Si l'élève est présent dans l'annuaire.
                            JsonArray rNeo = ((JsonObject) event.body()).getJsonArray("result",
                                    new fr.wseduc.webutils.collections.JsonArray());
                            if (rNeo.size() > 0) {
                                handler.handle(Neo4jResult.validUniqueResult(event));
                            } else {
                                // Sinon on recherche l'élève parmis les élèves supprimés.
                                String[] idEleves = new String[1];
                                idEleves[0] = idEleve;
                                new DefaultEleveService().getStoredDeletedStudent(null, null,
                                        idEleves,
                                        new Handler<Either<String, JsonArray>>() {
                                            public void handle(Either<String, JsonArray> event) {
                                                if (event.isRight()) {
                                                    UtilsService utilsService = new DefaultUtilsService();
                                                    JsonArray rPostgres = event.right().getValue();

                                                    if (rPostgres.size() > 0) {
                                                        String idGroupe = rPostgres.getJsonObject(0)
                                                                .getString("idClasse");
                                                        JsonObject result;
                                                        result = new JsonObject().put("c", new JsonObject().put("data",
                                                                new JsonObject().put("id", idGroupe)));
                                                        handler.handle(new Either.Right(result));
                                                    }

                                                } else {
                                                    handler.handle(new Either.Right(rNeo));
                                                }
                                            }
                                        });

                            }
                        } else {
                            handler.handle(new Either.Left<>("Error While get User in Neo4J "));
                        }
                    }
                });

    }

    private void getClassesName(String[] idClasses, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        query.append("MATCH (c:Class)-[BELONGS]->(s:Structure) WHERE c.id IN {idClasses} ")
                .append(" RETURN c.id as idClasse, c.name as name ORDER BY c.name ");
        params.put("idClasses", new fr.wseduc.webutils.collections.JsonArray(Arrays.asList(idClasses)));

        neo4j.execute(query.toString(),params, Neo4jResult.validResultHandler(handler));
    }
}
