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
import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.helper.FutureHelper;
import fr.openent.viescolaire.service.*;
import fr.wseduc.webutils.Either;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.VertxException;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.user.UserInfos;

import java.util.*;
import java.util.stream.Collectors;

import static fr.wseduc.webutils.Utils.handlerToAsyncHandler;
import static fr.wseduc.webutils.http.Renders.badRequest;
import static fr.wseduc.webutils.http.Renders.renderJson;

/**
 * Created by ledunoiss on 19/02/2016.
 */
public class DefaultClasseService extends SqlCrudService implements ClasseService {

    protected static final Logger log = LoggerFactory.getLogger(DefaultClasseService.class);
    private static final String mParameterIdClasse = "idClasse";
    private final Neo4j neo4j = Neo4j.getInstance();
    private ServicesService servicesService;
    private MultiTeachingService multiTeachingService;
    private UtilsService utilsService;

    public DefaultClasseService() {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.VSCO_CLASSE_TABLE);
        utilsService = new DefaultUtilsService();
        servicesService = new DefaultServicesService();
        multiTeachingService = new DefaultMultiTeachingService();
    }

    public DefaultClasseService(ServiceFactory serviceFactory) {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.VSCO_CLASSE_TABLE);
        utilsService = new DefaultUtilsService();
        servicesService = new DefaultServicesService();
        multiTeachingService = new DefaultMultiTeachingService();
    }

    @Override
    public void getClasseEtablissement(String idEtablissement, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        query.append("MATCH (c:Class)-[BELONGS]->(s:Structure) WHERE s.id = {idEtablissement} RETURN c.id as idClasse ORDER BY c.name");
        params.put("idEtablissement", idEtablissement);

        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(handler));
    }

    //TODO Revoir avec getEleveClasses
    @Override
    public void getEleveClasse(String idClasse, Long idPeriode, Handler<Either<String, JsonArray>> handler) {
        //Requête Neo4j optimisé
        StringBuilder returning = new StringBuilder();
        returning.append("RETURN DISTINCT u.id as id, u.firstName as firstName, u.lastName as lastName, ")
                .append("u.level as level, u.deleteDate as deleteDate, u.classes as classes, ")
                .append("CASE WHEN u.birthDate IS NULL THEN 'undefined' ELSE u.birthDate END AS birthDate ")
                .append("ORDER BY lastName, firstName ");

        StringBuilder query = new StringBuilder()
                .append("MATCH (c:Class{id:{idClasse}})<-[:DEPENDS]-(:ProfileGroup)<-[:IN]-(u:User {profiles:['Student']}) ")
                .append(returning)
                .append("UNION MATCH (c:FunctionalGroup {id :{idClasse}})-[:IN]-(u:User {profiles: ['Student']}) ")
                .append(returning)
                .append("UNION MATCH (c:ManualGroup {id :{idClasse}})-[:IN]-(u:User {profiles: ['Student']}) ")
                .append(returning)
                .append("UNION MATCH (u:User {profiles: ['Student']})-[:HAS_RELATIONSHIPS]->(b:Backup), (c:Class {id :{idClasse}}) ")
                .append("WHERE HAS(u.deleteDate) AND (c.externalId IN u.groups OR c.externalId IN u.classes) ")
                .append(returning);

        String[] sortedField = new String[2];
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
    // deprecated ?
    @Override
    public void getEleveClasses(String idEtablissement, JsonArray idClasse, Long idPeriode, Boolean isTeacher,
                                Handler<Either<String, JsonArray>> handler) {
        JsonObject params = new JsonObject();

        // Rajout de filtre pour les enseignants
        String filter = " ";
        if (isTeacher) {
            filter = " AND c.id IN {idClasse} ";
            params.put(mParameterIdClasse, idClasse);
        }

        // Format de retour des données
        StringBuilder returning = new StringBuilder()
                .append(" RETURN u.id as id, u.displayName as displayName, u.firstName as firstName, ")
                .append(" u.lastName as lastName, c.id as idClasse, u.deleteDate as deleteDate ")
                .append(" ORDER BY displayName ");

        // Requête Néo
        StringBuilder query = new StringBuilder()
                .append(" MATCH (u:User {profiles: ['Student']})-[ADMINISTRATIVE_ATTACHMENT]->(s:Structure {id:{idEtablissement}})<-[b:BELONGS]-(c:Class) ")
                .append(" WHERE c.externalId IN u.classes AND s.externalId IN u.structures ")
                .append(filter)
                .append(returning)

                .append(" UNION ")

                // Récupération des élèves supprimés mais présents dans l'annuaire
                .append(" MATCH (u:User{profiles:['Student']})-[IN]->(d:DeleteGroup), ")
                .append(" (s:Structure {id:{idEtablissement}}) ")
                .append(" WHERE s.externalId IN u.structures ")
                .append(" OPTIONAL MATCH (c:Class)-[b:BELONGS]->(s) ")
                .append(" WHERE  c.externalId IN u.classes ")
                .append(filter)
                .append(returning);

        params.put("idEtablissement", idEtablissement);

        // Rajout des élèves supprimés et absent de l'annuaire
        String[] sortedField = new String[1];
        sortedField[0] = "displayName";
        neo4j.execute(query.toString(), params, utilsService.addStoredDeletedStudent(isTeacher ? idClasse : null,
                !isTeacher ? idEtablissement : null, null, sortedField, idPeriode, handler));
    }

    public void listClasses(String idStructure, Boolean classOnly, UserInfos user, JsonArray idClassesAndGroups,
                            boolean forAdmin, Handler<Either<String, JsonArray>> handler, boolean isTeacherEdt) {
        // TODO ajouter filtre sur classes/groupes

        if (user != null && !forAdmin) {
            forAdmin = "Personnel".equals(user.getType()) || isTeacherEdt;
        }

        String queryClass = "MATCH (m:Class)-[b:BELONGS]->(s:Structure) ";
        String queryGroup = "MATCH (m:FunctionalGroup)-[d:DEPENDS]->(s:Structure) ";
        String queryLastGroup = "MATCH (s:Structure)<-[:SUBJECT]-(sub:Subject)<-[r:TEACHES]-(u:User) WHERE ";
        String paramEtab = "s.id = {idStructure} ";
        String paramClass = "m.id IN {classes} ";
        String paramGroup = "m.id IN {groups} ";
        String paramUser = "u.id = {userId} ";

        String paramGroupManuel;
        if (null == user || forAdmin) {
            paramGroupManuel = paramEtab;

            if (null == user && idClassesAndGroups != null) {
                paramGroupManuel += " m.id IN {idClassesAndGroups}";
            }
        } else {
            paramGroupManuel = paramGroup + " AND " + paramEtab;
        }

        // On date -> 08/02/2020 / 23:09, try a fix based on DBOI mail
        String queryGroupManuel = "MATCH (s:Structure)<-[:DEPENDS]-(m:ManualGroup)<-[:IN]-(:User{profiles: ['Student']})" +
                " WHERE " + paramGroupManuel +
                " AND m<-[:IN]-(:User {profiles: ['Teacher']}) RETURN m " +
                " UNION MATCH (s:Structure{id:{idStructure}})<-[:BELONGS]-(:Class)<-[:DEPENDS]-(m:ManualGroup)<-[:IN]-(:User {profiles: ['Student']})" +
                " WHERE " + paramGroupManuel +
                " AND m<-[:IN]-(:User {profiles: ['Teacher']}) RETURN distinct(m) ";

        String returnLastGroup = "WITH r.groups as libelleClasses, s, u, sub MATCH (s)--(c) " +
                "WHERE (c:FunctionalGroup OR c:ManualGroup) AND ALL(x IN c.externalId WHERE x in libelleClasses) " +
                "RETURN c AS m;";

        String param1;
        String param2;
        String param3;
        JsonObject params = new JsonObject();
        if (null == user || forAdmin) {
            param1 = "WHERE " + paramEtab + "RETURN m ";
            param2 = param1;
            param3 = paramEtab;
            params.put("idStructure", idStructure);

            if (null == user && idClassesAndGroups != null) {
                params.put("idClassesAndGroups", idClassesAndGroups);
            }
        } else {
            param1 = "WHERE " + paramClass + "AND " + paramEtab + "RETURN m ";
            param2 = "WHERE " + paramGroup + "AND " + paramEtab + "RETURN m ";
            param3 = paramUser + "AND " + paramEtab;
            params.put("classes", new fr.wseduc.webutils.collections.JsonArray(user.getClasses()))
                    .put("groups", new fr.wseduc.webutils.collections.JsonArray(user.getGroupsIds()))
                    .put("idStructure", idStructure)
                    .put("userId", user.getUserId());
        }

        String query;
        if (classOnly == null) {
            query = queryClass + param1 + " UNION " + queryGroup + param2;
            query = query + " UNION " + queryGroupManuel;
            query = query + " UNION " + queryLastGroup + param3 + returnLastGroup;
        } else if (classOnly) {
            query = queryClass + param1;
            //query += " UNION MATCH (s:Structure{id:{idStructure}})--(c) WHERE (c:Class) AND EXISTS(c.externalId) return c as m";
        } else {
            query = queryGroup + param2;
            query = query + " UNION " + queryLastGroup + param3 + returnLastGroup; // A TRANSFORMER EN FUTURE
        }

        neo4j.execute(query, params, Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void getElevesClasses(String[] idClasses, Long idPeriode, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();
        String RETURNING = " RETURN c.id as idClasse, " +
                " u.id as idEleve, c.name as name , u.lastName as lastName, u.firstName as firstName, " +
                " u.deleteDate as deleteDate ,u.displayName as displayName, " +
                " u.birthDate as birthDate " +
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
                .append(" UNION MATCH (u:User {profiles: ['Student']})-[:HAS_RELATIONSHIPS]->(b:Backup), ")
                .append(" (c:Group)")
                .append(" WHERE HAS(u.deleteDate) ")
                .append(" AND (c.id IN {idClasses} ")
                .append(" AND (c.externalId IN u.groups) ) ")
                .append(RETURNING)
                .append(" UNION MATCH (u:User {profiles: ['Student']})-[:HAS_RELATIONSHIPS]->(b:Backup),")
                .append(" (c:Class)")
                .append(" WHERE HAS(u.deleteDate) ")
                .append(" AND (c.id IN {idClasses} ")
                .append(" AND  c.externalId IN u.classes) ")
                .append(RETURNING);

        params.put("idClasses", new fr.wseduc.webutils.collections.JsonArray(Arrays.asList(idClasses)));
        try {
            neo4j.execute(query.toString(), params, utilsService.getEleveWithClasseName(idClasses, null,
                    idPeriode, handler));
        } catch (VertxException e) {
            String error = e.getMessage();
            log.error("getElevesClasses " + e.getMessage());
            if (error.contains("Connection was closed")) {
                getElevesClasses(idClasses, idPeriode, handler);
            }
        }

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
    public Future<JsonArray> getEtabClasses(String idClasses) {
        Promise<JsonArray> promise = Promise.promise();

        this.getEtabClasses(new String[]{idClasses}, FutureHelper.handlerJsonArray(promise));

        return promise.future();
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

        JsonObject params = new JsonObject();
        String query = "MATCH (c:Class {id: {idClasse}}) RETURN c " +
                "UNION MATCH (c:FunctionalGroup {id: {idClasse}}) RETURN c " +
                "UNION MATCH (c:ManualGroup {id: {idClasse}}) RETURN c";

        params.put("idClasse", idClasse);
        try {
            neo4j.execute(query, params, Neo4jResult.validUniqueResultHandler(handler));
        } catch (VertxException e) {
            String error = e.getMessage();
            log.error("getClasseInfo " + e.getMessage());
            if (error.contains("Connection was closed")) {
                getClasseInfo(idClasse, handler);
            }
        }
    }

    @Override
    public Future<JsonObject> getClasseInfo(String idClasse) {
        Promise<JsonObject> promise = Promise.promise();

        this.getClasseInfo(idClasse, FutureHelper.handlerJsonObject(promise));

        return promise.future();
    }

    @Override
    public void getClassesInfo(JsonArray idClasses, Handler<Either<String, JsonArray>> handler) {
        JsonObject params = new JsonObject();
        String returning = "RETURN c.id as id, c.name as name, c.externalId as externalId, labels(c) AS labels";

        String queries = "MATCH (c:Class) WHERE  c.id IN {idClasses} " + returning +
                " UNION MATCH (c:FunctionalGroup) WHERE  c.id IN {idClasses} " + returning +
                " UNION MATCH (c:ManualGroup) WHERE  c.id IN {idClasses}  " + returning;

        params.put("idClasses", idClasses);
        neo4j.execute(queries, params, Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void getGroupeClasse(String[] idClasses, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        query.append("MATCH (u:User {profiles:['Student']})--(:ProfileGroup)--(c:Class) ")
                .append("WHERE c.id IN {idClasses} ")
                .append("WITH u, c OPTIONAL MATCH (u)--(g) WHERE g:FunctionalGroup OR g:ManualGroup ")
                .append("RETURN c.id as id_classe, c.name as name_classe, COLLECT(DISTINCT g.id) AS id_groupes");
        params.put("idClasses", new fr.wseduc.webutils.collections.JsonArray(Arrays.asList(idClasses)));

        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(handler));
    }

    /**
     * get idClasse by idEleve
     *
     * @param idEleve
     * @param handler id_classe
     */

    @Override
    public void getClasseIdByEleve(String idEleve, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder()
                .append("MATCH (u:User {id :{id_eleve}}) with u MATCH (c:Class) ")
                .append("WHERE c.externalId IN u.classes return c");


        neo4j.execute(query.toString(), new JsonObject().put("id_eleve", idEleve),
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

    @Override
    public void getHeadTeachers(String idClasse, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder().append(" MATCH (c:Class {id:{idClasse}}) ")
                .append(" OPTIONAL MATCH (u:User {profiles: ['Teacher']})-[:IN]-(:ProfileGroup)-[:DEPENDS]")
                .append("-(c:Class {id :{idClasse}}) ")
                .append(" WHERE (c.externalId IN u.headTeacher OR  c.externalId IN u.headTeacherManual) ")
                .append(" RETURN CASE WHEN u.title IS NULL THEN \" \" ELSE u.title END as civility, ")
                .append(" u.lastName as name, u.firstName as firstName, u.birthDate as birthDate, u.id as id ");
        JsonObject params = new JsonObject().put("idClasse", idClasse);

        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void getGroupeFromClasse(String[] idClasses, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        query.append("MATCH (u:User {profiles:['Student']})--(:ProfileGroup)--(c:Class) ")
                .append("WHERE c.id IN {idClasses} ")
                .append("WITH u, c MATCH (u)--(g) WHERE g:FunctionalGroup OR g:ManualGroup ")
                .append("RETURN c.id as id_classe, c.name as name_classe, COLLECT(DISTINCT g.name) AS name_groups ,COLLECT(DISTINCT g.id) as id_groups");
        params.put("idClasses", new fr.wseduc.webutils.collections.JsonArray(Arrays.asList(idClasses)));

        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void getGroupFromStudents(String[] studentIds, Handler<Either<String, JsonArray>> handler) {
        String where = " WHERE u.id IN {studentIds} ";

        StringBuilder profileGroupMatch = new StringBuilder("")
                .append(" MATCH (u:User {profiles:['Student']})-[:IN]-(g:ProfileGroup)-[:DEPENDS]-(c:Class)--(s:Structure) ")
                .append(where)
                .append(" RETURN s.id as id_structure, c.id as id_classe, c.name as name_classe, ")
                .append(" COLLECT(DISTINCT g.name) AS name_groups,  COLLECT(DISTINCT g.id) as id_groups ");

        StringBuilder returnQuery = new StringBuilder("")
                .append(" RETURN s.id as id_structure, null as id_classe, null as name_classe, ")
                .append(" COLLECT(DISTINCT g.name) AS name_groups,  COLLECT(DISTINCT g.id) as id_groups ");

        StringBuilder functionalGroupMatch = new StringBuilder("")
                .append(" UNION MATCH (u:User {profiles:['Student']})-[:IN]-(g:FunctionalGroup)--(s:Structure) ")
                .append(where)
                .append(returnQuery);

        StringBuilder manualGroupMatch = new StringBuilder("")
                .append(" UNION MATCH (u:User {profiles:['Student']})-[:IN]-(g:ManualGroup)--(s:Structure) ")
                .append(where)
                .append(returnQuery);

        String query = new StringBuilder("")
                .append(profileGroupMatch)
                .append(functionalGroupMatch)
                .append(manualGroupMatch)
                .toString();

        JsonObject params = new JsonObject().put("studentIds", new JsonArray(Arrays.asList(studentIds)));
        neo4j.execute(query, params, Neo4jResult.validResultHandler(handler));
    }

    public Handler<Either<String, JsonArray>> addCycleClasses(final HttpServerRequest request, EventBus eb,
                                                              String idEtablissement, final boolean isPresence,
                                                              final boolean isEdt, final boolean isTeacherEdt,
                                                              final boolean noCompetence,
                                                              Map<String, JsonArray> info, Boolean classOnly) {
        return event -> {
            if (event.isLeft()) {
                badRequest(request);
            } else {
                JsonArray recipient = event.right().getValue();
                final JsonArray classes = new fr.wseduc.webutils.collections.JsonArray();
                List<String> idGroupes = new ArrayList<>();
                for (int i = 0; i < recipient.size(); i++) {
                    JsonObject classe = recipient.getJsonObject(i).getJsonObject("m");
                    JsonObject object = classe.getJsonObject("metadata");
                    classe = classe.getJsonObject("data");
                    if (object.getJsonArray("labels").contains("Class")) {
                        classe.put("type_groupe", Viescolaire.CLASSE_TYPE);
                    } else if (object.getJsonArray("labels").contains("FunctionalGroup")) {
                        classe.put("type_groupe", Viescolaire.GROUPE_TYPE);
                    } else if (object.getJsonArray("labels").contains("ManualGroup")) {
                        classe.put("type_groupe", Viescolaire.GROUPE_MANUEL_TYPE);
                    }
                    if (isEdt) {
                        classe.put("color", utilsService.getColor(classe.getString("name")));
                    }
                    idGroupes.add(classe.getString("id"));
                    classes.add(classe);
                }

                if (idGroupes.isEmpty()) {
                    renderJson(request, new fr.wseduc.webutils.collections.JsonArray(idGroupes));
                } else {
                    if (isPresence || isEdt || noCompetence) {
                        renderJson(request, classes);
                    } else {
                        JsonObject action = new JsonObject()
                                .put("action", "utils.getCycle")
                                .put("ids", new fr.wseduc.webutils.collections.JsonArray(idGroupes));
                        eb.send(Viescolaire.COMPETENCES_BUS_ADDRESS, action, handlerToAsyncHandler(message -> {
                            if ("ok".equals(message.body().getString("status"))) {
                                JsonArray returnedList = new fr.wseduc.webutils.collections.JsonArray();
                                JsonObject cycles = utilsService.mapListNumber(message.body()
                                        .getJsonArray("results"), "id_groupe", "id_cycle");
                                JsonObject cycleLibelle = utilsService.mapListString(message.body()
                                        .getJsonArray("results"), "id_groupe", "libelle");
                                for (int i = 0; i < classes.size(); i++) {
                                    JsonObject object = classes.getJsonObject(i);
                                    object.put("id_cycle", cycles.getLong(object.getString("id")));
                                    object.put("libelle_cycle", cycleLibelle.getString(object.getString("id")));
                                    object.put("services", info.get(object.getString("id")));
                                    returnedList.add(object);
                                }
                                renderJson(request, returnedList);
                            } else {
                                badRequest(request);
                            }
                        }));
                    }
                }
            }
        };
    }

    //TODO essayer de l implementer pour éviter de saturer le bus
//    public Handler<Either<String, JsonArray>> addServivesClasses(final HttpServerRequest request, EventBus eb,
//                                                                 String idEtablissement, final boolean isPresence,
//                                                                 final boolean isEdt, final boolean isTeacherEdt,
//                                                                 final boolean noCompetence,
//                                                                 Map<String, JsonArray> info,
//                                                                 Boolean classOnly, UserInfos user,
//                                                                 Handler<Either<String, JsonArray>> finalHandler){
//        return    event -> {
//            JsonObject action = new JsonObject()
//                    .put("idStructure", idEtablissement)
//                    .put("aIdEnseignant", new JsonArray().add(user.getUserId()));
//            servicesService.getServices(idEtablissement,
//                    new JsonObject().put("id_enseignant",  new JsonArray().add(user.getUserId())), new Handler<Either<String, JsonArray>>() {
//                        @Override
//                        public void handle(Either<String, JsonArray> event) {
//                            if(event.isRight()){
//                                Set<String> toAdd = new HashSet<>();
//                                event.right().getValue().stream().forEach(service -> {
//                                    JsonObject serviceObj = (JsonObject) service;
//                                    String idGroupe = serviceObj.getString("id_groupe");
//
//                                    if (!info.containsKey(idGroupe)) {
//                                        info.put(idGroupe, new JsonArray());
//                                    }
//                                    if (serviceObj.getBoolean("evaluable")) {
//                                        toAdd.add(idGroupe);
//                                    }
//                                    info.get(idGroupe).add(serviceObj);
//                                });
//
//                                Iterator iter = event.right().getValue().iterator();
//                                log.info(iter);
//
//                                while (iter.hasNext()) {
//                                    JsonObject classe = (JsonObject) iter.next();
//                                    log.info(classe);
//                                    if (toAdd.contains(classe.getJsonObject("m").getJsonObject("data").getString("id"))) {
//                                        toAdd.remove(classe.getJsonObject("m").getJsonObject("data").getString("id"));
//                                    }
//                                }
//
//                                getClassesInfo(new JsonArray(new ArrayList(toAdd)), classes -> {
//                                    if (classes.isRight() && classes.right().getValue().size() > 0) {
//                                        JsonArray mappedClasses = new JsonArray(
//                                                (List) classes.right().getValue().getList().stream().map(classe -> {
//                                                            JsonObject classeObj = (JsonObject) classe;
//                                                            JsonObject finalObject = new JsonObject();
//                                                            JsonArray labels = classeObj.getJsonArray("labels");
//                                                            classeObj.remove("labels");
//                                                            JsonObject metadata = new JsonObject().put("labels", labels);
//                                                            JsonObject m = new JsonObject().put("data", classeObj)
//                                                                    .put("metadata", metadata);
//                                                            return finalObject.put("m", m);
//                                                        }
//                                                ).collect(Collectors.toList()));
//                                        event.right().getValue().addAll(mappedClasses);
//                                        finalHandler.handle(event.right());
//                                    } else if (classes.isRight()) {
//                                        finalHandler.handle(event.right());
//                                    } else {
//                                        finalHandler.handle(classes.left());
//                                    }
//                                });
//                            }else{
//                                finalHandler.handle(new Either.Left<>("Error when getting services "));
//                            }
//                        }
//                    }
//
//            );
//
//        };
//    }
    public Handler<Either<String, JsonArray>> addServivesClasses(final HttpServerRequest request, EventBus eb,
                                                                 String idEtablissement, final boolean isPresence,
                                                                 final boolean isEdt, final boolean isTeacherEdt,
                                                                 final boolean noCompetence, Map<String, JsonArray> info,
                                                                 Boolean classOnly, UserInfos user,
                                                                 Handler<Either<String, JsonArray>> finalHandler) {
        return event -> {
            JsonObject oService = new JsonObject();
            oService.put("id_enseignant", user.getUserId());

            servicesService.getServicesSQL(idEtablissement, oService, new Handler<Either<String, JsonArray>>() {
                @Override
                public void handle(Either<String, JsonArray> resultEvent) {
                    Set<String> toAdd = new HashSet<>();

                    resultEvent.right().getValue().stream().forEach(service -> {
                        JsonObject serviceObj = (JsonObject) service;
                        String idGroupe = serviceObj.getString("id_groupe");

                        if (!info.containsKey(idGroupe)) {
                            info.put(idGroupe, new JsonArray());
                        }
                        if (serviceObj.getBoolean("evaluable")) {
                            toAdd.add(idGroupe);
                        }
                        info.get(idGroupe).add(serviceObj);
                    });
                    Iterator iter = event.right().getValue().iterator();
                    while (iter.hasNext()) {
                        JsonObject classe = (JsonObject) iter.next();
                        if (toAdd.contains(classe.getJsonObject("m").getJsonObject("data").getString("id"))) {
                            toAdd.remove(classe.getJsonObject("m").getJsonObject("data").getString("id"));
                        }
                    }
                    getClassesInfo(new JsonArray(new ArrayList(toAdd)), getClassHandler(event, finalHandler));
                }
            });
        };
    }

    private Handler<Either<String, JsonArray>> getClassHandler(Either<String, JsonArray> event, Handler<Either<String, JsonArray>> finalHandler) {
        return classes -> {
            if (classes.isRight() && classes.right().getValue().size() > 0) {
                JsonArray mappedClasses = new JsonArray(
                        (List) classes.right().getValue().getList().stream().map(classe -> {
                                    JsonObject classeObj = (JsonObject) classe;
                                    JsonObject finalObject = new JsonObject();
                                    JsonArray labels = classeObj.getJsonArray("labels");
                                    classeObj.remove("labels");
                                    JsonObject metadata = new JsonObject().put("labels", labels);
                                    JsonObject m = new JsonObject().put("data", classeObj)
                                            .put("metadata", metadata);
                                    return finalObject.put("m", m);
                                }
                        ).collect(Collectors.toList()));
                event.right().getValue().addAll(mappedClasses);
                finalHandler.handle(event.right());
            } else if (classes.isRight()) {
                finalHandler.handle(event.right());
            } else {
                finalHandler.handle(classes.left());
            }
        };
    }

    private void getNeoInfo(JsonArray classes, String userId, String idStructure, Handler<Either<String, JsonArray>> handler) {

        String query = "MATCH (c:Class) " +
                "WHERE NOT (:User {id: {userId}})-[:IN]->(:ProfileGroup)-[:DEPENDS]->(c:Class) " +
                "AND c.id IN {ids} " +
                "RETURN  c.id as id, c.name as name, true as remplacement, 0 as type_groupe";

        query += " UNION ALL ";

        query += "MATCH (c:FunctionalGroup) " +
                "WHERE NOT (:User {id:{userId}})-[:IN]->(c:FunctionalGroup) " +
                "AND c.id IN {ids} " +
                "RETURN  c.id as id, c.name as name, true as remplacement, 1 as type_groupe";

        JsonObject params = new JsonObject()
                .put("ids", classes)
                .put("userId", userId);

        Neo4j.getInstance().execute(query, params, Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void getGroupsMutliTeaching(String userId, String idStructure, Handler<Either<String, JsonArray>> handler) {
        multiTeachingService.getIdGroupsMutliTeaching(userId, idStructure, new Handler<Either<String, JsonArray>>() {
                    @Override
                    public void handle(Either<String, JsonArray> event) {

                        if (event.isRight()) {
                            ArrayList<String> groupsId = (ArrayList<String>) event.right().getValue()
                                    .stream()
                                    .map((oEvent) -> ((JsonObject) oEvent).getString("group_id"))
                                    .collect(Collectors.toList());

                            final JsonArray classeIds = new JsonArray(groupsId);
                            getNeoInfo(classeIds, userId, idStructure, new Handler<Either<String, JsonArray>>() {
                                @Override
                                public void handle(Either<String, JsonArray> event) {
                                    if (event.isRight()) {
                                        JsonArray values = event.right().getValue();
                                        handler.handle(new Either.Right<>(values));
                                    } else {
                                        handler.handle(new Either.Left<>("Error when getting remplacments classes from neo"));
                                    }
                                }
                            });
                        } else {
                            handler.handle(new Either.Left<>("Error when getting groups id classes from sql"));
                        }
                    }
                }
        );
    }

    @Override
    public Future<String> getClasseIdFromAudience(String audiences) {
        Promise<String> promise = Promise.promise();

        this.getClassesFromAudiences(Collections.singletonList(audiences))
                .onSuccess(result -> {
                    if (result.isEmpty()) {
                        promise.complete("");
                    }
                    promise.complete(result.getJsonObject(0).getString(Field.ID_CLASSES, ""));
                })
                .onFailure(promise::fail);
        return promise.future();
    }

    @Override
    public Future<JsonArray> getClassesFromAudiences(List<String> audiences) {
        Promise<JsonArray> promise = Promise.promise();
        JsonObject params = new JsonObject();

        String query = " MATCH(s:Structure)<-[:DEPENDS]-(n:FunctionalGroup)<-[:IN]-(u:User{profiles:['Student']}) " +
                " WHERE n.id IN {idsAudience} WITH s, n, u " +
                " MATCH (c:Class)-[:BELONGS]->(s) WHERE c.externalId IN u.classes RETURN n.id as id_audience, " +
                " c.id AS id_classes " +
                " UNION " +
                " MATCH (s:Structure)<-[:DEPENDS]-(n:ManualGroup)<-[:IN]-(u:User{profiles:['Student']}) " +
                " WHERE n.id IN {idsAudience} WITH  s, n, u " +
                " MATCH (c:Class)-[:BELONGS]->(s) WHERE c.externalId IN u.classes RETURN n.id as id_audience, " +
                " c.id AS id_classes " +
                " UNION " +
                " MATCH (c:Class) " +
                " WHERE c.id IN {idsAudience} " +
                " RETURN c.id AS id_audience, c.id AS id_classes ";
        params.put("idsAudience", audiences)
                .put("idsAudience", audiences)
                .put("idsAudience", audiences);

        neo4j.execute(query, params, Neo4jResult.validResultHandler(FutureHelper.handlerJsonArray(promise)));

        return promise.future();
    }

    @Override
    public Future<JsonArray> getClassIdFromTimeslot(String timeslotId) {
        Promise<JsonArray> promise = Promise.promise();
        String query = "SELECT " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_REL_TIME_SLOT_CLASS + ".id_class" +
                " FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_REL_TIME_SLOT_CLASS +
                " WHERE " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_REL_TIME_SLOT_CLASS + ".id_time_slot = ?";
        JsonArray params = new JsonArray().add(timeslotId);
        Sql.getInstance().prepared(query, params, SqlResult.validResultHandler(res -> {
            if (res.isLeft()) {
                promise.fail(res.left().getValue());
            } else {
                JsonArray result = new JsonArray();
                ((List<JsonObject>) res.right().getValue().getList()).forEach(jsonObject -> result.add(jsonObject.getString("id_class")));
                promise.complete(result);
            }
        }));
        return promise.future();
    }
}
