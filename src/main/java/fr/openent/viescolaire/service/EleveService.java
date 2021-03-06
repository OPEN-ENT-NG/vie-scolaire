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
 * Created by ledunoiss on 10/02/2016.
 */
public interface EleveService extends CrudService {

    /**
     * Récupération des élèves d'une classes en fonction de son identifiant
     *
     * @param pSIdClasse Identifiant de la classe
     * @param handler    Handler de retour
     */
    void getEleveClasse(String pSIdClasse, Handler<Either<String, JsonArray>> handler);

    /**
     * Récupération des évenements (absence/retard) d'un élève pour une période donnée
     *
     * @param psIdEleve   identifiant de l'élève
     * @param psDateDebut date de debut
     * @param psDateFin   date de fin
     * @param handler     Handler de retour
     */
    void getEvenements(String psIdEleve, String psDateDebut, String psDateFin,
                       Handler<Either<String, JsonArray>> handler);

    /**
     * Récupération des élèves d'un Etablissement
     *
     * @param idEtab
     * @param handler
     */
    void getEleve(String idEtab, Handler<Either<String, JsonArray>> handler);

    /**
     * Récupération des élèves d'un Etablissement chacun avec ces classes et groupes
     *
     * @param idEtab
     * @param handler
     */
    void getEleves(String idEtab, Handler<Either<String, JsonArray>> handler);

    /**
     * Récupération des informations des responsables d'un eleve
     *
     * @param idEleve Identifiant de l'eleve
     * @param handler Handler de retour
     */
    void getResponsables(String idEleve, Handler<Either<String, JsonArray>> handler);

    /**
     * Récupération de la liste d'enseignants d'un eleve
     *
     * @param idEleve Identifiant de l'eleve
     * @param handler Handler de retour
     */
    void getEnseignants(String idEleve, Handler<Either<String, JsonArray>> handler);

    /**
     * Récupère le nom, le prénom de chacun des élèves passés en paramètre, ainsi que l'id et le nom de sa classe
     *
     * @param idEleves        tableau contenant les ids des élèves
     * @param idEtablissement id de l'etblissement des élèves
     * @param handler         Handler portant le résultat de la requête.
     */
    void getInfoEleve(String[] idEleves, String idEtablissement, Handler<Either<String, JsonArray>> handler);

    /**
     * Récupère le nom, le prénom de chacun d'un ensemble d'Id passé en paramètre
     *
     * @param idUsers
     * @param result
     */
    void getUsers(JsonArray idUsers, Handler<Either<String, JsonArray>> result);

    /**
     * Récupère les competences-notes des devoirs d'un élève.
     *
     * @param idEleve
     * @param idGroups groupes et classes
     * @param result
     */
    void getCompetences(String idEleve, Long idPeriode, JsonArray idGroups, Long idCycle, String idMatiere,
                        Handler<Either<String, JsonArray>> result);

    /**
     * Récupère les annotations sur les devoirs d'un élève.
     *
     * @param idEleve
     * @param idGroups groupes et classes
     * @param result
     */
    void getAnnotations(String idEleve, Long idPeriode, JsonArray idGroups, String idMatiere,
                        Handler<Either<String, JsonArray>> result);

    /**
     * @param idClasse
     * @param handler
     */
    void getCycle(String idClasse, Handler<Either<String, JsonArray>> handler);


    /**
     * @param idDevoir
     * @param idEleve
     * @param handler
     */
    void getAppreciationDevoir(Long idDevoir, String idEleve, Handler<Either<String, JsonArray>> handler);

    /**
     * @param idEleve
     * @param result
     */
    void getGroups(String idEleve, Handler<Either<String, JsonArray>> result);

    /**
     * Récupère les élèves supprimés de l'annuaire et stockés dans la base de donnée de viesolaire
     *
     * @param idClasse
     * @param idStructure
     * @param idEleves
     * @param idsNeo
     * @param handler
     */
    void getStoredDeletedStudent(JsonArray idClasse, String idStructure, String[] idEleves,
                                 Handler<Either<String, JsonArray>> handler);

    /**
     * Récupère les élèves supprimés de l'annuaire et stockés dans la base de donnée de viesolaire
     *
     * @param idEleve
     * @param idPeriode
     * @param idEtablissement
     * @param handler
     */
    void isEvaluableOnPeriode(String idEleve, Long idPeriode, String idEtablissement,
                              Handler<Either<String, JsonArray>> handler);

    /**
     * Récupère les responsables légaux d'un élève
     *
     * @param idEleve
     * @param handler
     */
    void getResponsable(String idEleve, Handler<Either<String, JsonArray>> handler);

    /**
     * get deleted Student for a class and deletedDate > beginningPeriode
     * @param idClass idClass
     * @param beginningPeriode date of period
     * @param handler response
     */
    void getDeletedStudentByPeriodeByClass(String idClass, String beginningPeriode, Handler<Either<String,JsonArray>> handler);


    /**
     * Get list of students from a structure
     *
     * @param structureId       structure identifier
     * @param page              page number
     * @param studentId         student identifier list
     * @param groupNames        group name list
     */
    void getStudentsFromStructure(String structureId, Integer page, List<String> studentId, List<String> groupNames,
                                  Boolean crossFilter, Handler<Either<String, JsonArray>> handler);


    /**
     * Get list of primary relatives for given students
     * @param studentIds        students identifiers
     * @param handler           Function handler returning data
     */
    void getPrimaryRelatives(JsonArray studentIds, Handler<Either<String, JsonArray>> handler);

}