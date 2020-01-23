package fr.openent.viescolaire.controller;

import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import org.entcore.common.controller.ControllerHelper;

public class ConfigController extends ControllerHelper {

    @Get("/config")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getConfig(HttpServerRequest request) {
        JsonObject services = config.getJsonObject("services", new JsonObject());
        JsonObject config = new JsonObject()
                .put("competences", services.getBoolean("competences", false))
                .put("presences", services.getBoolean("presences", false))
                .put("diary", services.getBoolean("diary", false))
                .put("edt", services.getBoolean("edt", false))
                .put("massmailing", services.getBoolean("massmailing", false));
        renderJson(request, config);
    }
}
