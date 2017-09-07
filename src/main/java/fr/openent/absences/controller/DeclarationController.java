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
import fr.openent.absences.service.DeclarationService;
import fr.openent.absences.service.impl.DefaultDeclarationService;
import fr.openent.absences.utils.EventRegister;
import fr.openent.absences.utils.Events;
import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.email.EmailSender;
import fr.wseduc.webutils.request.RequestUtils;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
//import org.entcore.common.notification.ConversationNotification;


import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;

public class DeclarationController extends ControllerHelper {


    /**
     * Service relatif a des opérations concernant les élèves
     */
    private final DeclarationService declarationService;
    //    private final ConversationNotification conversationNotification;
    private final EventRegister eventRegister = new EventRegister();
    private static final Logger log = LoggerFactory.getLogger(DeclarationController.class);

    public DeclarationController(EventBus eb) {
        pathPrefix = Viescolaire.ABSC_PATHPREFIX;
        declarationService = new DefaultDeclarationService();
//        conversationNotification = new ConversationNotification(vertx, eb, container);
    }

    @Get("/declarations")
    @ApiDoc("Récupère les déclarations.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getDeclaration(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                try {
                    String psEtablissementId = request.params().get("idEtablissement");

                    if (psEtablissementId == null) {
                        log.error("idEtablissement should not be null.");
                        badRequest(request);
                    }

                    String psOwnerId = request.params().get("idOwner");
                    String psStudentId = request.params().get("idStudent");
                    String psDateDebut = request.params().get("dateDebut");
                    String psDateFin = request.params().get("dateFin");
                    Boolean pbTraitee = null;
                    Integer piNumber = null;
                    String psState = request.params().get("etat");
                    if(psState != null) {
                        pbTraitee = Boolean.parseBoolean(psState);
                    }
                    String psNumber = request.params().get("number");
                    if(psNumber != null) {
                        piNumber = Integer.parseInt(psNumber);
                    }
                    declarationService.getDeclaration(psEtablissementId, psOwnerId, psStudentId, psDateDebut, psDateFin,
                            pbTraitee, piNumber, arrayResponseHandler(request));
                } catch (ClassCastException e) {
                    log.error("Cannot cast idEtat or nombre to Integer on getDeclaration : " + e);
                    badRequest(request);
                }
            }
        });
    }

    @Put("/declarations")
    @ApiDoc("Met à jours la déclaration.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void updateDeclaration(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                RequestUtils.bodyToJson(request,
                        Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_DECLARATION_UPDATE,
                        new Handler<JsonObject>() {
                            @Override
                            public void handle(JsonObject poDeclaration) {
                                declarationService.updateDeclaration(poDeclaration,
                                        eventRegister.getEventRegisterHandler(request, user, poDeclaration,
                                                Events.UPDATE_DECLARATION.toString()));
                            }
                        });
            }
        });
    }

    @Post("/declarations")
    @ApiDoc("Création d'une déclaration.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void createDeclaration(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                RequestUtils.bodyToJson(request,
                        Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_DECLARATION_CREATE,
                        new Handler<JsonObject>() {
                            @Override
                            public void handle(JsonObject poDeclaration) {
                                declarationService.createDeclaration(poDeclaration, user,
                                        eventRegister.getEventRegisterHandler(request, user, poDeclaration,
                                                Events.CREATE_DECLARATION.toString()));
                            }
                        });
            }
        });
    }

    @Delete("/declarations/:idDeclaration")
    @ApiDoc("Supprime la déclaration.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void deleteDeclaration(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                try {
                    Number oDeclarationId = Long.parseLong(request.params().get("idDeclaration"));
                    declarationService.deleteDeclaration(oDeclarationId,
                            eventRegister.getEventRegisterHandler(request, user,
                                    new JsonObject().putNumber("id", oDeclarationId),
                                    Events.DELETE_DECLARATION.toString()));
                } catch (ClassCastException e) {
                    log.error("Cannot cast idDeclaration to Number on delete declaration : " + e);
                    badRequest(request);
                }
            }
        });
    }
}
