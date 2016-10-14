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

package fr.openent.evaluations.service;

import fr.wseduc.webutils.Either;
import org.entcore.common.service.CrudService;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public interface DevoirService extends CrudService {

    /**
     * Créer un devoir
     * @param devoir devoir à créer
     * @param user utilisateur
     * @param handler handler portant le résultat de la requête
     */
    public void createDevoir(JsonObject devoir, UserInfos user, Handler<Either<String, JsonObject>> handler);

    /**
     * Met à jour un devoir
     * @param id Identifian du devoir
     * @param devoir Devoir à mettre à jour
     * @param user Utilisateur
     * @param handler
     */
    public void updateDevoir(String id, JsonObject devoir, UserInfos user, Handler<Either<String, JsonObject>> handler);

    /**
     * Liste des devoirs de l'utilisateur
     * @param user utilisateur
     * @param handler handler portant le résultat de la requête
     */
    public void listDevoirs(UserInfos user, Handler<Either<String, JsonArray>> handler);

    /**
     * Liste des devoirs pour un établissement, une classe, une matière et une période donnée.
     * La liste est ordonnée selon la date du devoir (du plus ancien au plus récent).
     *
     * @param idEtablissement identifiant de l'établissement
     * @param idClasse identifiant de la classe
     * @param idMatiere identifiant de la matière
     * @param idPeriode identifiant de la période
     * @param handler handler portant le résultat de la requête
     */
    void listDevoirs(String idEtablissement, String idClasse, String idMatiere, Integer idPeriode, Handler<Either<String, JsonArray>> handler);

    /**
     * Liste des devoirs publiés pour un établissement et une période donnée.
     * La liste est ordonnée selon la date du devoir (du plus ancien au plus récent).
     *
     * @param idEtablissement identifiant de l'établissement
     * @param idPeriode identifiant de la période
     * @param idUser identifant de l'utilisateur
     * @param handler handler portant le résultat de la requête
     */
    void listDevoirs(String idEtablissement, Integer idPeriode, String idUser,Handler<Either<String, JsonArray>> handler);

    /**
     * Récupère le nombre de notes en fonction du devoir pour un utilisateur donné
     * @param userId identifiant de l'utilisateur
     * @param handler handler portant le résultat de la requête
     */
    void getNbNotesDevoirs(String userId, Handler<Either<String, JsonArray>> handler);
}
