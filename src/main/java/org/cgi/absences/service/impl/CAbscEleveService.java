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

        query.append("SELECT abs.evenement.*, to_char(abs.evenement.timestamp_d_arrive, 'HH24:MI') as heure ")
                .append("FROM abs.evenement, viesco.eleve, viesco.cours, abs.pv_appel ")
                .append("WHERE eleve.id = ? ")
                .append("AND eleve.id = evenement.id_eleve ")
                .append("AND evenement.id_appel = pv_appel.id_appel ")
                .append("AND pv_appel.id_cours = cours.id ")
                .append("AND to_date(?, 'DD-MM-YYYY') <= cours.timestamp_debut ")
                .append("AND cours.timestamp_fin < to_date(?, 'DD-MM-YYYY')");

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
                .append("WHERE absence_prev.id_eleve = ? ");

        values.addNumber(new Integer(psIdEleve));

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getAbsencesPrev(String psIdEleve, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        // recéupération de toutes les absences prévisionnelle dont la date de début ou la date de fin
        // est comprise entre la date de début et de fin passée en paramètre (exemple date début et date fin d'un cours)
        query.append("SELECT abs.absence_prev.* ")
                .append("FROM abs.absence_prev ")
                .append("WHERE absence_prev.id_eleve = ? ")
                .append("AND ( ")
                .append("(to_date(?, 'DD-MM-YYYY') <= absence_prev.timestamp_debut AND absence_prev.timestamp_fin < to_date(?, 'DD-MM-YYYY')) ")
                .append("OR ")
                .append("(to_date(?, 'DD-MM-YYYY') <= absence_prev.timestamp_fin  AND absence_prev.timestamp_fin < to_date(?, 'DD-MM-YYYY')) ")
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

        query.append("SELECT DISTINCT(evenement.id_evt), evenement.mot_pour_la_vie_scolaire, evenement.saisie_par_le_cpe," +
                " eleve.nom, eleve.prenom, evenement.id_eleve, cours.timestamp_debut, cours.timestamp_fin, evenement.id_appel ")
                .append("FROM viesco.eleve, viesco.est_membre_de, viesco.classe, abs.pv_appel, viesco.cours, abs.evenement LEFT OUTER JOIN abs.motif on (evenement.id_evt = motif.id_evt) ")
                .append("WHERE evenement.id_type = 1 ")
                .append("AND evenement.id_eleve = eleve.id ")
                .append("AND evenement.id_appel = pv_appel.id_appel ")
                .append("AND pv_appel.id_cours = cours.id ")
                .append("AND cours.timestamp_debut >= ? ")
                .append("AND cours.timestamp_debut <= ? ")
                .append("AND eleve.id = est_membre_de.id_eleve ")
                .append("AND est_membre_de.id_classe = classe.id ")
                .append("AND classe.id_etab_neo4j = ?::uuid");

        values.addString(psDateDebut).addString(psDateFin).addString(psIdEtablissement);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultsHandler(handler));
    }
}
