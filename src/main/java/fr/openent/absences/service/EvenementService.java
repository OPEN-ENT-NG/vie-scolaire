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

import java.util.List;

/**
 * Created by ledunoiss on 25/02/2016.
 */
public interface EvenementService extends CrudService {

    /**
     * Met à jours l'évènement
     * @param piIdEvenement identifiant de l'évènement.
     * @param piMotif identifiant du motif
     * @param handler handler portant le résultat de la requête.
     */
    void updateMotif(Integer piIdEvenement, Integer piMotif, Handler<Either<String, JsonArray>> handler);


    /**
     * Créé un evenement en base de données
     * @param poEvenement l'objet JSON représentant l'évenement
     * @param handler handler portant le résultat de la requête.
     */
    void createEvenement(JsonObject poEvenement, UserInfos user, Handler<Either<String, JsonObject>> handler);

    /**
     * Met à jour un évenement.
     * @param poEvenement l'objet JSON représentant l'évenement.
     * @param handler portant le résultat de la requête.
     */
    void updateEvenement(JsonObject poEvenement, Handler<Either<String, JsonObject>> handler);

    /**
     * Supprime un évenement à partir de son identifiant.
     * @param poEvenementId Identifiant de l'evenement à supprimer.
     * @param handler portant le résultat de la requête.
     */
    void deleteEvenement(Number poEvenementId, Handler<Either<String, JsonObject>> handler);

    /**
     * Récupère toutes les observations dans une période donnée.
     * @param psEtablissementId identifiant de l'établissement.
     * @param psDateDebut date de début de la période.
     * @param psDateFin date de fin de la période.
     * @param handler handler portant le résultat de la requête.
     */
    void getObservations(String psEtablissementId, String psDateDebut, String psDateFin,
                         Handler<Either<String, JsonArray>> handler);

    /**
     * Recupere les absences sur le cours précédent d'une classe ou d'un enseignant.
     * @param psCoursId identifiant du cours.
     * @param pbTeacher boolean indiquant si l'on désire les absences pour le dernier cours prodigué par l'enseignant
     *                  de psCoursId, ou pour le cours précédent de cette classe.
     * @param handler handler portant le résultat de la requête.
     */
    void getAbsencesDernierCours(Integer psCoursId, Boolean pbTeacher, Handler<Either<String, JsonArray>> handler);

    /**
     * Recupere tous les évènements pour une classe donnée sur une période donnée
     * @param piClasseId identifiant de la classe (id postgresql).
     * @param psDateDebut date de début de la période.
     * @param psDateFin date de fin de la période.
     * @param handler handler portant le résultat de la requête.
     */
    void getEvtClassePeriode(String piClasseId, String psDateDebut, String psDateFin,
                             Handler<Either<String, JsonArray>> handler);
}
