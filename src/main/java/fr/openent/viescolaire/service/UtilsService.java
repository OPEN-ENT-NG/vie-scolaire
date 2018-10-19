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
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public interface UtilsService {

    /**
     * Ajoute l'element en parametre dans la liste imbriquee dans la map a la cle fournie
     *
     * @param value     valeur a ajouter
     * @param key       cle a laquelle ajouter la valeur
     * @param map       map dans laquelle la valeur est ajoutee
     * @param <T>       type de la valeur a ajouter
     * @param <V>       type de la cle a laquelle ajouter la valeur
     */
     <T, V> void addToMap(V value, T key, Map<T, List<V>> map);

     JsonObject[] convertTo(Object[] value);

    /**
     * Renvoie le type des groupes identifies par id. True si le groupe est une classe, false si c'est un groupe
     *
     * @param idClasses       ids des groupes a identifier
     * @param handler          handler portant le resultat de la requete
     */
     void getTypeGroupe(String[] idClasses, Handler<Either<String, JsonArray>> handler);

    /**
     * Get a Fix Color for a List of Classes name
     */
     String getColor(String classes) ;


    /**
     * Map une JsonArray en un JsonObject contenant une clé et une valeur
     * @param list liste à mapper
     * @param key clé
     * @param value valeur
     * @return Un object Json contenant les clés et les valeurs
     */
     JsonObject mapListNumber(JsonArray list, String key, String value);

    /**
     * Map une JsonArray en un JsonObject contenant une clé et une valeur
     * @param list liste à mapper
     * @param key clé
     * @param value valeur
     * @return Un object Json contenant les clés et les valeurs
     */
     JsonObject mapListString(JsonArray list, String key, String value);

    /**
     * Réalise une union de deux JsonArray de String
     * @param recipient Tableau d'accueil
     * @param list Tableau à transférer
     * @return Un JsonArray contenant les deux tableau
     */
     JsonArray saUnion(JsonArray recipient, JsonArray list);

    /**
     * Récupère la liste des professeurs titulaires d'un remplaçant sur un établissement donné
     * (si lien titulaire/remplaçant toujours actif à l'instant T)
     * @param psIdRemplacant identifiant neo4j du remplaçant
     * @param psIdEtablissement identifiant de l'établissement
     * @param handler handler portant le resultat de la requête : la liste des identifiants neo4j des titulaires
     */
     void getTitulaires(String psIdRemplacant, String psIdEtablissement, Handler<Either<String, JsonArray>> handler);

    /**
     *
     * @param idClasse
     * @param idStructure
     * @param idEleves
     * @param handler
     * @return
     */
    Handler<Message<JsonObject>> addStoredDeletedStudent(JsonArray idClasse,
                                     String idStructure, String[] idEleves, String [] sortedField, Long idPeriode,
                                                         Handler<Either<String, JsonArray>> handler);

    /**
     * Trie une liste en fonction des champs passé en paramètre
     * @param jsonArr
     * @param sortedField
     * @return
     */
    JsonArray sortArray(JsonArray jsonArr, String[] sortedField);

    /**
     *
     * @param idClasses
     * @param idPeriode
     * @param handler
     * @return
     */
    Handler<Message<JsonObject>> getEleveWithClasseName(String[] idClasses, String[] idEleves, Long idPeriode,
                                                               Handler<Either<String, JsonArray>> handler);
    /**
     * Récupère les ids de classes et de groupes à partir de leur externalIds
     * @param externalIdStructures
     * @param handler handler comportant le resultat
     */
    void getIdGroupByExternalId(List<String> externalIdStructures, Handler<Either<String, JsonArray>> handler);

    /**
     * Récupère les informations d'un établissement
     * @param idStructure
     * @param handler
     */
    void getStructure(String idStructure, Handler<Either<String, JsonObject>> handler);
    }
