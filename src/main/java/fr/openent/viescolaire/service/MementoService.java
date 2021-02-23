package fr.openent.viescolaire.service;

import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public interface MementoService {

    /**
     * Retrieve student information.
     * It returns through the handler an object containing those data: id, name, birth date, classes, groups
     *
     * @param id      student identifier
     * @param user    Current user. We need it to retrieve the user comment
     * @param handler function handler returning data
     */
    void getStudent(String id, String user, Handler<Either<String, JsonObject>> handler);

    /**
     * Post comment for given student identifier
     *
     * @param student Student identifier
     * @param user    User that post the comment
     * @param comment Comment content
     * @param handler Function handler returning data
     */
    void postComment(String student, String user, String comment, Handler<Either<String, JsonObject>> handler);


    /**
     * Update primary relatives for given student.
     * @param studentId     student identifier
     * @param relativeIds   primary relative identifier list
     * @param handler       Function handler returning data
     */
    void updateRelativePriorities(String studentId, JsonArray relativeIds, Handler<Either<String, JsonObject>> handler);
}
