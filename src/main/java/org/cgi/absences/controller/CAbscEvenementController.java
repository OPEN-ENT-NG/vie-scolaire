package org.cgi.absences.controller;

import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Delete;
import fr.wseduc.rs.Post;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.request.RequestUtils;
import org.cgi.Viescolaire;
import org.cgi.absences.service.IAbscEvenementService;
import org.cgi.absences.service.impl.CAbscEvenementService;
import org.entcore.common.controller.ControllerHelper;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;

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

    @Put("/evenement/:idEvenement")
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

    @Put("/evenement/update")
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

    @Post("/evenement/create")
    @ApiDoc("Création d'un évènement.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void createEvenement(final HttpServerRequest request){
        RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
            @Override
            public void handle(JsonObject poEvenement) {
                Handler<Either<String,JsonObject>> handler = new Handler<Either<String, JsonObject>>() {
                    @Override
                    public void handle(Either<String, JsonObject> event) {
                        if(event.isRight()){
                            request.response()
                                    .putHeader("content-type", "application/json; charset=utf-8")
                                    .end(event.right().getValue().toString());
                        }else{
                            leftToResponse(request, event.left());
                        }
                    }
                };
                miAbscEvenementService.createEvenement(poEvenement,
                        handler);
            }
        });
    }

    @Delete("/evenement/:evenementId/delete")
    @ApiDoc("Supprile l'évènement.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void deleteEvenement(final HttpServerRequest request){
        String oEvenementId = request.params().get("evenementId");
        miAbscEvenementService.deleteEvenement(Integer.parseInt(oEvenementId), defaultResponseHandler(request));
    }
}
