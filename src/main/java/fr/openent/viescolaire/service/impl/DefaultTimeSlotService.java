package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.TimeSlotService;
import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
public class DefaultTimeSlotService implements TimeSlotService {

    @Override
    public void getSlotProfiles(String id_structure, Handler<Either<String, JsonArray>> handler) {
        String query = "SELECT * FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_TIME_SLOTS +
                " WHERE id_structure = ?";
        JsonArray params = new fr.wseduc.webutils.collections.JsonArray().add(id_structure);

        Sql.getInstance().prepared(query, params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void saveTimeProfil(JsonObject timeSlot, Handler<Either<String, JsonArray>> handler) {
        String query = "INSERT INTO " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_TIME_SLOTS +
                "(id_structure, id) " +  "VALUES (?, ?)" + "ON CONFLICT (id_structure) DO UPDATE SET id = ? RETURNING *;";
        JsonArray params = new fr.wseduc.webutils.collections.JsonArray()
                .add(timeSlot.getString("id_structure"))
                .add(timeSlot.getString("id"))
                .add(timeSlot.getString("id"));

        Sql.getInstance().prepared(query, params, SqlResult.validResultHandler(handler));
    }

}
