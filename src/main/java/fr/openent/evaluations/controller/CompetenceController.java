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
import fr.openent.evaluations.security.AccessEvaluationFilter;
import fr.openent.evaluations.service.CompetenceEvaluationService;
import fr.openent.evaluations.service.CompetencesService;
import fr.openent.evaluations.service.impl.DefaultCompetenceEvaluationService;
import fr.openent.evaluations.service.impl.DefaultCompetencesService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.leftToResponse;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public class CompetenceController extends ControllerHelper{

    /**
     * Déclaration des services
     */
    private final CompetencesService competencesService;
    private final CompetenceEvaluationService competencesNotesService;

    public CompetenceController() {
        pathPrefix = Viescolaire.EVAL_PATHPREFIX;
        competencesService = new DefaultCompetencesService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_COMPETENCES_TABLE);
        competencesNotesService = new DefaultCompetenceEvaluationService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_COMPETENCES_NOTES_TABLE);
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
            if(o.getInteger("id_parent") == id){
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
            if(o.getInteger("id_parent") == id){
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
            if(o.getInteger("id_parent") == 0){
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
    @ResourceFilter(AccessEvaluationFilter.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
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
}
