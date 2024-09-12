package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.db.DBService;
import fr.openent.viescolaire.helper.FutureHelper;
import fr.openent.viescolaire.helper.TrombinoscopeHelper;
import fr.openent.viescolaire.model.Trombinoscope.TrombinoscopeFailure;
import fr.openent.viescolaire.service.TrombinoscopeFailureService;
import fr.openent.viescolaire.utils.FileHelper;
import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.storage.Storage;

import java.util.List;
import java.util.stream.Collectors;

public class DefaultTrombinoscopeFailureService extends DBService implements TrombinoscopeFailureService {

    private static final Logger log = LoggerFactory.getLogger(DefaultTrombinoscopeFailureService.class);

    private final Storage storage;

    public DefaultTrombinoscopeFailureService(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void get(String structureId, Handler<AsyncResult<List<TrombinoscopeFailure>>> handler) {
        JsonArray params = new JsonArray();
        String query = getBasicQuery(structureId, params);

        sql.prepared(query, params, SqlResult.validResultHandler(result -> {
            if (result.isLeft()) {
                String messageError = "[Viescolaire@DefaultTrombinoscopeFailureService::get] Failed to get failures from structure "
                        + structureId + ".";
                log.error(messageError, result.left().getValue());
                handler.handle(Future.failedFuture(messageError));
                return;
            }
            handler.handle(Future.succeededFuture(TrombinoscopeHelper.toFailureList(result.right().getValue())));
        }));
    }

    @Override
    public void get(String structureId, String failureId, Handler<AsyncResult<TrombinoscopeFailure>> handler) {

        JsonArray params = new JsonArray();
        String query = getBasicQuery(structureId, params) + "AND id = ?";

        params.add(failureId);

        sql.prepared(query, params, SqlResult.validUniqueResultHandler(result -> {
            if (result.isLeft()) {
                String messageError = "[Viescolaire@DefaultTrombinoscopeFailureService::getFailures] Failed to get failures "
                        + failureId + " from structure " + structureId + ".";
                log.error(messageError, result.left().getValue());
                handler.handle(Future.failedFuture(messageError));
                return;
            }
            if (result.right().getValue().isEmpty()) {
                handler.handle(Future.failedFuture("404"));
            } else {
                handler.handle(Future.succeededFuture(new TrombinoscopeFailure(result.right().getValue())));
            }
        }));
    }

    private String getBasicQuery(String structureId, JsonArray params) {
        params.add(structureId);
        return " SELECT * FROM " + Viescolaire.VSCO_SCHEMA + ".trombinoscope_failure  WHERE structure_id = ? ";
    }

    @Override
    public void create(String structureId, String path, String pictureId, Handler<AsyncResult<JsonObject>> handler) {
        String query = " INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".trombinoscope_failure "
                + " (structure_id, path, picture_id) "
                + " VALUES (?, ?, ?) ";

        JsonArray params = new JsonArray()
                .add(structureId)
                .add(path)
                .add(pictureId);

        sql.prepared(query, params, SqlResult.validUniqueResultHandler(result -> {
            if (result.isLeft()) {
                String messageError = "[Viescolaire@DefaultTrombinoscopeFailureService::create] Failed to create failure.";
                log.error(messageError, result.left().getValue());
                handler.handle(Future.failedFuture(messageError));
                return;
            }
            handler.handle(Future.succeededFuture(result.right().getValue()));
        }));
    }

    @Override
    public void delete(String structureId, Handler<AsyncResult<Void>> handler) {
        get(structureId, failures -> {
            if (failures.failed()) {
                String message = "[Viescolaire@DefaultTrombinoscopeService::delete] Failed to get failures from structure "
                        + structureId + ".";
                handler.handle(Future.failedFuture(message));
                return;
            }

            List<String> pictureIds = failures.result()
                    .stream()
                    .map(TrombinoscopeFailure::getPictureId)
                    .collect(Collectors.toList());

            Promise<JsonObject> removePicturePromise = Promise.promise();
            Promise<JsonObject> deleteRowsPromise = Promise.promise();


            FileHelper.removeFiles(storage, pictureIds, FutureHelper.promiseHandler(removePicturePromise));
            deleteRequest(structureId, FutureHelper.promiseHandler(deleteRowsPromise));

            Future.all(removePicturePromise.future(), deleteRowsPromise.future()).onComplete(result -> {
                if (result.failed()) {
                    String message = "[Viescolaire@DefaultTrombinoscopeService::delete] Failed to delete failures from structure "
                            + structureId + ".";
                    handler.handle(Future.failedFuture(message));
                    return;
                }
                handler.handle(Future.succeededFuture());
            });

        });
    }

    private void deleteRequest(String structureId, Handler<AsyncResult<JsonObject>> handler) {

        String query = "DELETE FROM " + Viescolaire.VSCO_SCHEMA + ".trombinoscope_failure " +
                " WHERE structure_id = ? ";

        JsonArray params = new JsonArray()
                .add(structureId);

        sql.prepared(query, params, SqlResult.validUniqueResultHandler(result -> {
            if (result.isLeft()) {
                String messageError = "[Viescolaire@DefaultTrombinoscopeFailureService::getFailures] Failed to remove failures from structure "
                        + structureId + ".";
                log.error(messageError, result.left().getValue());
                handler.handle(Future.failedFuture(messageError));
                return;
            }

            handler.handle(Future.succeededFuture(result.right().getValue()));
        }));
    }

}
