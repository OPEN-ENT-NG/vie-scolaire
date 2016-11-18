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
import fr.openent.evaluations.security.AccessCompetenceNoteFilter;
import fr.openent.evaluations.security.AccessSuiviCompetenceFilter;
import fr.openent.evaluations.service.CompetenceNoteService;
import fr.openent.evaluations.service.impl.DefaultCompetenceNoteService;
import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.request.RequestUtils;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.List;

import static org.entcore.common.http.response.DefaultResponseHandler.*;

/**
 * Created by ledunoiss on 19/10/2016.
 */
public class CompetenceNoteController extends ControllerHelper {

    private final CompetenceNoteService competencesNotesService;

    public CompetenceNoteController() {
        pathPrefix = Viescolaire.EVAL_PATHPREFIX;
        competencesNotesService = new DefaultCompetenceNoteService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_COMPETENCES_NOTES_TABLE);
    }

    /**
     * Récupère la liste des compétences notes pour un devoir et un élève donné
     * @param request
     */
    @Get("/competences/note")
    @ApiDoc("Récupère la liste des compétences notes pour un devoir et un élève donné")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getCompetencesNotes(final HttpServerRequest request){

        Long idDevoir;
        try {
            idDevoir = Long.parseLong(request.params().get("iddevoir"));
        } catch(NumberFormatException e) {
            log.error("Error : idDevoir must be a long object", e);
            badRequest(request, e.getMessage());
            return;
        }

        competencesNotesService.getCompetencesNotes(idDevoir,
                request.params().get("ideleve"), arrayResponseHandler(request));
    }

    /**
     * Créé une note correspondante à une compétence pour un utilisateur donné
     * @param request
     */
    @Post("/competence/note")
    @ApiDoc("Créé une note correspondante à une compétence pour un utilisateur donné")
    @SecuredAction("viescolaire.evaluations.createEvaluation")
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
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AccessCompetenceNoteFilter.class)
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
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AccessCompetenceNoteFilter.class)
    public void delete (final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null){
                    String id = request.params().get("id");
                    competencesNotesService.delete(id, defaultResponseHandler(request));
                }else {
                    log.debug("User not found in session.");
                    Renders.unauthorized(request);
                }
            }
        });
    }

    @Get("/competence/notes/devoir/:devoirId")
    @ApiDoc("Retourne les compétences notes pour un devoir donné")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AccessSuiviCompetenceFilter.class)
    public void getCompetenceNotesDevoir (final HttpServerRequest request) {
        if (request.params().contains("devoirId")) {

            Long devoirId;
            try {
                devoirId = Long.parseLong(request.params().get("devoirId"));
            } catch(NumberFormatException e) {
                log.error("Error : devoirId must be a long object", e);
                badRequest(request, e.getMessage());
                return;
            }

            competencesNotesService.getCompetencesNotesDevoir(devoirId, arrayResponseHandler(request));
        } else {
            Renders.badRequest(request, "Invalid parameters");
        }
    }

    @Get("/competence/notes/eleve/:idEleve")
    @ApiDoc("Retourne les compétences notes pour un élève. Filtre possible sur la période avec l'ajout du paramètre idPeriode")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AccessSuiviCompetenceFilter.class)
    public void getCompetenceNoteEleve (final HttpServerRequest request) {
        if (request.params().contains("idEleve")) {
            String idEleve = request.params().get("idEleve");
            String idPeriode;
            if (request.params().contains("idPeriode")) {
                idPeriode = request.params().get("idPeriode");
            } else {
                idPeriode = null;
            }

            Long lIdPeriode;
            try {
                lIdPeriode = Long.parseLong(idPeriode);
            } catch(NumberFormatException e) {
                log.error("Error : idPeriode must be a long object", e);
                badRequest(request, e.getMessage());
                return;
            }

            competencesNotesService.getCompetencesNotesEleve(idEleve, lIdPeriode, arrayResponseHandler(request));
        } else {
            Renders.badRequest(request, "Invalid parameters");
        }
    }

    @Post("/competence/notes")
    @ApiDoc("Créer une liste de compétences notes pour un devoir donné")
    @SecuredAction("viescolaire.evaluations.createEvaluation")
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
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AccessCompetenceNoteFilter.class)
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
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AccessCompetenceNoteFilter.class)
    public void deleteCompetencesNotesDevoir (final HttpServerRequest request) {
        List<String> ids = request.params().getAll("id");

        if (ids == null || ids.size() == 0) {
            log.error("Error : one id must be present");
            badRequest(request);
            return;
        }

        JsonArray oIdsJsonArray = new JsonArray();
        try {
            for (int i = 0; i < ids.size(); i++) {
                oIdsJsonArray.addNumber(Long.parseLong(ids.get(i)));
            }
        } catch(NumberFormatException e) {
            log.error("Error : id must be a long object");
            badRequest(request);
        }

        competencesNotesService.dropCompetencesNotesDevoir(oIdsJsonArray, arrayResponseHandler(request));
    }

    @Get("/competence/notes/eleve")
    @ApiDoc("Récupère la liste des compétences notes pour une periode (optionnelle), un élève et un cycle")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AccessSuiviCompetenceFilter.class)
    public void getCompetencesNotesEleve(final HttpServerRequest request) {

    }
}
