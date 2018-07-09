package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.EleveService;
import fr.openent.viescolaire.service.UtilsService;
import fr.wseduc.webutils.Either;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.sql.Sql;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.entcore.common.sql.SqlResult.validResultHandler;

public class DefaultUtilsService implements UtilsService{

    private final Neo4j neo4j = Neo4j.getInstance();
    private static final String[] COLORS = {"cyan", "green", "orange", "pink", "yellow", "purple", "grey","orange","purple", "green", "yellow"};
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
            byte[] bytes = classes.getBytes();
            int number = 0;
            for (int i = 0; i < bytes.length ; i++){
                number += (int) bytes[i];
            }
            number = (int) Math.abs(Math.floor(Math.sin( (double) number) * 10 ) ) ;
        return COLORS[number] ;
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

    @Override
    public JsonArray sortArray(JsonArray jsonArr, String[] sortedField) {
        JsonArray sortedJsonArray = new JsonArray();

        List<JsonObject> jsonValues = new ArrayList<JsonObject>();
        if (jsonArr.size() > 0 && ! (jsonArr.getValue(0) instanceof  JsonObject)) {
            return jsonArr;
        }
        else{
            for (int i = 0; i < jsonArr.size(); i++) {
                jsonValues.add(jsonArr.getJsonObject(i));
            }
            Collections.sort(jsonValues, new Comparator<JsonObject>() {

                @Override
                public int compare(JsonObject a, JsonObject b) {
                    String valA = new String();
                    String valB = new String();

                    try {
                        for (int i = 0; i < sortedField.length; i++) {
                            valA += (String) a.getValue(sortedField[i]);
                            valB += (String) b.getValue(sortedField[i]);
                        }
                    } catch (Exception e) {
                        //do something
                        log.error("Pb While Sorting Two Array",e);
                    }

                    return valA.compareTo(valB);
                    //if you want to change the sort order, simply use the following:
                    //return -valA.compareTo(valB);
                }
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

        query.append("SELECT DISTINCT id_titulaire ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".rel_professeurs_remplacants ")
                .append("WHERE id_remplacant = ? ")
                .append("AND id_etablissement = ? ")
                .append("AND date_debut <= current_date ")
                .append("AND current_date <= date_fin ");

        values.add(psIdRemplacant);
        values.add(psIdEtablissement);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public Handler<Message<JsonObject>> addStoredDeletedStudent( JsonArray idClasse,
                                     String idStructure,String[] idEleves, String [] sortedField,
                                                                 Long idPeriode,
                                                                 Handler<Either<String, JsonArray>> handler) {

        return  new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> event) {
                if ("ok".equals(((JsonObject)event.body()).getString("status"))) {

                    // Récupération des élèves présents dans l'annuaire
                    JsonArray rNeo = ((JsonObject)event.body()).getJsonArray("result",
                            new fr.wseduc.webutils.collections.JsonArray());

                    // Récupération des élèves supprimés et stockés dans postgres
                    eleveService.getStoredDeletedStudent(idClasse,idStructure, idEleves,
                            new Handler<Either<String, JsonArray>>() {
                                public void handle(Either<String, JsonArray> event) {
                                    if (event.isRight()) {
                                        JsonArray rPostgres = event.right().getValue();
                                        JsonArray result =  saUnion(rNeo, rPostgres);
                                        if (null == idPeriode) {
                                            handler.handle(new Either.Right(sortArray(result, sortedField)));
                                        }
                                        else {
                                            // Si on veut filtrer sur la période
                                            new DefaultPeriodeService().getPeriodes(null,
                                                    (String[])idClasse.getList().toArray(new String[1]),
                                            new  Handler<Either<String, JsonArray>>() {
                                                @Override
                                                public void handle(Either<String, JsonArray> message) {

                                                    if (message.isRight()) {
                                                        JsonArray periodes = message.right().getValue();
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

                                                                getAvailableStudent(result, idPeriode,
                                                                        dateDebutPeriode, dateFinPeriode,
                                                                        sortedField,handler);

                                                            } catch (ParseException e) {
                                                                String messageLog = "Error :can not calcul students " +
                                                                        "of groupe : " + idClasse;
                                                                log.error(messageLog, e);
                                                                handler.handle(new Either.Left<>(messageLog));
                                                            }
                                                        } else {
                                                            handler.handle(new Either.Right<>(sortArray(result,
                                                                    sortedField)));
                                                        }
                                                    }

                                                }
                                            });
                                        }
                                    } else {
                                       handler.handle(new Either.Right<>(rNeo));
                                    }
                                }
                            });


                } else {
                    handler.handle(new Either.Left<>("Error While get User in Neo4J "));
                }
            }
        };
    }

    public void getAvailableStudent (JsonArray students, Long idPeriode,
                                      Date dateDebutPeriode, Date dateFinPeriode,String [] sortedField,
                                      Handler<Either<String, JsonArray>> handler ) {
        JsonArray eleveAvailable = new JsonArray();

            // Si aucune période n'est sélectionnée, on rajoute tous les élèves
            for (int i = 0; i < students.size(); i++) {
                JsonObject student = (JsonObject)students.getValue(i);
                // Sinon Si l'élève n'est pas Supprimé on l'ajoute
                if (    idPeriode == null ||
                        student.getValue("deleteDate") == null ){
                    eleveAvailable.add(student);
                }
                // Sinon S'il sa date sa suppression survient avant la fin de
                // la période, on l'ajoute aussi
                else {
                    Date deleteDate = new Date();

                    if (student.getValue("deleteDate")
                            instanceof Number) {
                        deleteDate = new Date(student.getLong("deleteDate"));
                    }
                    else {
                        try {

                            deleteDate = new SimpleDateFormat("yy-MM-dd")
                                    .parse(student.getString("deleteDate").split("T")[0]);

                        } catch (ParseException e) {
                            String messageLog = "PB While read date of deleted Student : "
                                    + student.getString("id");
                            log.error(messageLog, e);
                        }

                    }
                    if ( (deleteDate.after(dateFinPeriode) || deleteDate.equals(dateFinPeriode))
                            ||
                            ((deleteDate.after(dateDebutPeriode)
                                    || deleteDate.equals(dateDebutPeriode))
                                    && (deleteDate.before(dateFinPeriode)
                                    || deleteDate.equals(dateFinPeriode)))) {
                        eleveAvailable.add(student);
                    }
                }
            }
        handler.handle(new Either.Right<>(sortArray(eleveAvailable,sortedField)));
    }

    @Override
    public Handler<Message<JsonObject>> getEleveWithClasseName(String[] idClasses, String[] idEleves, Long idPeriode,
                                                                 Handler<Either<String, JsonArray>> handler) {
        return new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> eventNeo) {
                if ("ok".equals(((JsonObject) eventNeo.body()).getString("status"))) {
                    // Récupération des élèves supprimés et stockés dans postgres
                    JsonArray rNeo = eventNeo.body().getJsonArray("result");
                    new DefaultEleveService().getStoredDeletedStudent(
                            (null != idClasses)?new JsonArray(Arrays.asList(idClasses)) : null,
                            null, idEleves,
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
                                                                o.put("ClasseName", mapClasseName.get(studentIdClasse));
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
                                                            String [] sortedField = new  String[3];
                                                            sortedField[0] = "name";
                                                            sortedField[1] = "lastName";
                                                            sortedField[2] = "firstName";
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

        };
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
