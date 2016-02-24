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
        super(Viescolaire.ABSC_SCHEMA, Viescolaire.VSCO_APPEL_TABLE);
    }

    @Override
    public void getAppelPeriode(String psIdEtablissement, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray value = new JsonArray();

        query.append("SELECT personnel.nom, personnel.prenom, cours.id_etab_neo4j, pv_appel.id_appel, cours.timestamp_debut, cours.timestamp_fin, cours.matiere, cours.salle ")
                .append("FROM abs.pv_appel, viesco.personnel, viesco.cours ")
                .append("WHERE pv_appel.id_personnel = personnel.id_personnel ")
                .append("AND pv_appel.id_cours = cours.id ")
                .append("AND cours.id_etab_neo4j = ? ")
                .append("AND cours.timestamp_debut > to_date(?, 'DD-MM-YYYY HH24:MI:SS) ")
                .append("AND cours.timestamp_debut < to_date(?, 'DD-MM-YYYY HH24:MI:SS) ");

        value.addString(psIdEtablissement).addString(psDateDebut).addString(psDateFin);

        Sql.getInstance().prepared(query.toString(), value, SqlResult.validResultHandler(handler));
    }
}
