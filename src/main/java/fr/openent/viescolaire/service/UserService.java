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
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.List;

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
     * Recupere les établissements inactifs de l'utilisateur connecté
     * @param userInfos : utilisateur connecté
     * @param handler handler comportant le resultat
     */
    void getActivesIDsStructures(UserInfos userInfos,String module,Handler<Either<String, JsonArray>> handler);

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
     * récupère les responsables d'établissement
     * @param idsResponsable
     * @param handler
     */
    void getResponsablesEtabl(List<String> idsResponsable, Handler<Either<String,JsonArray>>handler);


    /**
     * récupère les externalId, firstName, lastName et relative des élèves et nom de la class
     * @param idsClass
     * @param handler
     */
    void getElevesRelatives(List<String> idsClass,Handler<Either<String,JsonArray>>handler);

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

}
