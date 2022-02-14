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
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

/**
 * Created by ledunoiss on 18/10/2016.
 */
public interface MatiereService extends CrudService {

    /**
     * Liste les matières pour un élève donné
     * @param userId identifiant de l'élève
     * @param handler handler portant le résultat de la requête
     */
    void listMatieresEleve(String userId, Handler<Either<String, JsonArray>> handler);

    void listMatieres(String structureId, JsonArray aIdEnseignant, JsonArray aIdMatiere, JsonArray aIdGroupe, Handler<Either<String, JsonArray>> result);

    void listAllMatieres(String structureId, String idEnseignant, Boolean onlyId, Handler<Either<String, JsonArray>> handler);

        /**
         * Récupère les enseignants en fonction d'une liste de matières données
         * @param classesFieldOfStudy Liste de matières
         * @param result handler portant le résulat de la requête
         */
    void getEnseignantsMatieres(ArrayList<String> classesFieldOfStudy, Handler<Either<String, JsonArray>> result);

    /**
     * Récupére les matières de l'établissment de l'utilisateurs (chef Etab)
     * @param onlyId
     * @param handler
     */
   void listMatieresEtab(String idStructure, Boolean onlyId, Handler<Either<String, JsonArray>> handler );

    /**
     * Recupère les matières de l'établissement avec ses sous matières
     * @param structureId
     * @param onlyId
     * @param handler
     */
    void listMatieresEtabWithSousMatiere(String structureId, Boolean onlyId,
                                         Handler<Either<String, JsonArray>> handler );

    /**
     * Fetch all Subjects and TimetableSubjects
     *
     * @param subjectIds list of Subjects and TimetableSubjects identifier fetched to process
     * @param result     handling data result
     */
    void getSubjectsAndTimetableSubjects(JsonArray subjectIds, Handler<Either<String, JsonArray>> result);

    /**
     * Fetch all Subjects and TimetableSubjects
     *
     * @param structureId structure identifier
     * @param result     handling data result
     */
    void getSubjectsAndTimetableSubjects(String structureId, Handler<Either<String, JsonArray>> result);


    /**
     * Récupère les matieres en fonction d'une liste de matières données
     * @param idMatieres Liste de matières
     * @param result handler portant le résulat de la requête
     */
    void getMatieres(JsonArray idMatieres, Handler<Either<String, JsonArray>> result);

    /**
     * Récupère une matieres en fonction d'un id
     * @param idMatiere id de matières
     * @param result handler portant le résulat de la requête
     */
    void getMatiere(String idMatiere, Handler<Either<String, JsonObject>> result);

    /**
     * Get subjects with under subjects from the subject ids
     * @param idsSubject ids
     * @param handler answer
     */

    void subjectsListWithUnderSubjects(JsonArray idsSubject, String idStructure,
                                       Handler<Either<String,JsonArray>> handler);

    /**
     * Recupère les matières de l'établissement liées à un service évaluable (avec ses sous matières)
     * @param structureId id de structure
     * @param onlyId
     * @param handler
     */
    void matieresFilteredByServices(String structureId, Boolean onlyId,
                                         Handler<Either<String, JsonArray>> handler );
}
