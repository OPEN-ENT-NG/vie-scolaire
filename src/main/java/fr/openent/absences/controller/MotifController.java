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

import fr.openent.absences.service.MotifService;
import fr.openent.absences.utils.EventRegister;
import fr.openent.absences.utils.Events;
import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.openent.Viescolaire;
import fr.openent.absences.service.impl.DefaultMotifService;
import fr.wseduc.webutils.request.RequestUtils;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;

/**
 * Created by ledunoiss on 23/02/2016.
 */
public class MotifController extends ControllerHelper {

    private final MotifService miAbscMotifService;
    private final EventRegister eventRegister;
    
    public MotifController(){
        pathPrefix = Viescolaire.ABSC_PATHPREFIX;
        miAbscMotifService = new DefaultMotifService();
        eventRegister = new EventRegister();
    }

    @Get("/motifs")
    @ApiDoc("Recupere tous les motifs d'absences en fonction de l'id de l'établissement")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getAbscMotifsEtablissement(final HttpServerRequest request){
        final String psIdEtablissement = request.params().get("idEtablissement");
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                miAbscMotifService.getAbscMotifsEtbablissement(psIdEtablissement, handler);
            }
        });
    }
    @Get("/motifs/categorie")
    @ApiDoc("Recupere toutes les catégories de motifs d'absences en fonction de l'id de l'établissement")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getCategorieAbscMotifsEtablissement(final HttpServerRequest request){
        final String psIdEtablissement = request.params().get("idEtablissement");
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                miAbscMotifService.getCategorieAbscMotifsEtbablissement(psIdEtablissement, handler);
            }
        });
    }
    

    @Get("/justificatifs")
    @ApiDoc("Recupere tous les justificatifs de MOTIFs en fonction de l'id de l'établissement")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getAbscJustificatifsEtablissement(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                String idEtablissement = request.params().get("idEtablissement");
                Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                miAbscMotifService.getAbscJustificatifsEtablissement(idEtablissement, handler);
            }
        });
    }

    @Post("/motif")
    @ApiDoc("Créé un motif.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void createMotifAbs(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_MOTIF_CREATE, new Handler<JsonObject>() {
                    @Override
                    public void handle(final JsonObject poMotif) {
                        miAbscMotifService.createMotifAbs(poMotif, notEmptyResponseHandler(request));
                    }
                });
            }
        });
    }

    @Put("/motif")
    @ApiDoc("Met à jour un motif.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void updateMotifAbs(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_MOTIF_UPDATE,new Handler<JsonObject>() {
                    @Override
                    public void handle(JsonObject poMotif) {
                        miAbscMotifService.updateMotifAbs(poMotif, eventRegister.getEventRegisterHandler(request, user, poMotif, Events.UPDATE_MOTIF.toString()));
                    }
                });
            }
        });
    }

    @Post("/categorieAbs")
    @ApiDoc("Créé un motif.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void createCategorieMotifAbs(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_CATEGORIE_ABS_CREATE, new Handler<JsonObject>() {
                    @Override
                    public void handle(final JsonObject poMotif) {
                        miAbscMotifService.createCategorieMotifAbs(poMotif, notEmptyResponseHandler(request));
                    }
                });
            }
        });
    }

    @Put("/categorieAbs")
    @ApiDoc("Met à jour un motif.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void updateCategorieMotifAbs(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
               RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_CATEGORIE_ABS_UPDATE,new Handler<JsonObject>() {
                    @Override
                    public void handle(JsonObject poMotif) {
                        miAbscMotifService.updateCategorieMotifAbs(poMotif, eventRegister.getEventRegisterHandler(request, user, poMotif, Events.UPDATE_MOTIF.toString()));
                    }
                });
            }
        });
    }
}
