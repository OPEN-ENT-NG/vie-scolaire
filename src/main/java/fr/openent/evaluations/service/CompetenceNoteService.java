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

package fr.openent.evaluations.service;

import fr.wseduc.webutils.Either;
import org.entcore.common.service.CrudService;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.List;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public interface CompetenceNoteService extends CrudService {

    /**
     * Créer une compétenceNote
     * @param competenceNote objet contenant les informations relatives à la compétenceNote
     * @param user utilisateur courant
     * @param handler handler portant le résultat de la requête
     */
    public void createCompetenceNote(JsonObject competenceNote, UserInfos user, Handler<Either<String, JsonObject>> handler);

    /**
     * Met à jour une compétence note
     * @param id identifiant de la compétence note à mettre à jour
     * @param competenceNote objet contenant les informations relatives à la compétenceNote
     * @param user utilisateur courant
     * @param handler handler portant le résultat de la requête
     */
    public void updateCompetenceNote(String id, JsonObject competenceNote, UserInfos user, Handler<Either<String, JsonObject>> handler);

    /**
     * Supprimer une compétence Note
     * @param id identifiant de la compétence note à supprimer
     * @param handler handler portant le résultat de la requête
     */
    public void deleteCompetenceNote(String id, Handler<Either<String, JsonObject>> handler);

    /**
     * Recupère toutes les notes des compétences pour un devoir donné et un élève donné
     * @param idDevoir identifiant du devoir
     * @param idEleve identifiant de l'élève
     * @param handler handler portant le résultat de la requête
     */
    public void getCompetencesNotes(Long idDevoir, String idEleve, Handler<Either<String, JsonArray>> handler);

    /**
     * Retourne toutes les notes des compétences pour un devoir donné
     * @param idDevoir identifiant du devoir
     * @param handler handler portant le résultat de la requête
     */
    public void getCompetencesNotesDevoir(Long idDevoir, Handler<Either<String, JsonArray>> handler);

    /**
     * Met à jour une liste de compétences notes pour un devoir donné
     * @param _datas liste des compétences notes à mettre à jour
     * @param handler handler portant le résultat de la requête
     */
    public void updateCompetencesNotesDevoir(JsonArray _datas, Handler<Either< String, JsonArray >> handler);

    /**
     * Créer une liste de compétences notes pour un devoir donné
     * @param _datas liste des compétences notes à créer
     * @param handler handler portant le résultat de la requête
     */
    public void createCompetencesNotesDevoir(JsonArray _datas, UserInfos user, Handler<Either<String, JsonArray>> handler);

    /**
     * Supprimer une liste de compétences notes
     * @param ids liste d'identifiant à supprimer
     * @param handler handler portant le résultat de la requête
     */
    public void dropCompetencesNotesDevoir(List<String> ids, Handler<Either<String, JsonArray>> handler);

    /**
     * Récupère toutes les compétences notes d'un élève
     * @param idEleve identifiant de l'élève
     * @param handler handler portant le résultat de la requête
     */
    public void getCompetencesNotesEleve(String idEleve, String idPeriode, Handler<Either<String,JsonArray>> handler);
}
