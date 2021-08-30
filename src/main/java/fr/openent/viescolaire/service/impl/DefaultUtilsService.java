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
import fr.openent.viescolaire.service.EleveService;
import fr.openent.viescolaire.service.UtilsService;
import fr.openent.viescolaire.utils.FormateFutureEvent;
import fr.wseduc.webutils.Either;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.sql.Sql;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


import static org.entcore.common.sql.SqlResult.validResultHandler;

public class DefaultUtilsService implements UtilsService{

    private final Neo4j neo4j = Neo4j.getInstance();
    private static final String[] COLORS = {"light-orange", "solid-orange", "spiro-blue", "solid-purple", "garden-blue",
            "sky-blue", "keppel-blue", "marine-black", "fuchsia", "indigo",
            "peach-orange", "light-purple", "magenta", "dark-blue", "pink", "honey-orange",
            "light-orange-lighter", "solid-orange-lighter", "spiro-blue-lighter", "garden-blue-lighter",
            "sky-blue-lighter", "keppel-blue-lighter", "marine-black-lighter", "fuchsia-lighter", "indigo-lighter",
            "peach-orange-lighter", "magenta-lighter",
            "dark-blue-lighter", "pink-lighter", "solid-purple-lighter"};
    private static final String[] COURSE_COLORS = {"cyan", "green", "orange", "pink", "yellow", "purple", "grey", "orange",
            "purple", "green", "yellow", "solid-green", "solid-blue", "magenta",
            "light-pink", "light-orange", "solid-red", "light-red", "light-green", "solid-orange"};

    private EleveService eleveService = new DefaultEleveService();
    protected static final Logger log = LoggerFactory.getLogger(DefaultUtilsService.class);

    @Override
    public <T, V> void addToMap(V value, T key, Map<T, List<V>> map) {
        if (map.get(key) == null) {
            map.put(key, new ArrayList<V>());
        }
        map.get(key).add(value);
    }

    @Override
    public JsonObject[] convertTo (Object[] value) {
        ArrayList<JsonObject> result = new ArrayList<>();
        for(Object o : value) {
            result.add(new JsonObject((Map<String, Object>) o));
        }
        return result.toArray(new JsonObject[0]);
    }

    public String getColor(String classes) {
        int number = Math.abs(((classes.hashCode()*31)%100)%(COLORS.length - 1));
        return COLORS[number];
    }

    @Override
    public String getSubjectColor(String subjectId) {
        byte[] bytes = subjectId.getBytes();
        int number = 0;
        for (int i = 0; i < bytes.length ; i++){
            number += (int) bytes[i];
        }

        number = number % COURSE_COLORS.length ;
        return COURSE_COLORS[number] ;
//        return "solid-green";
    }


    @Override
    public void getTypeGroupe(String[] id_classes, Handler<Either<String, JsonArray>> handler) {

        StringBuilder query = new StringBuilder();
        JsonObject values = new JsonObject();

        query.append("MATCH (c:Class) WHERE c.id IN {id_classes} RETURN c.id AS id, c IS NOT NULL AS isClass UNION ")
                .append(" MATCH (g:FunctionalGroup) WHERE g.id IN {id_classes} RETURN g.id AS id, NOT(g IS NOT NULL) ")
                .append(" AS isClass UNION")
                .append(" MATCH (g:ManualGroup) WHERE g.id IN {id_classes} RETURN g.id AS id, NOT(g IS NOT NULL) ")
                .append(" AS isClass ");

        values.put("id_classes", new fr.wseduc.webutils.collections.JsonArray(Arrays.asList(id_classes)))
                .put("id_classes", new fr.wseduc.webutils.collections.JsonArray(Arrays.asList(id_classes)));

        neo4j.execute(query.toString(), values, Neo4jResult.validResultHandler(handler));
    }

    @Override
    public JsonObject mapListNumber(JsonArray list, String key, String value) {
        JsonObject values = new JsonObject();
        JsonObject o;
        for (int i = 0; i < list.size(); i++) {
            o = list.getJsonObject(i);
            values.put(o.getString(key), o.getInteger(value));
        }
        return values;
    }

