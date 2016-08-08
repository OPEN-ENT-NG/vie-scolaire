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

    /**
     * Recupère la note d'un élève pour un devoir donné
     * @param request
     */
    @Get("/devoir/:idDevoir/note")
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
    @Post("/note")
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
//                            crudService.create(resource, user, notEmptyResponseHandler(request));
                            notesService.createNote(resource, user, notEmptyResponseHandler(request));
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
     * Modifie une note avec les données passées en PUT
     * @param request
     */
    @Put("/note")
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
                            notesService.updateNote(resource, user, defaultResponseHandler(request));
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
     * Supprime la note passé en paramètre
     * @param request
     */
    @Delete("/note")
    @ApiDoc("Supprimer une note donnée")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void deleteNoteDevoir(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null){
                    notesService.deleteNote(Integer.parseInt(request.params().get("idNote")), user, defaultResponseHandler(request));
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
    @Get("/widget")
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
//    /:idEleve/:idEtablissement/:idClasse/:idMatiere/:idPeriode
    @Get("/releve")
    @ApiDoc("Récupère les notes pour le relevé de notes")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getNoteElevePeriode(final HttpServerRequest request){
        if(request.params().get("idEleve") != "undefined"
                && request.params().get("idEtablissement") != "undefined"
                && request.params().get("idClasse") != "undefined"
                && request.params().get("idMatiere") != "undefined"
                && request.params().get("idPeriode") != "undefined"){
            notesService.getNoteElevePeriode(request.params().get("idEleve"),
                    request.params().get("idEtablissement"),
                    request.params().get("idClasse"),
                    request.params().get("idMatiere"),
                    Integer.parseInt(request.params().get("idPeriode")),
                    arrayResponseHandler(request));
        }
    }
}
