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

package fr.openent.evaluations.security;

import fr.openent.evaluations.security.utils.FilterDevoirUtils;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Binding;
import fr.wseduc.webutils.http.Renders;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by ledunoiss on 19/10/2016.
 */
public class AccessEvaluationFilter implements ResourcesProvider {

    @Override
    public void authorize(final HttpServerRequest resourceRequest, Binding binding, final UserInfos user, final Handler<Boolean> handler) {
        switch (user.getType()) {
            case "Teacher" : {
                resourceRequest.pause();

                if (!resourceRequest.params().contains("idDevoir")) {
                    handler.handle(false);
                }
                try {
                    Long idDevoir = Long.parseLong(resourceRequest.params().get("idDevoir"));
                    new FilterDevoirUtils().validateAccessDevoir(idDevoir, user, new Handler<Boolean>() {
                        @Override
                        public void handle(Boolean isOwner) {
                            resourceRequest.resume();
                            handler.handle(isOwner);
                        }
                    });
                } catch (NumberFormatException e) {
                    resourceRequest.resume();
                    Renders.badRequest(resourceRequest, "Error : idNote must be a long object");
                }
            }
            break;
            case "Personnel" : {
                resourceRequest.pause();
                if (!resourceRequest.params().contains("idDevoir")) {
                    handler.handle(false);
                }
                resourceRequest.resume();
                handler.handle(true);
            }
            break;
            default : {
                handler.handle(false);
            }
        }
    }
}
