package fr.openent.viescolaire.helper;

import fr.openent.viescolaire.utils.ServicesHelper;
import fr.openent.viescolaire.utils.SortUtils;

import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.collections.JsonArray;
import fr.wseduc.webutils.collections.JsonObject;

import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class SubjectHelper {

    private static Logger log = LoggerFactory.getLogger(ServicesHelper.class);

    public static void addRankForSubject(Either<String, io.vertx.core.json.JsonArray> responseNeo4j,
                                         Handler<Either<String, io.vertx.core.json.JsonArray>> handler) {
        if (responseNeo4j.isLeft()) {
            log.error("Problem before function addRankForSubject: " + responseNeo4j.left().getValue());
            handler.handle(new Either.Left<>(responseNeo4j.left().getValue()));
            return;
        }
        JsonArray subjects = (JsonArray) responseNeo4j.right().getValue();

        for (int i = 0; i < subjects.size(); i++) {
            JsonObject subject = (JsonObject) subjects.getJsonObject(i);
            if (subject.getValue("rank") == null) { //Add a rank to subjects that don't have one
                subject.put("rank", i);
            }
        }

        JsonArray result = SortUtils.sortJsonArrayIntValue("rank", subjects);
        handler.handle(new Either.Right<>(result));
    }
}
