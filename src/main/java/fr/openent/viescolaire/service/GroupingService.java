package fr.openent.viescolaire.service;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public interface GroupingService {
    Future<JsonObject> createGrouping(String name, String structureId);
}
