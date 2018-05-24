/*
 * Copyright (c) Région Hauts-de-France, Département 77, CGI, 2017.
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

package fr.openent.viescolaire.service.impl;

import fr.openent.viescolaire.service.UserService;
import fr.wseduc.webutils.Either;
import org.entcore.common.user.RepositoryEvents;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by ledunoiss on 29/03/2017.
 */
public class VieScolaireRepositoryEvents implements RepositoryEvents {

    private static final Logger log = LoggerFactory.getLogger(VieScolaireRepositoryEvents.class);
    private final UserService userService;
    private EventBus eb;

    public VieScolaireRepositoryEvents(EventBus eb) {
        this.eb = eb;
        userService = new DefaultUserService(eb);
    }

    @Override
    public void exportResources(String s, String s1, JsonArray jsonArray, String s2, String s3, String s4, Handler<Boolean> handler) {
        log.info("[VieScolaireRepositoryEvents] : export resources event is not implemented");
    }

    @Override
    public void deleteGroups(JsonArray jsonArray) {
        log.info("[VieScolaireRepositoryEvents] : delete groups event is not implemented");
    }

    @Override
    public void deleteUsers(JsonArray jsonArray) {
        userService.createPersonnesSupp(jsonArray, new Handler<Either<String, JsonObject>>() {
            @Override
            public void handle(Either<String, JsonObject> event) {
                if (event.isLeft()) {
                    log.error("[VieScolaireRepositoryEvents] : An error occured when managing deleted users");
                }
            }
        });
    }
}
