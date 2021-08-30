package fr.openent.viescolaire.helper;

import fr.openent.viescolaire.model.*;
import io.vertx.core.json.*;
import java.util.*;
import java.util.stream.*;

public class MultiTeachingHelper {

    private MultiTeachingHelper() {throw new IllegalStateException("Helper class");}

    public static List<MultiTeaching> toMultiTeachingList(JsonArray multiTeachings) {
        return multiTeachings.stream().map(m ->
                new MultiTeaching((JsonObject) m)).collect(Collectors.toList());
    }
}
