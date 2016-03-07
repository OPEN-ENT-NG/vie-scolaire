package org.cgi.absences.controller;

import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.request.RequestUtils;
import org.cgi.Viescolaire;
import org.cgi.absences.service.IAbscEvenementService;
import org.cgi.absences.service.impl.CAbscEvenementService;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.entcore.common.http.response.DefaultResponseHandler.*;

/**
 * Created by ledunoiss on 25/02/2016.
 */
public class CAbscEvenementController  extends ControllerHelper {

    private final String ABSC_EVENEMENT_SCHEMA = "";

    private final IAbscEvenementService miAbscEvenementService;
    public CAbscEvenementController(){
        pathPrefix = Viescolaire.ABSC_PATHPREFIX;
        miAbscEvenementService = new CAbscEvenementService();
    }

    @Put("/evenement/:idEvenement/updatemotif")
    @ApiDoc("Met à jours le motif de l'évènement.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void updateMotifEvenement(final HttpServerRequest request){
        RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
            @Override
            public void handle(JsonObject event) {
                String pIIdEvenement = request.params().get("idEvenement");
                Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                miAbscEvenementService.updateEvenement(pIIdEvenement, event.getObject("evenement"), handler);
            }
        });
    }

    @Put("/evenement")
    @ApiDoc("Met à jours l'évènement.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void updateEvenement(final HttpServerRequest request){
        RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
            @Override
            public void handle(JsonObject poEvenement) {
                miAbscEvenementService.updateEvenement(poEvenement,
                        notEmptyResponseHandler(request));
            }
        });
    }

    @Post("/evenement")
    @ApiDoc("Création d'un évènement.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void createEvenement(final HttpServerRequest request){
        RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
            @Override
            public void handle(JsonObject poEvenement) {
                miAbscEvenementService.createEvenement(poEvenement,notEmptyResponseHandler(request));
            }
        });
    }

    @Delete("/evenement/:evenementId")
    @ApiDoc("Supprile l'évènement.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void deleteEvenement(final HttpServerRequest request){
        String oEvenementId = request.params().get("evenementId");
        miAbscEvenementService.deleteEvenement(Integer.parseInt(oEvenementId), defaultResponseHandler(request));
    }

    @Get("/observations/:dateDebut/:dateFin")
    @ApiDoc("Recupere toutes les observations dans une période donnée")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getObservations(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                String psDateDebut = request.params().get("dateDebut")+" 00:00:00";
                String psDateFin = request.params().get("dateFin")+" "+new SimpleDateFormat("HH:mm:ss").format(new Date());

                Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

                miAbscEvenementService.getObservations(user.getStructures().get(0), psDateDebut, psDateFin, handler);
            }
        });
    }

    @Get("/evenement/classe/:classeId/cours/:coursId")
    @ApiDoc("Recupere tous les évènements d'une classe sur un cours donné")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getEvenementClasseCours(final HttpServerRequest request){
        String psClasseId = request.params().get("classeId");
        String psCoursId = request.params().get("coursId");

        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

        miAbscEvenementService.getEvenementClasseCours(psClasseId, psCoursId, handler);
    }
}
