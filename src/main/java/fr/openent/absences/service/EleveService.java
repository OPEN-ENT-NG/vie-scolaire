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
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;

import java.util.Date;
import java.util.List;

public interface EleveService extends CrudService {

    /**
     * Récupération des évenements (absence/retard) d'un élève pour une période donnée
     * @param psIdEleve identifiant de l'élève
     * @param psDateDebut date de debut
     * @param psDateFin date de fin
     * @param handler Handler de retour
     */
    public void getEvenements(String psIdEleve, String psDateDebut, String psDateFin,
                              Handler<Either<String, JsonArray>> handler);


    /**
     * Réccupération des absences prévisionnelles d'un élève
     * @param psIdEleve identifiant de l'élève
     * @param handler Handler de retour
     */
    public void getAbsencesPrev(String psIdEleve, Handler<Either<String, JsonArray>> handler);

    /**
     * Réccupération des absences prévisionnelles d'un élève pour une période donnée
     * @param psIdEleve identifiant de l'élève
     * @param psDateDebut date de debut
     * @param psDateFin date de fin
     * @param handler Handler de retour
     */
    public void getAbsencesPrev(String psIdEleve, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler);

    /**
     * Récupérationldes absences de tous les élèves sur une période donnée
     * @param psIdEtablissement identifiant de l'établissement concerné
     * @param psDateDebut date de début de la période
     * @param psDateFin date de fin de la période
     * @param handler handler portant le résultat de la requête
     */
    public void getAbsences(String psIdEtablissement, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler);

    /**
     * Récupération les absences sans motifs de tous les élèves sur une période donnée
     * @param psIdEtablissement identifiant de l'établissement concerné
     * @param psDateDebut date de début de la période
     * @param psDateFin date de fin de la période
     * @param handler handler portant le résultat de la requête
     */
    public void getAbsencesSansMotifs(String psIdEtablissement, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler);

    /**
     * Recupere toutes les absences prévisionnelle pour une classe donnée dans une période donnée
     * @param idEleves liste d'identifiants d'élèves
     * @param psDateDebut date de début de la période
     * @param psDateFin date de fin de la période
     * @param handler handler portant le résultat de la requête
     */
    public void getAbsencesPrevClassePeriode(List<String> idEleves, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler);

    /**
     * Récupère toutes les absences d'un élève et les ordonnes par la date de début
     * @param idEleve
     * @param handler
     */
    public void getAllAbsenceEleve(String idEleve, boolean isAscending, Handler<Either<String, JsonArray>> handler);

    /**
     *  Enregistre la zone d'absence indiquée, créer eventuellement une absence prev, des evenements, des appels
     * @param idUser
     * @param idEleve
     * @param idMotif
     * @param arrayAbscPrevToCreate
     * @param arrayAbscPrevToUpdate
     * @param arrayAbscPrevToDelete
     * @param listEventIdToUpdate
     * @param arrayEventToCreate
     * @param handler
     */
    void saveZoneAbsence(final String idUser, final String idEleve, final Integer idMotif,
                         final JsonArray arrayAbscPrevToCreate,
                         final JsonArray arrayAbscPrevToUpdate,
                         final JsonArray arrayAbscPrevToDelete,
                         final List<Integer> listEventIdToUpdate,
                    final JsonArray arrayEventToCreate, final JsonArray arrayCoursToCreate, final Handler<Either<String, JsonArray>> handler);
}
