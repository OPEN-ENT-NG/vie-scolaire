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

import fr.openent.evaluations.bean.NoteDevoir;
import fr.wseduc.webutils.Either;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.List;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public interface UtilsService {
    // TODO REDECOUPER LA STRUCTURE UNE FOIS L'ARCHITECTURE DEFINIE


    /**
     * Récupère la liste des professeurs remplaçants du titulaire sur un établissement donné
     * (si lien titulaire/remplaçant toujours actif à l'instant T)
     * @param psIdTitulaire identifiant neo4j du titulaire
     * @param psIdEtablissement identifiant de l'établissement
     * @param handler handler portant le resultat de la requête : la liste des identifiants neo4j des rempacants
     */
    public void getRemplacants(String psIdTitulaire, String psIdEtablissement, Handler<Either<String, JsonArray>> handler);


    /**
     * Récupère la liste des professeurs titulaires d'un remplaçant sur un établissement donné
     * (si lien titulaire/remplaçant toujours actif à l'instant T)
     * @param psIdRemplacant identifiant neo4j du remplaçant
     * @param psIdEtablissement identifiant de l'établissement
     * @param handler handler portant le resultat de la requête : la liste des identifiants neo4j des titulaires
     */
    public void getTitulaires(String psIdRemplacant, String psIdEtablissement, Handler<Either<String, JsonArray>> handler);

    /**
     * Liste les types de devoirs pour un etablissement donné
     *
     * @param idEtablissement identifiant de l'établissement
     * @param handler         handler portant le resultat de la requête
     */
    public void listTypesDevoirsParEtablissement(String idEtablissement, Handler<Either<String, JsonArray>> handler);

    /**
     * Recupère les informations de l'élève
     *
     * @param id     identifiant de l'élève
     * @param result handler portant le résultat de la requête
     */
    public void getInfoEleve(String id, Handler<Either<String, JsonObject>> result);

    /**
     * Récupère les enfants d'une parent donné
     *
     * @param id      identifiant du parent
     * @param handler handler portant la résultat de la requête
     */
    public void getEnfants(String id, Handler<Either<String, JsonArray>> handler);

    /**
     * Fonction de calcul générique de la moyenne
     *
     * @param listeNoteDevoirs : contient une liste de NoteDevoir.
     *                         Dans le cas ou les objets seraient des moyennes, toutes les propriétés ramener sur devront
     *                         être à false.
     * @param diviseurM        : diviseur de la moyenne. Par défaut, cette valeur est égale à 20 (optionnel).
     * @return Double : moyenne calculée
     **/
    public JsonObject calculMoyenne(List<NoteDevoir> listeNoteDevoirs, Boolean statistiques, Integer diviseurM);

    /**
     * Recupere un établissemnt sous sa representation en BDD
     *
     * @param id      identifiant de l'etablissement
     * @param handler handler comportant le resultat
     */
    public void getStructure(String id, Handler<Either<String, JsonObject>> handler);


    /**
     * Recupere les établissements inactifs de l'utilisateur connecté
     * @param userInfos : utilisateur connecté
     * @param handler handler comportant le resultat
     */
    public void getActivesIDsStructures(UserInfos userInfos,Handler<Either<String, JsonArray>> handler);


    /**
     * Récupère le cycle de la classe dans la relation classe_cycle
     * @param idClasse Identifiant de l'établissement.
     * @param handler Handler portant le résultat de la requête.
     */
    public void getCycle(List<String> idClasse, Handler<Either<String, JsonArray>> handler);

    /**
     * Récupère la liste des utilisateurs selon les paramètres précisés
     *
     *
     * @param structureId
     * @param classId
     * @param groupId
     * @param types
     * @param filterActive
     * @param nameFilter
     * @param user
     * @param eitherHandler
     */
    public void list(String structureId, String classId, String groupId, JsonArray types, String filterActive, String nameFilter, UserInfos user, Handler<Either<String, JsonArray>> eitherHandler);

    /**
     * Récupère la liste des classes de l'utilisateur
     *
     * @param handler handler portant le résultat de la requête
     */
    public void listClasses(String idEtablissement, UserInfos user, Handler<Either<String, JsonArray>> handler);

    /**
     * Map une JsonArray en un JsonObject contenant une clé et une valeur
     * @param list liste à mapper
     * @param key clé
     * @param value valeur
     * @return Un object Json contenant les clés et les valeurs
     */
    public JsonObject mapListNumber(JsonArray list, String key, String value);

    /**
     * Map une JsonArray en un JsonObject contenant une clé et une valeur
     * @param list liste à mapper
     * @param key clé
     * @param value valeur
     * @return Un object Json contenant les clés et les valeurs
     */
    public JsonObject mapListString(JsonArray list, String key, String value);

    /**
     * Réalise une union de deux JsonArray de String
     * @param recipient Tableau d'accueil
     * @param list Tableau à transférer
     * @return Un JsonArray contenant les deux tableau
     */
    public JsonArray saUnion(JsonArray recipient, JsonArray list);


}