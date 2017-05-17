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

/**
 * Created by ledunoiss on 19/02/2016.
 */
public interface ClasseService extends CrudService {
    /**
     * Récupère toutes les classes de l'établissement en fonction de son id Neo4j.
     * @param idEtablissement Identifiant de l'établissement.
     * @param handler Handler portant le résultat de la requête.
     */
    public void getClasseEtablissement(String idEtablissement, Handler<Either<String, JsonArray>> handler);

    /**
     * Récupère le nombre d'élève de chaque groupe dont l'id est passé en paramètre.
     * @param idGroupes Listes des identifiants de groupe.
     * @param handler Handler portant le résultat de la requête.
     */
    public void getNbElevesGroupe(JsonArray idGroupes, Handler<Either<String, JsonArray>> handler);

    /**
     * Récupère toutes les classes d'une classe donné en fonction de son id Neo4j.

     * @param idClasse
     * @param handler
     */
    public void getEleveClasse( String idClasse, Handler<Either<String, JsonArray>> handler);

    /**²
     * Récupère toutes les classes d'une Liste de classe donné en fonction de son id Neo4j.
      *@param idEtablissement
     * @param idClasse
     * @param isTeacher
     * @param handler
     */
    public void getEleveClasses(String idEtablissement, JsonArray idClasse, Boolean isTeacher,Handler<Either<String, JsonArray>> handler);

    /**
     * Recupere le nom, prenom et id de tous les eleves de toutes les classes dont l'id est passé en paramètre.
     * @param idClasses Tableau des id des classes
     * @param handler Handler portant le résultat de la requête.
     */
    public void getElevesClasses(String[] idClasses, Handler<Either<String, JsonArray>> handler);

    /**
     * Recupere les id des etablissements auxquels appartiennent les classes dont l'id est passé en paramètre.
     * @param idClasses Tableau des id des classes
     * @param handler Handler portant le résultat de la requête.
     */
    public void getEtabClasses(String[] idClasses, Handler<Either<String, JsonArray>> handler);
}
