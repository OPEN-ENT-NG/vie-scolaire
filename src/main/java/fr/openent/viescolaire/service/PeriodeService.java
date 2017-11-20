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
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

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
     * Creer des periodes pour les classes passees en parametre
     *
     * @param idEtablissement identifiant de l'établissement pour lesquel les periode seront creees
     * @param idClasses       identifiants des classes pour lesquelles les periode seront creees
     * @param periodes         tableau des periodes a inserer en base
     * @param handler          handler portant le resultat de la requete
     */
    public void createPeriodes(String idEtablissement, String[] idClasses, JsonObject[] periodes,
                               Handler<Either<String, JsonArray>> handler);

    /**
     * Supprime les periodes passees en parametre
     *
     * @param idPeriodes identifiants des periodes a supprimer
     * @param handler    handler portant le resultat de la requete
     */
    public void deletePeriodes(final Long[] idPeriodes, final Handler<Either<String, JsonArray>> handler);


    /**
     * Verifie qu'aucun devoir n'existe sur les periodes passees en parametre
     *
     * @param idPeriodes identifiants des periodes a verifier
     * @param handler    handler portant le resultat de la requete : True si une des periodes possede un devoir,
     *                   false sinon, une erreur si une erreur est survenue
     */
    public void checkEvalOnPeriode(Long[] idPeriodes, Handler<Either<String, JsonObject>> handler);

    /**
     * Retourne les periodes decorees de l'id de leur type
     *
     * @param periodes periodes a completer
     * @param handler  handler portant le resultat de la requete
     */
    public void getTypePeriode(final JsonObject[] periodes, final Handler<Either<String, JsonObject[]>> handler);

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

    //
//    /**
//     * repertorie les periodes surnuméraires pour une liste de classe en fonction du type de periode
//     *
//     * @param idClasses la liste de classe a selectionner
//     * @param type      le type de periode a comparer
//     * @param handler   handler portant le resultat de la requete
//     */
//    public void findPeriodeToDelete(String[] idClasses, Long type, Handler<Either<String, JsonArray>> handler);
//
//    /**
//     * repertorie les periodes a ajouter pour une liste de classe en fonction du type de periode
//     *
//     * @param idClasses la liste de classe a selectionner
//     * @param type      le type de periode a comparer
//     * @param handler   handler portant le resultat de la requete
//     */
//    public void findPeriodeToAdd(String[] idClasses, Long type, Handler<Either<String, JsonArray>> handler);

    //    /**
//     * recupere les periodes en fonction de leur type
//     *
//     * @param nbPeriode type des periodes
//     * @param handler   handler portant le resultat de la requete
//     */
//    public void getTypePeriode(Integer nbPeriode, Handler<Either<String, JsonArray>> handler);

    //    /**
//     * les periode Groupe à partir des classe appartenant à ce dernier
//     *
//     * @param idsClasse identifiant des classes
//     * @param handler   handler portant le résultat de la requête
//     */
//    public void getPeriodeGroupe(String[] idsClasse, Handler<Either<String, JsonArray>> handler);
}
