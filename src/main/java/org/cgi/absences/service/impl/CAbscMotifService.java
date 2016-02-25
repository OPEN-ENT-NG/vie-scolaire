package org.cgi.absences.service.impl;

import fr.wseduc.webutils.Either;
import org.cgi.Viescolaire;
import org.cgi.absences.service.IAbscMotifService;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;

/**
 * Created by ledunoiss on 23/02/2016.
 */
public class CAbscMotifService extends SqlCrudService implements IAbscMotifService {
    public CAbscMotifService() {
        super(Viescolaire.ABSC_SCHEMA, Viescolaire.ABSC_MOTIF_TABLE);
    }

    @Override
    public void getAbscMotifsEtbablissement(String psIdEtablissement, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT * ")
            .append("FROM abs.motif ")
            .append("WHERE fk4j_etab_id=?::uuid");

        values.addString(psIdEtablissement);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }
}
