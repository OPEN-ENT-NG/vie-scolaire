package fr.openent.absences.controller;

import fr.openent.Viescolaire;
import fr.openent.absences.service.UtilsService;
import fr.openent.absences.service.impl.DefaultUtilsService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

import static fr.wseduc.webutils.http.response.DefaultResponseHandler.arrayResponseHandler;

public class UtilsController extends ControllerHelper {

    UtilsService utilsService;

    public UtilsController () {
        pathPrefix = Viescolaire.ABSC_PATHPREFIX;
        utilsService = new DefaultUtilsService();
    }

    @Get("/user/structures/actives")
    @ApiDoc("Retourne la liste des identifiants des structures actives de l'utilisateur")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getIdsStructuresActive(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if (user != null) {
                    utilsService.getActivesIDsStructures(user, arrayResponseHandler(request));
                } else {
                    unauthorized(request);
                }
            }
        });
    }

}
