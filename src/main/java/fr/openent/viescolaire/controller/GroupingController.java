package fr.openent.viescolaire.controller;

import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.security.grouping.GroupAndClassManage;
import fr.openent.viescolaire.security.grouping.GroupingRights;
import fr.openent.viescolaire.security.grouping.StructureOwnerGroupingFilter;
import fr.openent.viescolaire.service.GroupingService;
import fr.openent.viescolaire.service.ServiceFactory;
import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.request.RequestUtils;
import io.vertx.core.http.HttpServerRequest;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.user.UserUtils;

public class GroupingController extends ControllerHelper {
    private final GroupingService groupingService;

    public GroupingController(ServiceFactory serviceFactory) {
        this.groupingService = serviceFactory.groupingService();
    }

    @Get("/grouping/structure/:id/list")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ApiDoc("List groupings")
    @ResourceFilter(StructureOwnerGroupingFilter.class)
    public void listGroupings(HttpServerRequest request) {
        String structureId = request.getParam(Field.ID);
        UserUtils.getUserInfos(eb, request, user -> {
            groupingService.listGrouping(structureId)
                    .onSuccess(res -> renderJson(request, res))
                    .onFailure(err -> renderError(request));
        });
    }

    @Post("/grouping/structure/:id")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ApiDoc("Create new grouping")
    @ResourceFilter(StructureOwnerGroupingFilter.class)
    public void createGrouping(HttpServerRequest request) {
        RequestUtils.bodyToJson(request, pathPrefix + "grouping_create", body -> {
            String structureId = request.getParam(Field.ID);
            String groupingName = body.getString(Field.NAME);
            groupingService.createGrouping(groupingName, structureId)
                    .onSuccess(res -> renderJson(request, res))
                    .onFailure(err -> renderError(request));
        });
    }

    @Put("/grouping/:id")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ApiDoc("Update a grouping")
    @ResourceFilter(GroupingRights.class)
    public void updateGrouping(HttpServerRequest request) {
        RequestUtils.bodyToJson(request, pathPrefix + "grouping_update", body -> {
            String groupingId = request.getParam(Field.ID);
            String groupingName = body.getString(Field.NAME);
            groupingService.updateGrouping(groupingId, groupingName)
                    .onSuccess(res -> renderJson(request, res))
                    .onFailure(err -> renderError(request));
        });
    }

    @Post("/grouping/:id/add")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ApiDoc("Add classes or groups to a grouping")
    @ResourceFilter(GroupAndClassManage.class)
    public void addGroupingAudience(HttpServerRequest request) {
        RequestUtils.bodyToJson(request, pathPrefix + "grouping_add_audience", body -> {
            String groupingId = request.getParam(Field.ID);
            String studentsDivisionId = body.getString(Field.STUDENT_DIVISION_ID);
            groupingService.addToGrouping(groupingId, studentsDivisionId)
                    .onSuccess(res -> renderJson(request, res))
                    .onFailure(err -> renderError(request));
        });
    }

    @Delete("/grouping/:id")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ApiDoc("Delete a grouping")
    @ResourceFilter(GroupingRights.class)
    public void deleteGrouping(HttpServerRequest request) {
        String groupingId = request.getParam(Field.ID);
        groupingService.deleteGrouping(groupingId)
                .onSuccess(res -> renderJson(request, res))
                .onFailure(err -> renderError(request));
    }

    @Delete("/grouping/:id/delete")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ApiDoc("Delete class or group to the grouping")
    @ResourceFilter(GroupAndClassManage.class)
    public void deleteGroupingAudience(HttpServerRequest request) {
        RequestUtils.bodyToJson(request, pathPrefix +  "grouping_delete_audience", body -> {
            String groupingId = request.getParam(Field.ID);
            String studentsDivisionId = body.getString(Field.STUDENT_DIVISION_ID);
            groupingService.deleteGroupingAudience(groupingId, studentsDivisionId)
                    .onSuccess(res -> renderJson(request, res))
                    .onFailure(err -> renderError(request));
        });
    }
}