    @Override
    public JsonObject mapListString (JsonArray list, String key, String value) {
        JsonObject values = new JsonObject();
        JsonObject o;
        for (int i = 0; i < list.size(); i++) {
            o = list.getJsonObject(i);
            values.put(o.getString(key), o.getString(value));
        }
        return values;
    }

    @Override
    public JsonArray saUnion(JsonArray recipient, JsonArray list) {
        for (int i = 0; i < list.size(); i++) {
            recipient.add(list.getValue(i));
        }
        return recipient;
    }

    public JsonArray saUnionUniq(JsonArray recipient, JsonArray list) {
        for (int i = 0; i < list.size(); i++) {
            JsonObject element = (JsonObject) list.getValue(i);
            Object eleve = recipient.getList().stream()
                    .filter(p ->

                                    ((JsonObject) p).getString("idEleve").equals(element.getString("idEleve"))
                            /*&& ((JsonObject) p).getString("idClasse").equals(element.getString("idClasse"))*/


                    )
                    .findFirst()
                    .orElse(null);

            if(eleve == null) {
                recipient.add(element);
            } else {
                JsonObject jsonEleve = (JsonObject) eleve;
                // si l'eleve existe déjà on ajoute uniquement ses anciennes classes
                // (cas élève ayant changé de classe ou supprimé)
                String oldClasseName =  element.getString("classeName");
                String oldClasseId =  element.getString("idClasse");

                JsonObject oldClasses = jsonEleve.getJsonObject("oldClasses");

                if(oldClasses == null) {
                    oldClasses = new JsonObject();
                }

                Map<String, Object> mapOldClasses = oldClasses.getMap();
                mapOldClasses.put(oldClasseId, oldClasseName);
                jsonEleve.put("oldClasses", new JsonObject(mapOldClasses));
            }
        }
        return recipient;
    }

    @Override
    public JsonArray sortArray(JsonArray jsonArr, String[] sortedField) {
        JsonArray sortedJsonArray = new JsonArray();

        List<JsonObject> jsonValues = new ArrayList<>();
        if (jsonArr.size() > 0 && !(jsonArr.getValue(0) instanceof JsonObject)) {
            return jsonArr;
        } else {
            for (int i = 0; i < jsonArr.size(); i++) {
                jsonValues.add(jsonArr.getJsonObject(i));
            }
            jsonValues.sort((a, b) -> {
                StringBuilder valA = new StringBuilder();
                StringBuilder valB = new StringBuilder();

                try {
                    for (String s : sortedField) {
                        if(a.containsKey(s)){
                            valA.append(((String) a.getValue(s)).toLowerCase());
                        }
                        if(b.containsKey(s)){
                            valB.append(((String) b.getValue(s)).toLowerCase());
                        }
                    }
                } catch (Exception e) {
                    log.error("Pb While Sorting Two Array with a : " + a + " and b : " + b, e);
                }

                return valA.toString().compareTo(valB.toString());
            });

            for (int i = 0; i < jsonArr.size(); i++) {
                sortedJsonArray.add(jsonValues.get(i));
            }
            return sortedJsonArray;
        }
    }

