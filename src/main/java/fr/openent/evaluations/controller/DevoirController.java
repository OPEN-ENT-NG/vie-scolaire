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
import fr.openent.evaluations.security.AccessPeriodeFilter;
import fr.openent.evaluations.service.CompetencesService;
import fr.openent.evaluations.service.NoteService;
import fr.openent.evaluations.service.UtilsService;
import fr.openent.evaluations.service.impl.DefaultCompetencesService;
import fr.openent.evaluations.service.impl.DefaultDevoirService;
import fr.openent.evaluations.service.impl.DefaultNoteService;
import fr.openent.evaluations.service.impl.DefaultUtilsService;
import fr.openent.viescolaire.service.ClasseService;
import fr.openent.viescolaire.service.impl.DefaultClasseService;
import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.request.RequestUtils;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.entcore.common.http.response.DefaultResponseHandler.*;

/**
 * Created by ledunoiss on 04/08/2016.
 */
public class DevoirController extends ControllerHelper {

    /**
     * Déclaration des services
     */
    private final DefaultDevoirService devoirsService;
    private final UtilsService utilsService;
    private final ClasseService classesService;
    private final NoteService notesService;
    private final CompetencesService competencesService;

    public DevoirController() {
        pathPrefix = Viescolaire.EVAL_PATHPREFIX;
        devoirsService = new DefaultDevoirService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_DEVOIR_TABLE);
        classesService = new DefaultClasseService();
        utilsService = new DefaultUtilsService();
        notesService = new DefaultNoteService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_NOTES_TABLE);
        competencesService = new DefaultCompetencesService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_COMPETENCES_TABLE);
    }

    @Get("/devoirs")
    @ApiDoc("Récupère les devoirs d'un utilisateurs")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void view(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null){
                    if(user.getType().equals("Personnel") && user.getFunctions().containsKey("DIR")){
                        final Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                        devoirsService.listDevoirsEtab(user, handler);
                    }else{
                        final Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                        if (request.params().size() == 2) {
                            String idEtablissement = request.params().get("idEtablissement");
                            devoirsService.listDevoirs(user,idEtablissement, handler);
                        } else {
                            String idEtablissement = request.params().get("idEtablissement");
                            String idClasse = request.params().get("idClasse");
                            String idMatiere = request.params().get("idMatiere");

                            Long idPeriode;
                            try {
                                idPeriode = Long.parseLong(request.params().get("idPeriode"));
                            } catch(NumberFormatException e) {
                                log.error("Error : idPeriode must be a long object", e);
                                badRequest(request, e.getMessage());
                                return;
                            }

                            if (idEtablissement != "undefined" && idClasse != "undefined"
                                    && idMatiere != "undefined" && request.params().get("idPeriode") != "undefined") {
                                devoirsService.listDevoirs(idEtablissement, idClasse, idMatiere, idPeriode, handler);
                            } else {
                                Renders.badRequest(request, "Invalid parameters");
                            }
                        }
                    }

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
    @SecuredAction("viescolaire.evaluations.createEvaluation")
    public void create(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if (user != null) {
                    RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
                        @Override
                        public void handle(final JsonObject resource) {
                            resource.removeField("competences");
                            resource.removeField("competencesAdd");
                            resource.removeField("competencesRem");
                            resource.removeField("competenceEvaluee");

                            RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX +
                                    Viescolaire.SCHEMA_DEVOIRS_CREATE, new Handler<JsonObject>() {
                                @Override
                                public void handle(final JsonObject devoir) {

                                    devoirsService.createDevoir(devoir, user, new Handler<Either<String, JsonObject>>() {
                                        @Override
                                        public void handle(Either<String, JsonObject> event) {
                                            if (event.isRight()) {
                                                final JsonObject devoirWithId = event.right().getValue();
                                                // recuperation des professeurs que l'utilisateur connecté remplacent
                                                utilsService.getTitulaires(user.getUserId(), devoir.getString("id_etablissement"), new Handler<Either<String, JsonArray>>() {
                                                    @Override
                                                    public void handle(Either<String, JsonArray> event) {
                                                        if (event.isRight()) {
                                                            // si l'utilisateur connecté remplace bien un professeur
                                                            // on partage à ce professeur (le titulaire) le devoir
                                                            JsonArray values = event.right().getValue();

                                                            if(values.size() > 0) {

                                                                // TODO potentielement il peut y avoir plusieurs titulaires pour un remplaçant sur le même établissement
                                                                String userIdTitulaire = ((JsonObject)values.get(0)).getString("id_titulaire");
                                                                List<String> actions = new ArrayList<String>();
                                                                actions.add(Viescolaire.DEVOIR_ACTION_UPDATE);

                                                                // TODO ne partager le devoir seulement si le titulaire enseigne sur la classe du remplaçant
                                                                shareService.userShare(user.getUserId(), userIdTitulaire, devoirWithId.getLong("id").toString(), actions, new Handler<Either<String, JsonObject>>() {
                                                                    @Override
                                                                    public void handle(Either<String, JsonObject> event) {
                                                                        if (event.isRight()) {
                                                                            renderJson(request, devoirWithId);
                                                                        } else {
                                                                            leftToResponse(request, event.left());
                                                                        }

                                                                    }
                                                                });
                                                            } else {
                                                                // sinon on renvoie la réponse, pas besoin de partage
                                                                renderJson(request, devoirWithId);
                                                            }
                                                        }else {
                                                            leftToResponse(request, event.left());
                                                        }
                                                    }
                                                });


                                            } else {
                                                badRequest(request);
                                            }
                                        }
                                    });

                                }
                            });
                        }
                    });
                } else {
                    log.debug("User not found in session.");
                    Renders.unauthorized(request);
                }
            }
        });
    }

    /**
     * Liste des devoirs publiés par l'utilisateur pour un établissement et une période donnée.
     * La liste est ordonnée selon la date du devoir (du plus ancien au plus récent).
     *
     * @param request
     */
    @Get("/devoirs/periode/:idPeriode")
    @ApiDoc("Liste des devoirs publiés par l'utilisateur pour un établissement et une période donnée.")
    @SecuredAction(value = "", type= ActionType.RESOURCE)
    @ResourceFilter(AccessPeriodeFilter.class)
    public void listDevoirsPeriode (final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if(user != null){
                    MultiMap params = request.params();

                    Long idPeriode;
                    try {
                        idPeriode = Long.parseLong(request.params().get("idPeriode"));
                    } catch(NumberFormatException e) {
                        log.error("Error : idPeriode must be a long object", e);
                        badRequest(request, e.getMessage());
                        return;
                    }

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

    @Get("/devoirs/evaluations/information")
    @ApiDoc("Recupère la liste des compétences pour un devoir donné")
    @ResourceFilter(AccessEvaluationFilter.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    public void isEvaluatedDevoir(final HttpServerRequest request){
        Long idDevoir;
        try {
            idDevoir = Long.parseLong(request.params().get("idDevoir"));
        } catch(NumberFormatException e) {
            log.error("Error : idPeriode must be a long object", e);
            badRequest(request, e.getMessage());
            return;
        }

        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
        devoirsService.getevaluatedDevoir(idDevoir,handler);

    }

    @Get("/devoirs/evaluations/informations")
    @ApiDoc("Recupère pour une liste de devoirs ne nombre de competences evaluer et de notes saisie")
    @ResourceFilter(AccessEvaluationFilter.class)
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    public void areEvaluatedDevoirs(final HttpServerRequest request){
        List<String> idDevoirsList = request.params().getAll("idDevoir");

        if (idDevoirsList == null || idDevoirsList.size() == 0) {
            log.error("Error : one id must be present");
            badRequest(request);
            return;
        }

        Long[] idDevoirsArray = new Long[idDevoirsList.size()] ;
        try {
            for (int i = 0; i < idDevoirsList.size(); i++) {
                idDevoirsArray[i] = Long.parseLong(idDevoirsList.get(i));
            }
        } catch(NumberFormatException e) {
            log.error("Error : id must be a long object", e);
            badRequest(request);
        }

        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
        devoirsService.getevaluatedDevoirs(idDevoirsArray,handler);

    }

    /**
     * Met à jour un devoir
     * @param request
     */
    @Put("/devoir")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AccessEvaluationFilter.class)
    @ApiDoc("Met à jour un devoir")
    public void updateDevoir (final HttpServerRequest request) {
        RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX +
                Viescolaire.SCHEMA_DEVOIRS_UPDATE, new Handler<JsonObject>() {
            @Override
            public void handle(final JsonObject devoir) {
                devoirsService.updateDevoir(request.params().get("idDevoir"),
                        devoir, arrayResponseHandler(request));
            }
        });
    }

    @Post("/devoirs/done")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    @ApiDoc("Calcul le pourcentage réalisé pour chaque devoir")
    public void getPercentDone (final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null) {
                    RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
                        @Override
                        public void handle(JsonObject devoirs) {

                            final HashMap<Long, String> idDevoirToGroupe = new HashMap<>();
                            final HashMap<String, Integer> nbElevesByGroupe = new HashMap<>();
                            final HashMap<Long, Integer> nbNotesByDevoir = new HashMap<>();
                            JsonArray idDevoirs = devoirs.getArray("idDevoirs");

                            final JsonArray result = new JsonArray();

                            devoirsService.getNbNotesDevoirs(user, idDevoirs , new Handler<Either<String, JsonArray>>() {
                                @Override
                                public void handle(Either<String, JsonArray> event) {
                                    if (event.isRight()) {
                                        if(event.right().getValue() != null) {
                                            JsonArray resultNbNotesDevoirs = event.right().getValue();

                                            JsonArray idGroupes = new JsonArray();

                                            for (int i = 0; i < resultNbNotesDevoirs.size(); i++) {
                                                JsonObject o = resultNbNotesDevoirs.get(i);

                                                if (null != o && !idGroupes.contains(o.getString("id_groupe"))) {
                                                    idGroupes.add(o.getString("id_groupe"));
                                                }
                                                idDevoirToGroupe.put(o.getLong("id"), o.getString("id_groupe"));
                                                nbNotesByDevoir.put(o.getLong("id"), o.getInteger("nb_notes"));
                                            }

                                            classesService.getNbElevesGroupe(idGroupes, new Handler<Either<String, JsonArray>>() {
                                                @Override
                                                public void handle(Either<String, JsonArray> event) {
                                                    if (event.isRight()) {
                                                        JsonArray resultNbElevesGroupes = event.right().getValue();

                                                        for (int i = 0; i < resultNbElevesGroupes.size(); i++) {
                                                            JsonObject o = resultNbElevesGroupes.get(i);
                                                            nbElevesByGroupe.put(o.getString("id_groupe"), o.getInteger("nb"));
                                                        }
                                                        for (Map.Entry devoirToGroupe : idDevoirToGroupe.entrySet()) {
                                                            JsonObject o = new JsonObject();
                                                            o.putNumber("id", (Number)devoirToGroupe.getKey());
                                                            o.putNumber("percent", nbNotesByDevoir.get(devoirToGroupe.getKey()) * 100 / nbElevesByGroupe.get(devoirToGroupe.getValue()));
                                                            result.add(o);
                                                        }
                                                        Renders.renderJson(request, result);
                                                    } else {
                                                        leftToResponse(request, event.left());
                                                    }
                                                }
                                            });
                                        }
                                    } else {
                                        leftToResponse(request, event.left());
                                    }
                                }
                            });




                        }
                    });
                }
            }
        });
    }

    /**
     *  Supprimer un devoir
     */
    @Delete("/devoir")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AccessEvaluationFilter.class)
    @ApiDoc("Supprime un devoir")
    public void remove(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if (user != null) {
                    devoirsService.delete(request.params().get("idDevoir"),user, notEmptyResponseHandler(request));
                } else {
                    unauthorized(request);
                }
            }
        });
    }

    @Post("/devoir/:idDevoir/duplicate")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    @ApiDoc("Duplique un devoir pour une liste de classe donnée")
    public void duplicateDevoir (final HttpServerRequest request) {
        if (!request.params().contains("idDevoir")) {
            badRequest(request);
        } else {
            UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
                @Override
                public void handle(final UserInfos user) {
                    RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
                        @Override
                        public void handle(final JsonObject body) {
                            try {
                                final Long idDevoir = Long.parseLong(request.params().get("idDevoir"));
                                devoirsService.retrieve(idDevoir.toString(), new Handler<Either<String, JsonObject>>() {
                                    @Override
                                    public void handle(Either<String, JsonObject> result) {
                                        if (result.isRight()) {
                                            final JsonObject devoir = result.right().getValue();
                                            competencesService.getDevoirCompetences(idDevoir, new Handler<Either<String, JsonArray>>() {
                                                @Override
                                                public void handle(Either<String, JsonArray> result) {
                                                    if (result.isRight()) {
                                                        JsonArray competences = result.right().getValue();
                                                        if (competences.size() > 0) {
                                                            JsonArray idCompetences = new JsonArray();
                                                            JsonObject o = new JsonObject();
                                                            for (int i = 0; i < competences.size(); i++) {
                                                                o = competences.get(i);
                                                                if (o.containsField("id")) {
                                                                    idCompetences.addNumber(o.getNumber("id_competence"));
                                                                }
                                                            }
                                                            devoir.putArray("competences", idCompetences);
                                                        }
                                                        devoirsService.duplicateDevoir(idDevoir, devoir, body.getArray("classes"), user, arrayResponseHandler(request));
                                                    } else {
                                                        log.error("An error occured when collecting competences for devoir id " + idDevoir);
                                                        renderError(request);
                                                    }
                                                }
                                            });
                                        } else {
                                            log.error("An error occured when collecting devoir data for id " + idDevoir);
                                            renderError(request);
                                        }
                                    }
                                });
                            } catch (ClassCastException e) {
                                log.error("idDevoir parameter must be a long object.");
                                renderError(request);
                            }
                        }
                    });
                }
            });
        }
    }
}