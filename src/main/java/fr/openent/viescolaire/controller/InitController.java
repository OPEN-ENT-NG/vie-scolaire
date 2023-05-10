package fr.openent.viescolaire.controller;

import fr.openent.viescolaire.core.constants.*;
import fr.openent.viescolaire.model.InitForm.*;
import fr.openent.viescolaire.security.*;
import fr.openent.viescolaire.service.*;
import fr.wseduc.rs.*;
import fr.wseduc.security.*;
import fr.wseduc.webutils.*;
import fr.wseduc.webutils.request.*;
import io.vertx.core.http.*;
import io.vertx.core.json.*;
import io.vertx.core.logging.*;
import org.entcore.common.controller.*;
import org.entcore.common.http.filter.*;
import org.entcore.common.user.*;

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
                .onSuccess(status -> renderJson(request, new JsonObject().put(Field.INITIALIZED, (status != null) && status)));
    }

    @Post("/structures/:structureId/initialize")
    @ApiDoc("Initialize structure")
    @SecuredAction(value="", type = ActionType.RESOURCE)
    @ResourceFilter(AdministratorRight.class)
    public void initialize(HttpServerRequest request) {

        RequestUtils.bodyToJson(request, pathPrefix + "init_structure", body -> {
            UserUtils.getUserInfos(eb, request, user -> {

                String structureId = request.getParam(Field.STRUCTUREID);
                JsonObject i18nParams = new JsonObject()
                        .put(Field.DOMAIN, getHost(request))
                        .put(Field.ACCEPT_LANGUAGE, I18n.acceptLanguage(request));

                this.initService.launchInitWorker(user, structureId, new InitFormModel(body), i18nParams)
                        .onFailure(fail -> {
                            log.error(String.format("[Viescolaire@%s::initialize] Failed to launch init worker",
                                    this.getClass().getSimpleName()), fail.getMessage());
                            renderError(request);
                        })
                        .onSuccess(success -> renderJson(request, new JsonObject().put(Field.SUCCESS, Field.OK)));
            });
        });
    }

    @Post("/structures/:structureId/initialize/reset")
    @ApiDoc("Reset structure initialization")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AdministratorRight.class)
    public void resetInitialisation(HttpServerRequest request) {
        String structureId = request.getParam(Field.STRUCTUREID);
        this.initService.resetInit(structureId)
                .onFailure(fail -> {
                    log.error(String.format("[Viescolaire@%s::resetInitialisation] Failed to reset init: %s",
                            this.getClass().getSimpleName(), fail.getMessage()), fail.getMessage());
                    renderError(request);
                })
                .onSuccess(success -> renderJson(request, new JsonObject().put(Field.SUCCESS, Field.OK)));

    }
}
