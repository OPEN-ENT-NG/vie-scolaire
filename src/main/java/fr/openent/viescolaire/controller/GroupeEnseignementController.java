package fr.openent.viescolaire.controller;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.GroupeService;
import fr.openent.viescolaire.service.impl.DefaultGroupeService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Renders;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.leftToResponse;

/**
 * Created by vogelmt on 13/02/2017.
 */
public class GroupeEnseignementController extends ControllerHelper {

    private final GroupeService groupeService;

    public GroupeEnseignementController() {
        pathPrefix = Viescolaire.VSCO_PATHPREFIX;
        groupeService = new DefaultGroupeService();
    }

    /**
     * Liste les groupes d'enseignement d'un utilisateur
     * @param request
     */
    @Get("/groupe/enseignement/user")
    @ApiDoc("Liste les groupes d'enseignement d'un utilisateur")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getGroupesEnseignementUser(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if (user != null) {
                    groupeService.listGroupesEnseignementsByUserId(user.getUserId(), new Handler<Either<String, JsonArray>>() {
                        @Override
                        public void handle(Either<String, JsonArray> event) {
                            if(event.isRight()){
                                JsonArray r = event.right().getValue();
                                JsonObject groupeEnseignement, g;
                                JsonArray groupesEnseignementJsonArray = new JsonArray();

                                for(int i = 0; i < r.size(); i++){
                                    JsonObject o = r.get(i);
                                    g = o.getObject("g");
                                    groupeEnseignement = g.getObject("data");
                                    groupesEnseignementJsonArray.addObject(groupeEnseignement);
                                }

                                Renders.renderJson(request, groupesEnseignementJsonArray);
                            }else{
                                leftToResponse(request, event.left());
                            }
                        }
                    });
                } else {
                    unauthorized(request);
                }
            }
        });
    }

    /**
     * Liste les groupes d'enseignement d'un utilisateur
     * @param request
     */
    @Get("/groupe/enseignement/users/:groupId")
    @ApiDoc("Liste les groupes dse utilisateurs d'un groupe d'enseignement")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getGroupesEnseignementUsers(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if (user != null) {
                    final String groupId = request.params().get("groupId");
                    if (groupId != null && !groupId.trim().isEmpty()) {
                        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                        final String profile = request.params().get("type");
                        groupeService.listUsersByGroupeEnseignementId(groupId, profile, handler);
                    }else{
                        log.error("Error getGroupesEnseignementUsers : groupId can't be null ");
                        badRequest(request);
                        return;
                    }
                } else {
                    unauthorized(request);
                }
            }
        });
    }

    /**
     * get name of groupe or classe
     * @param request
     */
    @Get("/class/group/:groupId")
    @ApiDoc("get the name of a groupe or classe ")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getNameOfGroupeClasse(final HttpServerRequest request) {

        String idGroupe = request.params().get("groupId");
        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

        groupeService.getNameOfGroupeClasse(idGroupe, handler);
    }
}
