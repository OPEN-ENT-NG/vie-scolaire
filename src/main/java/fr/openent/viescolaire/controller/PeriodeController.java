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
import fr.openent.viescolaire.service.PeriodeService;
import fr.openent.viescolaire.service.impl.DefaultPeriodeService;
import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.request.RequestUtils;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;

/**
 * Created by ledunoiss on 18/10/2016.
 */
public class PeriodeController extends ControllerHelper {

    private final PeriodeService periodeService;

    public PeriodeController () {
        pathPrefix = Viescolaire.VSCO_PATHPREFIX;
        periodeService = new DefaultPeriodeService();
    }

    @Get("/periodes")
    @ApiDoc("Retourne les periodes en fonction des paramètres passés")
    @SecuredAction(value="", type = ActionType.AUTHENTICATED)
    public void getPeriodes(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if (user != null) {
                    final String idEtablissement = request.params().get("idEtablissement");
                    final String[] idGroupes = request.params().getAll("idGroupe").toArray(new String[0]);

                    if (idEtablissement == null && idGroupes.length == 0) {
                        badRequest(request);
                        log.error("getPeriodes : incorrect parameter");
                    }

                    periodeService.getPeriodes(idEtablissement, idGroupes, arrayResponseHandler(request));
                } else {
                    unauthorized(request);
                }
            }
        });
    }

    @Get("/periodes/types")
    @ApiDoc("Retourne l'ensemble des types de periodes")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getTypePeriodes(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if (user != null) {
                    periodeService.getTypePeriodes(arrayResponseHandler(request));
                } else {
                    unauthorized(request);
                }
            }
        });
    }

    @Get("/periodes/eval")
    @ApiDoc("Informe si les periodes passes en parametres possedent des evaluations")
    @SecuredAction(value="", type = ActionType.AUTHENTICATED)
    public void getPeriodesEval(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if (user != null) {
                    final Long[] idPeriodes = request.params().getAll("idPeriode").toArray(new Long[0]);

                    if (idPeriodes.length == 0) {
                        badRequest(request);
                        log.error("getPeriodesEval : incorrect parameter");
                    }

                    periodeService.checkEvalOnPeriode(idPeriodes, defaultResponseHandler(request));
                } else {
                    unauthorized(request);
                }
            }
        });
    }

    @Put("/periodes")
    @ApiDoc("Met à jour des periodes")
    @SecuredAction(value="", type = ActionType.AUTHENTICATED)
    public void updatePeriodes(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if (user != null) {
                    RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
                        @Override
                        public void handle(JsonObject resource) {

                            final String idEtablissement = resource.getString("idEtablissement");
                            final String[] idClasses = (String[]) resource.getArray("idClasses").toArray();
                            final JsonObject[] periodes = (JsonObject[]) resource.getArray("periodes").toArray();

                            if (idEtablissement == null || idClasses == null || idClasses.length == 0) {
                                badRequest(request);
                                log.error("updatePeriodes : incorrect parameter");
                            }

                            periodeService.updatePeriodes(idEtablissement, idClasses, periodes, arrayResponseHandler(request));
                        }
                    });
                }else {
                    log.debug("User not found in session.");
                    Renders.unauthorized(request);
                }
            }
        });
    }

    @Post("/periodes")
    @ApiDoc("Cree des periodes")
    @SecuredAction(value="", type = ActionType.AUTHENTICATED)
    public void createPeriodes(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null){
                    RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
                        @Override
                        public void handle(JsonObject resource) {

                            final String idEtablissement = resource.getString("idEtablissement");
                            final String[] idClasses = (String[]) resource.getArray("idClasses").toArray();
                            final JsonObject[] periodes = (JsonObject[]) resource.getArray("periodes").toArray();

                            if (idEtablissement == null || idClasses == null || idClasses.length == 0) {
                                badRequest(request);
                                log.error("createPeriodes : incorrect parameter");
                            }


                        }
                    });
                }else {
                    log.debug("User not found in session.");
                    Renders.unauthorized(request);
                }
            }
        });
    }

    @Delete("/periodes/:idPeriode")
    @ApiDoc("Supprime les periodes")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void deletePeriodes(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos userInfos) {
                if (userInfos != null) {
                    String[] idDevoirsString = request.params().getAll("idDevoir").toArray(new String[0]);
                    List<Long> idDevoirs = new ArrayList<>(idDevoirsString.length);
                    for(String idDevoirString : idDevoirsString) {
                        idDevoirs.add(Long.valueOf(idDevoirString));
                    }

                    periodeService.deletePeriodes(idDevoirs.toArray(new Long[0]), arrayResponseHandler(request));
                } else {
                    log.debug("User not found in session.");
                    Renders.unauthorized(request);
                }
            }
        });
    }
}
