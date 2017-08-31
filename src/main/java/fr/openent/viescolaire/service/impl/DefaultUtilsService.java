package fr.openent.viescolaire.service.impl;

import fr.openent.viescolaire.service.UtilsService;
import fr.wseduc.webutils.Either;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultUtilsService implements UtilsService{

    private final Neo4j neo4j = Neo4j.getInstance();

    public <V> void addToList(V value, int index, List<List<V>> list) {
        if (list.get(index) == null) {
            list.add(index, new ArrayList<V>());
        }
        list.get(index).add(value);
    }

    public <T, V> void addToMap(V value, T key, Map<T, List<V>> map) {
        if (map.get(key) == null) {
            map.put(key, new ArrayList<V>());
        }
        map.get(key).add(value);
    }

    public void getTypeGroupe(String[] id_classes,
                              final Handler<Either<String, Map<Boolean, List<String>>>> handler) {

        StringBuilder query = new StringBuilder();
        JsonObject values = new JsonObject();

        query.append("MATCH (c:Class) WHERE c.id IN {id_classes} RETURN c.id AS id, c IS NOT NULL AS isClass UNION ")
                .append("MATCH (g:FunctionalGroup) WHERE g.id IN {id_classes} RETURN g.id AS id, NOT(g IS NOT NULL) AS isClass");

        values.putArray("id_classes", new JsonArray(id_classes));

        neo4j.execute(query.toString(), values, Neo4jResult.validResultHandler(new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> stringJsonArrayEither) {
                if(stringJsonArrayEither.isRight()) {
                    Map<Boolean, List<String>> result = new HashMap<>();

                    for(Object o : stringJsonArrayEither.right().getValue()) {
                        JsonObject classe = (JsonObject) o;
                        addToMap(classe.getString("id"), classe.getBoolean("isClass"), result);
                    }

                    handler.handle(
                            new Either.Right<String, Map<Boolean, List<String>>>(result));
                } else {
                    handler.handle(
                            new Either.Left<String, Map<Boolean, List<String>>>(stringJsonArrayEither.left().getValue()));
                }
            }
        }));
    }
}
