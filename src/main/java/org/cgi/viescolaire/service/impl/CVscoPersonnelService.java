package org.cgi.viescolaire.service.impl;

import fr.wseduc.webutils.Either;
import org.cgi.Viescolaire;
import org.cgi.viescolaire.service.IVscoPersonnelService;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;

/**
 * Created by ledunoiss on 19/02/2016.
 */
public class CVscoPersonnelService extends SqlCrudService implements IVscoPersonnelService {
    public CVscoPersonnelService() {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.VSCO_PERSONNEL_TABLE);
    }

    @Override
    public void getEnseignantEtablissement(String idEtablissement, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray params = new JsonArray();

        query.append("SELECT personnel.id_personnel, personnel.id_user_neo4j, personnel.nom, personnel.prenom, personnel.enseigne ")
        .append("FROM viesco.personnel WHERE personnel.id_etab_neo4j = ?::uuid ")
        .append("AND personnel.profil = 'Teacher'");

        params.addString(idEtablissement);

        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));
    }
}
