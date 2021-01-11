package fr.openent.viescolaire.helper;

import fr.openent.viescolaire.model.Trombinoscope.TrombinoscopeFailure;
import fr.openent.viescolaire.model.Trombinoscope.TrombinoscopeReport;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class TrombinoscopeHelper {

    /**
     * Convert JsonArray into Failure list
     *
     * @param failures  JsonArray of failures
     * @return new list of failures
     */
    public static List<TrombinoscopeFailure> toFailureList(JsonArray failures) {
        List<TrombinoscopeFailure> trombinoscopeFailureList = new ArrayList<>();
        for (Object o : failures) {
            if (!(o instanceof JsonObject)) continue;
            TrombinoscopeFailure trombinoscopeFailure = new TrombinoscopeFailure((JsonObject) o);
            trombinoscopeFailureList.add(trombinoscopeFailure);
        }
        return trombinoscopeFailureList;
    }

    /**
     * Convert List Failure into failure JsonArray
     *
     * @param trombinoscopeFailureList failure list
     * @return new JsonArray of Failures
     */
    public static JsonArray toJsonArray(List<TrombinoscopeFailure> trombinoscopeFailureList) {
        JsonArray failureArray = new JsonArray();
        for (TrombinoscopeFailure notebook : trombinoscopeFailureList) {
            failureArray.add(notebook.toJsonObject());
        }
        return failureArray;
    }
}
