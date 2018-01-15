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
import fr.openent.absences.service.EvenementService;
import fr.openent.absences.service.impl.DefaultEvenementService;
import fr.openent.absences.utils.EventRegister;
import fr.openent.absences.utils.Events;
import fr.openent.viescolaire.service.EventService;
import fr.openent.viescolaire.service.impl.DefaultEventService;
import fr.wseduc.rs.*;
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

import java.util.ArrayList;
import java.util.List;

import static org.entcore.common.http.response.DefaultResponseHandler.*;

/**
 * Created by ledunoiss on 25/02/2016.
 */
public class EvenementController extends ControllerHelper {

    private static final String CLASSE_ID = "classeId";

    private final EvenementService miAbscEvenementService;
    private final EventRegister eventRegister = new EventRegister();

    public EvenementController(){
        pathPrefix = Viescolaire.ABSC_PATHPREFIX;
        miAbscEvenementService = new DefaultEvenementService();
    }

    @Put("/evenement/:idEvenement/updatemotif")
    @ApiDoc("Met à jours le motif de l'évènement.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void updateMotifEvenement(final HttpServerRequest request){
        RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
            @Override
            public void handle(JsonObject event) {
                Integer piIdEvenement = Integer.parseInt(request.params().get("idEvenement"));
                Integer piIdMotif = event.getInteger("id_motif");
                Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                miAbscEvenementService.updateMotif(piIdEvenement, piIdMotif, handler);
            }
        });
    }


//    public void updateMotifEvenements(final HttpServerRequest request){
//        RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_EVENEMENTS_UPDATE_MOTIF, new Handler<JsonObject>() {
//            @Override
//            public void handle(JsonObject event) {
//                String str = request.params().get("EventsIds");
//                int[] arrayOfEventId;
//                //arrayOfEventId = Integer.parseInt(request.params().get("EventsIds"));
//
//                int idMotif = event.getInteger("MotifId");
//                Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
//
//            }
////        });
//        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
//            @Override
//            public void handle(final UserInfos user) {
//                RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_EVENEMENTS_UPDATE_MOTIF, new Handler<JsonObject>() {
//                    @Override
//                    public void handle(JsonObject poEvenement) {
//                        poEvenement.removeField("id_cours");
//                        miAbscEvenementService.createEvenement(poEvenement, user,
//                                eventRegister.getEventRegisterHandler(request, user, poEvenement, Events.CREATE_EVENEMENT.toString()));
//                    }
//                });
//            }
//        });
//    }

    @Put("/evenement")
    @ApiDoc("Met à jours l'évènement.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void updateEvenement(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_EVENEMENT_UPDATE, new Handler<JsonObject>() {
                    @Override
                    public void handle(JsonObject poEvenement) {
                        poEvenement.removeField("id_cours");
                        miAbscEvenementService.updateEvenement(poEvenement,
                                eventRegister.getEventRegisterHandler(request, user, poEvenement, Events.UPDATE_EVENEMENT.toString()));
                    }
                });
            }
        });
    }

    @Post("/evenement")
    @ApiDoc("Création d'un évènement.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void createEvenement(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_EVENEMENT_CREATE, new Handler<JsonObject>() {
                    @Override
                    public void handle(JsonObject poEvenement) {
                        poEvenement.removeField("id_cours");
                        miAbscEvenementService.createEvenement(poEvenement, user,
                                eventRegister.getEventRegisterHandler(request, user, poEvenement, Events.CREATE_EVENEMENT.toString()));
                    }
                });
            }
        });
    }

    @Delete("/evenement")
    @ApiDoc("Supprime l'évènement.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void deleteEvenement(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                try {
                    Number oEvenementId = Long.parseLong(request.params().get("evenementId"));
                    miAbscEvenementService.deleteEvenement(oEvenementId,
                            eventRegister.getEventRegisterHandler(request, user, new JsonObject().putNumber("id", oEvenementId), Events.DELETE_EVENEMENT.toString()));
                } catch (ClassCastException e) {
                    log.error("Cannot cast evenementId to Number on delete evenement : " + e);
                    badRequest(request);
                }
            }
        });

    }

    @Get("/observations/:dateDebut/:dateFin")
    @ApiDoc("Recupere toutes les observations dans une période donnée")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getObservations(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if(request.params().isEmpty()) {
                    badRequest(request);
                } else {
                    String idEtablissement = request.params().get("idEtablissement");
                    String psDateDebut = request.params().get("dateDebut") + " 00:00:00";
                    String psDateFin = request.params().get("dateFin") + " " + " 18:00:00";

                    Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

                    miAbscEvenementService.getObservations(idEtablissement, psDateDebut, psDateFin, handler);
                }
            }
        });
    }

    @Get("/precedentes/cours/:coursId/:isTeacher")
    @ApiDoc("Recupere toutes les absences du cours précédent en fonction de l'identifiant de la classe et de l'identifiant du cours")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getAbsencesDernierCours(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                Integer psCoursId = Integer.parseInt(request.params().get("coursId"));
                Boolean pbTeacher = Boolean.getBoolean(request.params().get("isTeacher"));

                Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

                miAbscEvenementService.getAbsencesDernierCours( psCoursId, pbTeacher, handler);
            }
        });
    }

    @Get("/evenement/classe/:classeId/periode/:dateDebut/:dateFin")
    @ApiDoc("Recupere tous les évènements pour une classe donnée dans une période donnée")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getEvtClassePeriode(final HttpServerRequest request){
        String iClasseId = request.params().get(CLASSE_ID);
        String oDateDebut = request.params().get("dateDebut")+" 00:00:00";
        String oDateFin = request.params().get("dateFin")+" 23:59:59";

        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
        miAbscEvenementService.getEvtClassePeriode(iClasseId, oDateDebut, oDateFin, handler);
    }
}
