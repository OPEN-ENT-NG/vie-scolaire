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

import fr.wseduc.webutils.Either;
import org.entcore.common.service.CrudService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;

import java.util.List;

/**
 * Created by vogelmt on 13/02/2017.
 */
public interface GroupeService extends CrudService {

    /**
     * Liste les groupes d'enseignement pour un élève donné
     * @param userId identifiant de l'utilisateur
     * @param handler handler portant le résultat de la requête
     */
    public void listGroupesEnseignementsByUserId(String userId, Handler<Either<String, JsonArray>> handler);

    /**
     * Récupère les classes des groupes passes en parametre
     *
     * @param idGroupes Identifiant des groupes
     * @param handler  Handler portant le résultat de la requête
     */
    public void getClasseGroupe(String[] idGroupes, Handler<Either<String, JsonArray>> handler);

    /**
     * Liste les élèves pour un groupe d'ensignement donné
     * @param groupeEnseignementId
     * @param idPeriode
     * @param handler
     */
    public void listUsersByGroupeEnseignementId(String groupeEnseignementId,String profile,
                                                Long idPeriode,
                                                Handler<Either<String, JsonArray>> handler);

    /**
     * get name of classe or groupe
     * @param idGroupe id of classe or groupe
     * @param handler
     */
    public void getNameOfGroupeClasse(String idGroupe, Handler<Either<String, JsonArray>> handler);

    /**
     * Search for group based on given name. Search for group in classes, functional groups and manual groups
     * @param structure_id Structure identifier
     * @param query query matcher
     * @param fields field matcher
     * @param handler Function handler returning data
     */
    void search(String structure_id, String query, List<String> fields, Handler<Either<String, JsonArray>> handler);

    public void getTypesOfGroup(JsonArray groupsIds, Handler<Either<String, JsonArray>> handler);
}
