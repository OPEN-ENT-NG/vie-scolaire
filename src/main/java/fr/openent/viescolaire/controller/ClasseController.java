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
import fr.openent.evaluations.security.AccessAuthorozed;
import fr.openent.evaluations.service.UtilsService;
import fr.openent.evaluations.service.impl.DefaultUtilsService;
import fr.openent.viescolaire.service.ClasseService;
import fr.openent.viescolaire.service.impl.DefaultClasseService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.BaseController;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;

/**
 * Created by ledunoiss on 19/02/2016.
 */
public class ClasseController extends BaseController {

    ClasseService classeService;
    UtilsService utilsService;

    public ClasseController(){
        pathPrefix = Viescolaire.VSCO_PATHPREFIX;
        classeService = new DefaultClasseService();
        utilsService = new DefaultUtilsService();
    }

    @Get("/classes/:idClasse/users")
    @ApiDoc("Recupere tous les élèves d'une Classe.")
    @ResourceFilter(AccessAuthorozed.class)
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getEleveClasse(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>(){
            @Override
            public void handle(UserInfos user){

                if(user != null){
                    if (request.params().isEmpty()){
                        badRequest(request);
                    } else {
                        if (("Personnel".equals(user.getType())
                                && !user.getFunctions().isEmpty()
                            ) || "Teacher".equals(user.getType())) {
                            final Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                            String idClasse = request.params().get("idClasse");
                            classeService.getEleveClasse(idClasse, handler);
                        }
                    }
                }else{
                    unauthorized(request);
                }
            }
        });
    }

    @Get("/eleves")
    @ApiDoc("Recupere tous les élèves d'une liste de classes.")
    @ResourceFilter(AccessAuthorozed.class)
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getElevesClasse(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if (user != null) {
                    if (request.params().isEmpty()){
                        badRequest(request);
                    } else {
                        final Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                        List<String> idClasse = request.params().getAll("idClasse");
                        JsonArray idClasseArray = new JsonArray(idClasse.toArray());
                        Boolean isTeacher = "Teacher".equals(user.getType());
                        String idEtablissement = request.params().get("idEtablissement");

                        classeService.getEleveClasses(idEtablissement, idClasseArray, isTeacher, handler);
                    }
                } else {
                    unauthorized(request);
                }
            }
        });
    }

    @Get("/classes")
    @ApiDoc("Retourne les classes de l'établissement")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getClasses(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if (user != null
                        && !request.params().isEmpty()
                        && request.params().contains("idEtablissement")) {
                    Boolean classOnly = null;
                    String idEtablissement = request.params().get("idEtablissement");
                    if(request.params().get("classOnly") != null) {
                        classOnly = Boolean.parseBoolean(request.params().get("classOnly"));
                    }

                    classeService.listClasses(idEtablissement, classOnly, user, new Handler<Either<String, JsonArray>>() {
                        @Override
                        public void handle(Either<String, JsonArray> event) {
                            if (event.isRight()) {
                                JsonArray recipient = event.right().getValue();
                                JsonObject classe;
                                JsonObject object;
                                final JsonArray classes = new JsonArray();
                                List<String> idGroupes = new ArrayList<>();
                                for (int i = 0; i < recipient.size(); i++) {
                                    classe = recipient.get(i);
                                    classe = classe.getObject("g");
                                    object = classe.getObject("metadata");
                                    classe = classe.getObject("data");
                                    classe.putNumber("type_groupe", object.getArray("labels").contains("Class") ? 0 : 1);
                                    idGroupes.add(classe.getString("id"));
                                    classes.addObject(classe);
                                }

                                if (!idGroupes.isEmpty()) {
                                    utilsService.getCycle(idGroupes, new Handler<Either<String, JsonArray>>() {
                                        @Override
                                        public void handle(Either<String, JsonArray> event) {
                                            if (event.isRight()) {
                                                JsonArray returnedList = new JsonArray();
                                                JsonObject object;
                                                JsonObject cycles = utilsService.mapListNumber(event.right().getValue(), "id_groupe", "id_cycle");
                                                JsonObject cycleLibelle = utilsService.mapListString(event.right().getValue(), "id_groupe", "libelle");
                                                for (int i = 0; i < classes.size(); i++) {
                                                    object = classes.get(i);
                                                    object.putNumber("id_cycle", cycles.getNumber(object.getString("id")));
                                                    object.putString("libelle_cycle", cycleLibelle.getString(object.getString("id")));
                                                    returnedList.addObject(object);
                                                }
                                                renderJson(request, returnedList);
                                            } else {
                                                badRequest(request);
                                            }
                                        }
                                    });
                                } else {
                                    renderJson(request, new JsonArray(idGroupes.toArray()));
                                }
                            } else {
                                badRequest(request);
                            }
                        }
                    });
                } else {
                    badRequest(request , "getClasses : Paramètre manquant iEtablissement ou Utilisateur null.");
                }
            }
        });
    }
}
