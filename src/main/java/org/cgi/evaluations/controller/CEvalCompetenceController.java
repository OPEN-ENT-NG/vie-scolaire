package org.cgi.evaluations.controller;

import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.request.RequestUtils;
import org.cgi.evaluations.service.IEvalCompetenceNoteService;
import org.cgi.evaluations.service.IEvalCompetencesService;
import org.cgi.evaluations.service.impl.CEvalCompetenceNoteServiceImpl;
import org.cgi.evaluations.service.impl.CEvalCompetencesServiceImpl;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.w3c.dom.events.Event;

import static org.entcore.common.http.response.DefaultResponseHandler.*;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public class CEvalCompetenceController extends ControllerHelper{

    /**
     * Déclaration des services
     */
    private final IEvalCompetencesService competencesService;
    private final IEvalCompetenceNoteService competencesNotesService;

    /**
     * Création des constantes liées au framework SQL
     */
    private final String COMPETENCES_TABLE = "competences";
    private final String COMPETENCES_NOTES_TABLE = "competences_notes";
    private static final String SCHEMA_CREATE_COMPETENCE_NOTE = "eval_createCompetenceNote";
    private static final String SCHEMA_UPDATE_COMPETENCE_NOTE = "eval_updateCompetenceNote";

    public CEvalCompetenceController () {
        competencesService = new CEvalCompetencesServiceImpl(COMPETENCES_TABLE);
        competencesNotesService = new CEvalCompetenceNoteServiceImpl(COMPETENCES_NOTES_TABLE);
    }

    /**
     * Regarde si la compétence a des enfants
     * @param competence
     * @param values
     * @return True si la compétence a des enfants, sinon False
     */
    public Boolean isParent(JsonObject competence, JsonArray values){
        Integer id = competence.getInteger("id");
        JsonObject o = new JsonObject();
        for(int i = 0 ; i < values.size(); i++){
            o = values.get(i);
            if(o.getInteger("idparent") == id){
                return true;
            }
        }
        return false;
    }

    /**
     * Cherche les enfants de la compétences
     * @param competence
     * @param values
     * @return Liste des enfants de la compétence
     */
    public JsonArray findChildren(JsonObject competence, JsonArray values){
        JsonArray children = new JsonArray();
        Integer id = competence.getInteger("id");
        JsonObject o = new JsonObject();
        for(int i = 0; i < values.size(); i++){
            o = values.get(i);
            if(o.getInteger("idparent") == id){
                children.addObject(o);
            }
        }
        return children;
    }

    /**
     * Ordonne les compétences pour retourner un arbre
     * @param values
     * @return Liste des compétences ordonnées
     */
    public JsonArray orderCompetences(JsonArray values){
        JsonArray resultat = new JsonArray();
        JsonObject o = new JsonObject();
        for(int i = 0; i < values.size(); i++){
            o = values.get(i);
            o.putBoolean("selected", false);
            if(isParent(o, values)){
                o.putArray("children", findChildren(o, values));
            }
            if(o.getInteger("idparent") == 0){
                resultat.addObject(o);
            }
        }
        return resultat;
    }

    /**
     * Recupère toute la liste des compétences
     * @param request
     */
    @Get("/competences")
    @ApiDoc("Recupère toute la liste des compétences")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getCompetences(final HttpServerRequest request){
        competencesService.getCompetences(new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> event) {
                if(event.isRight()){
                    request.response()
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .end(orderCompetences(event.right().getValue()).toString());
                }else{
                    leftToResponse(request, event.left());
                }
            }
        });
    }

    /**
     * Recupère la liste des compétences pour un devoir donné
     * @param request
     */
    @Get("/competences/devoir/:id")
    @ApiDoc("Recupère la liste des compétences pour un devoir donné")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getCompetencesDevoir(final HttpServerRequest request){
        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
        competencesService.getDevoirCompetences(Integer.parseInt(request.params().get("id")), new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> event) {
                if(event.isRight()){
                    request.response()
                            .putHeader("content-type", "application/json; charset=utf-8")
                            .end(event.right().getValue().toString());
                }else{
                    leftToResponse(request, event.left());
                }
            }
        });
    }

    /**
     * Recupère les dernière compétences utilisée lors de la création d'un devoir
     * @param request
     */
    @Get("/competences/last/devoir/")
    @ApiDoc("Recupère les dernière compétences utilisée lors de la création d'un devoir")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getLastCompetencesDevoir(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if(user != null){
                    competencesService.getLastCompetencesDevoir(user.getUserId(), new Handler<Either<String, JsonArray>>() {
                        @Override
                        public void handle(Either<String, JsonArray> event) {
                            if(event.isRight()){
                                request.response()
                                        .putHeader("content-type", "application/json; charset=utf-8")
                                        .end(event.right().getValue().toString());
                            }else{
                                leftToResponse(request, event.left());
                            }
                        }
                    });
                }else{
                    unauthorized(request);
                }
            }
        });
    }

    /**
     * Récupère la liste des sous compétences
     * @param request
     */
    @Get("/competence/:id/competences")
    @ApiDoc("Récupère la liste des sous compétences")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getSousCompetences(final HttpServerRequest request){
        competencesService.getSousCompetences(Integer.parseInt(request.params().get("id")), arrayResponseHandler(request));
    }

    /**
     * Récupère la liste des compétences pour un enseignement donné
     * @param request
     */
    @Get("/enseignement/:id/competences")
    @ApiDoc("Récupère la liste des compétences pour un enseignement donné")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getCompetencesEnseignement(final HttpServerRequest request){
        competencesService.getCompetencesEnseignement(Integer.parseInt(request.params().get("id")), arrayResponseHandler(request));
    }

    /**
     * Récupère la liste des compétences notes pour un devoir et un élève donné
     * @param request
     */
    @Get("/competencenote/:iddevoir/:ideleve")
    @ApiDoc("Récupère la liste des compétences notes pour un devoir et un élève donné")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getCompetencesNotes(final HttpServerRequest request){
       competencesNotesService.getCompetencesNotes(Integer.parseInt(request.params().get("iddevoir")), request.params().get("ideleve"), arrayResponseHandler(request));
    }

    // TODO MODIFIER LA ROUTE EN /COMPETENCE/NOTE
    /**
     * Créé une note correspondante à une compétence pour un utilisateur donné
     * @param request
     */
    @Post("/competencenote/create")
    @ApiDoc("Créé une note correspondante à une compétence pour un utilisateur donné")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void create(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null){
                    RequestUtils.bodyToJson(request, pathPrefix + SCHEMA_CREATE_COMPETENCE_NOTE, new Handler<JsonObject>() {
                        @Override
                        public void handle(JsonObject resource) {
                            competencesNotesService.create(resource, user, notEmptyResponseHandler(request));
                        }
                    });
                }else {
                    log.debug("User not found in session.");
                    Renders.unauthorized(request);
                }
            }
        });
    }

    // TODO MODIFIER LA ROUTE EN /COMPETENCE/NOTE
    /**
     * Met à jour une note relative à une compétence
     * @param request
     */
    @Put("/competencenote/update")
    @ApiDoc("Met à jour une note relative à une compétence")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void update(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null){
                    RequestUtils.bodyToJson(request, pathPrefix + SCHEMA_UPDATE_COMPETENCE_NOTE, new Handler<JsonObject>() {
                        @Override
                        public void handle(JsonObject resource) {
                            Integer id = resource.getInteger("id");
                            competencesNotesService.update(String.valueOf(id), resource, notEmptyResponseHandler(request));
                        }
                    });
                }else {
                    log.debug("User not found in session.");
                    Renders.unauthorized(request);
                }
            }
        });
    }

    // TODO MODIFIER LA ROUTE EN /COMPETENCE/NOTE
    // TODO MODIFIER LA ROUTE POUR PASSER L'ID DE LA NOTE EN PARAMETRE => ?id=<id>
    /**
     * Supprime une note relative à une compétence
     * @param request
     */
    @Delete("/competencenote/delete/:id")
    @ApiDoc("Supprime une note relative à une compétence")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void delete (final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null){
                    String id = request.params().get("id");
                    log.debug("Delete de la CompetenceNote id="+id);
                    competencesNotesService.delete(id, defaultResponseHandler(request));
                }else {
                    log.debug("User not found in session.");
                    Renders.unauthorized(request);
                }
            }
        });
    }
}
