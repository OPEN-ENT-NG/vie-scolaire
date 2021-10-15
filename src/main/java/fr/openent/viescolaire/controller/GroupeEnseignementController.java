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
import fr.openent.viescolaire.security.*;
import fr.openent.viescolaire.service.GroupeService;
import fr.openent.viescolaire.service.impl.DefaultGroupeService;
import fr.openent.viescolaire.service.ClasseService;
import fr.openent.viescolaire.service.impl.DefaultClasseService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Renders;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.*;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.leftToResponse;

/**
 * Created by vogelmt on 13/02/2017.
 */
public class GroupeEnseignementController extends ControllerHelper {

    private final GroupeService groupeService;
    private final ClasseService classeService;
    public GroupeEnseignementController() {
        pathPrefix = Viescolaire.VSCO_PATHPREFIX;
        groupeService = new DefaultGroupeService();
        classeService = new DefaultClasseService();
    }

    /**
     * Liste les groupes d'enseignement d'un utilisateur
     * @param request
     */
    @Get("/groupe/enseignement/user")
    @ApiDoc("Liste les groupes d'enseignement d'un utilisateur")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getGroupesEnseignementUser(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if (user != null) {
                    groupeService.listGroupesEnseignementsByUserId(user.getUserId(), new Handler<Either<String, JsonArray>>() {
                        @Override
                        public void handle(Either<String, JsonArray> event) {
                            if(event.isRight()){
                                JsonArray r = event.right().getValue();
                                JsonObject groupeEnseignement, g;
                                JsonArray groupesEnseignementJsonArray = new fr.wseduc.webutils.collections.JsonArray();

                                for(int i = 0; i < r.size(); i++){
                                    JsonObject o = r.getJsonObject(i);
                                    g = o.getJsonObject("g");
                                    groupeEnseignement = g.getJsonObject("data");
                                    groupesEnseignementJsonArray.add(groupeEnseignement);
                                }

                                Renders.renderJson(request, groupesEnseignementJsonArray);
                            }else{
                                leftToResponse(request, event.left());
                            }
                        }
                    });
                } else {
                    unauthorized(request);
                }
            }
        });
    }

    /**
     * Liste les groupes d'enseignement d'un utilisateur
     * @param request
     */
    @Get("/groupe/enseignement/users/:groupId")
    @ApiDoc("Liste les groupes dse utilisateurs d'un groupe d'enseignement")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getGroupesEnseignementUsers(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if (user != null) {
                    final String groupId = request.params().get("groupId");
                    if (groupId != null && !groupId.trim().isEmpty()) {
                        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                        final String profile = request.params().get("type");
                        groupeService.listUsersByGroupeEnseignementId(groupId, profile, null, handler);
                    }else{
                        log.error("Error getGroupesEnseignementUsers : groupId can't be null ");
                        badRequest(request);
                        return;
                    }
                } else {
                    unauthorized(request);
                }
            }
        });
    }

    /**
     * get name of groupe or classe
     * @param request
     */
    @Get("/class/group/:groupId")
    @ApiDoc("get the name of a groupe or classe ")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getNameOfGroupeClasse(final HttpServerRequest request) {

        String idGroupe = request.params().get("groupId");
        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

        groupeService.getNameOfGroupeClasse(idGroupe, handler);
    }

    /**
     * get the groups from a class
     */
    @Get("/group/from/class")
    @ApiDoc("get the groups of a class")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getGroupsFromClass(final HttpServerRequest request){

        List<String> classesId = request.params().getAll("classes");
        String student = request.params().get("student");
        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

        if (student != null) {
            classeService.getGroupFromClass(classesId.toArray(new String[0]), student, handler);
        } else {
            classeService.getGroupeFromClasse(classesId.toArray(new String[0]), handler);
        }
    }

    @Get("/group/search")
    @ApiDoc("Search group from name")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(SearchRight.class)
    public void searchGroups(HttpServerRequest request) {
        if (request.params().contains("q")
                && !"".equals(request.params().get("q").trim())
                && request.params().contains("field")
                && request.params().contains("structureId")) {

            String query = request.getParam("q");
            List<String> fields = request.params().getAll("field");
            String structureId = request.getParam("structureId");

            groupeService.search(structureId, query, fields, arrayResponseHandler(request));
        } else {
            badRequest(request);
        }
    }
}
