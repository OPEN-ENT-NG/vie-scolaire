package fr.openent.viescolaire.service;

import fr.openent.viescolaire.model.*;
import fr.wseduc.webutils.Either;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.service.CrudService;

import java.util.List;

public interface ServicesService extends CrudService {
    void createService(JsonObject oService, Handler<Either<String, JsonObject>> handler);
    Future<JsonObject> createService(InitServiceModel service);

    void getServicesSQL(String idEtablissement, JsonObject oService, Handler<Either<String, JsonArray>> handler) ;

    void getServicesNeo(String structureId, Handler<Either<String, JsonArray>> result);

    void deleteService(JsonObject oService, JsonObject moduleServices, Handler<Either<String, JsonObject>> handler);

    Future<JsonObject> deleteServiceBySubjectId(String structureId, String subjectId);
    void updateServices(JsonObject oServices, Handler<Either<String, JsonObject>> defaultResponseHandler);

    void getAllServices(String structureId, Boolean evaluable, Boolean notEvaluable,
                        Boolean classes, Boolean groups, Boolean manualGroups, Boolean compressed, JsonObject oService,
                        Handler<Either<String, JsonArray>> arrayResponseHandler);

    void getAllServicesNoFilter(String structureId, JsonObject oService,
                                Handler<Either<String, JsonArray>> arrayResponseHandler);

    void getAllEvaluableServicesNoFilter(String structureId, JsonObject oService,
                                Handler<Either<String, JsonArray>> arrayResponseHandler);

    Future<JsonArray> getEvaluableGroups(List<String> groupIds);
}
