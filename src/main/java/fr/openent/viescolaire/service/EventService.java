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

package fr.openent.viescolaire.service;

import org.entcore.common.user.UserInfos;
import io.vertx.core.json.JsonObject;

public interface EventService {

    /**
     * Ajouter une trace d'un évènement dans la collection MongoDB
     * @param user utilisateur courant
     * @param idRessource identifiant de la ressource impactée
     * @param ressource ressource impactée
     * @param event Evènement
     */
    public void add(UserInfos user, Number idRessource, JsonObject ressource, String event);

    /**
     * Ajouter une trace d'un évènement dans la collection MongoDB
     * @param user objet contenant les champs id, firstName, lastName, type
     * @param idRessource identifiant de la ressource impactée
     * @param ressource ressource impactée
     * @param event Evènement
     */
    public void add(JsonObject user, Number idRessource, JsonObject ressource, String event);
}
