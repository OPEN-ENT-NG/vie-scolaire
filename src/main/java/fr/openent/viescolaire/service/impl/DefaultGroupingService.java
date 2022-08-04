package fr.openent.viescolaire.service.impl;

import fr.openent.viescolaire.service.GroupingService;
import fr.openent.viescolaire.utils.DateHelper;
import io.vertx.core.json.JsonArray;
import org.entcore.common.sql.Sql;

import java.util.UUID;

public class DefaultGroupingService implements GroupingService {
    public void createGrouping(String name, String structureId) {
        String uuid = UUID.randomUUID().toString();
        JsonArray values = new JsonArray();
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO grouping(id, name, structure_id, created_at, updated_at) ")
                .append("VALUES(?, ?, ?, ?, ?) ON CONFLICT DO NOTHING");
        values.add(uuid);
        values.add(name);
        values.add(structureId);
        values.add(DateHelper.getCurrentDate(DateHelper.HOUR_MINUTES_SECONDS));
        values.add(DateHelper.getCurrentDate(DateHelper.HOUR_MINUTES_SECONDS));
        Sql.getInstance().prepared(query.toString(), values, null);
    }
}
