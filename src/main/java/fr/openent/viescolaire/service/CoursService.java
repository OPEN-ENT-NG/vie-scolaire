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

import java.util.List;

/**
 * Created by ledunoiss on 10/02/2016.
 */
public interface CoursService extends CrudService{
    /**
     * Récupération des cours d'une classe en fonction d'une date de début et d'une date de fin.
     * @param pSDateDebut Date de début de la période
     * @param pSDateFin Date de fin de la période
     * @param idClasse Identifiant de la classe
     * @param handler Handler de retour
     */
    void getClasseCours(String pSDateDebut, String pSDateFin, String idClasse,
                        Handler<Either<String, JsonArray>> handler);

    /**
     * Récupération des cours d'une classe en fonction d'une date de début ou d'une date de fin.
     * @param pSDateDebut
     * @param pSDateFin
     * @param idClasse
     * @param handler
     */
    void getClasseCoursBytime(String pSDateDebut, String pSDateFin, String idClasse,
                              Handler<Either<String, JsonArray>> handler);
    /**
     * Récupération des cours d'un tableau de classes en fonction d'une date de début et d'une date de fin.
     * @param pSDateDebut
     * @param pSDateFin
     * @param idClasse
     * @param handler
     */
    void getCoursByStudentId(String pSDateDebut, String pSDateFin, String[] idClasse,
                             Handler<Either<String, JsonArray>> handler);

    /**
     * Récupération des cours d'un enseignant en fonction d'une date de début et d'une date de fin.
     * @param pSDateDebut Date de début de la période
     * @param pSDateFin Date de fin de la période
     * @param psUserId Identifiant de l'enseignant
     * @param structureId Identifiant de l'établissement en cours
     * @param handler Handler de retour
     */
    void getCoursByUserId(String pSDateDebut, String pSDateFin, String psUserId, String structureId,
                          Handler<Either<String, JsonArray>> handler);

    /**
     * Recupere les cours en fonction de la liste d'id passee en parametre
     * @param idCours   Liste des id
     * @param handler   Handler de retour
     */
    public void getCoursById(List<Long> idCours, Handler<Either<String, JsonArray>> handler);
}
