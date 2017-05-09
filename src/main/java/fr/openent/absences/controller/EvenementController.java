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
import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.request.RequestUtils;
import fr.openent.absences.service.impl.DefaultEvenementService;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.entcore.common.http.response.DefaultResponseHandler.*;

/**
 * Created by ledunoiss on 25/02/2016.
 */
public class EvenementController extends ControllerHelper {

    private final String ABSC_EVENEMENT_SCHEMA = "";

    private final EvenementService miAbscEvenementService;
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

    @Put("/evenement")
    @ApiDoc("Met à jours l'évènement.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void updateEvenement(final HttpServerRequest request){
        RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_EVENEMENT_UPDATE, new Handler<JsonObject>() {
            @Override
            public void handle(JsonObject poEvenement) {
                Handler<Either<String, JsonObject>> handler = notEmptyResponseHandler(request);
                poEvenement.removeField("id_cours");
                poEvenement.removeField("timestamp_arrive");
                poEvenement.removeField("timestamp_depart");
                miAbscEvenementService.updateEvenement(poEvenement,
                        handler);
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
                        poEvenement.removeField("timestamp_arrive");
                        poEvenement.removeField("timestamp_depart");
                        miAbscEvenementService.createEvenement(poEvenement, user, defaultResponseHandler(request));
                    }
                });
            }
        });
    }

    @Delete("/evenement")
    @ApiDoc("Supprime l'évènement.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void deleteEvenement(final HttpServerRequest request){
        String oEvenementId = request.params().get("evenementId");
        miAbscEvenementService.deleteEvenement(Integer.parseInt(oEvenementId), defaultResponseHandler(request));
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
                    String psDateFin = request.params().get("dateFin") + " " + new SimpleDateFormat("HH:mm:ss").format(new Date());

                    Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

                    miAbscEvenementService.getObservations(idEtablissement, psDateDebut, psDateFin, handler);
                }
            }
        });
    }

    @Get("/evenement/classe/:classeId/cours/:coursId")
    @ApiDoc("Recupere tous les évènements d'une classe sur un cours donné")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getEvenementClasseCours(final HttpServerRequest request){
        String psClasseId = request.params().get("classeId");
        String psCoursId = request.params().get("coursId");

        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

        miAbscEvenementService.getEvenementClasseCours(psClasseId, psCoursId, handler);
    }

    @Get("/precedentes/classe/:classeId/cours/:coursId")
    @ApiDoc("Recupere toutes les absences du cours précédent en fonction de l'identifiant de la classe et de l'identifiant du cours")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getAbsencesDernierCours(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                String psClasseId = request.params().get("classeId");
                Integer psCoursId = Integer.parseInt(request.params().get("coursId"));
                String psUserId = user.getUserId();

                Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

                miAbscEvenementService.getAbsencesDernierCours(psUserId, psClasseId, psCoursId, handler);
            }
        });
    }

    @Get("/evenement/classe/:classeId/periode/:dateDebut/:dateFin")
    @ApiDoc("Recupere tous les évènements pour une classe donnée dans une période donnée")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getEvtClassePeriode(final HttpServerRequest request){
        String iClasseId = request.params().get("classeId");
        String oDateDebut = request.params().get("dateDebut")+" 00:00:00";
        String oDateFin = request.params().get("dateFin")+" 23:59:59";

        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
        miAbscEvenementService.getEvtClassePeriode(iClasseId, oDateDebut, oDateFin, handler);
    }
}
