package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.core.constants.*;
import fr.openent.viescolaire.helper.*;
import fr.openent.viescolaire.model.*;
import fr.openent.viescolaire.service.PeriodeAnneeService;
import fr.wseduc.webutils.Either;
import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;

public class DefaultPeriodeAnneeService implements PeriodeAnneeService {

    protected static final Logger log = LoggerFactory.getLogger(DefaultPeriodeAnneeService.class);

    @Override
    public Future<Period> getPeriodeAnnee(String structureId) {
        Promise<Period> promise = Promise.promise();
        this.getPeriodeAnnee(structureId, res -> {
            if (res.isRight()) {
                promise.complete(new Period(res.right().getValue()));
            } else {
                promise.fail(res.left().getValue());
            }
        });
        return promise.future();
    }

    @Override
    public void getPeriodeAnnee(String structure, Handler<Either<String, JsonObject>> handler) {
        String query = "SELECT * FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_SETTING_PERIOD +
                " WHERE id_structure = ? AND code = 'YEAR';";
        JsonArray params = new JsonArray().add(structure);

        Sql.getInstance().prepared(query, params, SqlResult.validUniqueResultHandler(handler));
    }

    @Override
    public void listExclusion(String structureId, Handler<Either<String, JsonArray>> handler) {
        String query = "SELECT * FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_SETTING_PERIOD +
                " WHERE id_structure = ? AND code = 'EXCLUSION'";
        JsonArray params = new JsonArray().add(structureId);

        Sql.getInstance().prepared(query, params, SqlResult.validResultHandler(handler));
    }

    @Override
    public Future<JsonArray> createPeriode(Period period, boolean isOpening) {
        Promise<JsonArray> promise = Promise.promise();
        this.createPeriode(period.toJson(), isOpening, FutureHelper.handlerEitherPromise(promise,
                String.format("[Viescolaire@%s::createPeriode] Failed to create period", this.getClass().getSimpleName())));
        return promise.future();
    }

    @Override
    public void createPeriode(JsonObject periode, boolean isOpening, Handler<Either<String, JsonArray>> handler) {
        String query = "INSERT INTO " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_SETTING_PERIOD + "(" +
                "start_date, end_date, description, id_structure, is_opening, code) " +
                "VALUES (to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS'), to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS'), ?, ?, ?";
        JsonArray params = new JsonArray()
                .add(periode.getString(Field.START_DATE))
                .add(periode.getString(Field.END_DATE))
                .add(periode.getString(Field.DESCRIPTION))
                .add(periode.getString(Field.ID_STRUCTURE))
                .add(isOpening);
        if (periode.containsKey(Field.CODE)) {
            params.add(PeriodeCode.YEAR);
        }
        else params.add(PeriodeCode.EXCLUSION);
        query += ", ? ";
        query += ") RETURNING *;";

        Sql.getInstance().prepared(query, params, SqlResult.validResultHandler(handler));
    }


    @Override
    public Future<JsonArray> updatePeriode(Integer id, Period period, boolean isOpening) {
        Promise<JsonArray> promise = Promise.promise();
        this.updatePeriode(id, period.toJson(), isOpening, FutureHelper.handlerEitherPromise(promise,
                String.format("[Viescolaire@%s::updatePeriode] Failed to update period", this.getClass().getSimpleName())));
        return promise.future();
    }

    @Override
    public void updatePeriode(Integer id, JsonObject periode, boolean isOpening, Handler<Either<String, JsonArray>> handler) {
        String query = "UPDATE "+ Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_SETTING_PERIOD +
                " SET start_date= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS'), end_date = to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS'), " +
                "description= ?, id_structure= ?" + " WHERE id = ? RETURNING *;";
        JsonArray params = new JsonArray()
                .add(periode.getString(Field.START_DATE))
                .add(periode.getString(Field.END_DATE))
                .add(periode.getString(Field.DESCRIPTION))
                .add(periode.getString(Field.ID_STRUCTURE))
                .add(id);

        Sql.getInstance().prepared(query, params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void deleteExclusion(Integer exclusionId, Handler<Either<String, JsonArray>> result) {
        String query = "DELETE FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_SETTING_PERIOD +
                " WHERE id = ?;";
        JsonArray params = new JsonArray().add(exclusionId);

        Sql.getInstance().prepared(query, params, SqlResult.validResultHandler(result));
    }
}