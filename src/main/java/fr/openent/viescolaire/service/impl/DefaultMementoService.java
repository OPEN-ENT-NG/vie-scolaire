package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.db.DBService;
import fr.openent.viescolaire.helper.FutureHelper;
import fr.openent.viescolaire.helper.RelativeHelper;
import fr.openent.viescolaire.model.Person.Relative;
import fr.openent.viescolaire.service.MementoService;
import fr.wseduc.webutils.Either;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.sql.SqlResult;
import java.util.Arrays;
import java.util.List;

public class DefaultMementoService extends DBService implements MementoService {

    private final DefaultEleveService studentService;

    public DefaultMementoService() {
        studentService = new DefaultEleveService();
    }

    @Override
    public void getStudent(String id, String user, Handler<Either<String, JsonObject>> handler) {
        Promise<JsonObject> classesPromise = Promise.promise();
        Promise<JsonObject> informationPromise = Promise.promise();
        Promise<JsonArray> relativesPromise = Promise.promise();
        Promise<String> commentPromise = Promise.promise();



        Future.all(classesPromise.future(), informationPromise.future(), relativesPromise.future(), commentPromise.future()).onComplete(res -> {
            if (res.failed()) handler.handle(new Either.Left<>(res.cause().toString()));
            else {
                JsonObject student = informationPromise.future().result();
                student.put("classes", classesPromise.future().result().getJsonArray("classes", new JsonArray()));
                student.put("class_id", classesPromise.future().result().getJsonArray("id", new JsonArray()));
                student.put("relatives", relativesPromise.future().result());
                student.put("comment", commentPromise.future().result());
                handler.handle(new Either.Right<>(student));
            }
        });

        getRelatives(id, relativesPromise);
        retrieveStudentAndItsFunctionalGroups(id, informationPromise);
        retrieveStudentClasses(id, classesPromise);
        getStudentComment(id, user, commentPromise);
    }

    @Override
    public void postComment(String student, String user, String comment, Handler<Either<String, JsonObject>> handler) {
        String query = "INSERT INTO " + Viescolaire.MEMENTO_SCHEMA + ".comments (student, owner, comment) VALUES (?, ?, ?) " +
                "ON CONFLICT ON CONSTRAINT uniq_comment DO UPDATE SET comment = ? WHERE comments.owner = ? AND comments.student = ?;";
        JsonArray params = new JsonArray(Arrays.asList(student, user, comment, comment, user, student));

        sql.prepared(query, params, SqlResult.validUniqueResultHandler(handler));
    }

    private void getStudentComment(String student, String user, Promise<String> promise) {
        String query = "SELECT comment FROM " + Viescolaire.MEMENTO_SCHEMA + ".comments WHERE owner = ? AND student = ?;";
        JsonArray params = new JsonArray(Arrays.asList(user, student));
        sql.prepared(query, params, SqlResult.validUniqueResultHandler(res -> {
            if (res.isLeft()) promise.fail(res.left().getValue());
            else promise.complete(res.right().getValue().getString("comment", ""));
        }));
    }

    /**
     * Retrieve student classes.
     * It returns an array containing its class names
     *
     * @param id     Student identifier
     * @param promise future completing the process
     */
    private void retrieveStudentClasses(String id, Promise<JsonObject> promise) {
        String query = "MATCH (u:User {id:{id}})-[:IN]->(:ProfileGroup)-[:DEPENDS]->(g:Class)" +
                "RETURN collect(g.name) AS classes, collect(g.id) as id";
        JsonObject params = new JsonObject().put("id", id);
        neo4j.execute(query, params, Neo4jResult.validUniqueResultHandler(res -> {
            if (res.isLeft()) promise.fail(res.left().getValue());
            else promise.complete(res.right().getValue());
        }));
    }

