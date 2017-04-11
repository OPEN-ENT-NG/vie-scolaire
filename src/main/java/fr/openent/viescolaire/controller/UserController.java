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
import fr.openent.viescolaire.service.UserService;
import fr.openent.viescolaire.service.impl.DefaultUserService;
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

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;

/**
 * Created by ledunoiss on 08/11/2016.
 */
public class UserController extends ControllerHelper {

    private UserService userService;

    public UserController() {
        pathPrefix = Viescolaire.VSCO_PATHPREFIX;
        userService = new DefaultUserService();
    }


    @Get("/user/deleted/classe/:groupid")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    @ApiDoc("Retourne les informations du référentiel VieScolaire relatives à l'utilisateur")
    public void getUsersDeletedByClasse(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if (user != null) {
                    final String groupId = request.params().get("groupId");
                    final String type_groupe = request.params().get("type_groupe");
                    final String idPeriode = request.params().get("periode");
                    if (type_groupe != null && !type_groupe.trim().isEmpty()) {
                        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                        userService.getUserDeletedByClasse(groupId,type_groupe,idPeriode, handler);
                    }else{
                        log.error("Error getUsersDeletedByClasse : type_groupe can't be null ");
                        badRequest(request);
                        return;
                    }
                } else {
                    unauthorized(request);
                }
            }
        });
    }


    @Get("/user")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    @ApiDoc("Retourne les informations du référentiel VieScolaire relatives à l'utilisateur")
    public void getUserInformation(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
                    @Override
                    public void handle(final UserInfos user) {
                        final JsonObject values = new JsonObject();
                        userService.getUserId(user, new Handler<Either<String, JsonObject>>() {
                            @Override
                            public void handle(Either<String, JsonObject> event) {
                                if (event.isRight()) {
                                    final JsonObject val = event.right().getValue();
                                    values.putNumber("userId", val.getInteger("id"));
                                    userService.getStructures(user, new Handler<Either<String, JsonArray>>() {
                                        @Override
                                        public void handle(Either<String, JsonArray> event) {
                                            if (event.isRight()) {
                                                values.putArray("structures", event.right().getValue());
                                                userService.getClasses(user, new Handler<Either<String, JsonArray>>() {
                                                    @Override
                                                    public void handle(Either<String, JsonArray> event) {
                                                        if (event.isRight()) {
                                                            values.putArray("classes", event.right().getValue());
                                                            userService.getMatiere(user, new Handler<Either<String, JsonArray>>() {
                                                                @Override
                                                                public void handle(Either<String, JsonArray> event) {
                                                                    if (event.isRight()) {
                                                                        values.putArray("matieres", event.right().getValue());
                                                                        Renders.renderJson(request, values);
                                                                    } else {
                                                                        unauthorized(request);
                                                                    }
                                                                }
                                                            });
                                                        } else {
                                                            unauthorized(request);
                                                        }
                                                    }
                                                });
                                            } else {
                                                unauthorized(request);
                                            }
                                        }
                                    });
                                } else {
                                    unauthorized(request);
                                }
                            }
                        });
                    }
                }
        );
    }
}
