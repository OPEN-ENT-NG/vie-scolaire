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

package fr.openent.absences.controller;

import fr.openent.Viescolaire;
import fr.openent.absences.service.AppelService;
import fr.openent.absences.service.impl.DefaultAppelService;
import fr.openent.absences.utils.EventRegister;
import fr.openent.absences.utils.Events;
import fr.openent.viescolaire.service.EventService;
import fr.openent.viescolaire.service.impl.DefaultEventService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.rs.Put;
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
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;

/**
 * Created by ledunoiss on 22/02/2016.
 */
public class AppelController extends ControllerHelper {
    private final AppelService miAbscAppelService;
    private final EventRegister eventRegister = new EventRegister();

    protected static final Logger log = LoggerFactory.getLogger(AppelController.class);

    public AppelController(){
        pathPrefix = Viescolaire.ABSC_PATHPREFIX;
        miAbscAppelService = new DefaultAppelService();
    }

    @Get("/appel/cours/:coursId")
    @ApiDoc("Recupere l'appel associé à un cours")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getAppelCours(final HttpServerRequest request){
        Integer poCoursId = Integer.valueOf(request.params().get("coursId"));
        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

        miAbscAppelService.getAppelCours(poCoursId, handler);
    }

    @Get("/appels/:dateDebut/:dateFin")
    @ApiDoc("Recupere tous les appels effectués dans une période donnée")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getAppelPeriode(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                String psDateDebut = request.params().get("dateDebut")+" 00:00:00";
                String psDateFin = request.params().get("dateFin")+" "+new SimpleDateFormat("HH:mm:ss").format(new Date());
                String idEtablissement = request.params().get("idEtablissement");
                Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                miAbscAppelService.getAppelPeriode(idEtablissement,psDateDebut, psDateFin, handler);
            }
        });
    }

    @Get("/appels/noneffectues/:dateDebut/:dateFin")
    @ApiDoc("Recupere tous les appels non effectués dans une période donnée")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getAppelsNonEffectues(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                String idEtablissement = request.params().get("idEtablissement");
                String psDateDebut = request.params().get("dateDebut")+" 00:00:00";
                String psDateFin = request.params().get("dateFin")+" "+new SimpleDateFormat("HH:mm:ss").format(new Date());

                Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

                miAbscAppelService.getAppelsNonEffectues(idEtablissement, psDateDebut, psDateFin, handler);
            }
        });
    }

    @Post("/appel")
    @ApiDoc("Créé un appel.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void createAppel(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_APPEL_CREATE, new Handler<JsonObject>() {
                    @Override
                    public void handle(final JsonObject poAppel) {
                        miAbscAppelService.createAppel(poAppel, user, eventRegister.getEventRegisterHandler(request, user, poAppel, Events.CREATE_APPEL.toString()));
                    }
                });
            }
        });
    }

    @Put("/appel")
    @ApiDoc("Met à jour un appel.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void updateAppel(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_APPEL_UPDATE, new Handler<JsonObject>() {
                    @Override
                    public void handle(JsonObject poAppel) {
                        miAbscAppelService.updateAppel(poAppel, eventRegister.getEventRegisterHandler(request, user, poAppel, Events.UPDATE_APPEL.toString()));
                    }
                });
            }
        });
    }
}
