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

import fr.openent.Viescolaire;
import fr.openent.viescolaire.core.constants.*;
import fr.openent.viescolaire.service.*;
import fr.wseduc.webutils.Either;
import org.entcore.common.user.RepositoryEvents;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


/**
 * Created by ledunoiss on 29/03/2017.
 */
public class VieScolaireRepositoryEvents implements RepositoryEvents {

    private static final Logger log = LoggerFactory.getLogger(VieScolaireRepositoryEvents.class);
    private final UserService userService;
    private final InitService initService;
    private final JsonObject config;

    public VieScolaireRepositoryEvents(ServiceFactory serviceFactory) {
        userService = serviceFactory.userService();
        initService = serviceFactory.initService();
        this.config = serviceFactory.config();
    }

    @Override
    public void exportResources(boolean exportDocuments, boolean exportSharedResources, String exportId, String userId, JsonArray groups, String exportPath, String locale, String host, Handler<Boolean> handler) {
        log.info("[VieScolaireRepositoryEvents] : export resources event is not implemented");
    }

    @Override
    public void deleteGroups(JsonArray jsonArray) {
        log.info("[VieScolaireRepositoryEvents] : delete groups event is not implemented");
    }

    @Override
    public void deleteUsers(JsonArray jsonArray) {
		if(jsonArray == null)
			return;
		for(int i = jsonArray.size(); i-- > 0;)
		{
			if(jsonArray.hasNull(i))
                jsonArray.remove(i);
            else if (jsonArray.getJsonObject(i) != null && jsonArray.getJsonObject(i).getString("id") == null)
                jsonArray.remove(i);
		}
		if(jsonArray.size() == 0)
            return;

        userService.createPersonnesSupp(jsonArray, new Handler<Either<String, JsonObject>>() {
            @Override
            public void handle(Either<String, JsonObject> event) {
                if (event.isLeft()) {
                    log.error("[VieScolaireRepositoryEvents] : An error occured when managing deleted users");
                }
                else {
                    log.info("[VieScolaireRepositoryEvents] : Stored ");
                }
            }
        });
    }

    @Override
    public void usersClassesUpdated(JsonArray users) {

        LocalDate enableDate = LocalDate.parse(Viescolaire.UPDATE_CLASSES_CONFIG.getString("enable-date"), DateTimeFormatter.ISO_LOCAL_DATE);

        LocalDate now = LocalDate.now();
        if (enableDate.isBefore(now)) {
            log.info("[VieScolaireRepositoryEvents] : usersClassesUpdated START");
            if (users != null) {
                log.info("users : " + users.size());
            }
            userService.parseUsersData(users, new Handler<Either<String, JsonArray>>() {
                @Override
                public void handle (Either<String, JsonArray> event) {
                    if (event.isRight()) {

                        JsonArray result = event.right().getValue();
                        if (result != null) {
                            log.info("resultusers : " + result.size());
                        }
                        userService.createPersonnesSupp(result, new Handler<Either<String, JsonObject>>() {
                            @Override
                            public void handle (Either<String, JsonObject> event) {
                                log.info("createPersonnesSupp OUT END");
                                if (event.isLeft()) {
                                    log.error("[VieScolaireRepositoryEvents] : An error occured when managing deleted users");
                                } else {
                                    userService.insertAnnotationsNewClasses(result, new Handler<Either<String, JsonObject>>() {
                                        @Override
                                        public void handle (Either<String, JsonObject> event) {
                                            if (event.isLeft()) {
                                                log.error("[VieScolaireRepositoryEvents] : An error occured when inserting annotations in new classes");
                                                log.error(event.left().getValue());
                                            } else {
                                                log.info("[VieScolaireRepositoryEvents] : Stored ");
                                                log.info("[VieScolaireRepositoryEvents] : usersClassesUpdated END");
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    } else {
                        log.error("[VieScolaireRepositoryEvents] : An error occured when retrieving users data");
                    }
                }
            });
        } else {
            log.info("[VieScolaireRepositoryEvents] : usersClassesUpdated is disable." +
                    "\nThe end of disability will be at " + enableDate +
                    ".\nThe date of the end of disability can be set in the conf of the module");
        }

    }

    @Override
    public void transition(JsonObject structure) {
        this.initService.setInitializationStatus(structure.getString(Field.ID), false)
                .onFailure(fail -> {
                    String message = String.format("[Viescolaire@%s::transition] An error occurred when setting " +
                            "initialization status : %s", this.getClass().getSimpleName(), fail.getMessage());
                    log.error(message);
                })
                .onSuccess(success -> log.info(String.format("[Viescolaire@%s::transition] Initialization status " +
                        "successfully set to false for structure %s", this.getClass().getSimpleName(),
                        structure.getString(Field.ID))));
    }

}
