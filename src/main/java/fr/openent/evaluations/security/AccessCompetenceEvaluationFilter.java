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

import fr.openent.evaluations.security.utils.FilterCompetenceEvaluationUtils;
import fr.wseduc.webutils.http.Binding;
import fr.wseduc.webutils.request.RequestUtils;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ledunoiss on 20/10/2016.
 */
public class AccessCompetenceEvaluationFilter implements ResourcesProvider {

    @Override
    public void authorize(final HttpServerRequest resourceRequest, Binding binding, final UserInfos user, final Handler<Boolean> handler) {
        switch (user.getType()) {
            case "Teacher" : {
                if (resourceRequest.params().contains("idNote")) {
                    resourceRequest.pause();
                    new FilterCompetenceEvaluationUtils()
                            .validateCompetenceNoteOwner(Integer.parseInt(resourceRequest.params().get("idNote")),
                                    user.getUserId(), new Handler<Boolean>() {
                                        @Override
                                        public void handle(Boolean isValid) {
                                            resourceRequest.resume();
                                            handler.handle(isValid);
                                        }
                                    });
                } else {
                    RequestUtils.bodyToJson(resourceRequest, new Handler<JsonObject>() {
                        public void handle(JsonObject body) {
                            JsonArray data = body.getArray("data");
                            List<Integer> idsList = new ArrayList<Integer>();
                            for (int i = 0; i < data.size(); i++) {
                                JsonObject _o = data.get(i);
                                idsList.add(_o.getInteger("id"));
                            }
                            new FilterCompetenceEvaluationUtils().validateCompetencesNotesOwner(idsList, user.getUserId(), new Handler<Boolean>() {
                                @Override
                                public void handle(Boolean isValid) {
                                    handler.handle(isValid);
                                }
                            });
                        }
                    });

                }
            }
            break;
            default: {
                handler.handle(false);
            }
        }
    }

}
