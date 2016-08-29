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

package org.cgi.absences.service.impl;

import fr.wseduc.webutils.Either;
import org.cgi.Viescolaire;
import org.cgi.absences.service.IAbscAppelService;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.security.Timestamp;
import java.util.Date;

import static org.entcore.common.sql.SqlResult.validUniqueResultHandler;

/**
 * Created by ledunoiss on 22/02/2016.
 */
public class CAbscAppelService extends SqlCrudService implements IAbscAppelService {
    public CAbscAppelService() {
        super(Viescolaire.ABSC_SCHEMA, Viescolaire.ABSC_APPEL_TABLE);
    }

    @Override
    public void getAppelPeriode(String psIdEtablissement, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray value = new JsonArray();

        query.append("SELECT DISTINCT cours.cours_id, cours.cours_timestamp_dt, cours.cours_timestamp_fn, cours.cours_matiere, cours.cours_salle, appel.id, personnel.personnel_prenom, personnel_nom, appel.fk_etat_appel_id, classe.classe_libelle, classe.classe_id, personnel.personnel_id " +
                "FROM viesco.personnel, viesco.classe, viesco.rel_personnel_cours, viesco.cours " +
                "LEFT OUTER JOIN abs.appel on (cours.cours_id = appel.fk_cours_id) " +
                "WHERE cours.fk4j_etab_id = ?::uuid " +
                "AND cours.cours_timestamp_dt > to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') " +
                "AND cours.cours_timestamp_fn <= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') " +
                "AND rel_personnel_cours.fk_cours_id = cours.cours_id " +
                "AND rel_personnel_cours.fk_personnel_id = personnel.personnel_id " +
                "AND cours.fk_classe_id = classe.classe_id "+
                "ORDER BY cours.cours_timestamp_dt DESC");

        value.addString(psIdEtablissement).addString(psDateDebut).addString(psDateFin);

        Sql.getInstance().prepared(query.toString(), value, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getAppelsNonEffectues(String psIdEtablissement, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT DISTINCT cours.cours_id, cours.cours_timestamp_dt, cours.cours_timestamp_fn, cours.cours_matiere, cours.cours_salle, appel.id, personnel.personnel_prenom, personnel_nom, appel.fk_etat_appel_id, classe.classe_libelle, classe.classe_id, personnel.personnel_id " +
                "FROM viesco.personnel, viesco.classe, viesco.rel_personnel_cours, viesco.cours " +
                "LEFT OUTER JOIN abs.appel on (cours.cours_id = appel.fk_cours_id) " +
                "WHERE cours.fk4j_etab_id = ?::uuid " +
                "AND cours.cours_timestamp_dt > to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') " +
                "AND cours.cours_timestamp_fn <= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') " +
                "AND rel_personnel_cours.fk_cours_id = cours.cours_id " +
                "AND rel_personnel_cours.fk_personnel_id = personnel.personnel_id " +
                "AND cours.fk_classe_id = classe.classe_id " +
                "AND (appel.fk_etat_appel_id != 3 OR appel.fk_etat_appel_id IS NULL) " +
                "ORDER BY cours.cours_timestamp_dt DESC");

        values.addString(psIdEtablissement).addString(psDateDebut).addString(psDateFin);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void createAppel(JsonObject data, UserInfos user, Handler<Either<String, JsonObject>> handler) {
        super.create(data, user, handler);
    }

    @Override
    public void updateAppel(JsonObject data, Handler<Either<String, JsonObject>> handler) {
//        StringBuilder query = new StringBuilder();
//        JsonArray values = new JsonArray();
//
//        query.append("UPDATE abs.appel SET (fk_personnel_id, fk_cours_id, fk_etat_appel_id, fk_justificatif_appel_id) ")
//                .append("= (?,?,?,?) WHERE id = ? RETURNING *");
//
//        values.addNumber(poPersonnelId);
//        values.addNumber(poCourId);
//        values.addNumber(poEtatAppelId);
//        values.addNumber(poJustificatifAppelId);
//        values.addNumber(poAppelId);

//        Sql.getInstance().prepared(query.toString(), values, validUniqueResultHandler(handler));
        super.update(data.getInteger("id").toString(), data, handler);
    }

    @Override
    public void getAppelCours(Integer poCoursId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT appel.id, appel.fk_personnel_id, appel.fk_cours_id, appel.fk_etat_appel_id, appel.fk_justificatif_appel_id " +
                "FROM abs.appel ")
                .append("WHERE appel.fk_cours_id = ?");

        values.addNumber(poCoursId);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }


}