    @Override
    /**
     * Récupère la liste des professeurs titulaires d'un remplaçant sur un établissement donné
     * (si lien titulaire/remplaçant toujours actif à l'instant T)
     *
     * @param psIdRemplacant identifiant neo4j du remplaçant
     * @param psIdEtablissement identifiant de l'établissement
     * @param handler handler portant le resultat de la requête : la liste des identifiants neo4j des titulaires
     */
    public void getTitulaires(String psIdRemplacant, String psIdEtablissement, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();

        query.append("SELECT DISTINCT main_teacher_id ")
                .append("FROM "+ Viescolaire.VSCO_SCHEMA +".multi_teaching ")
                .append("WHERE second_teacher_id = ? ")
                .append("AND structure_id = ? ")
                .append("AND ( (start_date <= current_date AND current_date <= entered_end_date AND NOT is_coteaching) OR ")
                .append("is_coteaching )");

        values.add(psIdRemplacant);
        values.add(psIdEtablissement);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public Handler<Message<JsonObject>> addStoredDeletedStudent(JsonArray idClasse, String idStructure,
                                                                String[] idEleves, String[] sortedField,
                                                                Long idPeriode,
                                                                Handler<Either<String, JsonArray>> handler) {
        return event -> {
            if("ok".equals(event.body().getString("status"))) {
                // Récupération des élèves présents dans l'annuaire
                JsonArray deletedStudentsNeo = event.body().getJsonArray("result", new JsonArray());
                JsonArray idsNeo = new JsonArray();
                for (int i = 0; i < deletedStudentsNeo.size(); i++) {
                    String idEleve = deletedStudentsNeo.getJsonObject(i).getString("id");
                    if(idEleve == null) {
                        idEleve = deletedStudentsNeo.getJsonObject(i).getString("idEleve");
                    }
                    idsNeo.add(idEleve);
                }
                // Récupération des élèves supprimés et stockés dans postgres
                eleveService.getStoredDeletedStudent(idClasse, idStructure, idEleves, deletedStudentEvent -> {
                    if(deletedStudentEvent.isRight()) {
                        JsonArray deletedStudentsPostgres = deletedStudentEvent.right().getValue();
                        if(deletedStudentsPostgres != null && deletedStudentsPostgres.size() > 0){
                            List<Object> eleveNeoInPostgres = deletedStudentsPostgres.stream()
                                    .filter(eleveDeleted -> idsNeo.contains(((JsonObject) eleveDeleted).getString("id")))
                                    .collect(Collectors.toList());

                            eleveNeoInPostgres.forEach(deletedStudentsPostgres::remove);
                        }

                        JsonArray result = deletedStudentsPostgres != null ? saUnion(deletedStudentsNeo, deletedStudentsPostgres) : deletedStudentsNeo;
                        if(null == idPeriode) {
                            handler.handle(new Either.Right<>(sortArray(result, sortedField)));
                        } else {
                            // Si on veut filtrer sur la période
                            String[] idGroupes = (String[]) idClasse.getList().toArray(new String[1]);
                            new DefaultPeriodeService().getPeriodes(null, idGroupes, message -> {
                                if (message.isRight()) {
                                    JsonArray periodes = message.right().getValue();
                                    JsonObject periode = (JsonObject) periodes.stream()
                                            .filter(p -> idPeriode.intValue() == ((JsonObject) p).getInteger("id_type"))
                                            .findFirst().orElse(null);
                                    if (periode != null) {
                                        String debutPeriode = periode.getString("timestamp_dt").split("T")[0];
                                        String finPeriode = periode.getString("timestamp_fn").split("T")[0];

                                        DateFormat formatter = new SimpleDateFormat("yy-MM-dd");
                                        try {
                                            final Date dateDebutPeriode = formatter.parse(debutPeriode);
                                            final Date dateFinPeriode = formatter.parse(finPeriode);

                                            getAvailableStudent(result, idPeriode, dateDebutPeriode,
                                                    dateFinPeriode, sortedField, handler);
                                        } catch (ParseException e) {
                                            String messageLog = "Error :can not calcul students " +
                                                    "of groupe : " + idClasse;
                                            log.error(messageLog, e);
                                            handler.handle(new Either.Left<>(messageLog));
                                        }
                                    } else {
                                        handler.handle(new Either.Right<>(sortArray(result, sortedField)));
                                    }
                                }
                            });
                        }
                    } else {
                        handler.handle(new Either.Right<>(deletedStudentsNeo));
                    }
                });
            } else {
                handler.handle(new Either.Left<>("Error While get User in Neo4J"));
            }
        };
    }

    public void getAvailableStudent(JsonArray students, Long idPeriode, Date dateDebutPeriode, Date dateFinPeriode,
                                    String[] sortedField, Handler<Either<String, JsonArray>> handler) {
        JsonArray eleveAvailable = new JsonArray();

        // Si aucune période n'est sélectionnée, on rajoute tous les élèves
        for (int i = 0; i < students.size(); i++) {
            JsonObject student = (JsonObject) students.getValue(i);
            // Sinon Si l'élève n'est pas Supprimé on l'ajoute
            if (idPeriode == null || student.getValue("deleteDate") == null){
                eleveAvailable.add(student);
            } else { //Sinon si sa date de suppression survient avant la fin de la période, on l'ajoute aussi
                Date deleteDate = new Date();

                if (student.getValue("deleteDate") instanceof Number) {
                    deleteDate = new Date(student.getLong("deleteDate"));
                } else {
                    try {
                        deleteDate = new SimpleDateFormat("yy-MM-dd")
                                .parse(student.getString("deleteDate").split("T")[0]);
                    } catch (ParseException e) {
                        String messageLog = "PB While read date of deleted Student : " + student.getString("id");
                        log.error(messageLog, e);
                    }
                }
                if ((deleteDate.after(dateFinPeriode) || deleteDate.equals(dateFinPeriode)) ||
                        ((deleteDate.after(dateDebutPeriode)|| deleteDate.equals(dateDebutPeriode))
                                && (deleteDate.before(dateFinPeriode) || deleteDate.equals(dateFinPeriode)))) {
                    eleveAvailable.add(student);
                }
            }
        }
        handler.handle(new Either.Right<>(sortArray(eleveAvailable, sortedField)));
    }

    private JsonArray formatDeletedStudent(JsonArray classesNames, JsonArray rPostgres){
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
            o.put("classeName", mapClasseName.get(studentIdClasse));
            o.put("idClasse", studentIdClasse);
            o.put("idEleve", student.getString("idEleve"));
            o.put("lastName", student.getString("lastName"));
            o.put("firstName", student.getString("firstName"));
            o.put("deleteDate", student.getString("deleteDate"));
            o.put("displayName", student.getString("displayName"));
            o.put("idGroupes", student.getString("idGroupes"));
            o.put("idEtablissement",student.getString("idEtablissement"));
            o.put("birthDate",student.getString("birthDate"));
            studentPostgres.add(o);
        }
        return studentPostgres;
    }

