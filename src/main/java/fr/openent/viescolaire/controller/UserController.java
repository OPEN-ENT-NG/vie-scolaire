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
import fr.wseduc.rs.Delete;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.request.RequestUtils;
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
        userService = new DefaultUserService(eb);
    }

    /**
     * Retourne retourne le cycle de la classe
     * @param request
     */
    @Get("/user/structures/actives")
    @ApiDoc("Retourne la liste des identifiants des structures actives de l'utilisateur pour un module donné ")
    @SecuredAction(value="", type = ActionType.AUTHENTICATED)
    public void getActivedStructures(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if(user != null){
                    Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                    final String module = request.params().get("module");
                    userService.getActivesIDsStructures(user,module,handler);
                }else{
                    unauthorized(request);
                }
            }
        });
    }


    /**
     * Retourne retourne le cycle de la classe
     * @param request
     */
    @Post("/user/structures/actives")
    @ApiDoc("Active un module pour une structure donnée")
    @SecuredAction(value="", type = ActionType.AUTHENTICATED)
    public void createActivedStructure(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
                    @Override
                    public void handle(JsonObject body) {
                        if(user != null && body.containsField("structureId")){
                            final String structureId = body.getString("structureId");
                            final String module = body.getString("module");
                            Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                            userService.createActiveStructure(structureId,module, user, handler);
                        }else{
                            badRequest(request);
                        }
                    }
                });
            }
        });
    }

    @Delete("/user/structures/actives")
    @ApiDoc("Supprime une structure active pour un module donné.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void deleteActivatedStructure(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if (user != null) {
                    final String structureId = request.params().get("structureId");
                    final String module = request.params().get("module");
                    Handler<Either<String, JsonArray>> handler =
                            org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler(request);

                    userService.deleteActiveStructure(structureId, module, handler);
                } else {
                    unauthorized(request);
                }
            }
        });

    }

}
