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

package fr.openent.viescolaire.service.impl;

import fr.openent.viescolaire.service.EventService;
import org.entcore.common.user.UserInfos;
import io.vertx.core.json.JsonObject;
import fr.wseduc.mongodb.MongoDb;

import java.sql.Timestamp;

/**
 * Created by ledunoiss on 02/05/2017.
 */
public class DefaultEventService implements EventService {

    private final String TRACE_COLLECTION = "vsco.events";
    private final MongoDb mongo;

    public DefaultEventService() {
        this.mongo = MongoDb.getInstance();
    }

    @Override
    public void add(UserInfos user, Number idRessource, JsonObject ressource, String event) {
        JsonObject trace = new JsonObject();
        trace.put("user", formatUser(user))
                .put("ressource_id", idRessource)
                .put("event", event)
                .put("ressource", ressource)
                .put("date", new Timestamp(System.currentTimeMillis()).toString());

        mongo.insert(TRACE_COLLECTION, trace);
    }

    @Override
    public void add(JsonObject formattedUser, Number idRessource, JsonObject ressource, String event) {
        JsonObject trace = new JsonObject();
        trace.put("user", formattedUser)
                .put("ressource_id", idRessource)
                .put("event", event)
                .put("ressource", ressource)
                .put("date", new Timestamp(System.currentTimeMillis()).toString());

        mongo.insert(TRACE_COLLECTION, trace);
    }

    private JsonObject formatUser (UserInfos user) {
        JsonObject u = new JsonObject();
        return  u.put("id", user.getUserId())
                .put("firstName", user.getFirstName())
                .put("lastName", user.getLastName())
                .put("type", user.getType());
    }
}
