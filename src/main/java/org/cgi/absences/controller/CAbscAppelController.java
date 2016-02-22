package org.cgi.absences.controller;

import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import org.cgi.Viescolaire;
import org.cgi.absences.service.IAbscAppelService;
import org.cgi.absences.service.impl.CAbscAppelService;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;

/**
 * Created by ledunoiss on 22/02/2016.
 */
public class CAbscAppelController extends ControllerHelper {
    private final IAbscAppelService miAbscAppelService;

    public CAbscAppelController(){
        pathPrefix = Viescolaire.ABSC_PATHPREFIX;
        miAbscAppelService = new CAbscAppelService();
    }

    @Get("/absences/appels/:dateDebut/:dateFin")
    @ApiDoc("Recupere tous les appels effectués dans une période donnée")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getAppelPeriode(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                String psDateDebut = request.params().get("dateDebut");
                String psDateFin = request.params().get("dateFin");

                Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                miAbscAppelService.getAppelPeriode(user.getStructures().get(0),psDateDebut, psDateFin, handler);
            }
        });
    }
}
