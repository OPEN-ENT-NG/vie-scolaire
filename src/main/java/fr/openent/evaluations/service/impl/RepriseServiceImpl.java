package fr.openent.evaluations.service.impl;

import fr.openent.evaluations.service.RepriseService;
import fr.wseduc.webutils.Either;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class RepriseServiceImpl implements RepriseService {

    @Override
    public void getDuplicationsList(Handler<Either<String, JsonArray>> handler) {
        String query = "SELECT id_competence, id_eleve, competences_notes.id_devoir, competences_notes.owner, username, name, date, id_groupe, type_groupe, competences.nom " +
                "FROM notes.competences_notes " +
                "INNER JOIN notes.users ON (competences_notes.owner = users.id) " +
                "INNER JOIN notes.devoirs ON (competences_notes.id_devoir = devoirs.id) " +
                "INNER JOIN notes.rel_devoirs_groupes ON (devoirs.id = rel_devoirs_groupes.id_devoir) " +
                "INNER JOIN notes.competences ON (competences_notes.id_competence = competences.id) " +
                "GROUP BY id_competence, id_eleve, competences_notes.id_devoir, competences_notes.owner, username, name, date, competences_notes.id_eleve, id_groupe, type_groupe, competences.nom " +
                "HAVING count(competences_notes.id) > 1 " +
                "ORDER BY competences_notes.id_devoir ";

        Sql.getInstance().raw(query, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getDuplication(Integer evaluationId, Integer skillId, String studentId, Handler<Either<String, JsonArray>> handler) {
        String query = "SELECT id,evaluation, extract(epoch from created) as created, extract(epoch from modified) as modified " +
                "FROM notes.competences_notes " +
                "WHERE id_devoir = ? " +
                "AND id_competence = ? " +
                "AND id_eleve = ?;";
        JsonArray params = new JsonArray()
                .addNumber(evaluationId)
                .addNumber(skillId)
                .addString(studentId);
        Sql.getInstance().prepared(query, params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getStudentInformation(final String id, final Handler<JsonObject> handler) {
        String query = "MATCH (u:User {id: {id}, profiles:['Student']})-[ADMINISTRATIVE_ATTACHMENT]->(s:Structure) return u.displayName, s.name";
        JsonObject params = new JsonObject()
                .putString("id", id);

        Neo4j.getInstance().execute(query, params, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> result) {
                JsonArray users = result.body().getArray("result");
                if (users.size() > 0) {
                    JsonObject user = users.get(0);
                    handler.handle(new JsonObject()
                        .putString("displayName", user.getString("u.displayName"))
                        .putString("structureName", user.getString("s.name"))
                    );
                } else {
                    handler.handle(new JsonObject().putString("id", id));
                }
            }
        });
    }

    @Override
    public void deleteDuplication(Integer evaluationId, Integer skillId, String studentId, Integer correctId, final Handler<Boolean> handler) {
        String query = "DELETE FROM notes.competences_notes " +
                "WHERE id_devoir = ? AND id_competence = ? AND id_eleve = ? " +
                "AND id != ?";

        JsonArray params = new JsonArray()
                .addNumber(evaluationId)
                .addNumber(skillId)
                .addString(studentId)
                .addNumber(correctId);

        Sql.getInstance().prepared(query, params, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> result) {
                handler.handle("ok".equals(result.body().getString("status")));
            }
        });
    }

    @Override
    public void getGroupName(final String id, Integer type_groupe, final Handler<String> handler) {
        String query = "MATCH (c:" + (type_groupe == 0 ? "Class" : "FunctionalGroup") + " {id: {id}}) RETURN c.name";

        Neo4j.getInstance().execute(query, new JsonObject().putString("id", id), new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> result) {
                JsonArray groups = result.body().getArray("result");
                if (groups.size() > 0) {
                    JsonObject group = groups.get(0);
                    handler.handle(group.getString("c.name"));
                } else {
                    handler.handle(id);
                }
            }
        });
    }

    @Override
    public void setConstraint(Handler<Boolean> handler) {
        //TODO
    }

}
