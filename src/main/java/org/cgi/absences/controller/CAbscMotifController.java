package org.cgi.absences.controller;

import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import org.cgi.Viescolaire;
import org.cgi.absences.service.IAbscMotifService;
import org.cgi.absences.service.impl.CAbscMotifService;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.impl.Json;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;

/**
 * Created by ledunoiss on 23/02/2016.
 */
public class CAbscMotifController extends ControllerHelper {

    private final IAbscMotifService miAbscMotifService;

    public CAbscMotifController(){
        pathPrefix = Viescolaire.ABSC_PATHPREFIX;
        miAbscMotifService = new CAbscMotifService();
    }

    @Get("/motifs")
    @ApiDoc("Recupere tous les profils d'absences en fonction de l'id de l'établissement")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getAbscMotifsEtablissement(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                miAbscMotifService.getAbscMotifsEtbablissement(user.getStructures().get(0), handler);
            }
        });
    }

    @Get("/justificatifs")
    @ApiDoc("Recupere tous les justificatifs d'appels en fonction de l'id de l'établissement")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getAbscJustificatifsEtablissement(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                miAbscMotifService.getAbscJustificatifsEtablissement(user.getStructures().get(0), handler);
            }
        });
    }
}
