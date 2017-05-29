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

package fr.openent.absences.service.impl;

import fr.openent.Viescolaire;
import fr.wseduc.webutils.Either;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.sql.SqlStatementsBuilder;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.entcore.common.sql.Sql.parseId;
import static org.entcore.common.sql.SqlResult.validRowsResultHandler;
import static org.entcore.common.sql.SqlResult.validUniqueResultHandler;

/**
 * Created by ledunoiss on 23/02/2016.
 */
public class DefaultMotifService extends SqlCrudService implements fr.openent.absences.service.MotifService {
    public DefaultMotifService() {
        super(Viescolaire.ABSC_SCHEMA, Viescolaire.ABSC_MOTIF_TABLE);
    }

    @Override
    public void getAbscMotifsEtbablissement(String psIdEtablissement, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT motif.id, motif.id_etablissement, motif.libelle, justifiant, commentaire, defaut")
                .append(",categorie_motif_absence.libelle as categorie, categorie_motif_absence.id as id_categorie" )
                .append("   FROM "+ Viescolaire.ABSC_SCHEMA +".motif\n ")
                .append("  INNER JOIN "+ Viescolaire.ABSC_SCHEMA +".categorie_motif_absence\n")
                .append("  ON (id_categorie = categorie_motif_absence.id)\n")
                .append("WHERE motif.id_etablissement = ?");

        values.addString(psIdEtablissement);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getCategorieAbscMotifsEtbablissement(String psIdEtablissement, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT id, libelle, id_etablissement ")
                .append(" FROM "+ Viescolaire.ABSC_SCHEMA +".categorie_motif_absence\n")
                .append(" WHERE "+ Viescolaire.ABSC_SCHEMA +".categorie_motif_absence.id_etablissement = ?");

        values.addString(psIdEtablissement);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getAbscJustificatifsEtablissement(String psIdEtablissement, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT * ")
                .append("FROM "+ Viescolaire.ABSC_SCHEMA +".justificatif_appel ")
                .append("WHERE justificatif_appel.id_etablissement = ?");

        values.addString(psIdEtablissement);
        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void createMotifAbs(JsonObject motif,  final Handler<Either<String, JsonObject>> handler) {
        sql.insert(Viescolaire.ABSC_SCHEMA+ ".motif", motif, "id", validUniqueResultHandler(handler));
    }

    @Override
    public void updateMotifAbs (JsonObject motif, Handler<Either<String, JsonObject>> handler) {
        StringBuilder sb = new StringBuilder();
        JsonArray values = new JsonArray();

        for (String attr : motif.getFieldNames()) {
            if (! attr.contains("id")) {
                sb.append(attr).append(" = ?, ");
                values.add(motif.getValue(attr));
            }
        }
        sb.append("id_categorie").append(" = ? ");
            values.add(motif.getValue("id_categorie"));

        String query ="UPDATE " + Viescolaire.ABSC_SCHEMA+ ".motif" +
                " SET " + sb.toString() +
                " WHERE id = ? ";
        sql.prepared(query, values.add(motif.getValue("id")), validRowsResultHandler(handler));
    }

    @Override
    public void createCategorieMotifAbs(JsonObject categorie,  final Handler<Either<String, JsonObject>> handler) {
        sql.insert(Viescolaire.ABSC_SCHEMA+ ".categorie_motif_absence", categorie, "id", validUniqueResultHandler(handler));
    }

    @Override
    public void updateCategorieMotifAbs (JsonObject categorie, Handler<Either<String, JsonObject>> handler) {
        StringBuilder sb = new StringBuilder();
        JsonArray values = new JsonArray();

        sb.append("libelle").append(" = ?, ");
        values.add(categorie.getValue("libelle"));

        sb.append("id_etablissement").append(" = ? ");
        values.add(categorie.getValue("id_etablissement"));


        String query ="UPDATE " + Viescolaire.ABSC_SCHEMA+ ".categorie_motif_absence" +
                " SET " + sb.toString() +
                " WHERE id = ? ";
        sql.prepared(query, values.add(categorie.getValue("id")), validRowsResultHandler(handler));
    }
}
