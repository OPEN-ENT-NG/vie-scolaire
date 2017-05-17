/*
 * Copyright (c) Région Hauts-de-France, Département 77, CGI, 2017.
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

package fr.openent.absences.service.impl;

import fr.openent.Viescolaire;
import fr.openent.absences.service.UtilsService;
import fr.wseduc.webutils.Either;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.sql.SqlStatementsBuilder;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class DefaultUtilsService implements UtilsService {
    @Override
    public void getActivesIDsStructures(UserInfos userInfos, Handler<Either<String, JsonArray>> handler) {
        JsonArray params = new JsonArray();
        String query =
                "SELECT id_etablissement " +
                "FROM " + Viescolaire.ABSC_SCHEMA + ".etablissements_actifs " +
                "WHERE etablissements_actifs.id_etablissement IN " + Sql.listPrepared(userInfos.getStructures().toArray()) + " " +
                "AND etablissements_actifs.actif = TRUE";

        for (String idStructure : userInfos.getStructures()) {
            params.addString(idStructure);
        }

        Sql.getInstance().prepared(query, params, SqlResult.validResultHandler(handler));
    }
    /**
     * Active un établissement
     * @param id : établissement
     * @param handler handler comportant le resultat
     */
    @Override
    public void createActiveStructure (String id,UserInfos user,Handler<Either<String, JsonArray>> handler) {
        SqlStatementsBuilder s = new SqlStatementsBuilder();
        JsonObject data = new JsonObject();
        String userQuery = "SELECT " +  Viescolaire.ABSC_SCHEMA + ".merge_users(?,?)";
        s.prepared(userQuery, (new JsonArray()).add(user.getUserId()).add(user.getUsername()));
        data.putString("id_etablissement", id);
        data.putBoolean("actif", true);
        s.insert( Viescolaire.ABSC_SCHEMA + ".etablissements_actifs", data, "id_etablissement");
        Sql.getInstance().transaction(s.build(), SqlResult.validResultHandler(handler));

    }

    /**
     * Supprime un étbalissement actif
     * @param id : utilisateur connecté
     * @param handler handler comportant le resultat
     */
    @Override
    public void deleteActiveStructure(String id,UserInfos user,Handler<Either<String, JsonArray>> handler) {
        String query = "DELETE FROM " +  Viescolaire.ABSC_SCHEMA + ".etablissements_actifs" + " WHERE id_etablissement = ?";
        Sql.getInstance().prepared(query, (new JsonArray()).add(Sql.parseId(id)), SqlResult.validResultHandler(handler));
    }

}
