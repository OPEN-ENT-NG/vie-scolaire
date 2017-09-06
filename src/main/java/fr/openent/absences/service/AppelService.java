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

package fr.openent.absences.service;

import fr.wseduc.webutils.Either;
import org.entcore.common.service.CrudService;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by ledunoiss on 22/02/2016.
 */
public interface AppelService extends CrudService {

    /**
     * Recupére tous les appels effectués sur un établissement dans une période donnée
     * @param psIdEtablissement identifiant de l'établissement.
     * @param psDateDebut date de début de la période.
     * @param psDateFin date de fin de la période.
     * @param handler handler portant le résultat de la requête.
     */
    public void getAppelPeriode(String psIdEtablissement, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler);

    /**
     * Recupere tous les appels non effectues sur un établissement
     * @param psIdEtablissement identifiant de l'établissement.
     * @param psDateDebut date de début de la période.
     * @param psDateFin date de fin de la période.
     * @param handler handler portant le résultat de la requête.
     */
    public void getAppelsNonEffectues(String psIdEtablissement, String psDateDebut,  String psDateFin, Handler<Either<String, JsonArray>> handler);

    /**
     * Créé un appel.
     * @param data Json Object contenant les données.
     * @param handler handler portant le résultat de la requête.
     */
    public void createAppel(JsonObject data, UserInfos user, Handler<Either<String, JsonObject>> handler);

    /**
     * Met à jour un appel.
     * @param data Json object contenant les données
     * @param handler handler portant le résultat de la requête.
     */
    public void updateAppel(JsonObject data, Handler<Either<String, JsonObject>> handler);

    /**
     * Recupere un appel grâce à l'identifiant d'un cours.
     * @param poCoursId identifiant d'un cours.
     * @param handler handler portant le résultat de la requête.
     */
    public void getAppelCours(Integer poCoursId, Handler<Either<String, JsonArray>> handler);
}