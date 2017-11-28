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
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;

import java.util.List;

public class DefaultEleveService extends SqlCrudService implements fr.openent.absences.service.EleveService {

    private static final String SELECT = "SELECT ";
    private static final String FROM = "FROM ";
    private static final String FILTER_COURS_ID = "AND appel.id_cours = cours.id ";
    private static final String TABLE_ABSENCE = ".absence_prev ";

    public DefaultEleveService() {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.VSCO_ELEVE_TABLE);
    }

    

    @Override
    public void getEvenements(String psIdEleve, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT evenement.*, cours.id as id_cours ")
                .append("FROM presences.evenement, viesco.cours, presences.appel, presences.type_evt ")
                .append("WHERE evenement.id_eleve = ? ")
                .append("AND evenement.id_appel = appel.id ")
                .append("AND appel.id_cours = cours.id ")
                .append("AND evenement.id_type = type_evt.id ")
                .append("AND cours.timestamp_dt >= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') ")
                .append("AND cours.timestamp_fn < to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS')");

        values.addString(psIdEleve);
        values.addString(psDateDebut);
        values.addString(psDateFin);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getAbsencesPrev(String psIdEleve, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append(SELECT+ Viescolaire.ABSC_SCHEMA +".absence_prev.* ")
                .append(FROM+ Viescolaire.ABSC_SCHEMA +TABLE_ABSENCE)
                .append("WHERE absence_prev.id_eleve = ? ");

        values.addString(psIdEleve);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getAbsencesPrev(String psIdEleve, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        // récupération de toutes les absences prévisionnelles dont la date de début ou la date de fin
        // est comprise entre la date de début et de fin passée en paramètre (exemple date début et date fin d'un cours)
        query.append(SELECT+ Viescolaire.ABSC_SCHEMA +".absence_prev.* ")
                .append(FROM+ Viescolaire.ABSC_SCHEMA +TABLE_ABSENCE)
                .append("WHERE absence_prev.id_eleve = ? ")
                .append("AND ( ")
                .append("(to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') <= absence_prev.timestamp_dt AND absence_prev.timestamp_dt < to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS')) ")
                .append("OR ")
                .append("(to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') <= absence_prev.timestamp_fn  AND absence_prev.timestamp_fn < to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS')) ")
                .append(")");

        values.addString(psIdEleve)
                .addString(psDateDebut)
                .addString(psDateFin)
                .addString(psDateDebut)
                .addString(psDateFin);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getAbsences(String psIdEtablissement, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT DISTINCT(evenement.id), cours.id_matiere, evenement.commentaire, evenement.saisie_cpe, ")
                .append("evenement.id_eleve, evenement.id_motif, cours.timestamp_dt, cours.timestamp_fn, cours.id_personnel, cours.id_classe, ")
                .append("evenement.id_appel, evenement.id_type ")
                .append(FROM+ Viescolaire.ABSC_SCHEMA +".appel, "+ Viescolaire.VSCO_SCHEMA +".cours, "+ Viescolaire.ABSC_SCHEMA +".evenement ")
                .append("LEFT OUTER JOIN "+ Viescolaire.ABSC_SCHEMA +".motif on (evenement.id_motif = motif.id) ")
                .append("WHERE evenement.id_appel = appel.id ")
                .append(FILTER_COURS_ID)
                .append("AND cours.timestamp_dt >= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS')  ")
                .append("AND cours.timestamp_fn <= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') ")
                .append("AND cours.id_etablissement = ? ")
                .append("AND evenement.id_type = 1");

        values.addString(psDateDebut).addString(psDateFin).addString(psIdEtablissement);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getAbsencesSansMotifs(String psIdEtablissement, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT evenement.id ,evenement.commentaire ,evenement.saisie_cpe ,evenement.id_eleve ,evenement.id_motif " )
                .append(",cours.timestamp_dt ,cours.timestamp_fn ,evenement.id_appel ,evenement.id_type ,cours.id_classe ,cours.id_personnel " )
                .append(",motif.id ,motif.libelle ,motif.justifiant " )
                .append(FROM).append(Viescolaire.ABSC_SCHEMA).append(".appel, ").append(Viescolaire.VSCO_SCHEMA).append(".cours, ").append(Viescolaire.ABSC_SCHEMA).append(".evenement " )
                .append("LEFT JOIN ").append(Viescolaire.ABSC_SCHEMA).append(".motif ON (evenement.id_motif = motif.id) " )
                .append("WHERE evenement.id_appel = appel.id " )
                    .append(FILTER_COURS_ID )
                    .append("AND cours.timestamp_dt >= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') " )
                    .append("AND cours.timestamp_fn <= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') " )
                    .append("AND evenement.id_type = 1 " )
                    .append("AND ( " )
                    .append("evenement.id_motif = 8 " )
                        .append("OR evenement.id_motif = 2 " )
                    .append(") " )
                    .append( "AND cours.id_etablissement = ? " )
                .append("ORDER BY cours.timestamp_dt DESC");

        values.addString(psDateDebut).addString(psDateFin).addString(psIdEtablissement);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getAbsencesPrevClassePeriode(List<String> idEleves, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT absence_prev.* ")
                .append(FROM+ Viescolaire.ABSC_SCHEMA +TABLE_ABSENCE )
                .append("WHERE absence_prev.timestamp_dt >= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') ")
                .append("AND absence_prev.timestamp_fn <= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') ")
                .append("AND id_eleve IN "+ Sql.listPrepared(idEleves.toArray()));

        values.addString(psDateDebut).addString(psDateFin);
        for(Integer i=0; i< idEleves.size(); i++){
            values.addString(idEleves.get(i));
        }

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }
}
