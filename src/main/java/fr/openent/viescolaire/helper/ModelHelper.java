package fr.openent.viescolaire.helper;

import fr.openent.viescolaire.model.IModel;
import fr.openent.viescolaire.model.Model;
import fr.openent.viescolaire.model.MultiTeaching;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ModelHelper {

    private ModelHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static JsonArray convertToJsonArray (List<? extends Model> modelInterfaceList) {
        JsonArray jsonArrayModel = new JsonArray();
        if (!modelInterfaceList.isEmpty()) {
            for (Model modelInstance : modelInterfaceList) {
                jsonArrayModel.add(modelInstance.toJsonObject());
            }
        }
        return jsonArrayModel;
    }

    public static JsonArray toJsonArray(List<? extends IModel<?>> dataList) {
        return new JsonArray(dataList.stream().map(IModel::toJson).collect(Collectors.toList()));
    }

    public static <T extends IModel<T>> List<T> toList(JsonArray results, Class<T> modelClass) {
        return ((List<JsonObject>) results.getList()).stream()
                .map(iModel -> {
                    try {
                        return modelClass.getConstructor(JsonObject.class).newInstance(iModel);
                    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                             InvocationTargetException e) {
                        return null;
                    }
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}

