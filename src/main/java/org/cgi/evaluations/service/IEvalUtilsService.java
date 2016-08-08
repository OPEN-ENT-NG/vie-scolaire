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

package org.cgi.evaluations.service;

import fr.wseduc.webutils.Either;
import org.cgi.evaluations.bean.CEvalNoteDevoir;
import org.entcore.common.service.CrudService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public interface IEvalUtilsService {
    // TODO REDECOUPER LA STRUCTURE UNE FOIS L'ARCHITECTURE DEFINIE
    /**
     * Liste les types de devoirs pour un etablissement donné
     * @param idEtablissement identifiant de l'établissement
     * @param handler handler portant le resultat de la requête
     */
    public void listTypesDevoirsParEtablissement(String idEtablissement, Handler<Either<String, JsonArray>> handler);

    /**
     * Liste les période pour un établissement donné
     * @param idEtablissement identifiant de l'établissement
     * @param handler handler portant le résultat de la requête
     */
    public void listPeriodesParEtablissement(String idEtablissement, Handler<Either<String, JsonArray>> handler);

    /**
     * Liste les matières pour un élève donné
     * @param userId identifiant de l'élève
     * @param handler handler portant le résultat de la requête
     */
    public void listMatieresEleve(String userId, Handler<Either<String, JsonArray>> handler);

    /**
     * Liste les sous matière d'une matière donnée
     * @param id identifiant de la matière
     * @param handler handler portant la résultat de la requête
     */
    public void listSousMatieres(String id, Handler<Either<String, JsonArray>> handler);

    //TODO A SUPPRIMER
    public void listMatieres(String id, Handler<Either<String, JsonArray>> result);

    /**
     *
     * @param codeMatieres
     * @param codeEtablissement
     * @param result
     */
    public void getCorrespondanceMatieres(JsonArray codeMatieres, JsonArray codeEtablissement, Handler<Either<String, JsonArray>> result);

    /**
     * Recupère les matières en fonction d'une liste d'identifiants donnée
     * @param ids identifiants
     * @param result handler portant le resultat de la requête
     */
    public void getMatiere(List<String> ids, Handler<Either<String, JsonArray>> result);

    /**
     * Recupère les informations de l'élève
     * @param id identifiant de l'élève
     * @param result handler portant le résultat de la requête
     */
    public void getInfoEleve(String id, Handler<Either<String, JsonObject>> result);

    /**
     * Récupère les enseignants en fonction d'une liste de matières données
     * @param classesFieldOfStudy Liste de matières
     * @param result handler portant le résulat de la requête
     */
    public void getEnseignantsMatieres(ArrayList<String> classesFieldOfStudy, Handler<Either<String, JsonArray>> result);

    /**
     * Récupère les enfants d'une parent donné
     * @param id identifiant du parent
     * @param handler handler portant la résultat de la requête
     */
    public void getEnfants(String id, Handler<Either<String, JsonArray>> handler);
    /**
     * Fonction de calcul générique de la moyenne
     * @param listeNoteDevoirs : contient une liste de NoteDevoir.
     * Dans le cas ou les objets seraient des moyennes, toutes les propriétés ramener sur devront
     * être à false.
     * @param diviseurM : diviseur de la moyenne. Par défaut, cette valeur est égale à 20 (optionnel).
     * @return Double : moyenne calculée
     **/
    public Double calculMoyenne(List<CEvalNoteDevoir> listeNoteDevoirs, Integer diviseurM);
    /**
     * Recupere un periode sous sa representation en BDD
     * @param idPeriode identifiant de la periode
     * @param handler handler comportant le resultat
     */
    public void getPeriode(Integer idPeriode, Handler<Either<String, JsonObject>> handler);
    /**
     * Recupere un établissemnt sous sa representation en BDD
     * @param id identifiant de l'etablissement
     * @param handler handler comportant le resultat
     */
    public void getStructure(String id, Handler<Either<String, JsonObject>> handler);
}
