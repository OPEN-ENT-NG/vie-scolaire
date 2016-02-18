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

        query.append("SELECT abs.evenement.* ")
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
}
