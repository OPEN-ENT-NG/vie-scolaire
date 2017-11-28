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
import fr.openent.evaluations.security.AccessAuthorozed;
import fr.openent.evaluations.security.AccessEvaluationFilter;
import fr.openent.evaluations.security.utils.FilterUserUtils;
import fr.openent.viescolaire.service.EleveService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.openent.viescolaire.service.impl.DefaultEleveService;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;

import java.util.List;

import static org.entcore.common.http.response.DefaultResponseHandler.*;


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
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getEleveClasse(final HttpServerRequest request){
        String idClasse = request.params().get("idClasse");
        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

        eleveService.getEleveClasse(idClasse, handler);
    }
    /**
     * récupère les éleves d'un etablissement juste avec leurs classes
     * @param request
     */
    @Get("/etab/eleves/:idEtab")
    @ApiDoc("Recupere tous les élèves d'un etablissment.")
    @ResourceFilter(AccessAuthorozed.class)
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getEleveEtab(final HttpServerRequest request){
        String idEtab = request.params().get("idEtab");
        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
        eleveService.getEleve(idEtab, handler);
    }

    /**
     * récupère les éleves d'un etablissement avec leurs classes et groupes
     * @param request
     */
    @Get("/etab/eleves/classes/groupes/:idEtab")
    @ApiDoc("Recupere tous les élèves d'un etablissment.")
    @ResourceFilter(AccessAuthorozed.class)
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getElevesEtab(final HttpServerRequest request){
        String idEtab = request.params().get("idEtab");
        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
        eleveService.getEleves(idEtab, handler);
    }

    @Get("/eleves/:idEleve/responsables")
    @ApiDoc("Recupere les relatives d'un élève.")
    @ResourceFilter(AccessAuthorozed.class)
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getResponsablesEleve(final HttpServerRequest request) {
        String idEleve = request.params().get("idEleve");
        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
        eleveService.getResponsables(idEleve, handler);
    }

    @Get("/eleve/enseignants")
    @ApiDoc("Récupère les enseingants rattaché à un élève.")
    @ResourceFilter(AccessAuthorozed.class)
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getEnseignantsEleve(final HttpServerRequest request) {
        String idEleve = request.params().get("idEleve");
        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
        eleveService.getEnseignants(idEleve, handler);
    }

    @Get("/enseignants")
    @ApiDoc("Récupère les enseingants ayant créé les devoir d'un élève.")
    @ResourceFilter(AccessAuthorozed.class)
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getUsersById(final HttpServerRequest request) {
        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
        final JsonArray idUsers = new JsonArray(request.params().getAll("idUser").toArray());
        eleveService.getUsers(idUsers,handler);
    }

}
