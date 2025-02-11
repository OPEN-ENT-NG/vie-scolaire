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
import fr.openent.viescolaire.security.*;
import fr.openent.viescolaire.service.EleveService;
import fr.openent.viescolaire.service.impl.DefaultEleveService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Renders;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;

import java.util.Arrays;
import java.util.List;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;


/**
 * Created by ledunoiss on 10/02/2016.
 */
public class EleveController extends ControllerHelper {

    private final EleveService eleveService;

    public EleveController(){
        pathPrefix = Viescolaire.VSCO_PATHPREFIX;
        eleveService = new DefaultEleveService();
    }

    @Get("/classe/:idClasse/eleves")
    @ApiDoc("Recupere tous les élèves d'une classe.")
    @SecuredAction(value = "", type= ActionType.RESOURCE)
    @ResourceFilter(AccessStructureMyClass.class)
    public void getEleveClasse(final HttpServerRequest request){
        String idClasse = request.params().get("idClasse");
        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

        eleveService.getEleveClasse(idClasse, handler);
    }

    @Get("/eleves")
    @ApiDoc("Récupère les informations des élèves.")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AccessIfMyStructure.class)
    public void getEleves(final HttpServerRequest request) {
        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
        String[] idEleves = request.params().getAll("idUser").toArray(new String[0]);
        String idEtablissement = request.params().get("idStructure");
        eleveService.getInfoEleve(idEleves, idEtablissement, handler);
    }

    @Get("/annotations/eleve")
    @ApiDoc("Récupère les annotations sur les devoirs d'un élève.")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AccessChildrenParentFilter.class)
    public void getAnnotationStudent(final HttpServerRequest request) {
        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
        String idEleve = request.params().get("idEleve");
        String idClasse = request.params().get("idClasse");
        Long idPeriode = null;
        if (request.params().get("idPeriode") != null) {
            idPeriode = testLongFormatParameter("idPeriode", request);
        }
        final Long fIdePeriode = idPeriode;

        if(idClasse != null) {
            eleveService.getGroups(idEleve, new Handler<Either<String, JsonArray>>() {
                @Override
                public void handle(Either<String, JsonArray> event) {
                    JsonArray idGroups = new JsonArray().add(idClasse);
                    if (event.isRight()) {
                        JsonArray values = event.right().getValue();
                        if (values.size() > 0) {
                            for (int i=0; i < values.size(); i++) {
                                JsonObject o = values.getJsonObject(i);
                                idGroups.add(o.getString("id_groupe"));
                            }
                        }
                    }

                    eleveService.getAnnotations(idEleve, fIdePeriode, idGroups, null, handler);
                }
            });
        } else {
            eleveService.getAnnotations(idEleve, fIdePeriode, null, null, handler);
        }
    }

    @Get("/competences/eleve")
    @ApiDoc("Récupère les competences-notes des devoirs d'un élève.")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AccessChildrenParentFilter.class)
    public void getCompetencesEleve(final HttpServerRequest request) {
        final String idEleve = request.params().get("idEleve");
        final String idClasse = request.params().get("idClasse");

        eleveService.getGroups(idEleve, new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> event) {
                final Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

                Long idPeriode = null;
                if (request.params().get("idPeriode") != null) {
                    idPeriode = testLongFormatParameter("idPeriode", request);
                }

                JsonArray idGroups = new JsonArray();
                if(idClasse != null){
                    idGroups.add(idClasse);
                }

                if (event.isRight()) {
                    JsonArray values = event.right().getValue();
                    if (values.size() > 0) {
                        for (int i=0; i < values.size(); i++) {
                            JsonObject o = values.getJsonObject(i);
                            idGroups.add(o.getString("id_groupe"));
                        }
                    }
                }

                Long idCycle = null;
                if (request.params().contains("idCycle")) {
                    try {
                        idCycle = Long.parseLong(request.params().get("idCycle"));
                    } catch (NumberFormatException e) {
                        log.error("Error : idCycle must be a long object ", e);
                        badRequest(request, e.getMessage());
                        return;
                    }
                }

                if(idGroups != null && !idGroups.isEmpty()) {
                    eleveService.getCompetences(idEleve, idPeriode, idGroups, idCycle, null, handler);
                } else {
                    Renders.renderJson(request, new JsonArray());
                }
            }
        });
    }

    @Get("/cycle/eleve/:idClasse")
    @ApiDoc("Récupère les competences-notes des devoirs d'un élève.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getCycleEleve(final HttpServerRequest request) {
        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
        String idClasse = request.params().get("idClasse");
        eleveService.getCycle(idClasse,handler);
    }

    @Get("appreciation/devoir/:idDevoir/eleve/:idEleve")
    @ApiDoc("Récupère l'appréciation d'un devoir pour un élève.")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AccessChildrenParentFilter.class)
    public void getAppreciationDevoirEleve(final HttpServerRequest request) {
        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
        String idEleve = request.params().get("idEleve");
        if (request.params().get("idDevoir") != null) {
            Long idDevoir = testLongFormatParameter("idDevoir", request);
            eleveService.getAppreciationDevoir(idDevoir, idEleve, handler);
        } else {
            badRequest(request, "Invalid parameter");
        }
    }

    @Get("/structures/:structureId/students")
    @ApiDoc("Get list of students from a structure")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AdminRight.class)
    public void getStudentsFromStructure(final HttpServerRequest request) {
        String structureId = request.getParam("structureId");

        Integer page = request.params().contains("page") ? Integer.parseInt(request.getParam("page")) : null;
        List<String> studentId = request.params().contains("studentId") ?
                Arrays.asList(request.getParam("studentId").split("\\s*,\\s*")) : null;

        List<String> groupName = request.params().contains("groupName") ?
                Arrays.asList(request.getParam("groupName").split("\\s*,\\s*")) : null;


        Boolean crossFilter = Boolean.valueOf(request.getParam("crossFilter"));

        if (request.params().contains("structureId")) {
            eleveService.getStudentsFromStructure(structureId,page,studentId,groupName, crossFilter, arrayResponseHandler(request));
        } else {
            badRequest(request);
        }
    }


    Long testLongFormatParameter(String name,final HttpServerRequest request) {
        Long param = null;
        try {
            param = Long.parseLong(request.params().get(name));
        } catch(NumberFormatException e) {
            log.error("Error :" +  name + " must be a long object", e);
            badRequest(request, e.getMessage());
            return null;
        }
        return param;
    }

}
