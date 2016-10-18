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
import org.vertx.java.core.json.JsonObject;

/**
 * Created by ledunoiss on 18/10/2016.
 */
public interface PeriodeService extends CrudService {

    /**
     * Liste les période pour un établissement donné
     * @param idEtablissement identifiant de l'établissement
     * @param handler handler portant le résultat de la requête
     */
    public void listPeriodesParEtablissement(String idEtablissement, Handler<Either<String, JsonArray>> handler);

    /**
     * Recupere un periode sous sa representation en BDD
     * @param idPeriode identifiant de la periode
     * @param handler handler comportant le resultat
     */
    public void getPeriode(Integer idPeriode, Handler<Either<String, JsonObject>> handler);
}
