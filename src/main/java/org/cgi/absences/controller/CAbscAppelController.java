package org.cgi.absences.controller;

import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.request.RequestUtils;
import org.cgi.Viescolaire;
import org.cgi.absences.service.IAbscAppelService;
import org.cgi.absences.service.impl.CAbscAppelService;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.impl.Json;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;

/**
 * Created by ledunoiss on 22/02/2016.
 */
public class CAbscAppelController extends ControllerHelper {
    private final IAbscAppelService miAbscAppelService;

    public CAbscAppelController(){
        pathPrefix = Viescolaire.ABSC_PATHPREFIX;
        miAbscAppelService = new CAbscAppelService();
    }

    @Get("/appel/:coursId")
    @ApiDoc("Recupere l'appel associé à un cours")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getAppelCours(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                Integer poCoursId = Integer.valueOf(request.params().get("coursId"));

                miAbscAppelService.getAppelCours(poCoursId, defaultResponseHandler(request));
            }
        });
    }

    @Get("/appels/:dateDebut/:dateFin")
    @ApiDoc("Recupere tous les appels effectués dans une période donnée")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getAppelPeriode(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                String psDateDebut = request.params().get("dateDebut")+" 00:00:00";
                String psDateFin = request.params().get("dateFin")+" "+new SimpleDateFormat("HH:mm:ss").format(new Date());

                Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                miAbscAppelService.getAppelPeriode(user.getStructures().get(0),psDateDebut, psDateFin, handler);
            }
        });
    }

    @Get("/appels/noneffectues/:dateDebut/:dateFin")
    @ApiDoc("Recupere tous les appels non effectués dans une période donnée")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getAppelsNonEffectues(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                String psDateDebut = request.params().get("dateDebut")+" 00:00:00";
                String psDateFin = request.params().get("dateFin")+" "+new SimpleDateFormat("HH:mm:ss").format(new Date());

                Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

                miAbscAppelService.getAppelsNonEffectues(user.getStructures().get(0), psDateDebut, psDateFin, handler);
            }
        });
    }

    @Post("/appel/create")
    @ApiDoc("Créé un appel.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void createAppel(final HttpServerRequest request){
        RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
            @Override
            public void handle(JsonObject poAppel) {
                miAbscAppelService.createAppel(poAppel.getInteger("fk_personnel_id"),
                        poAppel.getInteger("fk_cours_id"),
                        poAppel.getInteger("fk_etat_appel_id"),
                        poAppel.getInteger("fk_justificatif_appel_id"),
                        defaultResponseHandler(request));
            }
        });
    }

    @Post("/appel/update")
    @ApiDoc("Met à jour un appel.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void updateAppel(final HttpServerRequest request){
        RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
            @Override
            public void handle(JsonObject poAppel) {
                miAbscAppelService.updateAppel(poAppel.getInteger("appel_id"),
                        poAppel.getInteger("fk_personnel_id"),
                        poAppel.getInteger("fk_cours_id"),
                        poAppel.getInteger("fk_etat_appel_id"),
                        poAppel.getInteger("fk_justificatif_appel_id"),
                        defaultResponseHandler(request));
            }
        });
    }
}
