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
import org.entcore.common.user.UserInfos;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;

/**
 * Created by ledunoiss on 18/10/2016.
 */
public interface PeriodeService extends CrudService {

    /**
     * Retourne les periodes de l'etablissement ou des groupes passes en parametre
     *
     * @param idEtablissement   identifiant de l'etablissement
     * @param idGroupes         tableau des identifiants des groupes
     * @param handler           handler portant le resultat de la requete
     */
    public void getPeriodes(String idEtablissement, String[] idGroupes, Handler<Either<String, JsonArray>> handler);

    /**
     * Retourne le libelle d'une periode
     * @param type
     * @param ordre
     * @return
     */
    public String getLibellePeriode(Integer type, Integer ordre , HttpServerRequest request);

    /**
     *
     * @param idType
     * @param request
     * @param handler
     */
    public void getLibellePeriode(Long idType, HttpServerRequest request, Handler<Either<String, String>> handler);

    /**
     * Retourne l'ensemble des types de periode
     *
     * @param handler   handler portant le resultat de la requete
     */
    public void getTypePeriodes(Handler<Either<String, JsonArray>> handler);

    /**
     * Met a jour les periodes
     *
     * @param idEtablissement      identifiant de l'etablissement des periodes a mettre a jour
     * @param idClasses            identifiant des classes des periodes a mettre a jour
     * @param periodes              nouvelles valeurs pour les periodes
     * @param handler               handler portant le resultat de la requete (void si succes)
     */
    public void updatePeriodes(final String idEtablissement, final String[] idClasses, final JsonObject[] periodes,
                               final Handler<Either<String, JsonArray>> handler);

    /**
     * Verifie qu'aucun devoir n'existe sur les periodes passees en parametre
     *
     * @param idClasses  identifiants des classes des periodes a verifier
     * @param handler    handler portant le resultat de la requete : True si une des periodes possede un devoir,
     *                   false sinon, une erreur si une erreur est survenue
     */
    public void checkEvalOnPeriode(String[] idClasses, Handler<Either<String, JsonObject>> handler);

    /**
     * Gerenere les periode à partir des classe representees dans les groupes passes en parametre
     *
     * @param idEtablissement identifiant de l'etablissement
     * @param idGroupes identifiant des groupes
     * @param handler   handler portant le résultat de la requête
     */
    public void getPeriodesGroupe(String idEtablissement, String[] idGroupes, Handler<Either<String, JsonArray>> handler);

    /**
     * Recupere des periodes en fonction de l'etablissement et des classes desirees
     *
     * @param idEtablissement identifiant de l'etablissement
     * @param idClasses       tableau contenant les identifiants des classes
     * @param handler         handler portant le resultat de la requete
     */
    public void getPeriodesClasses(String idEtablissement, String[] idClasses,
                                   Handler<Either<String, JsonArray>> handler);

    /**
     *
     * @param idEtablissement identifiant de l'etablissement
     * @param idClasses identifiant des classes
     * @param handler handler portant pour chaque classe la date de début de la premiere periode et
     *                la date de fin de la derniere periode
     */
    public void getDatesDtFnAnneeByClasse(String idEtablissement, List<String> idClasses,
                                          Handler<Either<String, JsonArray>> handler);

    /**
     *
     * @param idPeriode identifiant de la periode
     * @param publiBulletin boolean
     * @param handler reponse
     */
    public void updatePublicationBulletin (Integer idPeriode, Boolean publiBulletin, Handler<Either<String,JsonObject>>handler);
}
