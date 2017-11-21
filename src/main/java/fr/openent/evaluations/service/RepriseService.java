package fr.openent.evaluations.service;

import fr.wseduc.webutils.Either;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public interface RepriseService {

    /**
     * Returns duplications stored in the database
     * @param handler Function handler returning data
     */
    public void getDuplicationsList (Handler<Either<String, JsonArray>> handler);

    /**
     * Returns skill evaluation stored ine the database based on provided duplication
     * @param evaluationId evaluation id
     * @param skillId skill id
     * @param studentId student id
     * @param handler Function handler returning data
     */
    public void getDuplication (Integer evaluationId, Integer skillId, String studentId, Handler<Either<String, JsonArray>> handler);

    /**
     * Returns student display name based on id
     * @param id Student id
     * @param handler Function handler retuning data
     */
    public void getStudentInformation (String id, Handler<JsonObject> handler);

    /**
     * Delete wrong duplications
     * @param evaluationId Evaluation id
     * @param skillId Skill id
     * @param studentId Student id
     * @param correctId Correct skill id to preserve
     * @param handler Function handler returning data
     */
    public void deleteDuplication (Integer evaluationId, Integer skillId, String studentId, Integer correctId, Handler<Boolean> handler);

    /**
     * Returns group name based on provided id
     * @param id Group id
     * @param type_groupe Group type
     * @param handler Function handler retuning data
     */
    public void getGroupName (String id, Integer type_groupe, Handler<String> handler);

    /**
     * Set unity constraint
     * @param handler function handler returning data
     */
    public void setConstraint (Handler<Boolean> handler);
}
