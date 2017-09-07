package fr.openent.absences.service.impl;

import fr.openent.Viescolaire;
import fr.openent.absences.service.DeclarationService;
import fr.wseduc.webutils.Either;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

/**
 * Created by rollinq on 14/08/2017.
 */
public class DefaultDeclarationService extends SqlCrudService implements DeclarationService {

    private static final Logger log = LoggerFactory.getLogger(DefaultDeclarationService.class);

    public DefaultDeclarationService() {
        super(Viescolaire.ABSC_SCHEMA, Viescolaire.ABSC_DECLARATION_TABLE);
    }

    @Override
    public void getDeclaration(String psEtablissementId, String psOwnerId, String psStudentId, String psDateDebut,
                               String psDateFin, Boolean pbTraitee, Integer piNumber,
                               Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        if(psStudentId == null && psOwnerId == null && psDateDebut == null && psDateFin == null && piNumber == null) {
            handler.handle(new Either.Left<String, JsonArray>("At least one parameter should be not null."));
            log.error("At least one parameter should be not null.");
        }

        query.append("SELECT * ")
                .append("FROM " + Viescolaire.ABSC_SCHEMA + ".declaration ")
                .append("WHERE id_etablissement = ? AND ");
        values.addString(psEtablissementId);

        if(psDateDebut != null && psDateFin != null) {
            query.append("created <= to_timestamp(?, 'YYYY-MM-DD') AND " +
                    "created >= to_timestamp(?, 'YYYY-MM-DD') AND ");
            values.addString(psDateFin).addString(psDateDebut);
        }

        if(psStudentId != null) {
            query.append("id_eleve = ? AND ");
            values.addString(psStudentId);
        }

        if(psOwnerId != null) {
            query.append("owner = ? AND ");
            values.addString(psOwnerId);
        }

        if(pbTraitee != null) {
            query.append("traitee = ? AND ");
            values.addBoolean(pbTraitee);
        }

        query.delete(query.toString().length() - 4, query.toString().length() - 1);
        query.append("ORDER BY created DESC ");
        if(piNumber != null) {
            query.append("LIMIT ?");
            values.addNumber(piNumber);
        }

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void createDeclaration(JsonObject poDeclaration, UserInfos user,
                                  Handler<Either<String, JsonObject>> handler) {
        super.create(poDeclaration, user, handler);
    }

    @Override
    public void updateDeclaration(JsonObject poDeclaration, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("UPDATE " + Viescolaire.ABSC_SCHEMA + ".declaration")
                .append(" SET titre = ?, timestamp_dt = to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS'), ")
                .append("timestamp_fn = to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS'), commentaire = ?, modified = NOW()")
                .append(" WHERE id = ?");
                values.addString(poDeclaration.getString("titre"))
                        .addString(poDeclaration.getString("timestamp_dt"))
                        .addString(poDeclaration.getString("timestamp_fn"))
                        .addString(poDeclaration.getString("commentaire"))
                        .addNumber(poDeclaration.getNumber("id"));

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validRowsResultHandler(handler));
    }

    @Override
    public void deleteDeclaration(Number oDeclarationId, Handler<Either<String, JsonObject>> handler) {
        super.delete(oDeclarationId.toString(), handler);
    }
}
