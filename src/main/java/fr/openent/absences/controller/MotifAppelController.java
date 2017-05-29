package fr.openent.absences.controller;

import fr.openent.Viescolaire;
import fr.openent.absences.service.MotifAppelService;
import fr.openent.absences.service.impl.DefaultMotifAppelService;
import fr.openent.absences.utils.EventRegister;
import fr.openent.absences.utils.Events;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.request.RequestUtils;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import org.entcore.common.controller.ControllerHelper;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;

/**
 * Created by anabah on 06/06/2017.
 */
public class MotifAppelController extends ControllerHelper {

    private final MotifAppelService miAbscMotifService;
    private final EventRegister eventRegister;

    public MotifAppelController (){
        pathPrefix = Viescolaire.ABSC_PATHPREFIX;
        miAbscMotifService = new DefaultMotifAppelService();
        eventRegister = new EventRegister();
    }

    @Get("/motifsAppel")
    @ApiDoc("Récupère tous les motifs d'appels oubliés en fonction de l'id de l'établissement")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getAbscMotifsAppelEtablissement(final HttpServerRequest request){
        final String psIdEtablissement = request.params().get("idEtablissement");
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                miAbscMotifService.getAbscMotifsAppelEtbablissement(psIdEtablissement, handler);
            }
        });
    }
    @Get("/motifsAppel/categorie")
    @ApiDoc("Récupère toutes les catégories de motifs d'appels oublié en fonction de l'id de l'établissement")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getCategorieAbscMotifsEtablissement(final HttpServerRequest request){
        final String psIdEtablissement = request.params().get("idEtablissement");
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                miAbscMotifService.getCategorieAbscMotifsAppelEtbablissement(psIdEtablissement, handler);
            }
        });
    }

    @Post("/motifAppel")
    @ApiDoc("Créé un motif.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void createMotifAbs(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_MOTIF_CREATE, new Handler<JsonObject>() {
                    @Override
                    public void handle(final JsonObject poMotif) {
                        miAbscMotifService.createMotifAppel(poMotif, notEmptyResponseHandler(request));
                    }
                });
            }
        });
    }

    @Put("/motifAppel")
    @ApiDoc("Met à jour un motif.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void updateMotifAbs(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_MOTIF_UPDATE,new Handler<JsonObject>() {
                    @Override
                    public void handle(JsonObject poMotif) {
                        miAbscMotifService.updateMotifAppel(poMotif, eventRegister.getEventRegisterHandler(request, user, poMotif, Events.UPDATE_MOTIF.toString()));
                    }
                });
            }
        });
    }

    @Post("/categorieAppel")
    @ApiDoc("Créé un motif.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void createCategorieMotifAbs(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_CATEGORIE_ABS_CREATE, new Handler<JsonObject>() {
                    @Override
                    public void handle(final JsonObject poMotif) {
                        miAbscMotifService.createCategorieMotifAppel(poMotif, notEmptyResponseHandler(request));
                    }
                });
            }
        });
    }

    @Put("/categorieAppel")
    @ApiDoc("Met à jour un motif.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void updateCategorieMotifAbs(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_CATEGORIE_ABS_UPDATE,new Handler<JsonObject>() {
                    @Override
                    public void handle(JsonObject poMotif) {
                        miAbscMotifService.updateCategorieMotifAppel(poMotif, eventRegister.getEventRegisterHandler(request, user, poMotif, Events.UPDATE_MOTIF.toString()));
                    }
                });
            }
        });
    }
}

