package fr.openent.viescolaire.controller;

import fr.openent.viescolaire.service.UserService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.controller.ControllerHelper;

public class GroupingController extends ControllerHelper {
    private UserService userService;
    protected static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Get("/structures/:structureId/trombinoscope/setting")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    @ApiDoc("Determine structure trombinoscope if active or disable")
    public void getTrombinoscopeSetting(HttpServerRequest request) {
        String structureId = request.getParam("structureId");

        trombinoscopeService.getSetting(structureId, settingAsync -> {
            if (settingAsync.failed()) {
                renderError(request, new JsonObject().put("error", settingAsync.cause().getMessage()));
            } else {
                renderJson(request, new JsonObject().put("active",  settingAsync.result()));
            }
        });
    }
}