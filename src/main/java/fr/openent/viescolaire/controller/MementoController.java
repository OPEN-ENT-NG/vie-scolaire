package fr.openent.viescolaire.controller;

import fr.openent.viescolaire.service.MementoService;
import fr.openent.viescolaire.service.impl.DefaultMementoService;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.request.RequestUtils;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
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

//    @Get("/public/template/behaviours/sniplet-memento.html")
//    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
//    public void memento(HttpServerRequest request) {
//        JsonObject options = new JsonObject().put("name", "Simon");
//        renderView(request, options, "../public/template/behaviours/sniplet-memento.html", null);
//    }

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
}
