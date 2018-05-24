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
import org.entcore.common.user.UserInfos;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Created by ledunoiss on 08/11/2016.
 */
public interface UserService {

    public void getUserId(UserInfos user, Handler<Either<String, JsonObject>> handler);

    public void getStructures(UserInfos user, Handler<Either<String, JsonArray>> handler);

    public void getClasses(UserInfos user, Handler<Either<String, JsonArray>> handler);

    public void getMatiere(UserInfos user, Handler<Either<String, JsonArray>> handler);

    public void getMoyenne(String idEleve, Long[] idDevoirs, final Handler<Either<String, JsonObject>> handler);

    public void createPersonnesSupp(JsonArray users, Handler<Either<String, JsonObject>> handler);

    /**
     * Recupere les établissements inactifs de l'utilisateur connecté
     * @param userInfos : utilisateur connecté
     * @param handler handler comportant le resultat
     */
    public void getActivesIDsStructures(UserInfos userInfos,String module,Handler<Either<String, JsonArray>> handler);

    /**
     * Active un établissement
     * @param id : id établissement
     * @param user : utilisateur connecté
     * @param handler handler comportant le resultat
     */
    public void createActiveStructure(String id,String module ,UserInfos user, Handler<Either<String, JsonArray>> handler);

    /**
     * met à jour les établissements inactifs de l'utilisateur connecté
     * @param id : établissement
     * @param module : le module
     * @param handler handler comportant le resultat
     */
    public void deleteActiveStructure(String id,String module ,Handler<Either<String, JsonArray>> handler);

    /**
     *récupère UAI d'un établissement
     * @param idEtabl
     * @param handler
     */
    public void getUAI(String idEtabl, Handler<Either<String,JsonObject>> handler);

    /**
     * récupère les responsables d'établissement
     * @param idsResponsable
     * @param handler
     */
    public void getResponsablesEtabl(List<String> idsResponsable, Handler<Either<String,JsonArray>>handler);


    /**
     * récupère les externalId, firstName, lastName et relative des élèves et nom de la class
     * @param idsClass
     * @param handler
     */
    public void getElevesRelatives(List<String> idsClass,Handler<Either<String,JsonArray>>handler);

    /**
     * Récupère les idDomaine, codification du domaine et le code des domaines
     * @param idClass
     * @param handler
     */
    public void getCodeDomaine(String idClass,Handler<Either<String,JsonArray>> handler);

    /**
     *
     * @param idStructure
     * @param handler
     */
    public void getResponsablesDirection(String idStructure,Handler<Either<String,JsonArray>> handler);

    /**
     * Retourne la liste des enfants pour un utilisateur donné
     *
     * @param idUser  Id de l'utilisateur
     * @param handler Handler comportant le resultat de la requete
     */
    public void getEnfants(String idUser, Handler<Either<String, JsonArray>> handler);

    /**
     * Retourne la liste des personnels pour une liste d'id donnée
     *
     * @param idPersonnels  ids des personnels
     * @param handler Handler comportant le resultat de la requete
     */
    public void getPersonnels(List<String> idPersonnels, Handler<Either<String, JsonArray>> handler);

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

}
