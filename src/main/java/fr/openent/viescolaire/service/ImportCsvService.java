package fr.openent.viescolaire.service;

import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.storage.Storage;

public interface ImportCsvService {
    void importAbsencesAndRetard(String idClasse, Long idPeriode, Storage storage,
                                 HttpServerRequest request,  Handler<Either<String, JsonObject>> handler);
}