    private void filterStudentOnPeriode(JsonArray students, JsonArray periodes, Long idPeriode,
                                        Handler<Either<String, JsonArray>> handler) {
        DefaultUtilsService utilsService = new DefaultUtilsService();
        String [] sortedField = new  String[2];
        sortedField[0] = "lastName";
        sortedField[1] = "firstName";


        if (null == idPeriode) {
            handler.handle(new Either.Right<>(utilsService.sortArray(students, sortedField)));
        }
        else {
            // On récupére la période de la classe
            JsonObject periode = null;
            for (int i = 0; i < periodes.size(); i++) {
                if (idPeriode.intValue() == periodes.getJsonObject(i).getInteger("id_type").intValue()) {
                    periode = periodes.getJsonObject(i);
                    break;
                }
            }
            if (periode != null) {
                String debutPeriode = periode.getString("timestamp_dt").split("T")[0];
                String finPeriode = periode.getString("timestamp_fn").split("T")[0];

                DateFormat formatter =  new SimpleDateFormat("yy-MM-dd");
                try {
                    final Date dateDebutPeriode = formatter.parse(debutPeriode);
                    final Date dateFinPeriode = formatter.parse(finPeriode);

                    utilsService.getAvailableStudent(students, idPeriode, dateDebutPeriode, dateFinPeriode,
                            sortedField, handler);

                } catch (ParseException e) {
                    String messageLog = "Error :can not" + "calcul students ";
                    handler.handle(new Either.Left<>(messageLog));

                }
            } else {
                handler.handle(new Either.Right<>(utilsService.sortArray(students, sortedField)));
            }
        }
    }

