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

import com.mongodb.util.JSON;
import fr.wseduc.webutils.Either;
import org.cgi.Viescolaire;
import org.cgi.absences.service.IAbscEleveService;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;

import static org.entcore.common.sql.SqlResult.validResultHandler;

public class CAbscEleveService extends SqlCrudService implements IAbscEleveService {
    public CAbscEleveService() {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.VSCO_ELEVE_TABLE);
    }

    @Override
    public void getEvenements(String psIdEleve, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT abs.evenement.*, to_char(abs.evenement.evenement_timestamp_arrive, 'hh24:mi') as evenement_heure_arrivee, to_char(abs.evenement.evenement_timestamp_depart, 'hh24:mi') as evenement_heure_depart ")
                .append("FROM abs.evenement, viesco.eleve, viesco.cours, abs.appel ")
                .append("WHERE eleve.eleve_id = ? ")
                .append("AND eleve.eleve_id = evenement.fk_eleve_id ")
                .append("AND evenement.fk_appel_id = appel.appel_id ")
                .append("AND appel.fk_cours_id = cours.cours_id ")
                .append("AND to_date(?, 'DD-MM-YYYY') <= cours.cours_timestamp_dt ")
                .append("AND cours.cours_timestamp_fn < to_date(?, 'DD-MM-YYYY')");

        values.addNumber(new Integer(psIdEleve));
        values.addString(psDateDebut);
        values.addString(psDateFin);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getAbsencesPrev(String psIdEleve, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT abs.absence_prev.* ")
                .append("FROM abs.absence_prev ")
                .append("WHERE absence_prev.fk_eleve_id = ? ");

        values.addNumber(new Integer(psIdEleve));

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getAbsencesPrev(String psIdEleve, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        // récupération de toutes les absences prévisionnelles dont la date de début ou la date de fin
        // est comprise entre la date de début et de fin passée en paramètre (exemple date début et date fin d'un cours)
        query.append("SELECT abs.absence_prev.* ")
                .append("FROM abs.absence_prev ")
                .append("WHERE absence_prev.fk_eleve_id = ? ")
                .append("AND ( ")
                .append("(to_date(?, 'DD-MM-YYYY') <= absence_prev.absence_prev_timestamp_dt AND absence_prev.absence_prev_timestamp_dt < to_date(?, 'DD-MM-YYYY')) ")
                .append("OR ")
                .append("(to_date(?, 'DD-MM-YYYY') <= absence_prev.absence_prev_timestamp_fn  AND absence_prev.absence_prev_timestamp_fn < to_date(?, 'DD-MM-YYYY')) ")
                .append(")");

        values.addNumber(new Integer(psIdEleve));

        values.addString(psDateDebut);
        values.addString(psDateFin);

        values.addString(psDateDebut);
        values.addString(psDateFin);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getAbsences(String psIdEtablissement, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT DISTINCT(evenement.evenement_id), cours.cours_matiere, evenement.evenement_commentaire, evenement.evenement_saisie_cpe," +
                " eleve.eleve_nom, eleve.eleve_prenom, evenement.fk_eleve_id, evenement.fk_motif_id, cours.cours_timestamp_dt, cours.cours_timestamp_fn, evenement.fk_appel_id, evenement.fk_type_evt_id, classe.classe_id, personnel.personnel_id ")
                .append("FROM viesco.eleve, viesco.rel_eleve_classe, viesco.classe, abs.appel, viesco.cours, viesco.rel_personnel_cours, viesco.personnel, abs.evenement LEFT OUTER JOIN abs.motif on (evenement.fk_motif_id = motif.motif_id) ")
                .append("WHERE evenement.fk_eleve_id = eleve.eleve_id ")
                .append("AND evenement.fk_appel_id = appel.appel_id ")
                .append("AND appel.fk_cours_id = cours.cours_id ")
                .append("AND cours.cours_timestamp_dt >= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS')  ")
                .append("AND cours.cours_timestamp_fn <= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') ")
                .append("AND eleve.eleve_id = rel_eleve_classe.fk_eleve_id ")
                .append("AND rel_eleve_classe.fk_classe_id = classe.classe_id ")
                .append("AND classe.fk4j_etab_id = ?::uuid ")
                .append("AND cours.fk_classe_id = classe.classe_id ")
                .append("AND rel_personnel_cours.fk_cours_id = cours.cours_id ")
                .append("AND personnel.personnel_id = rel_personnel_cours.fk_personnel_id ")
                .append("AND evenement.fk_type_evt_id = 1");

        values.addString(psDateDebut).addString(psDateFin).addString(psIdEtablissement);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getAbsencesSansMotifs(String psIdEtablissement, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT DISTINCT(evenement.evenement_id), evenement.evenement_commentaire, evenement.evenement_saisie_cpe, eleve.eleve_nom, eleve.eleve_prenom, evenement.fk_eleve_id, evenement.fk_motif_id, cours.cours_timestamp_dt, cours.cours_timestamp_fn, evenement.fk_appel_id, evenement.fk_type_evt_id, classe.classe_id, personnel.personnel_id, motif.motif_id, motif.motif_libelle, motif.motif_justifiant " +
                "FROM viesco.eleve, viesco.rel_eleve_classe, viesco.classe, abs.appel, viesco.cours, viesco.rel_personnel_cours, viesco.personnel, abs.evenement " +
                "LEFT OUTER JOIN abs.motif on (evenement.fk_motif_id = motif.motif_id) " +
                "WHERE evenement.fk_eleve_id = eleve.eleve_id AND evenement.fk_appel_id = appel.appel_id " +
                "AND appel.fk_cours_id = cours.cours_id " +
                "AND cours.cours_timestamp_dt >= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') " +
                "AND cours.cours_timestamp_fn <= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') " +
                "AND eleve.eleve_id = rel_eleve_classe.fk_eleve_id " +
                "AND evenement.fk_type_evt_id = 1 " +
                "AND (evenement.fk_motif_id = 8 OR evenement.fk_motif_id = 2)" +
                "AND rel_eleve_classe.fk_classe_id = classe.classe_id " +
                "AND classe.fk4j_etab_id = ?::uuid " +
                "AND cours.fk_classe_id = classe.classe_id AND rel_personnel_cours.fk_cours_id = cours.cours_id " +
                "AND personnel.personnel_id = rel_personnel_cours.fk_personnel_id " +
                "ORDER BY cours.cours_timestamp_dt DESC");

        values.addString(psDateDebut).addString(psDateFin).addString(psIdEtablissement);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getAbsencesPrevClassePeriode(Integer piClasseId, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT absence_prev.* " +
                "FROM abs.absence_prev, viesco.eleve, viesco.rel_eleve_classe " +
                "WHERE absence_prev.fk_eleve_id = eleve.eleve_id " +
                "AND eleve.eleve_id = rel_eleve_classe.fk_eleve_id " +
                "AND rel_eleve_classe.fk_classe_id = ? " +
                "AND absence_prev.absence_prev_timestamp_dt >= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') " +
                "AND absence_prev.absence_prev_timestamp_fn <= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS')");

        values.addNumber(piClasseId).addString(psDateDebut).addString(psDateFin);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }
}
