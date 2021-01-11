package fr.openent.viescolaire.service;

import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;

public interface TrombinoscopeReportService {

    /**
     * get list of trombinoscope failures
     *
     * @param structureId   Structure Identifier
     * @param handler       Function handler returning data
     */
    void get(String structureId, Integer limit, Integer offset, Handler<Either<String, JsonArray>> handler);
}
