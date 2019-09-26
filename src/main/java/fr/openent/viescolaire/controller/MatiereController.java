/*
 * Copyright (c) Région Hauts-de-France, Département de la Seine-et-Marne, Région Nouvelle Aquitaine, Mairie de Paris, CGI, 2016.
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
 */

package fr.openent.viescolaire.controller;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.MatiereService;
import fr.openent.viescolaire.service.impl.DefaultMatiereService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Renders;
import io.vertx.core.eventbus.EventBus;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

import static org.entcore.common.http.response.DefaultResponseHandler.*;

/**
 * Created by ledunoiss on 18/10/2016.
 */
public class MatiereController extends ControllerHelper {

    private final MatiereService matiereService;

    public MatiereController(EventBus eb) {
        pathPrefix = Viescolaire.VSCO_PATHPREFIX;
        matiereService = new DefaultMatiereService(eb);
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
                                matiereInter.put("surnameEnseignant", enseignant.getString("lastName"));
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
                    if("Personnel".equals(user.getType()) && null == request.params().get("isEnseignant")){
                        matiereService.listMatieresEtabWithSousMatiere(request.params().get("idEtablissement"), false, arrayResponseHandler(request));
                    }else{
                        matiereService.listAllMatieres(request.params().get("idEtablissement"), request.params().get("idEnseignant"), false, arrayResponseHandler(request));
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
                    matiereService.subjectsListWithUnderSubjects(idMatieres,handler);
                }
            }
        });
    }
}
