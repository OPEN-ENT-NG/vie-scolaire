/*
 * Copyright (c) Région Hauts-de-France, Département 77, CGI, 2016.
 *
 * This file is part of OPEN ENT NG. OPEN ENT NG is a versatile ENT Project based on the JVM and ENT Core Project.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 * For the sake of explanation, any module that communicate over native
 * Web protocols, such as HTTP, with OPEN ENT NG is outside the scope of this
 * license and could be license under its own terms. This is merely considered
 * normal use of OPEN ENT NG, and does not fall under the heading of "covered work".
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */

package fr.openent.evaluations.controller;

import fr.openent.Viescolaire;
import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.request.RequestUtils;
import fr.openent.evaluations.service.impl.DefaultCompetenceNoteService;
import fr.openent.evaluations.service.impl.DefaultCompetencesService;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.List;

import static org.entcore.common.http.response.DefaultResponseHandler.*;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public class CompetenceController extends ControllerHelper{

    /**
     * Déclaration des services
     */
    private final fr.openent.evaluations.service.CompetencesService competencesService;
    private final fr.openent.evaluations.service.CompetenceNoteService competencesNotesService;

    public CompetenceController() {
        pathPrefix = Viescolaire.EVAL_PATHPREFIX;
        competencesService = new DefaultCompetencesService(Viescolaire.EVALUATIONS_SCHEMA, Viescolaire.EVAL_COMPETENCES_TABLE);
        competencesNotesService = new DefaultCompetenceNoteService(Viescolaire.EVALUATIONS_SCHEMA, Viescolaire.EVAL_COMPETENCES_NOTES_TABLE);
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
    @Get("/competences/devoir/:idDevoir")
    @ApiDoc("Recupère la liste des compétences pour un devoir donné")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getCompetencesDevoir(final HttpServerRequest request){
        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
        competencesService.getDevoirCompetences(Integer.parseInt(request.params().get("idDevoir")), new Handler<Either<String, JsonArray>>() {
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
    @Get("/competences/note")
    @ApiDoc("Récupère la liste des compétences notes pour un devoir et un élève donné")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getCompetencesNotes(final HttpServerRequest request){
       competencesNotesService.getCompetencesNotes(Integer.parseInt(request.params().get("iddevoir")),
               request.params().get("ideleve"), arrayResponseHandler(request));
    }

    /**
     * Créé une note correspondante à une compétence pour un utilisateur donné
     * @param request
     */
    @Post("/competence/note")
    @ApiDoc("Créé une note correspondante à une compétence pour un utilisateur donné")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void create(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null){
                    RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_COMPETENCE_NOTE_CREATE, new Handler<JsonObject>() {
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

    /**
     * Met à jour une note relative à une compétence
     * @param request
     */
    @Put("/competence/note")
    @ApiDoc("Met à jour une note relative à une compétence")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void update(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null){
                    RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_COMPETENCE_NOTE_UPDATE, new Handler<JsonObject>() {
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

    /**
     * Supprime une note relative à une compétence
     * @param request
     */
    @Delete("/competence/note")
    @ApiDoc("Supprime une note relative à une compétence")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void delete (final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null){
                    String id = request.params().get("idNote");
                    competencesNotesService.delete(id, defaultResponseHandler(request));
                }else {
                    log.debug("User not found in session.");
                    Renders.unauthorized(request);
                }
            }
        });
    }

    @Get("/competence/notes")
    @ApiDoc("Retourne les compétences notes d'un devoir donné")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getCompetencesNotesDevoir (final HttpServerRequest request) {
        String devoirId = request.params().get("devoirId");
        competencesNotesService.getCompetencesNotesDevoir(Integer.parseInt(devoirId), arrayResponseHandler(request));
    }

    @Post("/competence/notes")
    @ApiDoc("Créer une liste de compétences notes pour un devoir donné")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void createCompetencesNotesDevoir (final HttpServerRequest request) {
        RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
            @Override
            public void handle(final JsonObject resource) {
                UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
                    @Override
                    public void handle(UserInfos user) {
                        competencesNotesService.createCompetencesNotesDevoir(resource.getArray("data"), user, arrayResponseHandler(request));
                    }
                });
            }
        });
    }

    @Put("/competence/notes")
    @ApiDoc("Met à jour une liste de compétences notes pour un devoir donné")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void updateCompetencesNotesDevoir (final HttpServerRequest request) {
        RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
            @Override
            public void handle(JsonObject resource) {
                competencesNotesService.updateCompetencesNotesDevoir(resource.getArray("data"), arrayResponseHandler(request));
            }
        });
    }

    @Delete("/competence/notes")
    @ApiDoc("Supprime une liste de compétences notes pour un devoir donné")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void deleteCompetencesNotesDevoir (final HttpServerRequest request) {
        List<String> ids = request.params().getAll("id");
        if (ids.size() > 0) {
            competencesNotesService.dropCompetencesNotesDevoir(ids, arrayResponseHandler(request));
        }
    }
}
