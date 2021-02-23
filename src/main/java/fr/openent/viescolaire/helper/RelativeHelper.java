package fr.openent.viescolaire.helper;

import fr.openent.viescolaire.model.Person.Relative;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class RelativeHelper {

    /**
     * Convert JsonArray into relative list
     *
     * @param relatives  JsonArray of relatives
     * @return new list of failures
     */
    public static List<Relative> toRelativeList(JsonArray relatives) {
        List<Relative> relativeList = new ArrayList<>();
        for (Object o : relatives) {
            if (!(o instanceof JsonObject)) continue;
            Relative relative = new Relative((JsonObject) o);
            relativeList.add(relative);
        }
        return relativeList;
    }


    /**
     * Convert List relative into relative JsonArray
     *
     * @param relativeList relative list
     * @return new JsonArray of Relatives
     */
    public static JsonArray toJsonArray(List<Relative> relativeList) {
        JsonArray relativeArray = new JsonArray();
        for (Relative relative : relativeList) {
            relativeArray.add(relative.toJsonObject());
        }
        return relativeArray;
    }

}