    /**
     * Retrieve student information.
     * It returns those information: id, name, birth date and an array containing its group names
     *
     * @param id     student identifier
     * @param promise future completing the process
     */
    private void retrieveStudentAndItsFunctionalGroups(String id, Promise<JsonObject> promise) {
        String query = "MATCH(u:User {id:{id}})" +
                "OPTIONAL MATCH (u)-[:IN]-(g:FunctionalGroup) " +
                "RETURN u.id as id, u.lastName + ' ' + u.firstName AS name, u.birthDate as birth_date, " +
                "u.accommodation as accommodation, u.transport as transport, collect(g.name) as groups";
        JsonObject params = new JsonObject().put("id", id);
        neo4j.execute(query, params, Neo4jResult.validUniqueResultHandler(res -> {
            if (res.isLeft()) promise.fail(res.left().getValue());
            else promise.complete(res.right().getValue());
        }));
    }


    /**
     * Get student relatives
     * @param studentId     student identifier
     * @param promise        future completing the process
     */

    @SuppressWarnings("unchecked")
    private void getRelatives(String studentId, Promise<JsonArray> promise) {

        Promise<JsonArray> getPrimaryRelativesIdsPromise = Promise.promise();
        Promise<JsonArray> getAllRelativesPromise = Promise.promise();

        Future.all(getPrimaryRelativesIdsPromise.future(), getAllRelativesPromise.future()).onComplete(asyncHandler -> {
            if (asyncHandler.failed()) {
                promise.fail(asyncHandler.cause().toString());
                return;
            }
            List<Relative> relatives = RelativeHelper.toRelativeList(getPrimaryRelativesIdsPromise.future().result());
            List<JsonObject> studentRelatives = getPrimaryRelativesIdsPromise.future().result().getList();
            JsonArray primaryRelativesIds = getRelativeIdsFromList(studentId, studentRelatives);

            for (Relative relative: relatives) {
                boolean primary = false;

                for (int i = 0; i < primaryRelativesIds.size(); i++) {
                    String relativeId = primaryRelativesIds.getString(i);
                    if (relativeId.equals(relative.getId())) {
                        primary = true;
                    }
                }
                relative.setPrimary(primary);
            }

            promise.complete(RelativeHelper.toJsonArray(relatives));
        });

        studentService.getPrimaryRelatives(new JsonArray().add(studentId), FutureHelper.handlerEitherPromise(getPrimaryRelativesIdsPromise));
        getAllRelatives(studentId, FutureHelper.handlerEitherPromise(getAllRelativesPromise));
    }

    /**
     * Get all relative objects for given student
     * @param studentId     student identifier
     * @param handler       Function handler returning data
     */
    private void getAllRelatives(String studentId, Handler<Either<String, JsonArray>> handler) {
        String query = "MATCH(:User {id:{id}})-[:RELATED]->(u:User) " +
                "RETURN u.id as id, u.lastName + ' ' + u.firstName AS name, u.title AS title, " +
                "CASE WHEN u.mobilePhone is null THEN u.mobile ELSE u.mobilePhone[0] END AS mobile, " +
                "u.homePhone AS phone, u.address + ' ' + u.zipCode + ' ' + u.city AS address, u.email as email, " +
                "NOT(HAS(u.activationCode)) AS activated";
        JsonObject params = new JsonObject().put("id", studentId);
        neo4j.execute(query, params, res -> {
            Either<String, JsonArray> relativeHandler = Neo4jResult.validResult(res);
            if (relativeHandler.isLeft()) {
                handler.handle(new Either.Left<>(relativeHandler.left().getValue()));
                return;
            }

            handler.handle(new Either.Right<>(relativeHandler.right().getValue()));
        });
    }

    private JsonArray getRelativeIdsFromList(String studentId, List<JsonObject> studentsRelativeIds) {
        if (studentsRelativeIds != null) {
            for (JsonObject studentsRelativeId : studentsRelativeIds) {
                if (studentsRelativeId.getString("id").equals(studentId)) {
                    return studentsRelativeId.getJsonArray("primaryRelatives");
                }
            }
        }
        return new JsonArray();
    }

    @Override
    public void updateRelativePriorities(String studentId, JsonArray relativeIds, Handler<Either<String, JsonObject>> handler) {

        String query = "MATCH(u:User {id:{id}}) SET u.primaryRelatives = {relativeIds} " +
                "RETURN u.primaryRelatives AS primaryRelatives";

        JsonObject params = new JsonObject().put("id", studentId)
                                            .put("relativeIds", relativeIds);

        neo4j.execute(query, params, Neo4jResult.validUniqueResultHandler(handler));
    }
}
