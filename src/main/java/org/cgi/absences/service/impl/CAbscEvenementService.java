package org.cgi.absences.service.impl;

import fr.wseduc.webutils.Either;
import org.cgi.Viescolaire;
import org.cgi.absences.service.IAbscEvenementService;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import static org.entcore.common.sql.SqlResult.validUniqueResultHandler;

/**
 * Created by ledunoiss on 25/02/2016.
 */
public class CAbscEvenementService extends SqlCrudService implements IAbscEvenementService{
    public CAbscEvenementService() {
        super(Viescolaire.ABSC_SCHEMA, Viescolaire.ABSC_EVENEMENT_TABLE);
    }
    protected static final Logger log = LoggerFactory.getLogger(CAbscEvenementService.class);
    public static String gsFormatTimestampWithoutTimeZone = "'yyyy-mm-dd\"T\"hh24:mi:ss.MS'";

    @Override
    public void updateEvenement(String pIIdEvenement, JsonObject pOEvenement, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("UPDATE abs.evenement SET fk_motif_id = ? WHERE abs.evenement.evenement_id = ?");
        values.addNumber(pOEvenement.getObject("motif").getInteger("motif_id")).addNumber(Integer.parseInt(pIIdEvenement));

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void createEvenement(JsonObject poEvenement, Handler<Either<String, JsonObject>> handler) {

        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("INSERT INTO abs.evenement ")
                .append("(evenement_timestamp_arrive,evenement_timestamp_depart,evenement_commentaire,evenement_saisie_cpe,fk_eleve_id,fk_appel_id,fk_type_evt_id,fk_pj_pj,fk_motif_id) ")
                .append("VALUES ");

        String sTimestampArrive = poEvenement.getString("evenement_timestamp_arrive");
        String sTimestamDepart = poEvenement.getString("evenement_timestamp_depart");

        if(sTimestampArrive != null && !sTimestampArrive.trim().isEmpty()) {
            query.append("(to_timestamp(?, "+gsFormatTimestampWithoutTimeZone+"),?,?,?,?,?,?,?,?) RETURNING *");
        } else if(sTimestamDepart != null && !sTimestamDepart.trim().isEmpty()) {
            query.append("(?, to_timestamp(?, "+gsFormatTimestampWithoutTimeZone+"),?,?,?,?,?,?,?) RETURNING *");
        } else {
            query.append("( ?,?,?,?,?,?,?,?,?) RETURNING *");
        }

        values.addString(sTimestampArrive);
        values.addString(sTimestamDepart);
        values.addString(poEvenement.getString("evenement_commentaire"));
        values.addBoolean(poEvenement.getBoolean("evenement_saisie_cpe"));
        values.addNumber(poEvenement.getInteger("fk_eleve_id"));
        values.addNumber(poEvenement.getInteger("fk_appel_id"));
        values.addNumber(poEvenement.getInteger("fk_type_evt_id"));
        values.addNumber(poEvenement.getInteger("fk_pj_pj"));
        values.addNumber(poEvenement.getInteger("fk_motif_id"));

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void updateEvenement(JsonObject poEvenement, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();


        query.append("UPDATE abs.evenement SET ")
                .append("(evenement_timestamp_arrive,evenement_timestamp_depart,evenement_commentaire,evenement_saisie_cpe,fk_eleve_id,fk_appel_id,fk_type_evt_id,fk_pj_pj,fk_motif_id) ")
                .append("= ");

        String sTimestampArrive = poEvenement.getString("evenement_timestamp_arrive");
        String sTimestamDepart = poEvenement.getString("evenement_timestamp_depart");

        if(sTimestampArrive != null && !sTimestampArrive.trim().isEmpty()) {
            query.append("(to_timestamp(?, "+gsFormatTimestampWithoutTimeZone+"),?,?,?,?,?,?,?,?) ");
        } else if(sTimestamDepart != null && !sTimestamDepart.trim().isEmpty()) {
            query.append("(?, to_timestamp(?, "+gsFormatTimestampWithoutTimeZone+"),?,?,?,?,?,?,?) ");
        } else {
            query.append("( ?,?,?,?,?,?,?,?,?) ");
        }

        query.append("WHERE evenement_id = ? RETURNING *");

        values.addString(sTimestampArrive);
        values.addString(sTimestamDepart);
        values.addString(poEvenement.getString("evenement_commentaire"));
        values.addBoolean(poEvenement.getBoolean("evenement_saisie_cpe"));
        values.addNumber(poEvenement.getInteger("fk_eleve_id"));
        values.addNumber(poEvenement.getInteger("fk_appel_id"));
        values.addNumber(poEvenement.getInteger("fk_type_evt_id"));
        values.addNumber(poEvenement.getInteger("fk_pj_pj"));
        values.addNumber(poEvenement.getInteger("fk_motif_id"));
        values.addNumber(poEvenement.getInteger("evenement_id"));

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void deleteEvenement(int poEvenementId, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("DELETE FROM abs.evenement WHERE abs.evenement.evenement_id = ?");
        values.addNumber(poEvenementId);

        Sql.getInstance().prepared(query.toString(), values, validUniqueResultHandler(handler));
    }

    @Override
    public void getObservations(String psEtablissementId, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT evenement.evenement_id, evenement.evenement_commentaire, cours.cours_timestamp_dt, cours.cours_timestamp_fn " +
                "FROM abs.evenement, viesco.cours, abs.appel " +
                "WHERE evenement.evenement_commentaire IS NOT NULL " +
                "AND evenement.fk_appel_id = appel.appel_id " +
                "AND appel.fk_cours_id = cours.cours_id " +
                "AND cours.cours_timestamp_dt > to_timestamp(?,'YYYY-MM-DD HH24:MI:SS') " +
                "AND cours.cours_timestamp_fn < to_timestamp(?,'YYYY-MM-DD HH24:MI:SS') "+
                "AND cours.fk4j_etab_id = ?::uuid");

        values.addString(psDateDebut).addString(psDateFin).addString(psEtablissementId);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getEvenementClasseCours(String psClasseId, String psCoursId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT evenement.*, eleve.eleve_id, eleve.fk4j_user_id, appel.fk_cours_id " +
                "FROM abs.evenement, viesco.eleve, viesco.rel_eleve_classe, abs.appel " +
                "WHERE evenement.fk_eleve_id = eleve.eleve_id " +
                "AND eleve.eleve_id = rel_eleve_classe.fk_eleve_id " +
                "AND rel_eleve_classe.fk_classe_id = ? " +
                "AND evenement.fk_appel_id = appel.appel_id " +
                "AND appel.fk_cours_id = ?");

        values.addNumber(Integer.parseInt(psClasseId)).addNumber(Integer.parseInt(psCoursId));

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getAbsencesDernierCours(String psUserId, Integer psClasseId, Integer psCoursId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT evenement.fk_eleve_id " +
                "FROM abs.evenement " +
                "WHERE evenement.fk_appel_id = ( " +
                "SELECT appel.appel_id " +
                "FROM viesco.cours, viesco.personnel, viesco.rel_personnel_cours, abs.appel " +
                "WHERE personnel.fk4j_user_id = ?::uuid " +
                "AND personnel.personnel_id = rel_personnel_cours.fk_personnel_id " +
                "AND rel_personnel_cours.fk_cours_id = cours.cours_id " +
                "AND cours.fk_classe_id = ? " +
                "AND appel.fk_cours_id = cours.cours_id " +
                "AND cours.cours_id != ? " +
                "AND cours.cours_timestamp_dt < (SELECT cours.cours_timestamp_dt FROM viesco.cours WHERE cours.cours_id = ?) " +
                "ORDER BY cours.cours_timestamp_dt DESC " +
                "LIMIT 1) " +
                "AND evenement.fk_type_evt_id = 1 ");

        values.addString(psUserId).addNumber(psClasseId).addNumber(psCoursId).addNumber(psCoursId);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getEvtClassePeriode(Integer piClasseId, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT evenement.*, cours.cours_id " +
                "FROM abs.evenement, abs.appel, viesco.cours " +
                "WHERE cours.fk_classe_id = ? " +
                "AND evenement.fk_appel_id = appel.appel_id " +
                "AND appel.fk_cours_id = cours.cours_id " +
                "AND cours.cours_timestamp_dt > to_timestamp(?,'YYYY-MM-DD HH24:MI:SS') " +
                "AND cours.cours_timestamp_fn < to_timestamp(?,'YYYY-MM-DD HH24:MI:SS')");

        values.addNumber(piClasseId).addString(psDateDebut).addString(psDateFin);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }
}
