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

package fr.openent.evaluations.controller;

import fr.openent.Viescolaire;
import fr.openent.evaluations.bean.NoteDevoir;
import fr.openent.evaluations.service.UtilsService;
import fr.openent.evaluations.service.impl.DefaultUtilsService;
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
import org.entcore.common.http.response.DefaultResponseHandler;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.entcore.common.http.response.DefaultResponseHandler.*;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public class UtilsController extends ControllerHelper {

    private final UtilsService utilsService;

    public UtilsController() {
        pathPrefix = Viescolaire.EVAL_PATHPREFIX;
        utilsService = new DefaultUtilsService();
    }



    /**
     * Retourne tous les types de devoir par etablissement
     * @param request
     */
    @Get("/types")
    @ApiDoc("Retourne tous les types de devoir par etablissement")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void view(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if(user != null){
                    Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                    utilsService.listTypesDevoirsParEtablissement(request.params().get("idEtablissement"), handler);
                }else{
                    unauthorized(request);
                }
            }
        });
    }

    /**
     * Retourne la liste des enfants pour un utilisateur donné
     * @param request
     */
    @Get("/enfants")
    @ApiDoc("Retourne la liste des enfants pour un utilisateur donné")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getEnfants(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if(user != null){
                    utilsService.getEnfants(request.params().get("userId"), arrayResponseHandler(request));
                }else{
                    unauthorized(request);
                }
            }
        });
    }

    @Get("/user/list")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void list(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if (user != null) {
                    final String structureId = request.params().get("structureId");
                    final String classId = request.params().get("classId");
                    final JsonArray types = new JsonArray(request.params().getAll("profile").toArray());
                    final String groupId = request.params().get("groupId");
                    final String nameFilter = request.params().get("name");
                    final String filterActive = request.params().get("filterActive");

                    utilsService.list(structureId, classId, groupId, types, filterActive, nameFilter, user, arrayResponseHandler(request));
                } else {
                    unauthorized(request);
                }
            }
        });
    }

}
