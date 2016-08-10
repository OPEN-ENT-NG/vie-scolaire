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

package org.cgi.evaluations.controller;

import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import org.cgi.Viescolaire;
import org.cgi.evaluations.service.IEvalEnseignementService;
import org.cgi.evaluations.service.impl.CEvalEnseignementServiceImpl;
import org.entcore.common.controller.ControllerHelper;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServerRequest;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public class CEvalEnseignementController extends ControllerHelper {


    /**
     * Déclaration des services
     */
    private final IEvalEnseignementService enseignementService;

    public CEvalEnseignementController() {
        pathPrefix = Viescolaire.EVAL_PATHPREFIX;
        enseignementService = new CEvalEnseignementServiceImpl(Viescolaire.EVALUATIONS_SCHEMA, Viescolaire.EVAL_ENSEIGNEMENTS_TABLE);
    }

    /**
     * Récupère la liste des enseignements
     * @param request
     */
    @Get("/enseignements")
    @ApiDoc("Recupère la liste des enseignements")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getEnseignements(final HttpServerRequest request){
        enseignementService.getEnseignements(arrayResponseHandler(request));
    }
}
