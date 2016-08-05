package org.cgi.evaluations.controller;

import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.request.RequestUtils;
import org.cgi.Viescolaire;
import org.cgi.evaluations.service.IEvalNoteService;
import org.cgi.evaluations.service.impl.CEvalNoteServiceImpl;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.entcore.common.http.response.DefaultResponseHandler.*;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public class CEvalNoteController extends ControllerHelper{

    /**
     * Création des constantes liés au framework SQL
     */
    private static final String SCHEMA_NOTES_CREATE = "eval_createNote";
    private static final String SCHEMA_NOTES_UPDATE = "eval_updateNote";
    private static final String NOTES_TABLE = "notes";

    /**
     * Déclaration des services
     */
    private final IEvalNoteService notesService;

    public CEvalNoteController() {
        pathPrefix = Viescolaire.EVAL_PATHPREFIX;
        notesService = new CEvalNoteServiceImpl(NOTES_TABLE);
    }

    /**
     * Recupère les notes d'un devoir donné
     * @param request
     */
    @Get("/devoir/:idDevoir/notes")
    @ApiDoc("Récupère les devoirs d'un utilisateurs")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void view(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if(user != null){
                    Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                    notesService.listNotesParDevoir(Integer.parseInt(request.params().get("idDevoir")), handler);
                }else{
                    unauthorized(request);
                }
            }
        });
    }

    // TODO MODIFIER LA ROUTE POUR PASSER L'ID DE L'ELEVE EN PARAMETRE
    /**
     * Recupère la note d'un élève pour un devoir donné
     * @param request
     */
    @Get("/devoir/:idDevoir/note/:idEleve")
    @ApiDoc("Récupère la note d'un élève pour un devoir donné")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void noteDevoir (final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if(user != null){
                    Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                    MultiMap params = request.params();
                    Integer idDevoir = Integer.parseInt(params.get("idDevoir"));
                    String idEleve = params.get("idEleve");
                    notesService.getNoteParDevoirEtParEleve(idDevoir, idEleve, handler);
                }else{
                    unauthorized(request);
                }
            }
        });
    }

    /**
     * Créer une note avec les données passées en POST
     * @param request
     */
    @Post("/devoir/:idDevoir/note")
    @ApiDoc("Créer une note")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void create(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null){
                    RequestUtils.bodyToJson(request, pathPrefix + SCHEMA_NOTES_CREATE, new Handler<JsonObject>() {
                        @Override
                        public void handle(JsonObject resource) {
                            crudService.create(resource, user, notEmptyResponseHandler(request));
                        }
                    });
                }else {
                    log.debug("User not found in session.");
                    Renders.unauthorized(request);
                }
            }
        });
    }

    // TODO MODIFIER LA ROUTE POUR UTILISER LE FRAMEWORK SQL
    /**
     * Modifie une note avec les données passées en PUT
     * @param request
     */
    @Put("/devoir/note")
    @ApiDoc("Modifie une note")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void update(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null){
                    RequestUtils.bodyToJson(request, pathPrefix + SCHEMA_NOTES_UPDATE, new Handler<JsonObject>() {
                        @Override
                        public void handle(JsonObject resource) {
                            notesService.updateNote(resource, defaultResponseHandler(request));
                        }
                    });
                }else {
                    log.debug("User not found in session.");
                    Renders.unauthorized(request);
                }
            }
        });
    }
    // TODO MODIFIER LA ROUTE POUR UTILISER LE FRAMEWORK SQL
    // TODO MODIFIER LA ROUTE POUR PASSER L'ID DE LA NOTE EN PARAMETRE => ?idnote=<id>
    /**
     * Supprime la note passé en paramètre
     * @param request
     */
    @Delete("/devoir/note/:idNote")
    @ApiDoc("Supprimer une note donnée")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void deleteNoteDevoir(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null){
                    notesService.deleteNote(Integer.parseInt(request.params().get("idNote")), defaultResponseHandler(request));
                }else{
                    unauthorized(request);
                }
            }
        });
    }

    /**
     * Récupère les notes pour le widget
     * @param request
     */
    @Get("/widget/:userId")
    @ApiDoc("Récupère les notes pour le widget")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getWidgetNotes(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if(user != null){
                    notesService.getWidgetNotes(request.params().get("userId"), arrayResponseHandler(request));
                }
            }
        });
    }

    // TODO MODIFIER LA ROUTE POUR PASSER L'ID DE LA NOTE EN PARAMETRE => ?idnote=<id>
    /**
     * Récupère les notes pour le relevé de notes
     * @param request
     */
    @Get("/releve/:idEleve/:idEtablissement/:idClasse/:idMatiere/:idPeriode")
    @ApiDoc("Récupère les notes pour le relevé de notes")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getNoteElevePeriode(final HttpServerRequest request){
        if(request.params().get("idEleve") != "undefined"
                && request.params().get("idEtablissement") != "undefined"
                && request.params().get("idClasse") != "undefined"
                && request.params().get("idMatiere") != "undefined"
                && request.params().get("idPeriode") != "undefined"){
            notesService.getNoteElevePeriode(request.params().get("idEleve"), request.params().get("idEtablissement"),
                    request.params().get("idClasse"), request.params().get("idMatiere"), Integer.parseInt(request.params().get("idPeriode")),
                    arrayResponseHandler(request));
        }
    }
}
