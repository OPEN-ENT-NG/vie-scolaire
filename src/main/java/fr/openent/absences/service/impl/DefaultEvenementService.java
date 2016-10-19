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
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import static org.entcore.common.sql.SqlResult.validUniqueResultHandler;

/**
 * Created by ledunoiss on 25/02/2016.
 */
public class DefaultEvenementService extends SqlCrudService implements fr.openent.absences.service.EvenementService {
    public DefaultEvenementService() {
        super(Viescolaire.ABSC_SCHEMA, Viescolaire.ABSC_EVENEMENT_TABLE);
    }
    protected static final Logger log = LoggerFactory.getLogger(DefaultEvenementService.class);
    public static String gsFormatTimestampWithoutTimeZone = "'yyyy-mm-dd\"T\"hh24:mi:ss.MS'";

    @Override
    public void updateEvenement(String pIIdEvenement, JsonObject pOEvenement, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("UPDATE "+ Viescolaire.ABSC_SCHEMA +".evenement SET fk_motif_id = ? WHERE "+ Viescolaire.ABSC_SCHEMA +".evenement.id = ? RETURNING *");
        values.addNumber(pOEvenement.getObject("motif").getInteger("motif_id")).addNumber(Integer.parseInt(pIIdEvenement));

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void createEvenement(JsonObject poEvenement, UserInfos user, Handler<Either<String, JsonObject>> handler) {
        super.create(poEvenement, user, handler);
    }

    @Override
    public void updateEvenement(JsonObject poEvenement, Handler<Either<String, JsonObject>> handler) {
        super.update(poEvenement.getInteger("id").toString(), poEvenement, handler);
    }

    @Override
    public void deleteEvenement(int poEvenementId, Handler<Either<String, JsonObject>> handler) {
        super.delete(Integer.toString(poEvenementId), handler);
    }

    @Override
    public void getObservations(String psEtablissementId, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT evenement.id, evenement.commentaire, cours.timestamp_dt, cours.timestamp_fn " +
                "FROM "+ Viescolaire.ABSC_SCHEMA +".evenement, "+ Viescolaire.VSCO_SCHEMA +".cours, "+ Viescolaire.ABSC_SCHEMA +".appel " +
                "WHERE evenement.commentaire IS NOT NULL " +
                "AND evenement.fk_appel_id = appel.id " +
                "AND appel.fk_cours_id = cours.id " +
                "AND evenement.fk_type_evt_id = 5 " +
                "AND cours.timestamp_dt > to_timestamp(?,'YYYY-MM-DD HH24:MI:SS') " +
                "AND cours.timestamp_fn < to_timestamp(?,'YYYY-MM-DD HH24:MI:SS') "+
                "AND cours.id_etablissement = ?::uuid " +
                "ORDER BY cours.timestamp_dt DESC");

        values.addString(psDateDebut).addString(psDateFin).addString(psEtablissementId);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getEvenementClasseCours(String psClasseId, String psCoursId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT evenement.id, evenement.timestamp_arrive, evenement.timestamp_depart, evenement.commentaire, " +
                "evenement.evenement_saisie_cpe, evenement.fk_eleve_id, evenement.fk_appel_id, evenement.fk_type_evt_id," +
                " evenement.fk_pj_pj, evenement.fk_motif_id," +
                " eleve.id, eleve.fk4j_user_id, appel.fk_cours_id " +
                "FROM "+ Viescolaire.ABSC_SCHEMA +".evenement, "+ Viescolaire.VSCO_SCHEMA +".eleve, "+ Viescolaire.VSCO_SCHEMA +".rel_eleve_classe, "+ Viescolaire.ABSC_SCHEMA +".appel " +
                "WHERE evenement.fk_eleve_id = eleve.id " +
                "AND eleve.id = rel_eleve_classe.fk_eleve_id " +
                "AND rel_eleve_classe.fk_classe_id = ? " +
                "AND evenement.fk_appel_id = appel.id " +
                "AND appel.fk_cours_id = ?");

        values.addNumber(Integer.parseInt(psClasseId)).addNumber(Integer.parseInt(psCoursId));

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getAbsencesDernierCours(String psUserId, Integer psClasseId, Integer psCoursId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT evenement.fk_eleve_id " +
                "FROM "+ Viescolaire.ABSC_SCHEMA +".evenement " +
                "WHERE evenement.fk_appel_id = ( " +
                "SELECT appel.id " +
                "FROM "+ Viescolaire.VSCO_SCHEMA +".cours, "+ Viescolaire.VSCO_SCHEMA +".personnel, "+ Viescolaire.VSCO_SCHEMA +".rel_personnel_cours, "+ Viescolaire.ABSC_SCHEMA +".appel " +
                "WHERE personnel.fk4j_user_id = ?::uuid " +
                "AND personnel.id = rel_personnel_cours.fk_personnel_id " +
                "AND rel_personnel_cours.fk_cours_id = cours.id " +
                "AND cours.fk_classe_id = ? " +
                "AND appel.fk_cours_id = cours.id " +
                "AND cours.id != ? " +
                "AND cours.timestamp_dt < (SELECT cours.timestamp_dt FROM "+ Viescolaire.VSCO_SCHEMA +".cours WHERE cours.id = ?) " +
                "ORDER BY cours.timestamp_dt DESC " +
                "LIMIT 1) " +
                "AND evenement.fk_type_evt_id = 1 ");

        values.addString(psUserId).addNumber(psClasseId).addNumber(psCoursId).addNumber(psCoursId);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getEvtClassePeriode(Integer piClasseId, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT evenement.id, evenement.timestamp_arrive, evenement.timestamp_depart, evenement.commentaire, " +
                "evenement.saisie_cpe, evenement.fk_eleve_id, evenement.fk_appel_id, evenement.fk_type_evt_id," +
                " evenement.fk_pj_pj, evenement.fk_motif_id, cours.id " +
                "FROM "+ Viescolaire.ABSC_SCHEMA +".evenement, "+ Viescolaire.ABSC_SCHEMA +".appel, "+ Viescolaire.VSCO_SCHEMA +".cours " +
                "WHERE cours.fk_classe_id = ? " +
                "AND evenement.fk_appel_id = appel.id " +
                "AND appel.fk_cours_id = cours.id " +
                "AND cours.timestamp_dt > to_timestamp(?,'YYYY-MM-DD HH24:MI:SS') " +
                "AND cours.timestamp_fn < to_timestamp(?,'YYYY-MM-DD HH24:MI:SS')");

        values.addNumber(piClasseId).addString(psDateDebut).addString(psDateFin);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }
}
