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
import fr.openent.evaluations.bean.NoteDevoir;
import fr.openent.evaluations.security.AccessEvaluationFilter;
import fr.openent.evaluations.security.AccessNoteFilter;
import fr.openent.evaluations.security.AccessReleveFilter;
import fr.openent.evaluations.security.CreateEvaluationWorkflow;
import fr.openent.evaluations.service.NoteService;
import fr.openent.evaluations.service.UtilsService;
import fr.openent.evaluations.service.impl.DefaultUtilsService;
import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.request.RequestUtils;
import fr.openent.evaluations.service.impl.DefaultNoteService;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.impl.Json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.entcore.common.http.response.DefaultResponseHandler.*;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public class NoteController extends ControllerHelper{

    /**
     * Création des constantes liés au framework SQL
     */



    /**
     * Déclaration des services
     */
    private final NoteService notesService;
    private final UtilsService utilsService;

    public NoteController() {
        pathPrefix = Viescolaire.EVAL_PATHPREFIX;
        notesService = new DefaultNoteService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_NOTES_TABLE);
        utilsService = new DefaultUtilsService();
    }

    /**
     * Recupère les notes d'un devoir donné
     * @param request
     */
    @Get("/devoir/:idDevoir/notes")
    @ApiDoc("Recupère les notes pour un devoir donné")
    @SecuredAction(value = "", type= ActionType.RESOURCE)
    @ResourceFilter(AccessEvaluationFilter.class)
    public void view(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if(user != null){
                    Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

                    Long idDevoir;
                    try {
                        idDevoir = Long.parseLong(request.params().get("idDevoir"));
                    } catch(NumberFormatException e) {
                        log.error("Error : idDevoir must be a long object", e);
                        badRequest(request, e.getMessage());
                        return;
                    }

                    notesService.listNotesParDevoir(idDevoir, handler);
                }else{
                    unauthorized(request);
                }
            }
        });
    }

