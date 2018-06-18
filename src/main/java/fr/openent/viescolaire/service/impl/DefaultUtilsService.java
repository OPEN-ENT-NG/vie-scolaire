package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.UtilsService;
import fr.wseduc.webutils.Either;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.entcore.common.sql.SqlResult.validResultHandler;

public class DefaultUtilsService implements UtilsService{

    private final Neo4j neo4j = Neo4j.getInstance();
    private static final String[] COLORS = {"cyan", "green", "orange", "pink", "yellow", "purple", "grey","orange","purple", "green", "yellow"};

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

    public String getColor(String classes) {
            byte[] bytes = classes.getBytes();
            int number = 0;
            for (int i = 0; i < bytes.length ; i++){
                number += (int) bytes[i];
            }
            number = (int) Math.abs(Math.floor(Math.sin( (double) number) * 10 ) ) ;
        return COLORS[number] ;
    }
    @Override
    public void getTypeGroupe(String[] id_classes, Handler<Either<String, JsonArray>> handler) {

        StringBuilder query = new StringBuilder();
        JsonObject values = new JsonObject();

        query.append("MATCH (c:Class) WHERE c.id IN {id_classes} RETURN c.id AS id, c IS NOT NULL AS isClass UNION ")
                .append(" MATCH (g:FunctionalGroup) WHERE g.id IN {id_classes} RETURN g.id AS id, NOT(g IS NOT NULL) ")
                .append(" AS isClass UNION")
                .append(" MATCH (g:ManualGroup) WHERE g.id IN {id_classes} RETURN g.id AS id, NOT(g IS NOT NULL) ")
                .append(" AS isClass ");

        values.put("id_classes", new fr.wseduc.webutils.collections.JsonArray(Arrays.asList(id_classes)))
                .put("id_classes", new fr.wseduc.webutils.collections.JsonArray(Arrays.asList(id_classes)));

        neo4j.execute(query.toString(), values, Neo4jResult.validResultHandler(handler));
    }

    @Override
    public JsonObject mapListNumber(JsonArray list, String key, String value) {
        JsonObject values = new JsonObject();
        JsonObject o;
        for (int i = 0; i < list.size(); i++) {
            o = list.getJsonObject(i);
            values.put(o.getString(key), o.getInteger(value));
        }
        return values;
    }

    @Override
    public JsonObject mapListString (JsonArray list, String key, String value) {
        JsonObject values = new JsonObject();
        JsonObject o;
        for (int i = 0; i < list.size(); i++) {
            o = list.getJsonObject(i);
            values.put(o.getString(key), o.getString(value));
        }
        return values;
    }

    @Override
    public JsonArray saUnion(JsonArray recipient, JsonArray list) {
        for (int i = 0; i < list.size(); i++) {
            recipient.add(list.getString(i));
        }
        return recipient;
    }

    @Override
    /**
     * Récupère la liste des professeurs titulaires d'un remplaçant sur un établissement donné
     * (si lien titulaire/remplaçant toujours actif à l'instant T)
     *
     * @param psIdRemplacant identifiant neo4j du remplaçant
     * @param psIdEtablissement identifiant de l'établissement
     * @param handler handler portant le resultat de la requête : la liste des identifiants neo4j des titulaires
     */
    public void getTitulaires(String psIdRemplacant, String psIdEtablissement, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();

        query.append("SELECT DISTINCT id_titulaire ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".rel_professeurs_remplacants ")
                .append("WHERE id_remplacant = ? ")
                .append("AND id_etablissement = ? ")
                .append("AND date_debut <= current_date ")
                .append("AND current_date <= date_fin ");

        values.add(psIdRemplacant);
        values.add(psIdEtablissement);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

}
