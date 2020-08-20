package fr.openent.viescolaire.controller;


import fr.openent.viescolaire.security.WorkflowActionUtils;
import fr.openent.viescolaire.service.ServicesService;
import fr.openent.viescolaire.service.impl.DefaultServicesService;
import fr.openent.viescolaire.utils.ServicesHelper;
import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.request.RequestUtils;
import io.vertx.core.http.HttpServerRequest;
import org.entcore.common.controller.ControllerHelper;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;

public class ServicesController extends ControllerHelper {

    /**
     * Déclaration des services
     */
    private final ServicesService servicesConfigurationService;
    public ServicesController() {
        this.servicesConfigurationService = new DefaultServicesService();
    }


    @Get("/services")
    @ApiDoc("Récupère les services")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getDefaultServices(final HttpServerRequest request) {
        if (!request.params().contains("idEtablissement")) {
            log.error("Error : idEtablissement should be provided.");
            badRequest(request, "idEtablissement is null");
        } else {
            boolean evaluable = false,
                    notEvaluable = false,
                    classes = false,
                    groups = false,
                    manualGroups = false,
                    hasFilter = false;

            if(request.params().contains("classes")
                    && request.params().contains("groups")
                    && request.params().contains("manualGroups")) {
                classes = Boolean.parseBoolean(request.params().get("classes"));
                groups = Boolean.parseBoolean(request.params().get("groups"));
                manualGroups = Boolean.parseBoolean(request.params().get("manualGroups"));
                hasFilter = classes || groups || manualGroups;
            }

            if(request.params().contains("evaluable") && request.params().contains("notEvaluable")){
                evaluable = Boolean.parseBoolean(request.params().get("evaluable"));
                notEvaluable = Boolean.parseBoolean(request.params().get("notEvaluable"));
                hasFilter = hasFilter || evaluable || notEvaluable;
            }

            String structureId = request.getParam("idEtablissement");
            if(hasFilter) {
                servicesConfigurationService.getAllServices(structureId, evaluable, notEvaluable, classes, groups,
                        manualGroups, true, ServicesHelper.getParams(request), arrayResponseHandler(request));
            } else {
                servicesConfigurationService.getAllServicesNoFilter(structureId, ServicesHelper.getParams(request),
                        arrayResponseHandler(request));
            }
        }
    }


    @Post("/service")
    @ApiDoc("Crée un nouveau service")
    @SecuredAction(value = WorkflowActionUtils.PARAM_SERVICES_RIGHT, type = ActionType.WORKFLOW)
    public void createService(final HttpServerRequest request) {
        RequestUtils.bodyToJson(request, pathPrefix + "eval_service", oService -> servicesConfigurationService.createService(oService, defaultResponseHandler(request)));
    }

    @Put("/service")
    @ApiDoc("Met à jour un service")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void updateService(final HttpServerRequest request) {
        RequestUtils.bodyToJson(request, pathPrefix + "eval_service", oService ->
                servicesConfigurationService.createService(oService, defaultResponseHandler(request)));
    }

    @Put("/services")
    @ApiDoc("Met un jours plusieurs services")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void updateServices(final HttpServerRequest request){
        RequestUtils.bodyToJson(request,oServices -> servicesConfigurationService.updateServices(oServices,defaultResponseHandler(request)));
    }

    @Delete("/service")
    @ApiDoc("Supprime un service")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void deleteService(final HttpServerRequest request) {
        if (!request.params().contains("id_groups") || !request.params().contains("id_enseignant")
                || !request.params().contains("id_matiere")) {
            log.error("Error : id_groups, id_enseignant and id_matiere should be given");
            badRequest(request, "id_groups or id_enseignant or id_matiere is null");
            return;
        } else {
            servicesConfigurationService.deleteService(ServicesHelper.getParams(request), defaultResponseHandler(request));
        }
    }
}
