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
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import javax.sound.sampled.Control;
import java.util.ArrayList;
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

                    JsonObject n = new JsonObject();
                    JsonObject enseignant = new JsonObject();
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
                    request.response().end(response.toString());

                }else{
                    leftToResponse(request, event.left());
                }
            }
        });
    }

    /**
     * Liste les matières d'un élève ou les matières de ces enfants
     * @param request
     */
    @Get("/matieres/eleve/:userid")
    @ApiDoc("Liste les matières d'un élève ou les matières de ces enfants")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void viewMatieresEleve(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null && user.getType().equals("Student")){
                    matiereService.listMatieresEleve(request.params().get("userId"), new Handler<Either<String, JsonArray>>() {
                        @Override
                        public void handle(Either<String, JsonArray> event) {
                            if(event.isRight()){
                                JsonArray r = event.right().getValue();
                                ArrayList<String> classesFieldOfStudy = new ArrayList<String>();
                                String key = new String();
                                JsonObject f = new JsonObject();
                                final JsonArray matieres = r;
                                final JsonArray response = new JsonArray();

                                for(int i = 0; i < r.size(); i++){
                                    JsonObject o = r.get(i);
                                    f = o.getObject("f");
                                    key = user.getClassNames().get(0)+"$"+f.getObject("data").getString("externalId");
                                    classesFieldOfStudy.add(key);
                                }

                                getEnseignantsMatieres(request, user, matieres, user.getClassNames().get(0), classesFieldOfStudy);

                            }else{
                                leftToResponse(request, event.left());
                            }
                        }
                    });
                }else if(user != null && user.getType().equals("Relative")){
                    utilsService.getEnfants(user.getUserId(), new Handler<Either<String, JsonArray>>() {
                        @Override
                        public void handle(Either<String, JsonArray> event) {
                            if(event.isRight()){
                                JsonArray values = event.right().getValue();
                                final JsonObject enfant = values.get(0);
                                matiereService.listMatieresEleve(enfant.getString("n.id"), new Handler<Either<String, JsonArray>>() {
                                    @Override
                                    public void handle(Either<String, JsonArray> event) {
                                        if(event.isRight()){
                                            JsonArray r = event.right().getValue();
                                            ArrayList<String> classesFieldOfStudy = new ArrayList<String>();
                                            String key = new String();
                                            JsonObject f = new JsonObject();
                                            final JsonArray matieres = r;
                                            final JsonArray response = new JsonArray();

                                            for(int i = 0; i < r.size(); i++){
                                                JsonObject o = r.get(i);
                                                f = o.getObject("f");
                                                key = enfant.getArray("n.classes").get(0)+"$"+f.getObject("data").getString("externalId");
                                                classesFieldOfStudy.add(key);
                                            }

                                            getEnseignantsMatieres(request, user, matieres, enfant.getArray("n.classes").get(0).toString(), classesFieldOfStudy);

                                        }else{
                                            leftToResponse(request, event.left());
                                        }
                                    }
                                });

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

    // TODO A REFAIRE APRES LES BERNCHS DE PERF
    /**
     * Retourne les matières enseignées par un enseignant donné
     * @param request
     */
    @Get("/matieres")
    @ApiDoc("Retourne les matières enseignées par un enseignant donné")
    @SecuredAction(value="", type = ActionType.AUTHENTICATED)
    public void viewMatiere(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>(){
            @Override
            public void handle(UserInfos user){
                if(user != null){
                    matiereService.listMatieres(request.params().get("idEnseignant"), new Handler<Either<String, JsonArray>>() {
                        @Override
                        public void handle(Either<String, JsonArray> event) {
                            if(event.isRight()){
                                JsonObject r = event.right().getValue().get(0);
                                JsonArray matieres = r.getArray("u.classesFieldOfStudy");

                                final JsonArray codeEtab = new JsonArray();
                                JsonArray codeMatieres = new JsonArray();
                                Pattern p = Pattern.compile("\\$");
                                final ArrayList<String> correspondanceMatiere = new ArrayList<String>();
                                final ArrayList<String> correspondanceEtablissement = new ArrayList<String>();
                                final ArrayList<String> correspondanceClasse = new ArrayList<String>();
                                for(int i = 0 ; i < matieres.size(); i++){
                                    String value = matieres.get(i).toString();
                                    String[] spliting = p.split(value);
                                    correspondanceMatiere.add(spliting[2]);
                                    correspondanceClasse.add(spliting[1]);
                                    correspondanceEtablissement.add(spliting[0]);
                                    if(!codeEtab.contains(spliting[0])){
                                        codeEtab.add(spliting[0]);
                                    }
                                    if(!codeMatieres.contains(spliting[2])) {
                                        codeMatieres.add(spliting[2]);
                                    }
                                }
                                matiereService.getCorrespondanceMatieres(codeMatieres, codeEtab, new Handler<Either<String, JsonArray>>() {
                                    @Override
                                    public void handle(Either<String, JsonArray> event) {
                                        JsonArray r = event.right().getValue();
                                        JsonObject etabListe = new JsonObject();
                                        JsonObject matiereList = new JsonObject();

                                        JsonObject n = new JsonObject();
                                        JsonObject nInter = new JsonObject();

                                        final JsonArray reponse = new JsonArray();
                                        String[] ids = new String[r.size()];
                                        for(int i = 0 ; i < r.size(); i ++){
                                            n = r.get(i);
                                            nInter = n.getObject("n");
                                            n = nInter.getObject("data");
                                            if(n.containsField("academy")){
                                                etabListe.putObject(n.getString("externalId"), n);
                                            }else{
                                                matiereList.putObject(n.getString("externalId"), n);
                                            }
                                        }
                                        for(int i = 0 ; i < correspondanceMatiere.size(); i++) {
                                            JsonObject obj = matiereList.getObject(correspondanceMatiere.get(i));
                                            JsonObject o = new JsonObject();
                                            o = obj.copy();

                                            o.putString("libelleClasse", correspondanceClasse.get(i));
                                            o.putString("idEtablissement", etabListe.getObject(correspondanceEtablissement.get(i)).getString("id"));
                                            reponse.add(o);
                                            ids[i]=o.getString("id");
                                        }
                                        sousMatiereService.getSousMatiereById(ids, new Handler<Either<String, JsonArray>>() {
                                            @Override
                                            public void handle(Either<String, JsonArray> event_ssmatiere) {
                                                if (event_ssmatiere.right().isRight()) {
                                                    JsonArray finalresponse = new JsonArray();
                                                    JsonArray res = event_ssmatiere.right().getValue();
                                                    for (int i = 0; i < reponse.size(); i++) {
                                                        JsonObject matiere = reponse.get(i);
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
                                                    request.response().putHeader("content-type", "application/json; charset=utf-8").end(finalresponse.toString());
                                                } else {
                                                    leftToResponse(request, event_ssmatiere.left());
                                                }
                                            }
                                        });
                                    }
                                });
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
     * Retourne les matières
     * @param request
     */
    @Get("/widget/matieres")
    @ApiDoc("Retourne les matières")
    @SecuredAction(value="", type = ActionType.AUTHENTICATED)
    public void getMatiere(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if(user != null){
                    Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                    matiereService.getMatiere(request.params().getAll("idmatiere"), handler);
                }else{
                    unauthorized(request);
                }
            }
        });
    }
}
