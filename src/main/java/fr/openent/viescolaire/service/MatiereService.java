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
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ledunoiss on 18/10/2016.
 */
public interface MatiereService extends CrudService {

    /**
     * Liste les matières pour un élève donné
     * @param userId identifiant de l'élève
     * @param handler handler portant le résultat de la requête
     */
    public void listMatieresEleve(String userId, Handler<Either<String, JsonArray>> handler);

    //TODO A SUPPRIMER
    public void listMatieres(String structureId, String id, JsonArray poTitulairesIdList, Handler<Either<String, JsonArray>> result);

    /**
     * Récupère les enseignants en fonction d'une liste de matières données
     * @param classesFieldOfStudy Liste de matières
     * @param result handler portant le résulat de la requête
     */
    public void getEnseignantsMatieres(ArrayList<String> classesFieldOfStudy, Handler<Either<String, JsonArray>> result);

    /**
     * Récupére les matiéres de l'établissment de l'utilisateurs (chef Etab)
     * @param user
     * @param handler
     */
   public void listMatieresEtab(String idStructure, UserInfos user, Handler<Either<String, JsonArray>> handler );

    /**
     * Récupère les matieres en fonction d'une liste de matières données
     * @param idMatieres Liste de matières
     * @param result handler portant le résulat de la requête
     */
    public void getMatieres(JsonArray idMatieres, Handler<Either<String, JsonArray>> result);

    /**
     * Récupère une matieres en fonction d'un id
     * @param idMatiere id de matières
     * @param result handler portant le résulat de la requête
     */
    public void getMatiere(String idMatiere, Handler<Either<String, JsonObject>> result);


}
