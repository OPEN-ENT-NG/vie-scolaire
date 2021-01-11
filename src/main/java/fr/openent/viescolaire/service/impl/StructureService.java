package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.db.DBService;
import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.sql.SqlResult;

public class StructureService extends DBService {

    public void store(Handler<Either<String, JsonArray>> handler) {
        String query = "MATCH (s:Structure) RETURN s.id as id, s.name as name, s.UAI as uai";
        neo4j.execute(query, new JsonObject(), Neo4jResult.validResultHandler(neo -> {
            if (neo.isRight()) {
                JsonArray params = new JsonArray();
                JsonArray structures = neo.right().getValue();

                if (structures.isEmpty()) {
                    handler.handle(new Either.Right<>(new JsonArray()));
                    return;
                }

                StringBuilder sqlQuery = new StringBuilder("INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".structures (id, name, uai) VALUES ");
                for (int i = 0; i < structures.size(); i++) {
                    JsonObject structure = structures.getJsonObject(i);
                    sqlQuery.append("(?, ?, ?),");
                    params.add(structure.getString("id"))
                            .add(structure.getString("name"))
                            .add(structure.getString("uai"));
                }

                sqlQuery = new StringBuilder(sqlQuery.substring(0, sqlQuery.length() - 1));
                sqlQuery.append(" ON CONFLICT ON CONSTRAINT structure_pkey DO NOTHING");
                sql.prepared(sqlQuery.toString(), params, SqlResult.validResultHandler(handler));
            } else {
                handler.handle(new Either.Left<>(neo.left().getValue()));
            }
        }));
    }

    public void retrieveStructureInfo(String id, Handler<Either<String, JsonObject>> handler) {
        String query = "MATCH (s:Structure {id: {id}}) RETURN s.id as id, s.name as name, s.UAI as UAI";
        JsonObject params = new JsonObject().put("id", id);
        neo4j.execute(query, params, Neo4jResult.validUniqueResultHandler(handler));
    }

}
