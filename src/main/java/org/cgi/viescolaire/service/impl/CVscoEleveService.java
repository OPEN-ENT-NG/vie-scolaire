package org.cgi.viescolaire.service.impl;

import fr.wseduc.webutils.Either;
import org.cgi.Viescolaire;
import org.cgi.viescolaire.service.IVscoEleveService;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;

import static org.entcore.common.sql.SqlResult.validResultHandler;

/**
 * Created by ledunoiss on 10/02/2016.
 */
public class CVscoEleveService extends SqlCrudService implements IVscoEleveService {
    public CVscoEleveService() {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.VSCO_ELEVE_TABLE);
    }

    @Override
    public void getEleveClasse(String pSIdClasse, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT eleve.* ")
        .append("FROM viesco.eleve, viesco.est_membre_de, viesco.classe ")
        .append("WHERE classe.id = ? ")
        .append("AND classe.id = est_membre_de.id_classe ")
        .append("AND eleve.id = est_membre_de.id_eleve");

        values.addNumber(new Integer(pSIdClasse));

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void getEvenements(String psIdEleve, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT abs.evenement.* ")
                .append("FROM abs.evenement, viesco.eleve, viesco.cours, abs.appel ")
                .append("WHERE eleve.eleve_id = ? ")
                .append("AND eleve.eleve_id = evenement.fk_eleve_id ")
                .append("AND evenement.fk_appel_id = appel.appel_id ")
                .append("AND appel.fk_cours_id = cours.cours_id ")
                .append("AND to_date(?, 'DD-MM-YYYY') < cours.cours_timestamp_dt ")
                .append("AND cours.cours_timestamp_fn < to_date(?, 'DD-MM-YYYY')");

        values.addNumber(new Integer(psIdEleve));
        values.addString(psDateDebut);
        values.addString(psDateFin);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }
}
