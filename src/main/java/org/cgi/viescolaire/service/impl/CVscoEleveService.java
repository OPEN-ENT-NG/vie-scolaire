package org.cgi.viescolaire.service.impl;

import fr.wseduc.webutils.Either;
import org.cgi.Viescolaire;
import org.cgi.viescolaire.service.IVscoEleveService;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
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

        query.append("SELECT eleve.id, eleve.id_user_neo4j, eleve.nom, eleve.prenom")
        .append("FROM viesco.eleve ")
        .append("WHERE classe.id_classe_neo4j = ? ")
        .append("AND classe.id = est_membre_de.id_classe ")
        .append("AND eleve.id = est_membre_de.id_eleve");

        values.addString(pSIdClasse);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }
}