    @Override
    public Handler<Message<JsonObject>> getEleveWithClasseName(String[] idClasses, String[] idEleves, Long idPeriode,
                                                               Handler<Either<String, JsonArray>> handler) {
        return  eventNeo -> {
            if (!"ok".equals(eventNeo.body().getString("status"))) {
                handler.handle(new Either.Left<>(eventNeo.body().getString("message")));
                return;
            }
            // Récupération des élèves supprimés et stockés dans postgres
            JsonArray rNeo = eventNeo.body().getJsonArray("result");
            new DefaultEleveService().getStoredDeletedStudent(
                    (null != idClasses)?new JsonArray(Arrays.asList(idClasses)) : null,
                    null, idEleves, eventPostgres -> {
                        if (eventPostgres.isLeft()) {
                            // Si on a un problème lors de la récupération postgres
                            // On retourne le résultat de Neo4J
                            handler.handle(new Either.Right<>(rNeo));
                        }

                        else {
                            DefaultUtilsService utilsService = new DefaultUtilsService();
                            JsonArray rPostgres = eventPostgres.right().getValue();


                            // On récupère les id des Classes des élèves Stockés dans Postgres
                            String[] idClassePostgresStudents = new String[rPostgres.size()];
                            for (int i=0; i< rPostgres.size(); i++) {
                                idClassePostgresStudents[i] = rPostgres.getJsonObject(i).getString("idClasse");
                            }

                            // Récupération des périodes si nécessaire
                            Future<JsonArray> periodeFuture = Future.future();
                            if(null == idPeriode){
                                periodeFuture.complete(null);
                            }
                            else{
                                new DefaultPeriodeService().getPeriodes(null, idClasses,
                                        message -> FormateFutureEvent.formate(periodeFuture, message));
                            }


                            Future<JsonArray> classesNameFuture = Future.future();
                            getClassesName(idClassePostgresStudents,
                                    event -> FormateFutureEvent.formate(classesNameFuture, event));

                            // On récupère les noms des Classes des élèves Stockés dans Postgres
                            CompositeFuture.all(classesNameFuture, periodeFuture).setHandler(
                                    event -> {
                                        if(event.failed()){
                                            String error = event.cause().getMessage();
                                            log.error(error);
                                            handler.handle(new Either.Left<>(error));
                                            return;
                                        }
                                        JsonArray classesNames = classesNameFuture.result();
                                        JsonArray studentPostgres = formatDeletedStudent( classesNames, rPostgres);

                                        JsonArray students =  utilsService.saUnionUniq(rNeo, studentPostgres);
                                        // Si on veut filtrer sur la période
                                        JsonArray periodes = periodeFuture.result();
                                        filterStudentOnPeriode(students, periodes, idPeriode, handler);
                                    });
                        }

                    });
        };
    }



