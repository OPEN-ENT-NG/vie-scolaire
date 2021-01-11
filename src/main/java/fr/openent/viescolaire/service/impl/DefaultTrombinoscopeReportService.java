package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.db.DBService;
import fr.openent.viescolaire.service.TrombinoscopeReportService;
import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.mongodb.MongoDbResult;

public class DefaultTrombinoscopeReportService extends DBService implements TrombinoscopeReportService {

    private static final Logger log = LoggerFactory.getLogger(DefaultTrombinoscopeReportService.class);

    @Override
    public void get(String structureId, Integer limit, Integer offset, Handler<Either<String, JsonArray>> handler) {
        JsonObject query = new JsonObject()
                .put("structureId", structureId);

       mongoDb.find(Viescolaire.VSCO_SCHEMA + ".trombinoscopeReport", query,
                new JsonObject().put("createdAt", -1), null, offset, limit, 2147483647,
                MongoDbResult.validResultsHandler(handler));
    }
}
