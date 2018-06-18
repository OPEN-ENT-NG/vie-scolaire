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
import fr.openent.viescolaire.security.AccessAuthorized;
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
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import static fr.wseduc.webutils.Utils.handlerToAsyncHandler;

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
    @ResourceFilter(AccessAuthorized.class)
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

    @Get("/classe/eleves")
    @ApiDoc("Recupere tous les élèves d'une liste de classes.")
    @ResourceFilter(AccessAuthorized.class)
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
                        JsonArray idClasseArray = new fr.wseduc.webutils.collections.JsonArray(idClasse);
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

                    final boolean isPresence;
                    if(request.params().get("isPresence") != null) {
                        isPresence = Boolean.parseBoolean(request.params().get("isPresence"));
                    } else {
                        isPresence = false;
                    }
                    final boolean isEdt;
                    if(request.params().contains("isEdt")) {
                        isEdt = Boolean.parseBoolean(request.params().get("isEdt"));
                    } else {
                        isEdt = false;
                    }
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
                                final JsonArray classes = new fr.wseduc.webutils.collections.JsonArray();
                                List<String> idGroupes = new ArrayList<>();
                                for (int i = 0; i < recipient.size(); i++) {
                                    classe = recipient.getJsonObject(i);
                                    classe = classe.getJsonObject("m");
                                    object = classe.getJsonObject("metadata");
                                    classe = classe.getJsonObject("data");
                                    if (object.getJsonArray("labels").contains("Class"))
                                        classe.put("type_groupe",  Viescolaire.CLASSE_TYPE);
                                    else if (object.getJsonArray("labels").contains("FunctionalGroup")){
                                        classe.put("type_groupe",  Viescolaire.GROUPE_TYPE);
                                    } else if (object.getJsonArray("labels").contains("ManualGroup")) {
                                        classe.put("type_groupe",  Viescolaire.GROUPE_MANUEL_TYPE);
                                    }
                                    if(isEdt){ classe.put("color", utilsService.getColor(classe.getString("name")));}
                                    idGroupes.add(classe.getString("id"));
                                    classes.add(classe);
                                }

                                if (!idGroupes.isEmpty()) {
                                    JsonObject action = new JsonObject()
                                            .put("action", "utils.getCycle")
                                            .put("ids", new fr.wseduc.webutils.collections.JsonArray(idGroupes));

                                    if (isPresence) {
                                            renderJson(request, classes);
                                    }
                                    else {
                                        eb.send(Viescolaire.COMPETENCES_BUS_ADDRESS, action, handlerToAsyncHandler(new Handler<Message<JsonObject>>() {
                                            @Override
                                            public void handle(Message<JsonObject> message) {
                                                if ("ok".equals(message.body().getString("status"))) {
                                                    JsonArray returnedList = new fr.wseduc.webutils.collections.JsonArray();
                                                    JsonObject object;
                                                    JsonObject cycles = utilsService.mapListNumber(message.body().getJsonArray("results"), "id_groupe", "id_cycle");
                                                    JsonObject cycleLibelle = utilsService.mapListString(message.body().getJsonArray("results"), "id_groupe", "libelle");
                                                    for (int i = 0; i < classes.size(); i++) {
                                                        object = classes.getJsonObject(i);
                                                        object.put("id_cycle", cycles.getLong(object.getString("id")));
                                                        object.put("libelle_cycle", cycleLibelle.getString(object.getString("id")));
                                                        returnedList.add(object);
                                                    }
                                                    renderJson(request, returnedList);
                                                } else {
                                                    badRequest(request);
                                                }
                                            }
                                        }));
                                    }
                                } else {
                                    renderJson(request, new fr.wseduc.webutils.collections.JsonArray(idGroupes));
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
