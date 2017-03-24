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
import fr.openent.evaluations.service.CompetenceNoteService;
import fr.openent.evaluations.service.UtilsService;
import fr.openent.evaluations.service.impl.DefaultCompetenceNoteService;
import fr.openent.evaluations.service.impl.DefaultCompetencesService;
import fr.openent.evaluations.service.impl.DefaultDevoirService;
import fr.openent.evaluations.service.impl.DefaultUtilsService;
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
import org.entcore.common.utils.StringUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.leftToResponse;
import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;

/**
 * Created by ledunoiss on 04/08/2016.
 */
public class DevoirController extends ControllerHelper {

    /**
     * Déclaration des services
     */
    private final DefaultDevoirService devoirsService;
    private final DefaultCompetencesService defaultCompetencesService;
    private final CompetenceNoteService competencesNotesService;
    private final UtilsService utilsService;

    public DevoirController() {
        pathPrefix = Viescolaire.EVAL_PATHPREFIX;
        devoirsService = new DefaultDevoirService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_DEVOIR_TABLE);
        defaultCompetencesService = new DefaultCompetencesService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_COMPETENCES_TABLE);
        competencesNotesService = new DefaultCompetenceNoteService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_COMPETENCES_NOTES_TABLE);
        utilsService = new DefaultUtilsService();
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
        // TODO MN-301 : MODIFIER LA REQUETE DE RECUPERATION
        renderJson(request, new JsonArray());
//        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
//            @Override
//            public void handle(final UserInfos user) {
//                RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
//                    @Override
//                    public void handle(JsonObject devoirs) {
//                        final JsonObject classes = devoirs.getObject("datas");
//                        devoirsService.getNbNotesDevoirs(user, new Handler<Either<String, JsonArray>>() {
//                            @Override
//                            public void handle(Either<String, JsonArray> event) {
//                                if (event.isRight()) {
//                                    JsonObject returns = new JsonObject();
//                                    JsonArray values = event.right().getValue();
//                                    for (int i = 0; i < values.size(); i++) {
//                                        Double percent = new Double(0);
//                                        JsonObject devoir = values.get(i);
//                                        if(null != devoir
//                                                && null != devoir.getInteger("id")) {
//                                            String idClasse = devoir.getString("id_groupe");
//                                            Integer idDevoir = devoir.getInteger("id");
//                                            if (null != classes
//                                                    && null != classes.getInteger(idClasse)) {
//                                                percent = Double.parseDouble(String.valueOf((devoir.getInteger("nb_notes") * 100 / classes.getInteger(idClasse))));
//                                            }
//                                            returns.putNumber(idDevoir.toString(), percent);
//                                        }
//                                    }
//                                    Renders.renderJson(request, returns);
//                                } else {
//                                    leftToResponse(request,event.left());
//                                }
//                            }
//                        });
//                    }
//                });
//            }
//        });
    }
    /**
     *  Supprimer un devoir
     */
    @Delete("/devoir")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
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
}
