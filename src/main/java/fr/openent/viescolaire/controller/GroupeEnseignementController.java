package fr.openent.viescolaire.controller;

import com.fasterxml.jackson.databind.util.JSONPObject;
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

import java.util.ArrayList;

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
    @Get("/groupe/enseignement/user/:userid")
    @ApiDoc("Liste les groupes d'enseignement d'un utilisateur")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getGroupesEnseignementUser(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if (user != null) {
                    groupeService.listGroupeEnseignementUser(user.getUserId(), new Handler<Either<String, JsonArray>>() {
                        @Override
                        public void handle(Either<String, JsonArray> event) {
                            if(event.isRight()){
                                JsonArray r = event.right().getValue();
                                ArrayList<String> classesFieldOfStudy = new ArrayList<String>();
                                JsonObject groupeEnseignement = new JsonObject();
                                JsonObject g = new JsonObject();
                                final JsonArray groupesEnseignementJsonArray = new JsonArray();

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
}
