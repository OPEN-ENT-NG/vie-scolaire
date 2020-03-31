package fr.openent.viescolaire.service;

import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.service.CrudService;

public interface ServicesService extends CrudService {

    void createService(JsonObject oService, Handler<Either<String, JsonObject>> handler);

    void getServicesSQL(String idEtablissement, JsonObject oService, Handler<Either<String, JsonArray>> handler) ;

    void getServicesNeo(String structureId, Handler<Either<String, JsonArray>> result);

    void deleteService(JsonObject oService, Handler<Either<String, JsonObject>> handler);

    void updateServices(JsonObject oServices, Handler<Either<String, JsonObject>> defaultResponseHandler);

    void getAllServices(String structureId, Boolean evaluable, Boolean notEvaluable, Boolean classes, Boolean groups, Boolean manualGroups, HttpServerRequest request, Handler<Either<String, JsonArray>> arrayResponseHandler);

    void getAllServices(String structureId, HttpServerRequest request, Handler<Either<String, JsonArray>> arrayResponseHandler);

    void getAllServices(String structureId, JsonObject oService, Handler<Either<String, JsonArray>> arrayResponseHandler);


}
