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

import java.util.*;
import java.util.stream.Collectors;

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
                            classeService.getEleveClasse(idClasse,null, handler);
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

                        classeService.getEleveClasses(idEtablissement, idClasseArray, null, isTeacher, handler);
                    }
                } else {
                    unauthorized(request);
                }
            }
        });
    }

    private static Boolean getBoolean(final HttpServerRequest request, String param) {
        Boolean value;
        if(request.params().get(param) != null) {
            value = Boolean.parseBoolean(request.params().get(param));
        } else {
            value = false;
        }

        return value;
    }
    @Get("/classes")
    @ApiDoc("Retourne les classes de l'établissement")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getClasses(final HttpServerRequest request) {

        UserUtils.getUserInfos(eb, request, user -> {

            if (!(user != null && !request.params().isEmpty() && request.params().contains("idEtablissement"))) {
                badRequest(request, "getClasses : Paramètre manquant iEtablissement ou Utilisateur null.");
            }
            else {
                String idEtablissement = request.params().get("idEtablissement");
                final boolean isPresence = getBoolean(request, "isPresence");
                final boolean isEdt = getBoolean(request,"isEdt");
                final boolean isTeacherEdt = getBoolean(request,"isTeacherEdt");
                final boolean noCompetence = getBoolean(request,"noCompetence");
                Map<String, JsonArray> info = new HashMap<>();
                Boolean classOnly = null;
                if(request.params().get("classOnly") != null) {
                    classOnly = Boolean.parseBoolean(request.params().get("classOnly"));
                }

                // On rajoute les info des cycles de chaque classe si !(isPresence || isEdt || noCompetence)
                Handler<Either<String, JsonArray>> finalHandler = classeService.addCycleClasses(request, eb,
                        idEtablissement, isPresence, isEdt, isTeacherEdt, noCompetence, info, classOnly );

                // Handler qui va contenir la réponse de l'API
                Handler<Either<String, JsonArray>> classeHandler;


                if (isPresence || isEdt || noCompetence) {
                    classeHandler = finalHandler;
                }

                // Si !(isPresence || isEdt || noCompetence)
                // On aurant en plus les services paramétrés pour chaque classe
                else {
                    classeHandler = classeService.addServivesClasses(request, eb, idEtablissement, isPresence, isEdt,
                            isTeacherEdt, noCompetence, info, classOnly, user, finalHandler );
                }
                String forAdminStr = request.params().get("forAdmin");
                Boolean forAdmin = (forAdminStr == null)?false:Boolean.valueOf(forAdminStr);
                classeService.listClasses(idEtablissement, classOnly, user, null, forAdmin,
                        classeHandler, isTeacherEdt);
            }
        });
    }
}
