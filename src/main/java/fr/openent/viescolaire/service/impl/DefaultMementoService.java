package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.MementoService;
import fr.wseduc.webutils.Either;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;

import java.util.Arrays;

public class DefaultMementoService implements MementoService {
    @Override
    public void getStudent(String id, String user, Handler<Either<String, JsonObject>> handler) {
        Future<JsonObject> classes = Future.future();
        Future<JsonObject> information = Future.future();
        Future<JsonArray> relatives = Future.future();
        Future<String> comment = Future.future();
        CompositeFuture.all(classes, information, relatives, comment).setHandler(res -> {
            if (res.failed()) handler.handle(new Either.Left<>(res.cause().toString()));
            else {
                JsonObject student = information.result();
                student.put("classes", classes.result().getJsonArray("classes", new JsonArray()));
                student.put("class_id", classes.result().getJsonArray("id", new JsonArray()));
                student.put("relatives", relatives.result());
                student.put("comment", comment.result());
                handler.handle(new Either.Right<>(student));
            }
        });

        getRelatives(id, relatives);
        retrieveStudentAndItsFunctionalGroups(id, information);
        retrieveStudentClasses(id, classes);
        getStudentComment(id, user, comment);
    }

    @Override
    public void postComment(String student, String user, String comment, Handler<Either<String, JsonObject>> handler) {
        String query = "INSERT INTO " + Viescolaire.MEMENTO_SCHEMA + ".comments (student, owner, comment) VALUES (?, ?, ?) " +
                "ON CONFLICT ON CONSTRAINT uniq_comment DO UPDATE SET comment = ? WHERE comments.owner = ? AND comments.student = ?;";
        JsonArray params = new JsonArray(Arrays.asList(student, user, comment, comment, user, student));

        Sql.getInstance().prepared(query, params, SqlResult.validUniqueResultHandler(handler));
    }

    private void getStudentComment(String student, String user, Future<String> future) {
        String query = "SELECT comment FROM " + Viescolaire.MEMENTO_SCHEMA + ".comments WHERE owner = ? AND student = ?;";
        JsonArray params = new JsonArray(Arrays.asList(user, student));
        Sql.getInstance().prepared(query, params, SqlResult.validUniqueResultHandler(res -> {
            if (res.isLeft()) future.fail(res.left().getValue());
            else future.complete(res.right().getValue().getString("comment", ""));
        }));
    }

    /**
     * Retrieve student classes.
     * It returns an array containing its class names
     *
     * @param id     Student identifier
     * @param future future completing the process
     */
    private void retrieveStudentClasses(String id, Future<JsonObject> future) {
        String query = "MATCH (u:User {id:{id}})-[:IN]->(:ProfileGroup)-[:DEPENDS]->(g:Class)" +
                "RETURN collect(g.name) AS classes, collect(g.id) as id";
        JsonObject params = new JsonObject().put("id", id);
        Neo4j.getInstance().execute(query, params, Neo4jResult.validUniqueResultHandler(res -> {
            if (res.isLeft()) future.fail(res.left().getValue());
            else future.complete(res.right().getValue());
        }));
    }

    /**
     * Retrieve student information.
     * It returns those information: id, name, birth date and an array containing its group names
     *
     * @param id     student identifier
     * @param future future completing the process
     */
    private void retrieveStudentAndItsFunctionalGroups(String id, Future<JsonObject> future) {
        String query = "MATCH(u:User {id:{id}})" +
                "OPTIONAL MATCH (u)-[:IN]-(g:FunctionalGroup) " +
                "RETURN u.id as id, u.lastName + ' ' + u.firstName AS name, u.birthDate as birth_date, u.accommodation as accommodation, u.transport as transport, collect(g.name) as groups";
        JsonObject params = new JsonObject().put("id", id);
        Neo4j.getInstance().execute(query, params, Neo4jResult.validUniqueResultHandler(res -> {
            if (res.isLeft()) future.fail(res.left().getValue());
            else future.complete(res.right().getValue());
        }));
    }

    private void getRelatives(String studentId, Future<JsonArray> future) {
        String query = "MATCH(:User {id:{id}})-[:RELATED]->(u:User) " +
                "RETURN u.id as id, u.lastName + ' ' + u.firstName AS name, u.title AS title, " +
                "CASE WHEN u.mobilePhone is null THEN u.mobile ELSE u.mobilePhone[0] END AS mobile, " +
                "u.homePhone AS phone, u.address + ' ' + u.zipCode + ' ' + u.city AS address, u.email as email, NOT(HAS(u.activationCode)) as activated";
        JsonObject params = new JsonObject().put("id", studentId);

        Neo4j.getInstance().execute(query, params, res -> {
            Either<String, JsonArray> handler = Neo4jResult.validResult(res);
            if (handler.isLeft()) future.fail(handler.left().getValue());
            else future.complete(handler.right().getValue());
        });
    }
}
