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
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by ledunoiss on 19/02/2016.
 */
public interface ClasseService extends CrudService {
    /**
     * Récupère toutes les classes de l'établissement en fonction de son id Neo4j.
     * @param idEtablissement Identifiant de l'établissement.
     * @param handler Handler portant le résultat de la requête.
     */
    void getClasseEtablissement(String idEtablissement, Handler<Either<String, JsonArray>> handler);

    /**
     * Récupère le nombre d'élève de chaque groupe dont l'id est passé en paramètre.
     * @param idGroupes Listes des identifiants de groupe.
     * @param handler Handler portant le résultat de la requête.
     */
    void getNbElevesGroupe(JsonArray idGroupes, Handler<Either<String, JsonArray>> handler);

    /**
     * Récupère toutes les classes d'une classe donné en fonction de son id Neo4j.

     * @param idClasse
     * @param handler
     */
    void getEleveClasse( String idClasse, Long idPeriode, Handler<Either<String, JsonArray>> handler);

    /**²
     * Récupère toutes les classes d'une Liste de classe donné en fonction de son id Neo4j.
     *@param idEtablissement
     * @param idClasse
     * @param isTeacher
     * @param handler
     */
    void getEleveClasses(String idEtablissement, JsonArray idClasse,
                         Boolean isTeacher,Handler<Either<String, JsonArray>> handler);


    /**
     * Recupere la liste des classes et/ou groupes de l'etablissement
     * @param idEtablissement   identifiant de l'etablissement
     * @param classOnly         boolean indiquant si seulement les classes seront recuperees. Si null, les classes
     *                          ET les groupes seront recuperes
     * @param user              les informations du users connecte
     * @param handler           handler portant le resultat de la requete
     */
    void listClasses(String idEtablissement, Boolean classOnly, UserInfos user,
                     Handler<Either<String, JsonArray>> handler);

    /**
     * Recupere le nom, prenom et id de tous les eleves de toutes les classes dont l'id est passé en paramètre.
     * @param idClasses Tableau des id des classes
     * @param idPeriode
     * @param handler Handler portant le résultat de la requête.
     */
    void getElevesClasses(String[] idClasses,
                          Long idPeriode,
                          Handler<Either<String, JsonArray>> handler);

    /**
     * Recupere les id des etablissements auxquels appartiennent les classes dont l'id est passé en paramètre.
     * @param idClasses Tableau des id des classes
     * @param handler Handler portant le résultat de la requête.
     */
    void getEtabClasses(String[] idClasses, Handler<Either<String, JsonArray>> handler);

    /**
     *
     * @param idEtablissement
     * @param eleveId
     * @param handler
     */
    void getClasseEleve(String idEtablissement, String eleveId, Handler<Either<String, JsonArray>> handler);

    /**
     * récupère les informations d'une classe
     * @param idClasse
     * @param handler
     */
    void getClasseInfo(String idClasse, Handler<Either<String, JsonObject>> handler);

    /**
     * récupère les informations de plusieurs classes
     * @param idClasses
     * @param handler
     */
    void getClassesInfo(JsonArray idClasses, Handler<Either<String, JsonArray>> handler);

    /**
     *
     * @param idClasses
     * @param handler
     */
    void getGroupeClasse(String[] idClasses, Handler<Either<String, JsonArray>> handler);

    /**
     * récupère l'id de la classe de l'élève dont l'id est passé en paramètre
     * @param idEleve
     * @param handler
     */
    void getClasseIdByEleve(String idEleve, Handler<Either<String,JsonObject>> handler);
}
