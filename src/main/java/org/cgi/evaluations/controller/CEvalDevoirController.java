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

package org.cgi.evaluations.controller;

import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.rs.Put;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.request.RequestUtils;
import org.cgi.Viescolaire;
import org.cgi.evaluations.service.impl.CEvalCompetencesServiceImpl;
import org.cgi.evaluations.service.impl.CEvalDevoirServiceImpl;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.leftToResponse;
import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;

/**
 * Created by ledunoiss on 04/08/2016.
 */
public class CEvalDevoirController extends ControllerHelper {

    /**
     * Déclaration des services
     */
    private final CEvalDevoirServiceImpl devoirsService;
    private final CEvalCompetencesServiceImpl competencesService;

    /**
     * Création des constantes liées au framework SQL
     */
    private static final String COMPETENCES_TABLE = "competences";
    public static final String DEVOIR_TABLE = "devoirs";
    private static final String SCHEMA_DEVOIRS_CREATE = "eval_createDevoir";
    private static final String SCHEMA_COMPETENCES_DEVOIR = "eval_createCompetence";
    private static final String SCHEMA_DEVOIRS_UPDATE = "eval_updateDevoir";

    public CEvalDevoirController () {
        pathPrefix = Viescolaire.EVAL_PATHPREFIX;
        devoirsService = new CEvalDevoirServiceImpl(DEVOIR_TABLE);
        competencesService = new CEvalCompetencesServiceImpl(COMPETENCES_TABLE);
    }

    @Get("/devoirs")
    @ApiDoc("Récupère les devoirs d'un utilisateurs")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void view(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if(user != null){
                    Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                    devoirsService.listDevoirs(user, handler);
                }else{
                    unauthorized(request);
                }
            }
        });
    }

    // TODO MODIFIER LA ROUTE POUR PASSER LES PARAMETRES EN PARAMETRE REQUETE : Ex : idetablissement=<id>
    /**
     * Liste des devoirs pour un établissement, une classe, une matière et une période donnée.
     * La liste est ordonnée selon la date du devoir (du plus ancien au plus récent).
     *
     * @param request
     */
    @Get("/devoir/:idEtablissement/:idClasse/:idMatiere/:idPeriode")
    @ApiDoc("Récupère la liste des devoirs pour un établissement, une classe," +
            "une matière et une période donnée.")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void listDevoirs (final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if(user != null){
                    MultiMap params = request.params();
                    String idEtablissement = params.get("idEtablissement");
                    String idClasse = params.get("idClasse");
                    String idMatiere = params.get("idMatiere");
                    Integer idPeriode = Integer.parseInt(params.get("idPeriode"));

                    Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                    devoirsService.listDevoirs(idEtablissement, idClasse, idMatiere, idPeriode, handler);
                }else{
                    unauthorized(request);
                }
            }
        });
    }

    /**
     * Créer un devoir avec les paramètres passés en post.
     * @param request
     */
    @Post("/devoir")
    @ApiDoc("Créer un devoir")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void create(final HttpServerRequest request){
        final Integer[] devoirId = {0};
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null){
                    RequestUtils.bodyToJson(request, pathPrefix + "/" + SCHEMA_DEVOIRS_CREATE, new Handler<JsonObject>() {
                        @Override
                        public void handle(JsonObject resource) {
                            resource.removeField("competences");
                            devoirsService.createDevoir(resource, user,
                                    new Handler<Either<String, JsonObject>>() {
                                        @Override
                                        public void handle(final Either<String, JsonObject> event) {
                                            if (event.isRight() && event.right().getValue().containsField("id")) {
                                                devoirId[0] = event.right().getValue().getInteger("id");
                                                RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
                                                    @Override
                                                    public void handle(JsonObject createdDevoir) {
                                                        createdDevoir.putNumber("id", devoirId[0]);
                                                        JsonArray competences = createdDevoir.getArray("competences");
                                                        if(competences.size() != 0) {
                                                            competencesService.setDevoirCompetences(createdDevoir.getInteger("id"), competences, new Handler<Either<String, JsonObject>>() {
                                                                public void handle(Either<String, JsonObject> event) {
                                                                    if (event.isRight()) {
                                                                        JsonObject o = new JsonObject();
                                                                        o.putNumber("id", devoirId[0]);
                                                                        request.response().putHeader("content-type", "application/json; charset=utf-8").end(o.toString());
                                                                    } else {
                                                                        leftToResponse(request, event.left());
                                                                    }
                                                                }
                                                            });
                                                        }else{
                                                            if(event.isRight()){
                                                                request.response().putHeader("content-type", "application/json; charset=utf-8").end(event.right().getValue().toString());
                                                            }
                                                        }
                                                    }
                                                });
                                            }else{
                                                leftToResponse(request,event.left());
                                            }
                                        }
                                    });
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
     * Liste des devoirs publiés pour un établissement et une période donnée.
     * La liste est ordonnée selon la date du devoir (du plus ancien au plus récent).
     *
     * @param request
     */
    @Get("/devoirseleves/:idEtablissement/:idPeriode/:idUser")
    @ApiDoc("Liste des devoirs publiés pour un établissement et une période donnée.")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void listDevoirsPeriode (final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if(user != null){
                    MultiMap params = request.params();
                    Integer idPeriode = Integer.parseInt(params.get("idPeriode"));
                    String idEtablissement = params.get("idEtablissement");
                    String idUser = params.get("idUser");
                    Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                    devoirsService.listDevoirs(idEtablissement, idPeriode, idUser, handler);
                }else{
                    unauthorized(request);
                }
            }
        });
    }

    /**
     * Met à jour un devoir
     * @param request
     */
    @Put("/devoir")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    @ApiDoc("Met à jour un devoir")
    public void updateDevoir (final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if (user != null) {
                    RequestUtils.bodyToJson(request, pathPrefix + "/" + SCHEMA_DEVOIRS_CREATE, new Handler<JsonObject>() {
                        @Override
                        public void handle(JsonObject devoir) {
                            devoirsService.updateDevoir(request.params().get("devoirid"), devoir, user, notEmptyResponseHandler(request));
                        }
                    });

                } else {
                    unauthorized(request);
                }
            }
        });
    }
}
