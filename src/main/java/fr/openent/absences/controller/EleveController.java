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
import fr.openent.absences.service.EleveService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.openent.absences.service.impl.DefaultEleveService;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;

import java.util.List;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;

public class EleveController extends ControllerHelper {

    /**
     * Service relatif a des opérations concernant les élèves
     */
    private final EleveService miAbscEleveService;


    public EleveController(){
        pathPrefix = Viescolaire.ABSC_PATHPREFIX;
        miAbscEleveService = new DefaultEleveService();
    }

    @Get("/eleve/:idEleve/evenements/:dateDebut/:dateFin")
    @ApiDoc("Recupere tous le evenements d'un eleve sur une periode")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getEvenements(final HttpServerRequest request){
        String sIdEleve = request.params().get("idEleve");
        String sDateDebut = request.params().get("dateDebut");
        String sDateFin = request.params().get("dateFin");

        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

        miAbscEleveService.getEvenements(sIdEleve, sDateDebut, sDateFin, handler);
    }

    @Get("/eleve/:idEleve/absencesprev")
    @ApiDoc("Recupere tous le absences previsonnelles d'un eleve")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getAbsencesPrev(final HttpServerRequest request){
        String sIdEleve = request.params().get("idEleve");

        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

        miAbscEleveService.getAbsencesPrev(sIdEleve, handler);
    }

    @Get("/eleve/:idEleve/absencesprev/:dateDebut/:dateFin")
    @ApiDoc("Recupere tous les absences previsonnelles d'un eleve")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getAbsencesPrevInPeriod (final HttpServerRequest request){
        String sIdEleve = request.params().get("idEleve");
        String sDateDebut = request.params().get("dateDebut");
        String sDateFin = request.params().get("dateFin");

        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

        miAbscEleveService.getAbsencesPrev(sIdEleve, sDateDebut, sDateFin, handler);
    }

    @Get("/eleves/evenements/:dateDebut/:dateFin")
    @ApiDoc("Recupere toutes les absences dans une période donnée")
    @SecuredAction(value = "", type=ActionType.AUTHENTICATED)
    public void getAbsences(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                String idEtablissement = request.params().get("idEtablissement");
                String sDateDebut = request.params().get("dateDebut")+" 00:00:00";
                String sDateFin = request.params().get("dateFin")+" 23:59:59";

                Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

                miAbscEleveService.getAbsences(idEtablissement, sDateDebut, sDateFin, handler);
            }
        });
    }

    @Get("/sansmotifs/:dateDebut/:dateFin")
    @ApiDoc("Recupere toutes les absences sans motifs dans une période donnée")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getAbsencesSansMotifs(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                String idEtablissement = request.params().get("idEtablissement");
                String psDateDebut = request.params().get("dateDebut")+" 00:00:00";
                String psDateFin = request.params().get("dateFin")+" 23:59:59";

                miAbscEleveService.getAbsencesSansMotifs(idEtablissement, psDateDebut, psDateFin, handler);
            }
        });
    }

    @Get("/absencesprev/eleves")
    @ApiDoc("Recupere toutes les absences prévisionnelles pour une classe donnée dans une péiode données")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getAbsencesPrevClassePeriode(final HttpServerRequest request){
        String psDateDebut = request.params().get("dateDebut")+" 00:00:00";
        String psDateFin = request.params().get("dateFin")+" 23:59:59";
        List<String> idEleves = request.params().getAll("id_eleve");
        miAbscEleveService.getAbsencesPrevClassePeriode(idEleves, psDateDebut, psDateFin, arrayResponseHandler(request));
    }
}
