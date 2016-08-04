package org.cgi.viescolaire.service.impl;

import fr.wseduc.webutils.Either;
import org.cgi.Viescolaire;
import org.cgi.viescolaire.service.IVscoClasseService;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.vertx.java.core.Handler;
import org.entcore.common.sql.SqlResult;
import org.vertx.java.core.json.JsonArray;

/**
 * Created by ledunoiss on 19/02/2016.
 */
public class CVscoClasseService extends SqlCrudService implements IVscoClasseService {
    public CVscoClasseService() {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.VSCO_CLASSE_TABLE);
    }

    @Override
    public void getClasseEtablissement(String idEtablissement, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray params = new JsonArray();

        query.append("SELECT classe.classe_id, classe.fk4j_classe_id, classe.classe_libelle ")
                .append("FROM viesco.classe ")
                .append("WHERE classe.fk4j_etab_id = ?::uuid");

        params.addString(idEtablissement);

        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));
    }
}
