package fr.openent.viescolaire.controller;

import fr.openent.viescolaire.core.constants.*;
import fr.openent.viescolaire.security.*;
import fr.openent.viescolaire.service.*;
import fr.wseduc.rs.*;
import fr.wseduc.security.*;
import io.vertx.core.http.*;
import io.vertx.core.json.*;
import io.vertx.core.logging.*;
import org.entcore.common.controller.*;
import org.entcore.common.http.filter.*;

public class InitController extends ControllerHelper {

    private static final Logger log = LoggerFactory.getLogger(InitController.class);

    private final InitService initService;

    public InitController(ServiceFactory serviceFactory) {
        this.initService = serviceFactory.initService();
    }

    @Get("/structures/:structureId/initialization/teachers")
    @ApiDoc("Retrieve teachers initialization status")
    @SecuredAction(value="", type = ActionType.RESOURCE)
    @ResourceFilter(AdministratorRight.class)
    public void getTeachersInitializationStatus(HttpServerRequest request) {
        String structureId = request.getParam(Field.STRUCTUREID);
        this.initService.getTeachersStatus(structureId)
                .onFailure(fail -> {
                    log.error(String.format("[Viescolaire@%s::getTeachersInitializationStatus] Failed to retrieve " +
                            "teachers initialization status", this.getClass().getSimpleName()), fail.getMessage());
                    renderError(request);
                })
                .onSuccess(teachers -> renderJson(request, teachers.toJson()));
    }


    @Get("/structures/:structureId/initialization")
    @ApiDoc("Retrieve initialization status")
    @SecuredAction(value="", type = ActionType.RESOURCE)
    @ResourceFilter(AdministratorRight.class)
    public void getInitializationStatus(HttpServerRequest request) {
        String structureId = request.getParam(Field.STRUCTUREID);
        this.initService.getInitializationStatus(structureId)
                .onFailure(fail -> {
                    log.error(String.format("[Viescolaire@%s::getInitializationStatus] Failed to retrieve " +
                            "initialization status", this.getClass().getSimpleName()), fail.getMessage());
                    renderError(request);
                })
                .onSuccess(status -> renderJson(request, new JsonObject().put(Field.INITIALIZED, status)));
    }
}
