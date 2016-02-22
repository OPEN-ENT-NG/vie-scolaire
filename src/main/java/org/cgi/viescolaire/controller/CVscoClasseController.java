package org.cgi.viescolaire.controller;

import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.BaseController;
import org.cgi.Viescolaire;
import org.cgi.viescolaire.service.IVscoClasseService;
import org.cgi.viescolaire.service.impl.CVscoClasseService;
import org.entcore.common.http.BaseServer;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;

/**
 * Created by ledunoiss on 19/02/2016.
 */
public class CVscoClasseController extends BaseController {

    IVscoClasseService mIVscoClasseService;

    public CVscoClasseController(){
        pathPrefix = Viescolaire.VSCO_PATHPREFIX;
        mIVscoClasseService = new CVscoClasseService();
    }

    @Get("/classes/etablissement")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    @ApiDoc("Recupere toutes les classes d'un établissement donné.")
    public void getClasseEtablissement(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                mIVscoClasseService.getClasseEtablissement(user.getStructures().get(0), handler);
            }
        });
    }
}
