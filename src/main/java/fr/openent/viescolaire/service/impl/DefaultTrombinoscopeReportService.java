package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.helper.TrombinoscopeHelper;
import fr.openent.viescolaire.model.Trombinoscope.TrombinoscopeFailure;
import fr.openent.viescolaire.model.Trombinoscope.TrombinoscopeReport;
import fr.openent.viescolaire.service.TrombinoscopeReportService;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.webutils.Either;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.mongodb.MongoDbResult;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;

import java.util.List;

public class DefaultTrombinoscopeReportService implements TrombinoscopeReportService {

    private static final Logger log = LoggerFactory.getLogger(DefaultTrombinoscopeReportService.class);

    @Override
    public void get(String structureId, Integer limit, Integer offset, Handler<Either<String, JsonArray>> handler) {
        JsonObject query = new JsonObject()
                .put("structureId", structureId);

        MongoDb.getInstance().find(Viescolaire.VSCO_SCHEMA + ".trombinoscopeReport", query,
                new JsonObject().put("createdAt", -1), null, offset, limit, 2147483647,
                MongoDbResult.validResultsHandler(handler));
    }
}
