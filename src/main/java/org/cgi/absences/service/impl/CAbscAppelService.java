package org.cgi.absences.service.impl;

import fr.wseduc.webutils.Either;
import org.cgi.Viescolaire;
import org.cgi.absences.service.IAbscAppelService;
import org.cgi.absences.service.IAbscEleveService;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;

/**
 * Created by ledunoiss on 22/02/2016.
 */
public class CAbscAppelService extends SqlCrudService implements IAbscAppelService {
    public CAbscAppelService() {
        super(Viescolaire.ABSC_SCHEMA, Viescolaire.ABSC_APPEL_TABLE);
    }

    @Override
    public void getAppelPeriode(String psIdEtablissement, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray value = new JsonArray();

//        query.append("SELECT personnel.nom, personnel.prenom, cours.id_etab_neo4j, pv_appel.id_appel, cours.timestamp_debut, cours.timestamp_fin, cours.matiere, cours.salle ")
//                .append("FROM abs.pv_appel, viesco.personnel, viesco.cours ")
//                .append("WHERE pv_appel.id_personnel = personnel.id_personnel ")
//                .append("AND pv_appel.id_cours = cours.id ")
//                .append("AND cours.id_etab_neo4j = ? ")
//                .append("AND cours.timestamp_debut > to_date(?, 'DD-MM-YYYY HH24:MI:SS) ")
//                .append("AND cours.timestamp_debut < to_date(?, 'DD-MM-YYYY HH24:MI:SS) ");
        query.append("SELECT DISTINCT cours.cours_id, cours.cours_timestamp_dt, cours.cours_timestamp_fn, cours.cours_matiere, cours.cours_salle, appel.appel_id, personnel.personnel_prenom, personnel_nom, appel.fk_etat_appel_id, classe.classe_libelle, classe.classe_id, personnel.personnel_id " +
                "FROM viesco.personnel, viesco.classe, viesco.rel_personnel_cours, viesco.cours " +
                "LEFT OUTER JOIN abs.appel on (cours.cours_id = appel.fk_cours_id) " +
                "WHERE cours.fk4j_etab_id = ?::uuid " +
                "AND cours.cours_timestamp_dt > to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') " +
                "AND cours.cours_timestamp_fn <= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') " +
                "AND rel_personnel_cours.fk_cours_id = cours.cours_id " +
                "AND rel_personnel_cours.fk_personnel_id = personnel.personnel_id " +
                "AND cours.fk_classe_id = classe.classe_id");

        value.addString(psIdEtablissement).addString(psDateDebut).addString(psDateFin);

        Sql.getInstance().prepared(query.toString(), value, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getAppelsNonEffectues(String psIdEtablissement, String psDateDebut, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT DISTINCT cours.cours_id, cours.cours_timestamp_dt, cours.cours_timestamp_fn, cours.cours_matiere, cours.cours_salle, appel.appel_id, personnel.personnel_prenom, personnel_nom " +
                "FROM viesco.cours LEFT OUTER JOIN abs.appel on (cours.cours_id = appel.fk_cours_id) LEFT OUTER JOIN viesco.personnel on (appel.fk_personnel_id = personnel.personnel_id) " +
                "WHERE cours.fk4j_etab_id = ? " +
                "AND cours.cours_timestamp_dt > ? " +
                "AND cours.cours_timestamp_fn <= Now();");

        values.addString(psIdEtablissement).addString(psDateDebut);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }
}
