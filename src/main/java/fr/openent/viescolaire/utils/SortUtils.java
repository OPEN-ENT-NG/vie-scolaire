package fr.openent.viescolaire.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class SortUtils {
    
    private static Logger log =  LoggerFactory.getLogger(ServicesHelper.class);
    
    public static JsonArray sortJsonArrayIntValue(final String KEY_NAME, final JsonArray jsonArray) {
        final List<JsonObject> jsonValues = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            final JsonObject objectNoSorted = (JsonObject) jsonArray.getJsonObject(i);
            jsonValues.add(objectNoSorted);
        }
        Collections.sort(jsonValues, (firstObject, secondeObject) -> {
            int firstValue = 0;
            int secondValue = 0;
            try {
                firstValue = (int) firstObject.getInteger(KEY_NAME);
                secondValue = (int) secondeObject.getInteger(KEY_NAME);
            } catch (final Exception error) {
                log.error("Sort fail when he get integers values: " + error);
            }
            return Integer.compare(firstValue, secondValue);
        });
        
        final JsonArray sortedJsonArray = new JsonArray();
        for (final JsonObject objectSorted : jsonValues) {
            sortedJsonArray.add(objectSorted);
        }
        return sortedJsonArray;
    }
}
