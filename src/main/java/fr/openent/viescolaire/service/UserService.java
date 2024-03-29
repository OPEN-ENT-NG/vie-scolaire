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
import io.vertx.core.*;
import org.entcore.common.user.UserInfos;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Set;

/**
 * Created by ledunoiss on 08/11/2016.
 */
public interface UserService {

    void getUserId(UserInfos user, Handler<Either<String, JsonObject>> handler);

    void getStructures(UserInfos user, Handler<Either<String, JsonArray>> handler);

    void getClasses(UserInfos user, Handler<Either<String, JsonArray>> handler);

    void getMatiere(UserInfos user, Handler<Either<String, JsonArray>> handler);

    void getMoyenne(String idEleve, Long[] idDevoirs, final Handler<Either<String, JsonObject>> handler);

    void createPersonnesSupp(JsonArray users, Handler<Either<String, JsonObject>> handler);

    /**
     * Insertion des annotations NN sur les anciens des nouvelles classes
     * @param users : utilisateur connecté
     * @param handler handler comportant le resultat
     */
    void insertAnnotationsNewClasses(JsonArray users, Handler<Either<String, JsonObject>> handler);

    /**
     * Récupère les données des utilisateurs à supprimer
     * @param users : utilisateurs à supprimer
     * @param handler handler comportant le resultat
     */
    void parseUsersData(JsonArray users, Handler<Either<String, JsonArray>> handler);

    /**
     * Recupere les établissements inactifs de l'utilisateur connecté
     * @param userInfos : utilisateur connecté
     * @param handler handler comportant le resultat
     */
    void getActivesIDsStructures(UserInfos userInfos,String module,Handler<Either<String, JsonArray>> handler);

    /**
     * Recupere les établissements inactifs de l'utilisateur connecté
     * @param handler handler comportant le resultat
     */
    void getActivesIDsStructures( String module, Handler<Either<String, JsonArray>> handler);

    /**
     * Active un établissement
     * @param id : id établissement
     * @param user : utilisateur connecté
     * @param handler handler comportant le resultat
     */
    void createActiveStructure(String id,String module ,UserInfos user, Handler<Either<String, JsonArray>> handler);

    /**
     * met à jour les établissements inactifs de l'utilisateur connecté
     * @param id : établissement
     * @param module : le module
     * @param handler handler comportant le resultat
     */
    void deleteActiveStructure(String id,String module ,Handler<Either<String, JsonArray>> handler);

    /**
     *récupère UAI d'un établissement
     * @param idEtabl
     * @param handler
     */
    void getUAI(String idEtabl, Handler<Either<String,JsonObject>> handler);

    /**
     * récupère les infos des utilisateurs
     * @param idUsers
     * @param handler
     */
    void getUsers(List<String> idUsers, Handler<Either<String,JsonArray>>handler);


    /**
     * récupère les externalId, firstName, lastName et relative des élèves et nom de la class
     * @param idsClass
     * @param handler
     */
    void getElevesRelatives(List<String> idsClass,Handler<Either<String,JsonArray>>handler);

    /**
     * get specifications of all Students (student in Classes list and students have changed class)
     * @param idStructure idStructure
     * @param idsClass  list of Classes
     * @param deletedStudentsPostegre ids of postgres deleted Student
     * @param handler response JsonArray
     */
    void getAllElevesWithTheirRelatives(String idStructure,List<String> idsClass, List<String> deletedStudentsPostegre,Handler<Either<String,JsonArray>>handler);
    /**
     * Récupère les idDomaine, codification du domaine et le code des domaines
     * @param idClass
     * @param handler
     */
    void getCodeDomaine(String idClass,Handler<Either<String,JsonArray>> handler);

    /**
     *
     * @param idStructure
     * @param handler
     */
    void getResponsablesDirection(String idStructure,Handler<Either<String,JsonArray>> handler);

    /**
     * Retourne la liste des enfants pour un utilisateur donné
     *
     * @param idUser  Id de l'utilisateur
     * @param handler Handler comportant le resultat de la requete
     */
    void getEnfants(String idUser, Handler<Either<String, JsonArray>> handler);

    /**
     * Retourne la liste des personnels pour une liste d'id donnée
     *
     * @param idPersonnels  ids des personnels
     * @param handler Handler comportant le resultat de la requete
     */
    void getPersonnels(List<String> idPersonnels, Handler<Either<String, JsonArray>> handler);

    void getTeachers(String idStructure, Handler<Either<String, JsonArray>> handler);
    Future<JsonArray> getTeachersWithClassIds(String structureId);

    /**
     * Récupère la liste des utilisateurs selon les paramètres précisés
     *
     *
     * @param structureId
     * @param profile
     * @param eitherHandler
     */
    void list(String structureId, String profile, Handler<Either<String, JsonArray>> eitherHandler);


    /**
     * @param structure_id  structure identifier
     * @param userId        user identifier
     * @param query         query searching
     * @param fields        neo4j fields
     * @param profile       user type
     * @param handler
     */
    void search(String structure_id, String userId, String query, List<String> fields, String profile, Handler<Either<String, JsonArray>> handler);

    /**
     * get lastName and first name of deleted teachers
     *
     * @param idsTeacher ids of teacher
     * @param handler response
     */
    void getDeletedTeachers(List<String> idsTeacher, Handler<Either<String, JsonArray>> handler);
}
