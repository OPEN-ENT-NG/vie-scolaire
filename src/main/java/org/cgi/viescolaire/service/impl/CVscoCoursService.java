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

        query.append("SELECT id, id_etab, timestamp_debut, timestamp_fin, salle, matiere, id_classe ")
        .append("FROM viesco.cours, viesco.classe ")
        .append("WHERE cours.id_classe = classe.id ")
        .append("AND classe.id_classe_neo4j = ? ")
        .append("AND cours.timestamp_debut > ?")
        .append("AND cours.timestamp_fin < dateFin");

        values.addString(pSIdClasse).addString(pSDateDebut).addString(pSDateFin);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void getCoursByUserId(String pSDateDebut, String pSDateFin, String psUserId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT viesco.cours.* ")
                .append("FROM viesco.cours, viesco.est_assure_par, viesco.personnel ")
                .append("WHERE personnel.id_user_neo4j = ? ")
                .append("AND personnel.id_personnel = est_assure_par.id_personnel ")
                .append("AND est_assure_par.id_cours = cours.id ")
                .append("AND to_date('?', 'DD-MM-YYYY') < cours.timestamp_debut ")
                .append("AND cours.timestamp_fin < to_date('?', 'DD-MM-YYYY')");

        values.addString(psUserId);
        values.addString(pSDateDebut);
        values.addString(pSDateFin);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }


}
