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
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ledunoiss on 02/11/2016.
 */
public interface ReferentielService {

    // STRUCTURE
    public void syncStructure (List<String> externaIds, Handler<Either<String, JsonArray>> handler);

    public void createStructure (JsonObject structure, Handler<Either<String, JsonObject>> handler);

    //CLASSES
    public void syncClassesStructure(String externalId, Handler<Either<String, JsonArray>> handler);

    public void createClasses(JsonArray classes, Integer idStructure, Integer idTypeClasse, Handler<Either<String, JsonObject>> handler);

    //GROUPES FONCTIONNELS
    public void syncFunctionalsGroups(String externalId, Handler<Either<String, JsonArray>> handler);

    //ELEVES + PARENTs
    public void syncStudentsParents(String externalId, Handler<Either<String, JsonArray>> handler);

    public void createStudentsParents(JsonArray students, Handler<Either<String, JsonObject>> handler);

    public void linkStudentsParents(JsonArray students, Handler<Either<String, JsonObject>> handler);

    //PERSONNELS + TEACHERS
    public void syncPersonnels(String externalId, Handler<Either<String, JsonArray>> handler);

    public void createPersonnels(JsonArray personnels, String externalId, Handler<Either<String, JsonObject>> handler);

    public void createPersonnel(Integer structureId, JsonObject teacher, Handler<Boolean> handler);

    public void syncTeachers(String externalId, Handler<Either<String, JsonArray>> handler);

    public void createTeachers(JsonArray teachers, String externalId, Handler<Either<String, JsonObject>> handler);

    public void createPersonnelDB(JsonObject personnel, Boolean enseigne, Handler<Either<String, JsonObject>> handler);

    public void findPersonnel(String userId, Handler<Either<String, JsonObject>> handler);

    public void linkPersonnelStructure(Integer structureId, Integer userId, Handler<Either<String, JsonObject>> handler);

    public void linkPersonnelClasses(String userId, JsonArray classes, Handler<Either<String, JsonObject>> handler);

    //MATIERES
    public void syncMatieres(JsonArray matieres, Handler<Either<String, JsonArray>> handler);

    public void createMatiere(JsonArray matieres, Handler<Either<String, JsonObject>> handler);

}
