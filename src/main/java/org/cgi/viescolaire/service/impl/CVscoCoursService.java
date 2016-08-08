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

package org.cgi.viescolaire.service.impl;

import fr.wseduc.webutils.Either;
import org.cgi.Viescolaire;
import org.cgi.viescolaire.service.IVscoCoursService;
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
public class CVscoCoursService extends SqlCrudService implements IVscoCoursService {
    public CVscoCoursService() {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.VSCO_COURS_TABLE);
    }

    @Override
    public void getClasseCours(String pSDateDebut, String pSDateFin, String pSIdClasse, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT cours.cours_id, cours.fk4j_etab_id, cours.cours_timestamp_dt, cours.cours_timestamp_fn, cours.cours_salle, cours.cours_matiere, cours.fk_classe_id ")
        .append("FROM viesco.cours, viesco.classe ")
        .append("WHERE cours.fk_classe_id = classe.classe_id ")
        .append("AND cours.fk_classe_id = ? ")
        .append("AND cours.cours_timestamp_dt > to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') ")
        .append("AND cours.cours_timestamp_fn < to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') ")
        .append("ORDER BY cours.cours_timestamp_fn ASC");

        values.addNumber(Integer.parseInt(pSIdClasse)).addString(pSDateDebut).addString(pSDateFin);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void getCoursByUserId(String pSDateDebut, String pSDateFin, String psUserId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT viesco.cours.*, to_char(viesco.cours.cours_timestamp_dt, 'HH24:MI') as heure_debut, viesco.classe.classe_libelle as libelle_classe, rel_personnel_cours.fk_personnel_id ")
                .append("FROM viesco.cours, viesco.classe, viesco.rel_personnel_cours, viesco.personnel ")
                .append("WHERE personnel.fk4j_user_id::varchar = ? ")
                .append("AND personnel.personnel_id = rel_personnel_cours.fk_personnel_id ")
                .append("AND rel_personnel_cours.fk_cours_id = cours.cours_id ")
                .append("AND to_date(?, 'DD-MM-YYYY') < cours.cours_timestamp_dt ")
                .append("AND cours.cours_timestamp_fn < to_date(?, 'DD-MM-YYYY') ")
                .append("AND cours.fk_classe_id = classe.classe_id ")
                .append("AND rel_personnel_cours.fk_cours_id = cours.cours_id ")
                .append("ORDER BY cours.cours_timestamp_dt ASC");

        values.addString(psUserId);
        values.addString(pSDateDebut);
        values.addString(pSDateFin);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }


}
