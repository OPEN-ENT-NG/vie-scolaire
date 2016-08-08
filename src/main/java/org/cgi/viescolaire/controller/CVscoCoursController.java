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

package org.cgi.viescolaire.controller;

import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import org.cgi.Viescolaire;
import org.cgi.viescolaire.service.IVscoCoursService;
import org.cgi.viescolaire.service.impl.CVscoCoursService;
import org.entcore.common.controller.ControllerHelper;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;

import static org.entcore.common.http.response.DefaultResponseHandler.*;

/**
 * Created by ledunoiss on 10/02/2016.
 */
public class CVscoCoursController extends ControllerHelper{
    private final IVscoCoursService iVscoCoursService;
    public CVscoCoursController(){
        pathPrefix = Viescolaire.VSCO_PATHPREFIX;
        iVscoCoursService = new CVscoCoursService();
    }

    // TODO : Ajouter le filtre
    @Get("/:idClasse/cours/:dateDebut/:dateFin")
    @ApiDoc("Recupere tous les cours d'une classe dans une période donnée.")
    @SecuredAction(value="", type= ActionType.AUTHENTICATED)
    public void getClasseCours(final HttpServerRequest request){
        String idClasse = request.params().get("idClasse");
        String dateDebut= request.params().get("dateDebut")+" 00:00:00";
        String dateFin= request.params().get("dateFin")+" 23:59:59";

        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

        iVscoCoursService.getClasseCours(dateDebut, dateFin, idClasse, handler);
    }

    @Get("/enseignant/:userId/cours/:dateDebut/:dateFin")
    @ApiDoc("Récupère tous les cours d'un utilisateur dans une période donnée.")
    @SecuredAction(value="", type= ActionType.AUTHENTICATED)
    public void getCoursByUserId(final HttpServerRequest request){
        String userId = request.params().get("userId");
        String dateDebut= request.params().get("dateDebut");
        String dateFin= request.params().get("dateFin");

        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

        iVscoCoursService.getCoursByUserId(dateDebut, dateFin, userId, handler);
    }

}
