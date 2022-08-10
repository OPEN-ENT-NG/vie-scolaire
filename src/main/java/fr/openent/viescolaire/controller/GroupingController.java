package fr.openent.viescolaire.controller;

import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.service.impl.DefaultGroupingService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Post;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.controller.ControllerHelper;

public class GroupingController extends ControllerHelper {
    private final DefaultGroupingService groupingService = new DefaultGroupingService();

    @Post("/grouping/structure/:id")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    @ApiDoc("Create a grouping")
    public void createGrouping(HttpServerRequest request) {
        String structureId = request.getParam(Field.ID);
        String groupingName = request.getParam(Field.NAME);
        groupingService.createGrouping(groupingName, structureId)
                .onSuccess(res -> renderJson(request, res))
                .onFailure(err -> renderError(request, new JsonObject().put(Field.ERROR, err.getMessage())));
    }

    @Put("/grouping/:id")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    @ApiDoc("Update a grouping")
    public void updateGrouping(HttpServerRequest request) {
        String groupingId = request.getParam(Field.ID);
        String groupingName = request.getParam(Field.NAME);
        groupingService.updateGrouping(groupingId, groupingName)
                .onSuccess(res -> renderJson(request, res))
                .onFailure(err -> renderError(request, new JsonObject().put(Field.ERROR, err.getMessage())));
    }

    @Put("/grouping/:id/add")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    @ApiDoc("Add classes or groups to a grouping")
    public void addGrouping(HttpServerRequest request) {
        String groupingId = request.getParam(Field.ID);
        String groupId = request.getParam(Field.GROUP_ID);
        String classId = request.getParam(Field.CLASS_ID);
        groupingService.addToGrouping(groupingId, groupId, classId)
                .onSuccess(res -> renderJson(request, res))
                .onFailure(err -> renderError(request, new JsonObject().put(Field.ERROR, err.getMessage())));
    }

}