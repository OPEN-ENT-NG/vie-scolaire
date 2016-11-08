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

package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.UserService;
import fr.wseduc.webutils.Either;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ledunoiss on 08/11/2016.
 */
public class DefaultUserService implements UserService {

    @Override
    public void getUserId(UserInfos user, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder()
                .append("SELECT id FROM " + Viescolaire.VSCO_SCHEMA);
        switch (user.getType()) {
            case "Teacher":
            case "Personnel": {
                query.append(".personnel");
            }
            break;
            case "Relative": {
                query.append(".parent");
            }
            break;
            case "Student": {
                query.append(".eleve");
            }
            break;
        }
        query.append(" WHERE fk4j_user_id = ?;");

        Sql.getInstance().prepared(query.toString(), new JsonArray().addString(user.getUserId()), SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void getStructures(UserInfos user, Handler<Either<String, JsonArray>> handler) {
        List<String> structures = user.getStructures();
        StringBuilder query = new StringBuilder()
                .append("SELECT * FROM " + Viescolaire.VSCO_SCHEMA + ".structure " +
                        "WHERE structure.fk4j_structure_id IN " + Sql.listPrepared(structures.toArray()));
        JsonArray params = new JsonArray();
        for (int i = 0; i < structures.size(); i++) {
            params.addString(structures.get(i));
        }
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getClasses(UserInfos user, Handler<Either<String, JsonArray>> handler) {
        List<String> classes = user.getClasses();
        JsonArray params = new JsonArray();
        StringBuilder query = new StringBuilder()
                .append("SELECT * FROM " + Viescolaire.VSCO_SCHEMA + ".classe " +
                        "WHERE classe.fk4j_classe_id IN " + Sql.listPrepared(classes.toArray()));
        for (int i = 0; i < classes.size(); i++) {
            params.addString(classes.get(i));
        }
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getMatiere(UserInfos user, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray params = new JsonArray();
        switch (user.getType()) {
            case "Teacher" : {
                query.append("SELECT matiere.* " +
                        "FROM " + Viescolaire.VSCO_SCHEMA + ".matiere " +
                        "INNER JOIN " + Viescolaire.VSCO_SCHEMA + ".personnel ON (personnel.id = matiere.id_professeur)" +
                        "WHERE personnel.fk4j_user_id = ?");
                params.addString(user.getUserId());
            }
            break;
            case "Eleve" : {
                List<String> classes = user.getClasses();
                query.append("SELECT matiere.* " +
                        "FROM " + Viescolaire.VSCO_SCHEMA + ".matiere INNER JOIN " + Viescolaire.VSCO_SCHEMA + ".personnel ON (matiere.id_professeur = personnel.id) " +
                        "INNER JOIN " + Viescolaire.VSCO_SCHEMA + ".rel_personnel_classe ON (personnel.id = rel_personnel_classe.id_personnel) " +
                        "INNER JOIN " + Viescolaire.VSCO_SCHEMA + ".classe ON (rel_personnel_classe.id_classe = classe.id) " +
                        "WHERE classe.externalid IN " + Sql.listPrepared(classes.toArray()));
                for (int i = 0; i < classes.size(); i++) {
                    params.addString(classes.get(i));
                }
            }
            break;
            default : {
                handler.handle(new Either.Right<String, JsonArray>(new JsonArray()));
            }
        }
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));
    }

}
