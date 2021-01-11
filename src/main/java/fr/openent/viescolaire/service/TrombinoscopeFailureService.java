package fr.openent.viescolaire.service;

import fr.openent.viescolaire.model.Trombinoscope.TrombinoscopeFailure;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.List;

public interface TrombinoscopeFailureService {

    /**
     * get list of trombinoscope failures
     *
     * @param structureId   Structure Identifier
     * @param handler       Function handler returning data
     */
    void get(String structureId, Handler<AsyncResult<List<TrombinoscopeFailure>>> handler);

    /**
     * get specific trombinoscope failure
     *
     * @param structureId   Structure Identifier
     * @param failureId     Failure Identifier
     * @param handler       Function handler returning data
     */
    void get(String structureId, String failureId, Handler<AsyncResult<TrombinoscopeFailure>> handler);

    /**
     * create trombinoscope failure
     *
     * @param structureId   Structure Identifier
     * @param path          path root (e.g ../trombinoscope/..) from directory where it tried to import
     * @param pictureId     student Identifier
     * @param handler       Function handler returning data
     */
    void create(String structureId, String path, String pictureId, Handler<AsyncResult<JsonObject>> handler);

    /**
     * clear trombinoscope failures on structure identifier
     *
     * @param structureId   Structure Identifier
     * @param handler       Function handler returning data
     */
    void delete(String structureId, Handler<AsyncResult<Void>> handler);
}
