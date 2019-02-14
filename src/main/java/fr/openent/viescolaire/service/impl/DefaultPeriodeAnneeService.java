package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.PeriodeAnneeService;
import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;

public class DefaultPeriodeAnneeService implements PeriodeAnneeService {

    protected static final Logger log = LoggerFactory.getLogger(DefaultCoursService.class);

    @Override
    public void getPeriodeAnnee(String structure, Handler<Either<String, JsonObject>> handler) {
        String query = "SELECT * FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_SETTING_PERIOD +
                " WHERE id_structure = ? AND code = 'YEAR';";
        JsonArray params = new fr.wseduc.webutils.collections.JsonArray().add(structure);

        Sql.getInstance().prepared(query, params, SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void listExclusion(String structureId, Handler<Either<String, JsonArray>> handler) {
        String query = "SELECT * FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_SETTING_PERIOD +
                " WHERE id_structure = ? AND code = 'EXCLUSION'";
        JsonArray params = new fr.wseduc.webutils.collections.JsonArray().add(structureId);

        Sql.getInstance().prepared(query, params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void createPeriode(JsonObject periode, boolean isOpening, Handler<Either<String, JsonArray>> handler) {
        String query = "INSERT INTO " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_SETTING_PERIOD + "(" +
                "start_date, end_date, description, id_structure, is_opening, code) " +
                "VALUES (to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS'), to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS'), ?, ?, ?";
        JsonArray params = new fr.wseduc.webutils.collections.JsonArray()
                .add(periode.getString("start_date"))
                .add(periode.getString("end_date"))
                .add(periode.getString("description"))
                .add(periode.getString("id_structure"))
                .add(isOpening);
        if (periode.containsKey("code")) {
            params.add("YEAR");
        }
        else params.add("EXCLUSION");
        query += ", ? ";
        query += ") RETURNING *;";

        Sql.getInstance().prepared(query, params, SqlResult.validResultHandler(handler));
    }


    @Override
    public void updatePeriode(Integer id, JsonObject periode, boolean isOpening, Handler<Either<String, JsonArray>> handler) {
        String query = "UPDATE "+ Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_SETTING_PERIOD +
                " SET start_date= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS'), end_date = to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS'), " +
                "description= ?, id_structure= ?" + " WHERE id = ? RETURNING *;";
        JsonArray params = new fr.wseduc.webutils.collections.JsonArray()
                .add(periode.getString("start_date"))
                .add(periode.getString("end_date"))
                .add(periode.getString("description"))
                .add(periode.getString("id_structure"))
                .add(id);

        Sql.getInstance().prepared(query, params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void deleteExclusion(Integer exclusionId, Handler<Either<String, JsonArray>> result) {
        String query = "DELETE FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_SETTING_PERIOD +
                " WHERE id = ?;";
        JsonArray params = new fr.wseduc.webutils.collections.JsonArray().add(exclusionId);

        Sql.getInstance().prepared(query, params, SqlResult.validResultHandler(result));
    }
}