//    /**
//     * Recupère la note d'un élève pour un devoir donné
//     * @param request
//     */
//    @Get("/devoir/:idDevoir/note")
//    @ApiDoc("Récupère la note d'un élève pour un devoir donné")
//    @SecuredAction(value = "", type= ActionType.RESOURCE)
//    @ResourceFilter(AccessEvaluationFilter.class)
//    public void noteDevoir (final HttpServerRequest request){
//        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
//            @Override
//            public void handle(UserInfos user) {
//                if(user != null){
//                    Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
//                    MultiMap params = request.params();
//                    Long idDevoir;
//                    try {
//                        idDevoir = Long.parseLong(request.params().get("idDevoir"));
//                    } catch(NumberFormatException e) {
//                        log.error("Error : idDevoir must be a long object", e);
//                        badRequest(request, e.getMessage());
//                        return;
//                    }
//                    String idEleve = params.get("idEleve");
//                    notesService.getNoteParDevoirEtParEleve(idDevoir, idEleve, handler);
//                }else{
//                    unauthorized(request);
//                }
//            }
//        });
//    }

    /**
     * Créer une note avec les données passées en POST
     * @param request
     */
    @Post("/note")
    @ApiDoc("Créer une note")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
	@ResourceFilter(CreateEvaluationWorkflow.class)
    public void create(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null){
                    RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_NOTES_CREATE, new Handler<JsonObject>() {
                        @Override
                        public void handle(JsonObject resource) {
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
    @SecuredAction(value = "", type= ActionType.RESOURCE)
    @ResourceFilter(AccessNoteFilter.class)
    public void update(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null){
                    RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_NOTES_UPDATE, new Handler<JsonObject>() {
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
    @SecuredAction(value = "", type= ActionType.RESOURCE)
    @ResourceFilter(AccessNoteFilter.class)
    public void deleteNoteDevoir(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null){

                    Long idNote;
                    try {
                        idNote = Long.parseLong(request.params().get("idNote"));
                    } catch(NumberFormatException e) {
                        log.error("Error : idNote must be a long object", e);
                        badRequest(request, e.getMessage());
                        return;
                    }

                    notesService.deleteNote(idNote, user, defaultResponseHandler(request));
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

    /**
     * Récupère les notes pour le relevé de notes
     * @param request
     */
//    /:idEleve/:idEtablissement/:idClasse/:idMatiere/:idPeriode
    @Get("/releve")
    @ApiDoc("Récupère les notes pour le relevé de notes")
    @SecuredAction(value = "", type= ActionType.RESOURCE)
    @ResourceFilter(AccessReleveFilter.class)
    public void getNoteElevePeriode(final HttpServerRequest request) {

        String idEleve = request.params().get("idEleve");
        String idEtablissement = request.params().get("idEtablissement");
        String idClasse = request.params().get("idClasse");
        String idMatiere = request.params().get("idMatiere");
        String idPeriodeString = request.params().get("idPeriode");
        Long idPeriode = null;

        if (idPeriodeString != null) {
            try {
                idPeriode = Long.parseLong(idPeriodeString);
            } catch (NumberFormatException e) {
                log.error("Error : idPeriode must be a long object", e);
                badRequest(request, e.getMessage());
                return;
            }
        }

        Handler<Either<String, JsonArray>> handler = new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> event) {
                if(event.isRight()) {
                    JsonObject result = new JsonObject();
                    JsonArray listNotes = event.right().getValue();
                    JsonArray listMoyDevoirs = new JsonArray();
                    JsonArray listMoyEleves = new JsonArray();
                    HashMap<Long, ArrayList<NoteDevoir>> notesByDevoir = new HashMap<>();
                    HashMap<String, ArrayList<NoteDevoir>> notesByEleve = new HashMap<>();

                    for (int i = 0; i < listNotes.size(); i++) {

                        JsonObject note = listNotes.get(i);

                        if(note.getString("valeur") == null || !note.getBoolean("is_evaluated")) {
                            continue; //Si la note fait partie d'un devoir qui n'est pas évalué, elle n'est pas prise en compte dans le calcul de la moyenne
                        }

                        NoteDevoir noteDevoir = new NoteDevoir(
                                Double.valueOf(note.getString("valeur")),
                                note.getBoolean("ramener_sur"),
                                Double.valueOf(note.getString("coefficient")));

                        Long idDevoir = note.getLong("id_devoir");
                        utilsService.addToMap(idDevoir, notesByDevoir, noteDevoir);

                        String idEleve = note.getString("id_eleve");
                        utilsService.addToMap(idEleve, notesByEleve, noteDevoir);
                    }

                    for(Map.Entry<Long, ArrayList<NoteDevoir>> entry : notesByDevoir.entrySet()) {
                        JsonObject moyenne = utilsService.calculMoyenne(entry.getValue(), true, 20);
                        moyenne.putValue("id", entry.getKey());
                        listMoyDevoirs.add(moyenne);
                    }
                    result.putArray("devoirs", listMoyDevoirs);

                    for(Map.Entry<String, ArrayList<NoteDevoir>> entry : notesByEleve.entrySet()) {
                        JsonObject moyenne = utilsService.calculMoyenne(entry.getValue(), false, 20);
                        moyenne.putValue("id", entry.getKey());
                        listMoyEleves.add(moyenne);
                    }
                    result.putArray("eleves", listMoyEleves);

                    result.putArray("notes", listNotes);

                    Renders.renderJson(request, result);
                } else {
                    JsonObject error = (new JsonObject()).putString("error", (String)event.left().getValue());
                    Renders.renderJson(request, error, 400);
                }
            }
        };

        if(idEleve != null){

            notesService.getNoteElevePeriode(idEleve,
                    idEtablissement,
                    idClasse,
                    idMatiere,
                    idPeriode,
                    handler);
        }
        else if(idPeriode != null){

            notesService.getNotesReleve(idEtablissement,
                    idClasse,
                    idMatiere,
                    idPeriode,
                    handler);
        }
        else{
            notesService.getNotesReleve(idEtablissement,
                    idClasse,
                    idMatiere,
                    null,
                    handler);
        }
    }
}
