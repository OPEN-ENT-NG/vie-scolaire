package fr.openent.viescolaire.service;

import fr.openent.viescolaire.model.Trombinoscope.TrombinoscopeReport;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;


public interface TrombinoscopeService {

    /**
     * get trombinoscope setting feature
     *
     * @param structureId   Structure Identifier
     * @param handler       Function handler returning data
     */
    void getSetting(String structureId, Handler<AsyncResult<Boolean>> handler);

    /**
     * set trombinoscope setting to toggle enable/disable (create if no exist)
     *
     * @param structureId   Structure Identifier
     * @param active        student Identifier
     * @param handler       Function handler returning data
     */
    void setSetting(String structureId, Boolean active, Handler<AsyncResult<JsonObject>> handler);

    /**
     * get trombinoscope info from a student
     *
     * @param structureId   Structure Identifier
     * @param studentId     student Identifier
     * @param handler       Function handler returning data
     */
    void get(String structureId, String studentId, final Handler<AsyncResult<JsonObject>> handler);

    /**
     * import trombinoscope
     *
     * @param structureId   Structure Identifier
     * @param path          Uploaded from file unzipped
     * @param request       HttpServerRequest
     * @param handler       Function handler returning data
     */
    void process(String structureId, String path, TrombinoscopeReport trombinoscopeReport,
                 final HttpServerRequest request, final Handler<AsyncResult<Void>> handler);

    /**
     * create trombinoscope
     *
     * @param structureId   Structure Identifier
     * @param studentId     Student identifier
     * @param pictureId     picture identifier
     * @param handler       Function handler returning data
     */
    void create(String structureId, String studentId, String pictureId, Handler<AsyncResult<JsonObject>> handler);

    /**
     * delete picture from a studentId
     *
     * @param structureId   Structure Identifier {@link String}
     * @param studentId     Student identifier {@link String}
     * @return Future       {@link Future} of {@link JsonObject} completed or failure
     */
    Future<JsonObject> deletePicture(String structureId, String studentId);
}
