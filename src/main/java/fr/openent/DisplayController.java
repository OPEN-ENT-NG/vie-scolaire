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

package fr.openent;

import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.SecuredAction;
import io.vertx.core.json.JsonObject;
import org.entcore.common.controller.ControllerHelper;
import io.vertx.core.http.HttpServerRequest;

public class DisplayController extends ControllerHelper {

    public DisplayController(){
        pathPrefix = "";
    }

    @Get(Viescolaire.VSCO_PATHPREFIX)
    @ApiDoc("Get Vie Scolaire HTML view")
    @SecuredAction(value = "Viescolaire.view")
    public void view(final HttpServerRequest request) {
        JsonObject services = config.getJsonObject("services", new JsonObject());
        JsonObject params = new JsonObject()
                .put("ENABLE_COMPETENCES", services.getBoolean("competences", false))
                .put("ENABLE_PRESENCES", services.getBoolean("presences", false))
                .put("ENABLE_DIARY", services.getBoolean("diary", false))
                .put("ENABLE_EDT", services.getBoolean("edt", false))
                .put("ENABLE_MASSMAILING", services.getBoolean("massmailing", false));
        renderView(request, params, "viescolaire/vsco_personnel.html", null);
    }
}