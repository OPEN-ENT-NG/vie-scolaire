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
import fr.openent.evaluations.service.UtilsService;
import fr.openent.evaluations.service.impl.DefaultUtilsService;
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
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import javax.sound.sampled.Control;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
                final JsonArray response = new JsonArray();
                if(event.isRight()){
                    JsonArray r = event.right().getValue();

                    ArrayList<String> matieresExternalList = new ArrayList<String>();

                    for(int i = 0 ; i < matieres.size(); i++){
                        JsonObject matiere = matieres.get(i);
                        matieresExternalList.add(classe + "$" + matiere.getObject("f").getObject("data").getString("externalId"));
                    }

                    JsonObject n, enseignant;
                    for(int i = 0; i < r.size(); i++){
                        n = r.get(i);
                        enseignant = n.getObject("n").getObject("data");
                        JsonArray classes = enseignant.getField("classesFieldOfStudy");
                        for(int j = 0; j < classes.size(); j++){
                            if(matieresExternalList.contains(classes.get(j))){
                                JsonObject matiere = matieres.get(matieresExternalList.indexOf(classes.get(j)));
                                JsonObject matiereInter = matiere.getObject("f").getObject("data");
                                matiereInter.putString("displayEnseignantName", enseignant.getString("displayName"));

                                String firstNameEnsiegnant = enseignant.getString("firstName");
                                matiereInter.putString("firstNameEnseignant", firstNameEnsiegnant);
                                matiereInter.putString("firstNameInitialeEnseignant", firstNameEnsiegnant.substring(0,1));
                                matiereInter.putString("surnameEnseignant", enseignant.getString("surname"));
                                matiereInter.putString("idEnseignant", enseignant.getString("id"));
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

    private void listMatieres(final UserInfos user, final String structureId , JsonArray poTitulairesIdList, final HttpServerRequest request) {
        matiereService.listMatieres(structureId , request.params().get("idEnseignant"), poTitulairesIdList, new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> event) {
                if(event.isRight()){
                    final JsonArray resultats = event.right().getValue();
                    if (resultats.size() > 0) {

                        final List<String> ids = new ArrayList<String>();

                        final JsonArray reponseJA = new JsonArray();
                        JsonArray libelleGroups, libelleClasses;
                        for (Object res : resultats) {
                            final JsonObject r = (JsonObject) res;
                            libelleGroups = r.getArray("libelleGroupes");
                            libelleClasses = r.getArray("libelleClasses");
                            libelleGroups = libelleGroups == null ? new JsonArray() : libelleGroups;
                            libelleClasses = libelleClasses == null ? new JsonArray() : libelleClasses;
                             r.putArray("libelleClasses", utilsService.saUnion(libelleClasses, libelleGroups));
                            r.removeField("libelleGroupes");
                            reponseJA.addObject(r);
                            ids.add(r.getString("id"));
                        }
                        sousMatiereService.getSousMatiereById(ids.toArray(new String[0]), new Handler<Either<String, JsonArray>>() {
                            @Override
                            public void handle(Either<String, JsonArray> event_ssmatiere) {
                                if (event_ssmatiere.right().isRight()) {
                                    JsonArray finalresponse = new JsonArray();
                                    JsonArray res = event_ssmatiere.right().getValue();
                                    for (int i = 0; i < reponseJA.size(); i++) {
                                        JsonObject matiere = reponseJA.get(i);
                                        String id = matiere.getString("id");
                                        JsonArray ssms = new JsonArray();
                                        for (int j = 0; j < res.size(); j++) {
                                            JsonObject ssm = res.get(j);
                                            if (ssm.getString("id_matiere").equals(id)) {
                                                ssms.addObject(ssm);
                                            }
                                        }
                                        matiere.putArray("sous_matieres", ssms);
                                        finalresponse.addObject(matiere);
                                    }
                                    Renders.renderJson(request, finalresponse);
                                } else {
                                    leftToResponse(request, event_ssmatiere.left());
                                }
                            }
                        });
                    } else {
                        matiereService.listMatieresEtab(structureId, user, arrayResponseHandler(request));
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
						matiereService.listMatieresEtab(request.params().get("idEtablissement"), user, handler);
					}else{
						utilsService.getTitulaires(request.params().get("idEnseignant"), user.getStructures().get(0), new Handler<Either<String, JsonArray>>() {
									@Override
									public void handle(Either<String, JsonArray> event) {
										if (event.isRight()) {
											JsonArray oTitulairesIdList = event.right().getValue();
											listMatieres(user, request.params().get("idEtablissement"), oTitulairesIdList, request);
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

}
