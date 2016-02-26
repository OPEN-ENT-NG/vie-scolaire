package org.cgi.absences.service.impl;

import fr.wseduc.webutils.Either;
import org.cgi.Viescolaire;
import org.cgi.absences.service.IAbscEvenementService;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.entcore.common.http.response.DefaultResponseHandler.leftToResponse;
import static org.entcore.common.sql.SqlResult.validUniqueResultHandler;

/**
 * Created by ledunoiss on 25/02/2016.
 */
public class CAbscEvenementService extends SqlCrudService implements IAbscEvenementService{
    public CAbscEvenementService() {
        super(Viescolaire.ABSC_SCHEMA, Viescolaire.ABSC_EVENEMENT_TABLE);
    }

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
        .append("VALUES ")
        .append("(?,?,?,?,?,?,?,?,?)");

        values.addString(poEvenement.getString("evenement_timestamp_arrive"));
        values.addString(poEvenement.getString("evenement_timestamp_depart"));
        values.addString(poEvenement.getString("evenement_commentaire"));
        values.addBoolean(poEvenement.getBoolean("evenement_saisie_cpe"));
        values.addNumber(poEvenement.getInteger("fk_eleve_id"));
        values.addNumber(poEvenement.getInteger("fk_appel_id"));
        values.addNumber(poEvenement.getInteger("fk_type_evt_id"));
        values.addNumber(poEvenement.getInteger("fk_pj_pj"));
        values.addNumber(poEvenement.getInteger("fk_motif_id"));

        Handler<Message<JsonObject>> handlerSql = new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> event) {
                JsonObject bod = event.body();
            }
        };
        Sql.getInstance().prepared(query.toString(), values, handlerSql);
    }

    @Override
    public void updateEvenement(JsonObject poEvenement, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("UPDATE abs.evenement SET ")
        .append("(evenement_timestamp_arrive,evenement_timestamp_depart,evenement_commentaire,evenement_saisie_cpe,fk_eleve_id,fk_appel_id,fk_type_evt_id,fk_pj_pj,fk_motif_id) ")
        .append("= ")
        .append("(?,?,?,?,?,?,?,?,?) ")
        .append("WHERE evenement_id = ?");

        values.addString(poEvenement.getString("evenement_timestamp_arrive"));
        values.addString(poEvenement.getString("evenement_timestamp_depart"));
        values.addString(poEvenement.getString("evenement_commentaire"));
        values.addBoolean(poEvenement.getBoolean("evenement_saisie_cpe"));
        values.addNumber(poEvenement.getInteger("fk_eleve_id"));
        values.addNumber(poEvenement.getInteger("fk_appel_id"));
        values.addNumber(poEvenement.getInteger("fk_type_evt_id"));
        values.addNumber(poEvenement.getInteger("fk_pj_pj"));
        values.addNumber(poEvenement.getInteger("fk_motif_id"));
        values.addNumber(poEvenement.getInteger("evenement_id"));

        Sql.getInstance().prepared(query.toString(), values, validUniqueResultHandler(handler));
    }

    @Override
    public void deleteEvenement(int poEvenementId, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("DELETE FROM abs.evenement WHERE abs.evenement.evenement_id = ?");
        values.addNumber(poEvenementId);

        Sql.getInstance().prepared(query.toString(), values, validUniqueResultHandler(handler));
    }
}
