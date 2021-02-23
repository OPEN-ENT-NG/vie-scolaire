package fr.openent.viescolaire.controller;

import fr.openent.viescolaire.service.MementoService;
import fr.openent.viescolaire.service.impl.DefaultMementoService;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.request.RequestUtils;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserUtils;

import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;

public class MementoController extends ControllerHelper {
    private final MementoService mementoService;
    private final EventBus eb;

    public MementoController(EventBus eb) {
        super();
        this.eb = eb;
        mementoService = new DefaultMementoService();
    }

    @Get("/memento/students/:id")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getStudentInfo(HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, user -> mementoService.getStudent(request.getParam("id"), user.getUserId(), defaultResponseHandler(request)));
    }

    @Post("/memento/students/:id/comments")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void postComment(HttpServerRequest request) {
        RequestUtils.bodyToJson(request, pathPrefix + "memento_comment", body -> UserUtils.getUserInfos(eb, request,
                user -> mementoService.postComment(request.getParam("id"), user.getUserId(), body.getString("comment"), defaultResponseHandler(request))));
    }

    @Put("/memento/students/:id/relatives/priority")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void updateRelativePriorities(HttpServerRequest request) {
        RequestUtils.bodyToJson(request, body -> {
            String studentId = request.getParam("id");
            JsonArray relativeIds = body.getJsonArray("relativeIds");
            mementoService.updateRelativePriorities(studentId, relativeIds, defaultResponseHandler(request));
        });
    }
}