    private void getClassesName(String[] idClasses, Handler<Either<String, JsonArray>> handler) {

        try {
            if(idClasses== null || idClasses.length == 0) {
                handler.handle(new Either.Right(new JsonArray()));
                return;
            }

            StringBuilder query = new StringBuilder();
            JsonObject params = new JsonObject();

            query.append("MATCH (c:Class)-[BELONGS]->(s:Structure) WHERE c.id IN {idClasses} ")
                    .append(" RETURN c.id as idClasse, c.name as name ORDER BY c.name ");
            params.put("idClasses", new fr.wseduc.webutils.collections.JsonArray(Arrays.asList(idClasses)));

            neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(handler));
        }
        catch (VertxException e) {
            String error = e.getMessage();
            log.error("getClassesName " + e.getMessage());
            if(error.contains("Connection was closed")){
                getClassesName(idClasses, handler);
            }
        }
    }

    @Override
    public void getIdGroupByExternalId(List<String> externalIdGroups, Handler<Either<String, JsonArray>> handler){
        StringBuilder query = new StringBuilder();
        query.append("MATCH (g:Group) " +
                "WHERE g.externalId IN {id} " +
                "RETURN g.id as id, g.externalId as externalId " +
                "UNION MATCH (c:Class) " +
                "WHERE c.externalId IN {id} " +
                "RETURN c.id as id, c.externalId as externalId");

        JsonObject values = new JsonObject();
        values.put("id", new fr.wseduc.webutils.collections.JsonArray(externalIdGroups));
        neo4j.execute(query.toString(), values, Neo4jResult.validResultHandler(handler));
    }



    @Override
    public Future<JsonArray> getClassGroupExternalIdsFromIds(List<String> classGroupIds, Promise<JsonArray> promise){
        StringBuilder query = new StringBuilder();
        query.append("MATCH (g: Group) WHERE g.id IN {ids} RETURN g.externalId AS externalId " +
                "UNION " +
                "MATCH (c: Class) WHERE c.id IN {ids} RETURN c.externalId AS externalId");

        JsonObject values = new JsonObject();
        values.put("ids", new JsonArray(classGroupIds));

        neo4j.execute(query.toString(), values, res -> {
            if (!res.body().getString("status").equals("ok")) {
                String message = String.format("[Viescolaire@%s::getClassGroupExternalIdsFromIds] " +
                                "Error fetching classes/groups external ids : %s",
                        this.getClass().getSimpleName(), res.body().getString("status"));
                log.error(message);
                promise.fail(message);
            } else {
                promise.complete(res.body().getJsonArray("result"));
            }
        });
        return promise.future();
    }

    @Override
    public Future<JsonArray> getManualGroupNameById(List<String> groupIds, Promise<JsonArray> promise){
        StringBuilder query = new StringBuilder();
        query.append("MATCH (g:ManualGroup) " +
                "WHERE g.id IN {ids} " +
                "RETURN g.name as name");

        JsonObject values = new JsonObject();
        values.put("ids", new JsonArray(groupIds));
        neo4j.execute(query.toString(), values, res -> {
            if (!res.body().getString("status").equals("ok")) {
                String message = String.format("[Viescolaire@%s::getManualGroupNameById] " +
                                "Error fetching manual group names : %s",
                        this.getClass().getSimpleName(), res.body().getString("status"));
                log.error(message);
                promise.fail(message);
            } else {
                promise.complete(res.body().getJsonArray("result"));
            }
        });
        return promise.future();
    }

    @Override
    public void getStructure(String idStructure, Handler<Either<String, JsonObject>> handler){
        StringBuilder query = new StringBuilder();
        query.append("MATCH (s:Structure {id: {idStructure}}) return s ");
        JsonObject params = new JsonObject().put("idStructure", idStructure);
        try{
            neo4j.execute(query.toString(), params, Neo4jResult.validUniqueResultHandler(handler));
        } catch (VertxException e) {
            String error = e.getMessage();
            log.error("getStructure " + e.getMessage());
            if(error.contains("Connection was closed")){
                getStructure(idStructure, handler);
            }

        }

    }

    @Override
    public void getStructures(Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        query.append("MATCH (s:Structure) return s.id ");
        neo4j.execute(query.toString(), new JsonObject(), Neo4jResult.validResultHandler(handler));
    }

    public JsonObject findWhere(JsonArray collection, JsonObject oCriteria) {

        Integer matchNeeded = oCriteria.size();
        Integer matchFound = 0;

        for (Object o : collection) {
            JsonObject object = (JsonObject) o;
            for (Map.Entry<String, Object> criteriaValue : oCriteria.getMap().entrySet()) {
                if (object.containsKey(criteriaValue.getKey()) && object.getValue(criteriaValue.getKey()).equals(criteriaValue.getValue())) {
                    matchFound++;
                    if (matchFound.equals(matchNeeded)) {
                        return object;
                    }
                }
            }
            matchFound = 0;
        }
        return null;
    }

    public Object find(Iterable collection, Predicate predicate) {

        for (Object o : collection) {
            if (predicate.test(o)) {
                return o;
            }
        }

        return null;
    }

    public JsonArray filter(JsonArray collection, Predicate predicate) {
        JsonArray result = new JsonArray();

        for(Object o : collection) {
            if(predicate.test(o)) {
                result.add(o);
            }
        }

        return result;
    }

    public Collection pluck(Iterable collection, String key) {
        Set result = new HashSet();

        for (Object o : collection) {
            JsonObject castedO = (JsonObject) o;
            if (castedO.containsKey(key)) {
                if(castedO.getValue(key) instanceof Collection) {
                    result.addAll((Collection) castedO.getValue(key));
                } else {
                    result.add(castedO.getValue(key));
                }
            }
        }

        return result;
    }

    public Collection map(Iterable collection, Function fct) {
        List result = new ArrayList();

        for(Object o : collection) {
            result.add(fct.apply(o));
        }

        return result;
    }


    public JsonArray flatten(JsonArray collection, String keyToFlatten) {
        log.debug("DEBUT flatten");
        JsonArray result = new JsonArray();
        for (Object o : collection) {
            JsonObject castedO = (JsonObject) o;
            if (castedO.containsKey(keyToFlatten)) {
                JsonArray arrayToFlatten = castedO.getJsonArray(keyToFlatten);
                for (Object item : arrayToFlatten) {
                    JsonObject newObject = new JsonObject(new HashMap<>(castedO.getMap()));
                    newObject.remove(keyToFlatten);
                    newObject.put(keyToFlatten, item);
                    result.add(newObject);
                }
            }
        }
        log.debug("FIN flatten");
        return result;
    }

}
