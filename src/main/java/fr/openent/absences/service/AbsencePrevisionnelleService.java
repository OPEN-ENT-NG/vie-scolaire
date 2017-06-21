package fr.openent.absences.service;

import fr.wseduc.webutils.Either;
import org.entcore.common.service.CrudService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.impl.Json;

/**
 * Created by rahnir on 20/06/2017.
 */
public interface AbsencePrevisionnelleService extends CrudService {
    /**
     * Creer une absence previsionnelle
     * @param poAbscencePrev
     * @param handler
     */
    public void createAbsencePrev(JsonObject poAbscencePrev,final Handler<Either<String, JsonObject>> handler);
}
