package fr.openent.viescolaire.service.impl;

import fr.openent.viescolaire.service.UtilsService;
import fr.wseduc.webutils.Either;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultUtilsService implements UtilsService{

    private final Neo4j neo4j = Neo4j.getInstance();

    @Override
    public <T, V> void addToMap(V value, T key, Map<T, List<V>> map) {
        if (map.get(key) == null) {
            map.put(key, new ArrayList<V>());
        }
        map.get(key).add(value);
    }

    @Override
    public JsonObject[] convertTo (Object[] value) {
        ArrayList<JsonObject> result = new ArrayList<>();
        for(Object o : value) {
            result.add(new JsonObject((Map<String, Object>) o));
        }
        return result.toArray(new JsonObject[0]);
    }

    @Override
    public void getTypeGroupe(String[] id_classes, Handler<Either<String, JsonArray>> handler) {

        StringBuilder query = new StringBuilder();
        JsonObject values = new JsonObject();

        query.append("MATCH (c:Class) WHERE c.id IN {id_classes} RETURN c.id AS id, c IS NOT NULL AS isClass UNION ")
                .append("MATCH (g:FunctionalGroup) WHERE g.id IN {id_classes} RETURN g.id AS id, NOT(g IS NOT NULL) AS isClass");

        values.putArray("id_classes", new JsonArray(id_classes));

        neo4j.execute(query.toString(), values, Neo4jResult.validResultHandler(handler));
    }
}
