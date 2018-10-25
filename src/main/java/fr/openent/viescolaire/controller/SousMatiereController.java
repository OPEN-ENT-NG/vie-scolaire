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
import fr.openent.viescolaire.service.SousMatiereService;
import fr.openent.viescolaire.service.impl.DefaultSousMatiereService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;

/**
 * Created by ledunoiss on 18/10/2016.
 */
public class SousMatiereController extends ControllerHelper {

    private final SousMatiereService sousMatiereService;

    public SousMatiereController () {
        pathPrefix = Viescolaire.VSCO_PATHPREFIX;
        sousMatiereService = new DefaultSousMatiereService();
    }

    /**
     * Recupère les sous matières pour une matière donnée
     * @param request
     */
    @Get("/matieres/:id/sousmatieres")
    @ApiDoc("Récupère les sous matières pour une matière donnée")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void viewSousMatieres(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if(user != null){
                    Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                    sousMatiereService.listSousMatieres(request.params().get("id"), handler);
                }else{
                    unauthorized(request);
                }
            }
        });
    }

    @Get("/types/sousmatieres")
    @ApiDoc("Récupère les types de sous matières")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void listTypeSousMatieres(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if(user != null){
                    Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                    sousMatiereService.listTypeSousMatieres(handler);
                }else{
                    unauthorized(request);
                }
            }
        });
    }
}
