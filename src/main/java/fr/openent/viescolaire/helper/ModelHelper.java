package fr.openent.viescolaire.helper;

import fr.openent.viescolaire.model.Model;
import fr.openent.viescolaire.model.MultiTeaching;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class ModelHelper {
    public static JsonArray convetToJsonArray (List<? extends Model> modelInterfaceList) {
        JsonArray jsonArrayModel = new JsonArray();
        if (!modelInterfaceList.isEmpty()) {
            for (Model modelInstance : modelInterfaceList) {
                jsonArrayModel.add(modelInstance.toJsonObject());
            }
        }
        return jsonArrayModel;
    }
}

