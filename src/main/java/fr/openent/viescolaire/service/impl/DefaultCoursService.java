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
import fr.openent.viescolaire.service.CoursService;
import fr.wseduc.webutils.Either;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;

import static org.entcore.common.sql.SqlResult.validResultHandler;

/**
 * Sercice pour la gestion et récupération des cours
 * Schéma : viesco
 * Table : cours
 * Created by ledunoiss on 10/02/2016.
 */
public class DefaultCoursService extends SqlCrudService implements CoursService {
    public DefaultCoursService() {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.VSCO_COURS_TABLE);
    }

    @Override
    public void getClasseCours(String pSDateDebut, String pSDateFin, String pSIdClasse, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT cours.id, cours.id_etablissement, cours.timestamp_dt, cours.timestamp_fn, cours.salle, cours.matiere, cours.fk_classe_id ")
        .append("FROM "+ Viescolaire.VSCO_SCHEMA +".cours, "+ Viescolaire.VSCO_SCHEMA +".classe ")
        .append("WHERE cours.fk_classe_id = classe.id ")
        .append("AND cours.fk_classe_id = ? ")
        .append("AND cours.timestamp_dt > to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') ")
        .append("AND cours.timestamp_fn < to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') ")
        .append("ORDER BY cours.timestamp_fn ASC");

        values.addNumber(Integer.parseInt(pSIdClasse)).addString(pSDateDebut).addString(pSDateFin);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void getCoursByUserId(String pSDateDebut, String pSDateFin, String psUserId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT cours.*, to_char(cours.timestamp_dt, 'HH24:MI') as heure_debut, classe.libelle as libelle_classe, rel_personnel_cours.fk_personnel_id ")
                .append("FROM "+ Viescolaire.VSCO_SCHEMA +".cours, "+ Viescolaire.VSCO_SCHEMA +".classe, "+ Viescolaire.VSCO_SCHEMA +".rel_personnel_cours, "+ Viescolaire.VSCO_SCHEMA +".personnel ")
                .append("WHERE personnel.fk4j_user_id::varchar = ? ")
                .append("AND personnel.id = rel_personnel_cours.fk_personnel_id ")
                .append("AND rel_personnel_cours.fk_cours_id = cours.id ")
                .append("AND to_date(?, 'DD-MM-YYYY') < cours.timestamp_dt ")
                .append("AND cours.timestamp_fn < to_date(?, 'DD-MM-YYYY') ")
                .append("AND cours.fk_classe_id = classe.id ")
                .append("AND rel_personnel_cours.fk_cours_id = cours.id ")
                .append("ORDER BY cours.timestamp_dt ASC");

        values.addString(psUserId);
        values.addString(pSDateDebut);
        values.addString(pSDateFin);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }


}
