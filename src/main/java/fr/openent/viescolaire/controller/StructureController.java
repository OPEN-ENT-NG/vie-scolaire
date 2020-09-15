package fr.openent.viescolaire.controller;

import fr.openent.viescolaire.service.impl.StructureService;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import io.vertx.core.http.HttpServerRequest;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.http.filter.SuperAdminFilter;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;

public class StructureController extends ControllerHelper {
    private StructureService structureService = new StructureService();

    @Get("/structures/store")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(SuperAdminFilter.class)
    public void storeStructures(HttpServerRequest request) {
        structureService.store(arrayResponseHandler(request));
    }
}
