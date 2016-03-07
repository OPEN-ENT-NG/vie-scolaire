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

        query.append("SELECT cours_id, fk4j_etab_id, cours_timestamp_dt, cours_timestamp_fn, cours_salle, cours_matiere, fk_classe_id ")
        .append("FROM viesco.cours, viesco.classe ")
        .append("WHERE cours.fk_classe_id = classe.classe_id ")
        .append("AND classe.fk4j_classe_id = ? ")
        .append("AND cours.cours_timestamp_dt > ?")
        .append("AND cours.cours_timestamp_fn < ?");

        values.addString(pSIdClasse).addString(pSDateDebut).addString(pSDateFin);

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
                .append("AND rel_personnel_cours.fk_cours_id = cours.cours_id");

        values.addString(psUserId);
        values.addString(pSDateDebut);
        values.addString(pSDateFin);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }


}
