package fr.openent.viescolaire.helper;

import fr.openent.viescolaire.model.IModel;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class IModelHelper {
    private IModelHelper() {
        throw new IllegalStateException("Utility class");
    }

    @SuppressWarnings("unchecked")
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

    public static JsonArray toJsonArray(List<? extends IModel<?>> dataList) {
        return new JsonArray(dataList.stream().map(IModel::toJsonObject).collect(Collectors.toList()));
    }
}

