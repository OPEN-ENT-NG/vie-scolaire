package fr.openent.viescolaire.service.impl;


import fr.openent.Viescolaire;
import fr.openent.viescolaire.helper.ModelHelper;
import fr.openent.viescolaire.model.Model;
import fr.openent.viescolaire.model.MultiTeaching;
import fr.openent.viescolaire.model.ServiceModel;
import fr.openent.viescolaire.service.MultiTeachingService;
import fr.openent.viescolaire.service.ServicesService;
import fr.openent.viescolaire.service.UtilsService;
import fr.openent.viescolaire.utils.ServicesHelper;
import fr.wseduc.webutils.Either;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;

import javax.security.auth.Subject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.entcore.common.sql.SqlResult.validResultHandler;
import static org.entcore.common.sql.SqlResult.validUniqueResultHandler;

public class DefaultServicesService extends SqlCrudService implements ServicesService {

    private UtilsService utilsService;
    public final static String COEFFICIENT = "coefficient";
    private static Logger log =  LoggerFactory.getLogger(DefaultServicesService.class);
    private final Neo4j neo4j = Neo4j.getInstance();
    private static MultiTeachingService multiTeachingService;

    public DefaultServicesService() {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.SERVICES_TABLE);
        multiTeachingService = new DefaultMultiTeachingService();
        this.utilsService = new DefaultUtilsService();
    }

    public void createService(JsonObject oService, Handler<Either<String, JsonObject>> handler){
        String query = "";
        String columns = "id_matiere, id_groupe, id_enseignant, coefficient";
        String params = "?,?,?,?";
        JsonArray values = new JsonArray();

        for(Object id_groupe : oService.getJsonArray("id_groupes")) {

            values.add(oService.getString("id_matiere"));
            values.add(id_groupe);
            values.add(oService.getString("id_enseignant"));
            values.add(oService.getValue("coefficient"));

            columns = "id_matiere, id_groupe, id_enseignant, coefficient";
            params = "?,?,?,?";

            if (oService.containsKey("id_etablissement")) {
                columns += ", id_etablissement";
                params += ",?";
                values.add(oService.getString("id_etablissement"));
            }

            if (oService.containsKey("modalite")) {
                columns += ", modalite";
                params += ",?";
                values.add(oService.getString("modalite"));
            }

            if (oService.containsKey("evaluable")) {
                columns += ", evaluable";
                params += ",?";
                values.add(oService.getBoolean("evaluable"));
            }

            query += "INSERT INTO " + this.resourceTable + " (" + columns + ") "
                    + "VALUES (" + params + ") ON CONFLICT ON CONSTRAINT pk_services DO UPDATE SET";

            if (oService.containsKey("modalite")) {
                query += " modalite=?";
                values.add(oService.getValue("modalite"));
            }
            if (oService.containsKey("evaluable")) {
                query += oService.containsKey("modalite") ? ", evaluable=?" : " evaluable=?";
                values.add(oService.getBoolean("evaluable"));
            }
            if (oService.containsKey(COEFFICIENT)) {
                query += oService.containsKey("modalite") || oService.containsKey("evaluable") ? ", coefficient=?" : " coefficient=?";
                values.add(oService.getLong(COEFFICIENT));
            }
            if (oService.containsKey("is_visible")) {
                query += oService.containsKey("modalite") || oService.containsKey("evaluable")
                        || oService.containsKey(COEFFICIENT) ? ", is_visible=?" : " is_visible=?";
                values.add(oService.getBoolean("is_visible"));
            }

            query += "; ";
        }

        Sql.getInstance().prepared(query, values, validUniqueResultHandler(handler));
    }

    public void getServicesSQL(String idEtablissement, JsonObject oService, Handler<Either<String, JsonArray>> handler) {
        String sqlQuery = "SELECT * FROM " + this.resourceTable + " WHERE id_etablissement = ?";
        JsonArray sqlValues = new JsonArray();
        sqlValues.add(idEtablissement);

        if (!oService.isEmpty()) {
            for (Map.Entry<String, Object> entry : oService.getMap().entrySet()) {
                if (entry.getValue() instanceof JsonArray) {
                    sqlQuery += " AND " + entry.getKey() + " IN " + Sql.listPrepared(((JsonArray) entry.getValue()).getList());
                    for (Object o : ((JsonArray) entry.getValue()).getList()) {
                        sqlValues.add(o);
                    }
                } else {
                    sqlQuery += " AND " + entry.getKey() + " = ?";
                    sqlValues.add(entry.getValue());
                }
            }
        }

        Sql.getInstance().prepared(sqlQuery, sqlValues, validResultHandler(handler));
    }

    public void getClassesFromStructureForServices(String structureId, Handler<Either<String, JsonArray>> result){
        String query = "MATCH (s:Structure{id:{structureId}})<-[:SUBJECT]-(sub:Subject)<-[r:TEACHES]-(u:User) "+
                "RETURN u.id as idEnseignant, s.id as idEtablissement, sub.id as id, sub.code as externalId, sub.label as name, r.classes + r.groups as libelleClasses";
        JsonObject params = new JsonObject().put("structureId", structureId);

        neo4j.execute(query, params, Neo4jResult.validResultHandler(result));
    }

    public void getSubjectANdTeachersForServices(String structureId, Handler<Either<String,JsonArray>> result){
        String query = "MATCH (s:Structure{id:{structureId}})--(c) "+
                " WHERE (c:Class OR c:FunctionalGroup OR c:ManualGroup) and EXISTS(c.externalId) " +
                " RETURN c.id as id,c.externalId  as externalId";
        JsonObject params = new JsonObject().put("structureId", structureId);

        neo4j.execute(query ,params , Neo4jResult.validResultHandler(result));
    }

    @Override
    public void getServicesNeo(String structureId, Handler<Either<String, JsonArray>> result){
        List<Future> futures = new ArrayList<>();

        Future<JsonArray>  getSubjectANdTeachersFuture = Future.future();
        futures.add(getSubjectANdTeachersFuture);
        getSubjectANdTeachersForServices(structureId,getHandlerJsonArray(getSubjectANdTeachersFuture));

        Future<JsonArray> getClassesFromStructureForFuture = Future.future();
        futures.add(getClassesFromStructureForFuture);
        getClassesFromStructureForServices(structureId,getHandlerJsonArray(getClassesFromStructureForFuture));

        CompositeFuture.all(futures).setHandler(event -> {
            if (event.succeeded()) {
                JsonArray subjectANdTeachersResult = (JsonArray) event.result().list().get(0);
                JsonArray classResult = (JsonArray) event.result().list().get(1);
                result.handle(new Either.Right(createServicesFromNeo(subjectANdTeachersResult, classResult))); ;
            }else{
                result.handle(new Either.Left<>("Error when gettings subjects and classes"));
            }
        });
    }

    private JsonArray createServicesFromNeo(JsonArray subjectANdTeachersResult, JsonArray classResult) {
        HashMap<String,String> courseMap = new HashMap<>();
        for(int i =0 ; i< subjectANdTeachersResult.size(); i++){
            JsonObject subjectResult = subjectANdTeachersResult.getJsonObject(i);
            if(subjectResult.getValue("id") != null && subjectResult.getValue("externalId") != null) {
                String id, externalId;
                id = subjectResult.getString("id");
                externalId = subjectResult.getString("externalId");
                courseMap.put(externalId, id);
            }
        }
        for(int j = 0 ;j< classResult.size() ; j++) {
            JsonObject resultClass = classResult.getJsonObject(j);
            if(resultClass.getValue("libelleClasses") != null) {
                JsonArray libelleClasses = resultClass.getJsonArray("libelleClasses");
                for (Object externalId : libelleClasses) {
                    if (courseMap.containsKey(externalId))
                        if (resultClass.containsKey("idClasses")) {
                            resultClass.put("idClasses", resultClass.getJsonArray("idClasses").add(courseMap.get(externalId)));
                        } else {
                            resultClass.put("idClasses", new JsonArray().add(courseMap.get(externalId)));
                        }
                }
            }
        }
        return classResult;
    }

    private Handler<Either<String, JsonArray>> getHandlerJsonArray(Future<JsonArray> serviceFuture) {
        return event -> {
            if (event.isRight()) {
                serviceFuture.complete(event.right().getValue());
            } else {
                serviceFuture.fail(event.left().getValue());
            }
        };
    }


    public void deleteService(JsonObject oService, Handler<Either<String, JsonObject>> handler) {
        JsonArray classOrGroupIds = oService.getJsonArray("id_groups");

        String query = "DELETE FROM " + this.resourceTable + " WHERE id_matiere=? AND id_enseignant=? " +
                "AND id_groupe IN " + Sql.listPrepared(classOrGroupIds.getList());

        JsonArray values = new JsonArray().add(oService.getString("id_matiere"))
                .add(oService.getString("id_enseignant")).addAll(classOrGroupIds);

        Sql.getInstance().prepared(query, values, validUniqueResultHandler(handler));
    }

    protected Handler<Either<String, JsonObject>> getHandler(Future<JsonObject> future) {
        return event -> {
            if (event.isRight()) {
                future.complete(event.right().getValue());
            } else {
                future.fail(event.left().getValue());
            }
        };
    }

    @Override
    public void updateServices(JsonObject oServices, Handler<Either<String, JsonObject>> defaultResponseHandler) {
        List<Future> futures = new ArrayList<>();

        CompositeFuture.all(futures).setHandler(event -> {
            if (event.succeeded()) {
                defaultResponseHandler.handle(new Either.Right<>(new JsonObject().put("services" , new JsonArray(event.result().list()))));
            } else {
                defaultResponseHandler.handle(new Either.Left<>("error when throwing futures"));
            }
        });

        JsonArray services = oServices.getJsonArray("services");
        for(Object service : services){
            Future<JsonObject> serviceFuture = Future.future();
            futures.add(serviceFuture);
            createService((JsonObject) service, getHandler(serviceFuture));
        }
    }

    @Override
    public void getAllServices(String structureId, Boolean evaluable, Boolean notEvaluable, Boolean classes,
                               Boolean groups, Boolean manualGroups, Boolean compressed, JsonObject oService,
                               Handler<Either<String, JsonArray>> arrayResponseHandler) {
        List<Future> futures = new ArrayList<>();

        Future<JsonArray> getServicesNeoFuture = Future.future();
        Future<JsonArray> getServiceSQLFuture = Future.future();
        Future<JsonArray> getMutliTeachingFuture = Future.future();

        futures.add(getServicesNeoFuture);
        futures.add(getServiceSQLFuture);
        futures.add(getMutliTeachingFuture);

        getServicesNeo(structureId, getHandlerJsonArray(getServicesNeoFuture));
        getServicesSQL(structureId, oService, getHandlerJsonArray(getServiceSQLFuture));
        multiTeachingService.getMultiTeaching(structureId, getHandlerJsonArray(getMutliTeachingFuture));
        CompositeFuture.all(futures).setHandler(event -> {
            if (event.succeeded()) {
                JsonArray getServicesNeoResult = (JsonArray) event.result().list().get(0);
                JsonArray getServicesSQLResult = (JsonArray) event.result().list().get(1);
                JsonArray multiTeachingServiceResult = (JsonArray) event.result().list().get(2);
                List<ServiceModel> services = new ArrayList<>() ;
                List<MultiTeaching> mutliTeachings = new ArrayList<>() ;
                ServicesHelper.handleMultiTeaching(utilsService.flatten(getServicesNeoResult, "idClasses"),
                        arrayResponseHandler, manualGroups, groups, classes, notEvaluable, evaluable, compressed,
                        getServicesSQLResult, services, mutliTeachings, multiTeachingServiceResult);
            } else {
                arrayResponseHandler.handle(new Either.Left<>("Error when gettings subjects and classes"));
            }
        });
    }

    @Override
    public void getAllServicesNoFilter(String structureId, JsonObject oService,
                                       Handler<Either<String, JsonArray>> arrayResponseHandler) {
        getAllServices(structureId, true,true,true,true,
                true, false, oService, arrayResponseHandler);
    }
}
