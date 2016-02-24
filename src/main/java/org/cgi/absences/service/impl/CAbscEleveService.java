package org.cgi.absences.service.impl;

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

        query.append("SELECT abs.evenement.*, to_char(abs.evenement.evenement_timestamp_arrive, 'HH24:mm') as heure ")
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
    public void getAbsencesSansMotifs(String psIdEtablissement, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT DISTINCT(evenement.evenement_id), evenement.evenement_commentaire, evenement.evenement_saisie_cpe," +
                " eleve.eleve_nom, eleve.eleve_prenom, evenement.fk_eleve_id, cours.cours_timestamp_dt, cours.cours_timestamp_fn, evenement.fk_appel_id, evenement.fk_type_evt_id, classe.classe_id as id_classe, personnel.personnel_id ")
                .append("FROM viesco.eleve, viesco.rel_eleve_classe, viesco.classe, abs.appel, viesco.cours, viesco.rel_personnel_cours, viesco.personnel, abs.evenement LEFT OUTER JOIN abs.motif on (evenement.evenement_id = motif.fk_evenement_id) ")
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
                .append("AND personnel.personnel_id = rel_personnel_cours.fk_personnel_id");

        values.addString(psDateDebut).addString(psDateFin).addString(psIdEtablissement);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }
}
