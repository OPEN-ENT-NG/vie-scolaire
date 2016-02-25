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
}
