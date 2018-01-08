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
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import java.util.List;

import static org.entcore.common.sql.SqlResult.validUniqueResultHandler;

/**
 * Created by ledunoiss on 25/02/2016.
 */
public class DefaultEvenementService extends SqlCrudService implements fr.openent.absences.service.EvenementService {
    private static final String FROM = "FROM ";
    private static final String TABLE_APPEL = ".appel ";
    private static final String TABLE_EVENEMENT = ".evenement, ";
    private static final String FILTRE_APPEL_ID = "AND evenement.id_appel = appel.id ";
    private static final String FILTRE_COURS_ID = "AND appel.id_cours = cours.id ";
    protected static final Logger log = LoggerFactory.getLogger(DefaultEvenementService.class);

    public DefaultEvenementService() {
        super(Viescolaire.ABSC_SCHEMA, Viescolaire.ABSC_EVENEMENT_TABLE);
    }

    public void updateMotif(Integer piIdEvenement, Integer piMotif, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("UPDATE "+ Viescolaire.ABSC_SCHEMA +".evenement SET id_motif = ? WHERE "+ Viescolaire.ABSC_SCHEMA +".evenement.id = ? RETURNING *");
        values.addNumber(piMotif).addNumber(piIdEvenement);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }



    @Override
    public void createEvenement(JsonObject poEvenement, UserInfos user, Handler<Either<String, JsonObject>> handler) {
        super.create(poEvenement, user, handler);
    }

    @Override
    public void updateEvenement(JsonObject poEvenement, Handler<Either<String, JsonObject>> handler) {
        if(poEvenement.getInteger("id_type") == 2 || poEvenement.getInteger("id_type") == 3) {
            StringBuilder query = new StringBuilder();
            JsonArray values = new JsonArray();

            query.append("UPDATE " + Viescolaire.ABSC_SCHEMA + ".evenement ")
                    .append("SET ");

            for (String attr : poEvenement.getFieldNames()) {
                if(attr.split("_")[0].equals("timestamp")) {
                    query.append(attr).append(" = (?::timestamptz), ");
                } else {
                    query.append(attr).append(" = ?, ");
                }
                values.add(poEvenement.getValue(attr));
            }

            query.append("modified = NOW() WHERE id = ? ");
            Sql.getInstance().prepared(query.toString(), values.add(poEvenement.getInteger("id")), SqlResult.validRowsResultHandler(handler));
        } else {
            super.update(poEvenement.getInteger("id").toString(), poEvenement, handler);
        }
    }

    @Override
    public void deleteEvenement(Number poEvenementId, Handler<Either<String, JsonObject>> handler) {
        super.delete(poEvenementId.toString(), handler);
    }

    @Override
    public void getObservations(String psEtablissementId, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT evenement.id, evenement.commentaire, cours.timestamp_dt, cours.timestamp_fn " +
                FROM+ Viescolaire.ABSC_SCHEMA +TABLE_EVENEMENT+ Viescolaire.VSCO_SCHEMA +".cours, "+ Viescolaire.ABSC_SCHEMA +TABLE_APPEL +
                "WHERE evenement.commentaire IS NOT NULL " +
                FILTRE_APPEL_ID +
                FILTRE_COURS_ID +
                "AND evenement.id_type = 5 " +
                "AND cours.timestamp_dt >to_timestamp(?,'YYYY-MM-DD HH24:MI:SS')  " +
                "AND cours.timestamp_fn < to_timestamp(?,'YYYY-MM-DD HH24:MI:SS') "+
                "AND cours.id_etablissement = ? " +
                "ORDER BY cours.timestamp_dt DESC");

        values.addString(psDateDebut).addString(psDateFin).addString(psEtablissementId);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getAbsencesDernierCours(Integer psCoursId, Boolean pbTeacher, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT evenement.id_eleve "
                + "FROM presences.evenement "
                + "WHERE evenement.id_appel = "
                + "(SELECT appel.id FROM presences.appel WHERE appel.id_cours = "
                    + "(SELECT cours1.id FROM viesco.cours cours1 "
                    + "INNER JOIN viesco.cours cours2 ON cours1.id_classe = cours2.id_classe "
                        + "AND cours2.id = ? ");
        if(pbTeacher) {
            query.append("AND cours1.id_personnel = cours2.id_personnel ");
        }

        query.append("AND cours1.timestamp_dt<cours2.timestamp_dt "
                + "ORDER BY cours1.timestamp_dt DESC "
                + "LIMIT 1)) "
                + "AND evenement.id_type = 1");

        values.addNumber(psCoursId);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getEvtClassePeriode(String piClasseId, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT evenement.id, evenement.timestamp_arrive, evenement.timestamp_depart, evenement.commentaire, " +
                "evenement.saisie_cpe, evenement.id_eleve, evenement.id_appel, evenement.id_type," +
                " evenement.id_pj, evenement.id_motif, cours.id as id_cours " +
                FROM+ Viescolaire.ABSC_SCHEMA +TABLE_EVENEMENT+ Viescolaire.ABSC_SCHEMA +".appel, "+ Viescolaire.VSCO_SCHEMA +".cours " +
                "WHERE cours.id_classe = ? " +
                FILTRE_APPEL_ID +
                FILTRE_COURS_ID +
                "AND cours.timestamp_dt > to_timestamp(?,'YYYY-MM-DD HH24:MI:SS') " +
                "AND cours.timestamp_fn < to_timestamp(?,'YYYY-MM-DD HH24:MI:SS')");

        values.addString(piClasseId).addString(psDateDebut).addString(psDateFin);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }
}
