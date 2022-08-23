package fr.openent.viescolaire.controller;

import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.security.Grouping.GroupAndClassManage;
import fr.openent.viescolaire.security.Grouping.GroupingRights;
import fr.openent.viescolaire.security.Grouping.StructureOwnerFilter;
import fr.openent.viescolaire.service.GroupingService;
import fr.openent.viescolaire.service.ServiceFactory;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Delete;
import fr.wseduc.rs.Post;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;

public class GroupingController extends ControllerHelper {
    private final GroupingService groupingService;

    public GroupingController(ServiceFactory serviceFactory) {
        this.groupingService = serviceFactory.groupingService();
    }

    @Post("/grouping/structure/:id")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ApiDoc("Create a grouping")
    @ResourceFilter(StructureOwnerFilter.class)
    public void createGrouping(HttpServerRequest request) {
        String structureId = request.getParam(Field.ID);
        String groupingName = request.getParam(Field.NAME);
        groupingService.createGrouping(groupingName, structureId)
                .onSuccess(res -> renderJson(request, res))
                .onFailure(err -> renderError(request, new JsonObject().put(Field.ERROR, err.getMessage())));

    }

    @Put("/grouping/:id")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ApiDoc("Update a grouping")
    @ResourceFilter(GroupingRights.class)
    public void updateGrouping(HttpServerRequest request) {
        String groupingId = request.getParam(Field.ID);
        String groupingName = request.getParam(Field.NAME);
        groupingService.updateGrouping(groupingId, groupingName)
                .onSuccess(res -> renderJson(request, res))
                .onFailure(err -> renderError(request, new JsonObject().put(Field.ERROR, err.getMessage())));
    }

    @Put("/grouping/:id/add")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ApiDoc("Add classes or groups to a grouping")
    @ResourceFilter(GroupAndClassManage.class)
    public void addGrouping(HttpServerRequest request) {
        String groupingId = request.getParam(Field.ID);
        String studentsDivisionId = request.getParam(Field.STUDENT_DIVISION_ID);
        groupingService.addToGrouping(groupingId, studentsDivisionId)
                .onSuccess(res -> renderJson(request, res))
                .onFailure(err -> renderError(request, new JsonObject().put(Field.ERROR, err.getMessage())));
    }

    @Delete("/grouping/:id")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ApiDoc("Delete a grouping")
    @ResourceFilter(GroupingRights.class)
    public void deleteGrouping(HttpServerRequest request) {
        String groupingId = request.getParam(Field.ID);
        groupingService.deleteGrouping(groupingId)
                .onSuccess(res -> renderJson(request, res))
                .onFailure(err -> renderError(request, new JsonObject().put(Field.ERROR, err.getMessage())));
    }
}
