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
import fr.openent.viescolaire.core.constants.*;
import fr.openent.viescolaire.security.*;
import fr.openent.viescolaire.service.UserService;
import fr.openent.viescolaire.service.impl.DefaultUserService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Delete;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.request.RequestUtils;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.*;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;


import java.util.List;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
/**
 * Created by ledunoiss on 08/11/2016.
 */
public class UserController extends ControllerHelper {

    private UserService userService;
    protected static final Logger log = LoggerFactory.getLogger(UserController.class);

    public UserController() {
        pathPrefix = Viescolaire.VSCO_PATHPREFIX;
        userService = new DefaultUserService(eb);
    }

    /**
     * @param request
     * @queryParam {structureId} mandatory
     */
    @Get("/user/structures/actives")
    @ApiDoc("Retourne la liste des identifiants des structures actives de l'utilisateur pour un module donné ")
    @SecuredAction(value="", type = ActionType.AUTHENTICATED)
    public void getActivedStructures(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, user -> {
            if(user != null){
                Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                final String module = request.params().get("module");
                userService.getActivesIDsStructures(user,module,handler);
            }else{
                unauthorized(request);
            }
        });
    }


    /**
     * @param request
     * @queryParam {structureId} mandatory
     */
    @Post("/user/structures/actives")
    @ApiDoc("Active un module pour une structure donnée")
    @SecuredAction(value="", type = ActionType.RESOURCE)
    @ResourceFilter(AdministratorRight.class)
    public void createActivedStructure(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
                    @Override
                    public void handle(JsonObject body) {
                        if(user != null && body.containsKey("structureId")){
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


    /**
     * @param request
     * @queryParam {structureId} mandatory
     */
    @Delete("/user/structures/actives")
    @ApiDoc("Supprime une structure active pour un module donné.")
    @SecuredAction(value="", type = ActionType.RESOURCE)
    @ResourceFilter(AdministratorRight.class)
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



    /**
     * Retourne la liste des personnels dont l'id est passe en parametre
     *
     * @param request
     */
    @Get("/teachers")
    @ApiDoc("Retourne la liste des enseignants pour un etablissement donne")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(ParamServicesRight.class)
    public void getTeachers(final HttpServerRequest request) {
        if (request.params().contains("idEtablissement")) {
            userService.getTeachers(request.params().get("idEtablissement"), arrayResponseHandler(request));
        } else {
            badRequest(request);
        }
    }

    @Get("/user/list")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AccessIfMyStructure.class)
    public void list(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, user -> {
            if (user != null) {
                final String structureId = request.params().get(Field.STRUCTUREID);
                final String profile = request.params().get(Field.PROFILE);

                userService.list(structureId, profile, arrayResponseHandler(request));
            } else {
                unauthorized(request);
            }
        });
    }

    @Get("/user/search")
    @ApiDoc("Search student through displayName, firstName and lastName")
    @SecuredAction(Viescolaire.SEARCH)
    public void search(HttpServerRequest request) {
        if (request.params().contains(Field.Q) && !"".equals(request.params().get(Field.Q).trim())
                && request.params().contains(Field.FIELD)
                && request.params().contains(Field.PROFILE)
                && request.params().contains(Field.STRUCTUREID)) {

            UserUtils.getUserInfos(eb, request, user ->
                    new SearchRight().authorize(request, null, user, isAuthorized -> {
                        if (isAuthorized.equals(Boolean.TRUE)) {
                            String query = request.getParam(Field.Q);
                            List<String> fields = request.params().getAll(Field.FIELD);
                            String profile = request.getParam(Field.PROFILE);
                            String structureId = request.getParam(Field.STRUCTUREID);
                            String userId = (WorkflowActionUtils.hasRight(user, WorkflowActionUtils.VIESCO_SEARCH_RESTRICTED)
                                    && Field.TEACHER.equals(user.getType())) ? user.getUserId() : null;
                            userService.search(structureId, userId, query, fields, profile, arrayResponseHandler(request));
                        } else {
                            unauthorized(request);
                        }
                    }));
        } else {
            badRequest(request);
        }
    }
}
