package fr.openent.viescolaire.service.impl;


import fr.openent.Viescolaire;
import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.core.enums.ServicesFieldEnum;
import fr.openent.viescolaire.helper.*;
import fr.openent.viescolaire.model.*;
import fr.openent.viescolaire.service.MultiTeachingService;
import fr.openent.viescolaire.service.ServicesService;
import fr.openent.viescolaire.service.UtilsService;
import fr.openent.viescolaire.utils.ServicesHelper;
import fr.wseduc.webutils.Either;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;

import java.util.*;
import java.util.stream.*;

import static fr.wseduc.webutils.Utils.handlerToAsyncHandler;
import static java.util.Objects.isNull;
import static org.entcore.common.sql.SqlResult.*;
import static org.entcore.common.sql.SqlResult.validUniqueResultHandler;

public class DefaultServicesService extends SqlCrudService implements ServicesService {

    public final static String COEFFICIENT = "coefficient";
    private static Logger log = LoggerFactory.getLogger(DefaultServicesService.class);
    private static MultiTeachingService multiTeachingService;
    private final Neo4j neo4j = Neo4j.getInstance();
    private UtilsService utilsService;
    private EventBus eb;

    public DefaultServicesService() {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.SERVICES_TABLE);
        multiTeachingService = new DefaultMultiTeachingService();
        this.utilsService = new DefaultUtilsService();
        this.eb = null;
    }

    public DefaultServicesService(EventBus eb) {
        this();
        this.eb = eb;
    }

    @Override
    public Future<JsonObject> createService(InitServiceModel service) {
        Promise<JsonObject> promise = Promise.promise();
        this.createService(service.toJson(), FutureHelper.handlerEitherPromise(promise,
                String.format("[Viescolaire@%s::createService] Failed to create service", this.getClass().getSimpleName())));
        return promise.future();
    }

    @Override
    public void createService(JsonObject oService, Handler<Either<String, JsonObject>> handler) {
        List<TransactionElement> statements = new ArrayList<>();

        List<String> groupIds = oService.getJsonArray(Field.ID_GROUPES).stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .collect(Collectors.toList());

        for (String id_groupe : groupIds) {
            statements.add(this.createServiceForGroup(oService, id_groupe));
        }

        String message = String.format("[Viescolaire@%s::createService] Failed to create service", this.getClass().getSimpleName());

        TransactionHelper.executeTransaction(statements, message)
                        .onFailure(fail -> handler.handle(new Either.Left<>(fail.getMessage())))
                        .onSuccess(success -> handler.handle(new Either.Right<>(new JsonObject().put(Field.SUCCESS, true))));
    }

    private TransactionElement createServiceForGroup(JsonObject oService, String groupId) {
        JsonArray values = new JsonArray();
        String query = "";
        String columns = "id_matiere, id_groupe, id_enseignant, coefficient";
        String params = "?,?,?,?";

        values.add(oService.getString(Field.ID_MATIERE));
        values.add(groupId);
        values.add(oService.getString(Field.ID_ENSEIGNANT));
        values.add(oService.getValue(Field.COEFFICIENT));

        if (oService.containsKey(Field.ID_ETABLISSEMENT)) {
            columns += ", id_etablissement";
            params += ",?";
            values.add(oService.getString(Field.ID_ETABLISSEMENT));
        }

        if (oService.containsKey(Field.MODALITE)) {
            columns += ", modalite";
            params += ",?";
            values.add(oService.getString(Field.MODALITE));
        }

        if (oService.containsKey(Field.EVALUABLE)) {
            columns += ", evaluable";
            params += ",?";
            values.add(oService.getBoolean(Field.EVALUABLE));
        }

        query += "INSERT INTO " + this.resourceTable + " (" + columns + ") "
                + "VALUES (" + params + ") ON CONFLICT (id_enseignant, id_matiere, id_groupe) DO UPDATE SET";

        if (oService.containsKey(Field.MODALITE)) {
            query += " modalite=?";
            values.add(oService.getValue(Field.MODALITE));
        }
        if (oService.containsKey(Field.EVALUABLE)) {
            query += oService.containsKey(Field.MODALITE) ? ", evaluable=?" : " evaluable=?";
            values.add(oService.getBoolean(Field.EVALUABLE));
        }
        if (oService.containsKey(Field.COEFFICIENT)) {
            query += oService.containsKey(Field.MODALITE) || oService.containsKey(Field.EVALUABLE)
                    ? ", coefficient=?" : " coefficient=?";
            values.add(oService.getLong(Field.COEFFICIENT));
        }
        if (oService.containsKey(Field.IS_VISIBLE)) {
            query += oService.containsKey(Field.MODALITE) || oService.containsKey(Field.EVALUABLE)
                    || oService.containsKey(Field.COEFFICIENT) ? ", is_visible=?" : " is_visible=?";
            values.add(oService.getBoolean(Field.IS_VISIBLE));
        }

        query += "; ";

        return new TransactionElement(query, values);
    }

    public void getServicesSQL(String idEtablissement, JsonObject oService, Handler<Either<String, JsonArray>> handler) {
        String sqlQuery = "SELECT * FROM " + this.resourceTable + " WHERE id_etablissement = ?";
        JsonArray sqlValues = new JsonArray();
        sqlValues.add(idEtablissement);

        if (!oService.isEmpty()) {
            for (Map.Entry<String, Object> entry : oService.getMap().entrySet()) {
                String servicesField = ServicesFieldEnum.getServicesField(entry.getKey());
                if (servicesField != null) {
                    if (entry.getValue() instanceof JsonArray) {
                        sqlQuery += " AND " + servicesField + " IN " + Sql.listPrepared(((JsonArray) entry.getValue()).getList());
                        for (Object o : ((JsonArray) entry.getValue()).getList()) {
                            sqlValues.add(o);
                        }
                    } else {
                        sqlQuery += " AND " + servicesField + " = ?";
                        sqlValues.add(entry.getValue());
                    }
                }
            }
        }

        Sql.getInstance().prepared(sqlQuery, sqlValues, validResultHandler(handler));
    }

    public void getClassesFromStructureForServices(String structureId, Handler<Either<String, JsonArray>> result) {
        String query = "MATCH (s:Structure{id:{structureId}})<-[:SUBJECT]-(sub:Subject)<-[r:TEACHES]-(u:User) " +
                "RETURN u.id as idEnseignant, s.id as idEtablissement, sub.id as id, sub.code as externalId, sub.label as name, r.classes + r.groups as libelleClasses";
        JsonObject params = new JsonObject().put("structureId", structureId);

        neo4j.execute(query, params, Neo4jResult.validResultHandler(result));
    }

    public void getSubjectANdTeachersForServices(String structureId, Handler<Either<String, JsonArray>> result) {
        String query = "MATCH (s:Structure{id:{structureId}})--(c) " +
                " WHERE (c:Class OR c:FunctionalGroup OR c:ManualGroup) and EXISTS(c.externalId) " +
                " RETURN c.id as id,c.externalId  as externalId";
        JsonObject params = new JsonObject().put("structureId", structureId);

        neo4j.execute(query, params, Neo4jResult.validResultHandler(result));
    }

    @Override
    public void getServicesNeo(String structureId, Handler<Either<String, JsonArray>> result) {
        List<Future<JsonArray>> futures = new ArrayList<>();

        Promise<JsonArray> getSubjectANdTeachersPromise = Promise.promise();
        futures.add(getSubjectANdTeachersPromise.future());
        getSubjectANdTeachersForServices(structureId, getHandlerJsonArray(getSubjectANdTeachersPromise));

        Promise<JsonArray> getClassesFromStructureForPromise = Promise.promise();
        futures.add(getClassesFromStructureForPromise.future());
        getClassesFromStructureForServices(structureId, getHandlerJsonArray(getClassesFromStructureForPromise));

        Future.all(futures).onComplete(event -> {
            if (event.succeeded()) {
                JsonArray subjectANdTeachersResult = (JsonArray) event.result().list().get(0);
                JsonArray classResult = (JsonArray) event.result().list().get(1);
                result.handle(new Either.Right(createServicesFromNeo(subjectANdTeachersResult, classResult)));
            } else {
                result.handle(new Either.Left<>("Error when gettings subjects and classes"));
            }
        });
    }

    private JsonArray createServicesFromNeo(JsonArray subjectANdTeachersResult, JsonArray classResult) {
        HashMap<String, String> courseMap = new HashMap<>();
        for (int i = 0; i < subjectANdTeachersResult.size(); i++) {
            JsonObject subjectResult = subjectANdTeachersResult.getJsonObject(i);
            if (subjectResult.getValue("id") != null && subjectResult.getValue("externalId") != null) {
                String id, externalId;
                id = subjectResult.getString("id");
                externalId = subjectResult.getString("externalId");
                courseMap.put(externalId, id);
            }
        }
        for (int j = 0; j < classResult.size(); j++) {
            JsonObject resultClass = classResult.getJsonObject(j);
            if (resultClass.getValue("libelleClasses") != null) {
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

    private Handler<Either<String, JsonArray>> getHandlerJsonArray(Promise<JsonArray> servicePromise) {
        return event -> {
            if (event.isRight()) {
                servicePromise.complete(event.right().getValue());
            } else {
                servicePromise.fail(event.left().getValue());
            }
        };
    }


    @Override
    public void deleteService(JsonObject oService, JsonObject moduleServices, Handler<Either<String, JsonObject>> handler) {
        JsonArray classOrGroupIds = oService.getJsonArray("id_groups");

        String query = "DELETE FROM " + this.resourceTable + " WHERE id_matiere=? AND id_enseignant=? " +
                "AND id_groupe IN " + Sql.listPrepared(classOrGroupIds.getList());

        JsonArray values = new JsonArray().add(oService.getString("id_matiere"))
                .add(oService.getString("id_enseignant")).addAll(classOrGroupIds);

        sql.prepared(query, values, validUniqueResultHandler(event -> {
            if (event.isLeft()) {
                log.error(String.format("[Viescolaire@%s::deleteService] Failed to send subtopics infos to delete: %s",
                        this.getClass().getSimpleName(), event.left().getValue()));
                handler.handle(new Either.Left<>(event.left().getValue()));
            } else {
                if (isNull(moduleServices) || Boolean.TRUE.equals(moduleServices.getBoolean(Field.COMPETENCES))) {
                    deleteSubtopicsOfService(oService)
                            .onSuccess(res -> handler.handle(new Either.Right<>(res)))
                            .onFailure(err -> {
                                log.error(String.format("[Viescolaire@%s::deleteService] Failed to send subtopics infos to delete: %s",
                                        this.getClass().getSimpleName(), event.left().getValue()));
                                handler.handle(new Either.Left<>(event.left().getValue()));
                            });
                } else {
                    handler.handle(new Either.Right<>(event.right().getValue()));
                }
            }
        }));
    }

    @Override
    public Future<JsonObject> deleteServiceBySubjectId(String structureId, String subjectId) {
        Promise<JsonObject> promise = Promise.promise();

        String query = "DELETE FROM " + this.resourceTable + " WHERE id_etablissement = ? AND id_matiere = ?";
        JsonArray values = new JsonArray().add(structureId).add(subjectId);

        sql.prepared(query, values, validUniqueResultHandler(
                FutureHelper.handlerEitherPromise(promise, String.format("[Viescolaire@%s::deleteServiceBySubjectId] " +
                                "Failed to delete services",
                        this.getClass().getSimpleName()))));
        return promise.future();
    }

    private Future<JsonObject> deleteSubtopicsOfService(JsonObject oService) {
        Promise<JsonObject> promise = Promise.promise();
        JsonObject action = new JsonObject()
                .put("action", "subtopics.deleteSubtopics")
                .put("id_matiere", oService.getString("id_matiere"))
                .put("id_enseignant", oService.getString("id_enseignant"))
                .put("id_groups", oService.getJsonArray("id_groups"));
        this.eb.request(Viescolaire.COMPETENCES_BUS_ADDRESS, action, handlerToAsyncHandler(event -> {
            if (event.body().getString(Field.STATUS).equals(Field.OK)) {
                promise.complete(new JsonObject().put(Field.RESULTS, event.body().getJsonArray(Field.RESULTS)));
            } else {
                log.error(String.format("[Viescolaire@%s::deleteSubtopicsOfService] Failed to send subtopics infos to delete",
                        this.getClass().getSimpleName()));
                promise.fail(event.body().getString(Field.MESSAGE));
            }
        }));
        return promise.future();
    }

    protected Handler<Either<String, JsonObject>> getHandler(Promise<JsonObject> promise) {
        return event -> {
            if (event.isRight()) {
                promise.complete(event.right().getValue());
            } else {
                promise.fail(event.left().getValue());
            }
        };
    }

    @Override
    public void updateServices(JsonObject oServices, Handler<Either<String, JsonObject>> defaultResponseHandler) {
        List<Future<JsonObject>> futures = new ArrayList<>();

        Future.all(futures).onComplete(event -> {
            if (event.succeeded()) {
                defaultResponseHandler.handle(new Either.Right<>(new JsonObject().put("services", new JsonArray(event.result().list()))));
            } else {
                defaultResponseHandler.handle(new Either.Left<>("error when throwing futures"));
            }
        });

        JsonArray services = oServices.getJsonArray("services");
        for (Object service : services) {
            Promise<JsonObject> servicePromise = Promise.promise();
            futures.add(servicePromise.future());
            createService((JsonObject) service, getHandler(servicePromise));
        }
    }

    @Override
    public void getAllServices(String structureId, Boolean evaluable, Boolean notEvaluable, Boolean classes,
                               Boolean groups, Boolean manualGroups, Boolean compressed, JsonObject oService,
                               Handler<Either<String, JsonArray>> arrayResponseHandler) {
        List<Future<JsonArray>> futures = new ArrayList<>();


        Promise<JsonArray> getServicesNeoPromise = Promise.promise();
        Promise<JsonArray> getServiceSQLPromise = Promise.promise();
        Promise<JsonArray> getMutliTeachingPromise = Promise.promise();

        futures.add(getServicesNeoPromise.future());
        futures.add(getServiceSQLPromise.future());
        futures.add(getMutliTeachingPromise.future());

        getServicesNeo(structureId, getHandlerJsonArray(getServicesNeoPromise));
        getServicesSQL(structureId, oService, getHandlerJsonArray(getServiceSQLPromise));
        multiTeachingService.getMultiTeaching(structureId, getHandlerJsonArray(getMutliTeachingPromise));
        Future.all(futures).onComplete(event -> {
            if (event.succeeded()) {
                JsonArray getServicesNeoResult = (JsonArray) event.result().list().get(0);
                JsonArray getServicesSQLResult = (JsonArray) event.result().list().get(1);
                JsonArray multiTeachingServiceResult = (JsonArray) event.result().list().get(2);
                List<ServiceModel> services = new ArrayList<>();
                List<MultiTeaching> mutliTeachings = new ArrayList<>();
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
        getAllServices(structureId, true, true, true, true, true,
                false, oService, arrayResponseHandler);
    }

    @Override
    public void getAllEvaluableServicesNoFilter(String structureId, JsonObject oService,
                                                Handler<Either<String, JsonArray>> arrayResponseHandler) {
        getAllServices(structureId, true, false, true, true, true,
                false, oService, arrayResponseHandler);
    }

    /**
     * get evaluables groups in service SQL
     *
     * @param groupIds      list of group id
     * @return  {@link JsonArray}
     *  Containing response of
     *  [
     *    {
     *      "id_groupe": String
     *    }
     *  ]
     */
    public Future<JsonArray> getEvaluableGroups(List<String> groupIds) {
        Promise<JsonArray> promiseEvaluableGroups = Promise.promise();

        StringBuilder query = new StringBuilder("SELECT DISTINCT (id_groupe) FROM ").append(this.resourceTable)
                .append(" WHERE evaluable = true ");

        JsonArray valuesRequest = new JsonArray();

        if (groupIds != null &&  !groupIds.isEmpty()) {
            query.append("AND id_groupe IN ").append(Sql.listPrepared(groupIds));
            valuesRequest.addAll(new JsonArray(groupIds));
        }

        Sql.getInstance().prepared(query.toString(),valuesRequest, validResultHandler(FutureHelper.handlerEitherPromise(promiseEvaluableGroups,
                String.format("[Viescolaire@%s::getEvaluableGroups] error request sql : ", this.getClass().getSimpleName()))));

        return promiseEvaluableGroups.future();
    }
}
