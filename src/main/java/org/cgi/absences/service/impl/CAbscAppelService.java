package org.cgi.absences.service.impl;

import fr.wseduc.webutils.Either;
import org.cgi.Viescolaire;
import org.cgi.absences.service.IAbscAppelService;
import org.cgi.absences.service.IAbscEleveService;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.entcore.common.sql.SqlResult.validUniqueResultHandler;

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

        query.append("SELECT DISTINCT cours.cours_id, cours.cours_timestamp_dt, cours.cours_timestamp_fn, cours.cours_matiere, cours.cours_salle, appel.appel_id, personnel.personnel_prenom, personnel_nom, appel.fk_etat_appel_id, classe.classe_libelle, classe.classe_id, personnel.personnel_id " +
                "FROM viesco.personnel, viesco.classe, viesco.rel_personnel_cours, viesco.cours " +
                "LEFT OUTER JOIN abs.appel on (cours.cours_id = appel.fk_cours_id) " +
                "WHERE cours.fk4j_etab_id = ?::uuid " +
                "AND cours.cours_timestamp_dt > to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') " +
                "AND cours.cours_timestamp_fn <= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') " +
                "AND rel_personnel_cours.fk_cours_id = cours.cours_id " +
                "AND rel_personnel_cours.fk_personnel_id = personnel.personnel_id " +
                "AND cours.fk_classe_id = classe.classe_id "+
                "ORDER BY cours.cours_timestamp_dt DESC");

        value.addString(psIdEtablissement).addString(psDateDebut).addString(psDateFin);

        Sql.getInstance().prepared(query.toString(), value, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getAppelsNonEffectues(String psIdEtablissement, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT DISTINCT cours.cours_id, cours.cours_timestamp_dt, cours.cours_timestamp_fn, cours.cours_matiere, cours.cours_salle, appel.appel_id, personnel.personnel_prenom, personnel_nom, appel.fk_etat_appel_id, classe.classe_libelle, classe.classe_id, personnel.personnel_id " +
                "FROM viesco.personnel, viesco.classe, viesco.rel_personnel_cours, viesco.cours " +
                "LEFT OUTER JOIN abs.appel on (cours.cours_id = appel.fk_cours_id) " +
                "WHERE cours.fk4j_etab_id = ?::uuid " +
                "AND cours.cours_timestamp_dt > to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') " +
                "AND cours.cours_timestamp_fn <= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') " +
                "AND rel_personnel_cours.fk_cours_id = cours.cours_id " +
                "AND rel_personnel_cours.fk_personnel_id = personnel.personnel_id " +
                "AND cours.fk_classe_id = classe.classe_id " +
                "AND (appel.fk_etat_appel_id != 3 OR appel.fk_etat_appel_id IS NULL)");

        values.addString(psIdEtablissement).addString(psDateDebut).addString(psDateFin);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void createAppel(Integer poPersonnelId, Integer poCourId, Integer poEtatAppelId, Integer poJustificatifAppelId,
                            Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("INSERT INTO abs.appel (fk_personnel_id, fk_cours_id, fk_etat_appel_id, fk_justificatif_appel_id) ")
                .append("VALUES (?,?,?,?) RETURNING *");

        values.addNumber(poPersonnelId);
        values.addNumber(poCourId);
        values.addNumber(poEtatAppelId);
        values.addNumber(poJustificatifAppelId);

        Sql.getInstance().prepared(query.toString(), values, validUniqueResultHandler(handler));
    }

    @Override
    public void updateAppel(Integer poAppelId, Integer poPersonnelId, Integer poCourId, Integer poEtatAppelId,
                            Integer poJustificatifAppelId, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("UPDATE abs.appel SET (fk_personnel_id, fk_cours_id, fk_etat_appel_id, fk_justificatif_appel_id) ")
                .append("= (?,?,?,?) WHERE appel_id = ? RETURNING *");

        values.addNumber(poPersonnelId);
        values.addNumber(poCourId);
        values.addNumber(poEtatAppelId);
        values.addNumber(poJustificatifAppelId);
        values.addNumber(poAppelId);

        Sql.getInstance().prepared(query.toString(), values, validUniqueResultHandler(handler));
    }

    @Override
    public void getAppelCours(Integer poCoursId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT * FROM abs.appel ")
            .append("WHERE appel.fk_cours_id = ?");

        values.addNumber(poCoursId);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }


}
