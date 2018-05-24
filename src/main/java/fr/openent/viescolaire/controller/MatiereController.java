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

package fr.openent.viescolaire.controller;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.UtilsService;
import fr.openent.viescolaire.service.impl.DefaultUtilsService;
import fr.openent.viescolaire.service.MatiereService;
import fr.openent.viescolaire.service.SousMatiereService;
import fr.openent.viescolaire.service.impl.DefaultMatiereService;
import fr.openent.viescolaire.service.impl.DefaultSousMatiereService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Renders;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.leftToResponse;

/**
 * Created by ledunoiss on 18/10/2016.
 */
public class MatiereController extends ControllerHelper {

    private final MatiereService matiereService;
    private final UtilsService utilsService;
    private final SousMatiereService sousMatiereService;

    public MatiereController() {
        pathPrefix = Viescolaire.VSCO_PATHPREFIX;
        matiereService = new DefaultMatiereService();
        utilsService = new DefaultUtilsService();
        sousMatiereService = new DefaultSousMatiereService();
    }

    public void getEnseignantsMatieres(final HttpServerRequest request, final UserInfos user, final JsonArray matieres, final String classe, ArrayList<String>classesFieldOfStudy){
        matiereService.getEnseignantsMatieres(classesFieldOfStudy, new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> event) {
                final JsonArray response = new fr.wseduc.webutils.collections.JsonArray();
                if(event.isRight()){
                    JsonArray r = event.right().getValue();

                    ArrayList<String> matieresExternalList = new ArrayList<String>();

                    for(int i = 0 ; i < matieres.size(); i++){
                        JsonObject matiere = matieres.getJsonObject(i);
                        matieresExternalList.add(classe + "$" + matiere.getJsonObject("f").getJsonObject("data").getString("externalId"));
                    }

                    JsonObject n, enseignant;
                    for(int i = 0; i < r.size(); i++){
                        n = r.getJsonObject(i);
                        enseignant = n.getJsonObject("n").getJsonObject("data");
                        JsonArray classes = enseignant.getJsonArray("classesFieldOfStudy");
                        for(int j = 0; j < classes.size(); j++){
                            if(matieresExternalList.contains(classes.getString(j))){
                                JsonObject matiere = matieres.getJsonObject(matieresExternalList.indexOf(classes.getString(j)));
                                JsonObject matiereInter = matiere.getJsonObject("f").getJsonObject("data");
                                matiereInter.put("displayEnseignantName", enseignant.getString("displayName"));

                                String firstNameEnsiegnant = enseignant.getString("firstName");
                                matiereInter.put("firstNameEnseignant", firstNameEnsiegnant);
                                matiereInter.put("firstNameInitialeEnseignant", firstNameEnsiegnant.substring(0,1));
                                matiereInter.put("surnameEnseignant", enseignant.getString("surname"));
                                matiereInter.put("idEnseignant", enseignant.getString("id"));
                                response.add(matiereInter);
                            }
                        }
                    }
                    Renders.renderJson(request, response);

                }else{
                    leftToResponse(request, event.left());
                }
            }
        });
    }

    public void listMatieres(final String structureId , JsonArray poTitulairesIdList,
                             final HttpServerRequest request, final Message<JsonObject> message, String idEnseignant,
                             final Boolean onlyId) {
        String _idEnseignant = (null == request) ? idEnseignant : request.params().get("idEnseignant");
        matiereService.listMatieres(structureId , _idEnseignant,
                poTitulairesIdList, new Handler<Either<String, JsonArray>>() {
                    @Override
                    public void handle(Either<String, JsonArray> event) {
                        if(event.isRight()){
                            final JsonArray resultats = event.right().getValue();
                            if (resultats.size() > 0) {

                                final List<String> ids = new ArrayList<String>();

                                final JsonArray reponseJA = new fr.wseduc.webutils.collections.JsonArray();
                                JsonArray libelleGroups, libelleClasses;
                                for (Object res : resultats) {
                                    final JsonObject r = (JsonObject) res;
                                    libelleGroups = r.getJsonArray("libelleGroupes");
                                    libelleClasses = r.getJsonArray("libelleClasses");
                                    libelleGroups = libelleGroups == null ? new fr.wseduc.webutils.collections.JsonArray() : libelleGroups;
                                    libelleClasses = libelleClasses == null ? new fr.wseduc.webutils.collections.JsonArray() : libelleClasses;
                            r.put("libelleClasses", utilsService.saUnion(libelleClasses, libelleGroups));
                                    r.remove("libelleGroupes");
                                    reponseJA.add(r);
                                    ids.add(r.getString("id"));
                                }
                        sousMatiereService.getSousMatiereById(ids.toArray(new String[0]),
                                new Handler<Either<String, JsonArray>>() {
                                            @Override
                                            public void handle(Either<String, JsonArray> event_ssmatiere) {
                                                if (event_ssmatiere.right().isRight()) {
                                                    JsonArray finalresponse = new fr.wseduc.webutils.collections.JsonArray();
                                                    JsonArray res = event_ssmatiere.right().getValue();
                                                    for (int i = 0; i < reponseJA.size(); i++) {
                                                        JsonObject matiere = reponseJA.getJsonObject(i);
                                                        String id = matiere.getString("id");
                                                        JsonArray ssms = new fr.wseduc.webutils.collections.JsonArray();
                                                        for (int j = 0; j < res.size(); j++) {
                                                            JsonObject ssm = res.getJsonObject(j);
                                                            if (ssm.getString("id_matiere").equals(id)) {
                                                                ssms.add(ssm);
                                                            }
                                                        }
                                                        matiere.put("sous_matieres", ssms);
                                                        finalresponse.add(matiere);
                                                    }

                                                    if (message != null) {
                                                        JsonArray _res = (onlyId)? new fr.wseduc.webutils.collections.JsonArray(ids):
                                                                finalresponse;
                                                        message.reply(new JsonObject()
                                                                .put("status", "ok")
                                                                .put("results",_res));
                                                    } else {
                                                        Renders.renderJson(request, finalresponse);
                                                    }
                                                }
                                                else {
                                                    if (message != null) {
                                                        message.reply(new JsonObject()
                                                                .put("status", "error")
                                                                .put("message",
                                                                        event_ssmatiere.left().getValue()));
                                                    } else {
                                                        leftToResponse(request, event_ssmatiere.left());
                                                    }

                                                }
                                            }
                                        });
                            } else {
                                if (request == null) {
                                    matiereService.listMatieresEtab(structureId, onlyId,
                                            new Handler<Either<String, JsonArray>>() {
                                        @Override
                                        public void handle(Either<String, JsonArray> result) {
                                            if (result.isRight()) {
                                                message.reply(new JsonObject()
                                                        .put("status", "ok")
                                                        .put("results", result.right().getValue()));
                                            } else {
                                                message.reply(new JsonObject()
                                                        .put("status", "error")
                                                        .put("message", result.left().getValue()));
                                            }
                                        }
                                    });

                                }
                                else {
                                    matiereService.listMatieresEtab(structureId, onlyId, arrayResponseHandler(request));
                                }
                            }
                        }else{
                            leftToResponse(request, event.left());
                        }
                    }
                });
    }

    /**
     * Retourne les matières enseignées par un enseignant donné
     * Ou les matiére de l'établissement, si (Chef ETab).
     * @param request
     */
    @Get("/matieres")
    @ApiDoc("Retourne les matières enseignées par un enseignant donné")
    @SecuredAction(value="", type = ActionType.AUTHENTICATED)
    public void viewMatiere(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>(){
            @Override
            public void handle(final UserInfos user){
                if(user != null && null != request.params().get("idEtablissement")){
                    if("Personnel".equals(user.getType())){
                        final Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                        matiereService.listMatieresEtab(request.params().get("idEtablissement"), false, handler);
                    }else{
                        utilsService.getTitulaires(request.params().get("idEnseignant"), user.getStructures().get(0),
                                new Handler<Either<String, JsonArray>>() {
                                    @Override
                                    public void handle(Either<String, JsonArray> event) {
                                        if (event.isRight()) {
                                            JsonArray oTitulairesIdList = event.right().getValue();
                                            listMatieres(request.params().get("idEtablissement"),
                                                    oTitulairesIdList, request,null,
                                            null,false);
                                        } else {
                                            leftToResponse(request, event.left());
                                        }
                                    }
                                }
                        );
                    }
                }else{

                    badRequest(request);
                }
            }
        });
    }
    /**
     * Retourne les suivies par un élève donné
     * Ou les matiére de l'établissement, si (Chef ETab).
     * @param request
     */
    @Get("/matieres/eleve/:idEleve")
    @ApiDoc("Retourne les matières suivie par l'élève")
    @SecuredAction(value="", type = ActionType.AUTHENTICATED)
    public void matieresEleve(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if (user != null && null != request.params().get("idEleve")) {
                    if("Student".equals(user.getType()) || "Relative".equals(user.getType())){
                        final Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                        matiereService.listMatieresEleve(request.params().get("idEleve"), handler);
                    }
                }
            }
        });
    }

    /**
     * Retourne les information des matières dont il reçoit les 'id'
     * @param request
     */
    @Get("/matieres/infos")
    @ApiDoc("Retourne les information des matières dont il reçoit les id")
    @SecuredAction(value="", type = ActionType.AUTHENTICATED)
    public void getMatieresInfo(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if("Student".equals(user.getType()) || "Relative".equals(user.getType())){
                    final Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                    final JsonArray idMatieres = new fr.wseduc.webutils.collections.JsonArray(request.params().getAll("idMatiere"));
                    matiereService.getMatieres(idMatieres,handler);
                }
            }
        });
    }
}
