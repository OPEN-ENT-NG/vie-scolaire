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

package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.SousMatiereService;
import fr.wseduc.webutils.Either;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;

import static org.entcore.common.sql.SqlResult.validResultHandler;

/**
 * Created by ledunoiss on 18/10/2016.
 */
public class DefaultSousMatiereService extends SqlCrudService implements SousMatiereService {

    public DefaultSousMatiereService() {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.VSCO_SOUSMATIERE_TABLE);
    }

    @Override
    public void listSousMatieres(String id, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();

        query.append("SELECT sousmatiere.id ,type_sousmatiere.libelle ")
                .append("FROM "+ Viescolaire.VSCO_SCHEMA +".sousmatiere, "+ Viescolaire.VSCO_SCHEMA +".type_sousmatiere ")
                .append("WHERE sousmatiere.id_type_sousmatiere = type_sousmatiere.id ")
                .append("AND sousmatiere.id_matiere = ? ");

        values.add(id);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void getSousMatiereById(String[] ids, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray params = new fr.wseduc.webutils.collections.JsonArray();
        query.append("SELECT sousmatiere.*,type_sousmatiere.libelle FROM "+ Viescolaire.VSCO_SCHEMA +
                ".sousmatiere INNER JOIN "+ Viescolaire.VSCO_SCHEMA +
                ".type_sousmatiere on sousmatiere.id_type_sousmatiere = type_sousmatiere.id" +
                " WHERE sousmatiere.id_matiere IN ")
                .append(Sql.listPrepared(ids))
                .append(";");
        for (int i = 0; i < ids.length; i++) {
            params.add(ids[i]);
        }
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));
    }

    public void listTypeSousMatieres(Handler<Either<String, JsonArray>> handler) {
        String query = "SELECT * FROM "+ Viescolaire.VSCO_SCHEMA +".type_sousmatiere ORDER BY id ";
        Sql.getInstance().raw(query, validResultHandler(handler));
    }
}
