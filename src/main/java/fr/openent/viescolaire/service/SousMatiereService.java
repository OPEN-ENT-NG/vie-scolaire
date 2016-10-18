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

package fr.openent.viescolaire.service;

import fr.wseduc.webutils.Either;
import org.entcore.common.service.CrudService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;

/**
 * Created by ledunoiss on 18/10/2016.
 */
public interface SousMatiereService extends CrudService {

    /**
     * Liste les sous matière d'une matière donnée
     * @param id identifiant de la matière
     * @param handler handler portant la résultat de la requête
     */
    public void listSousMatieres(String id, Handler<Either<String, JsonArray>> handler);

    /**
     * Recupère les sous matières en fonction d'un tableau d'id de matière
     * @param ids tableau d'identifiants de matières
     * @param handler handler portant le résultat de la requête
     */
    public void getSousMatiereById(String[] ids, Handler<Either<String, JsonArray>> handler);

}
