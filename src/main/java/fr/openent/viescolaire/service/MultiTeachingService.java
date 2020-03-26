package fr.openent.viescolaire.service;

import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public interface MultiTeachingService {

    /**
     *  @param structureId Structure Identifier
     * @param mainTeacherId Main teacher Identifier
     * @param secondTeacherIds Co or Substitute teacher Identifiers
     * @param subjectId Subject Identifier
     * @param classOrGroupIds Class or group Identifiers
     * @param startDate Replacement start date
     * @param endDate Replacement end date
     * @param enteredEndDate Share ressources end date
     * @param coTeaching is a co teaching or replacement
     * @param handler
     * @param hasCompetences
     */
    void createMultiTeaching(String structureId, String mainTeacherId, JsonArray secondTeacherIds, String subjectId, JsonArray classOrGroupIds, String startDate, String endDate,
                             String enteredEndDate, Boolean coTeaching, Handler<Either<String, JsonArray>> handler,
                             EventBus eb, boolean hasCompetences);

    /**
     *
     * @param id
     * @param structureId Structure Identifier
     * @param mainTeacherId Main teacher Identifier
     * @param secondTeacher_id Co or Substitute teacher Identifier
     * @param subjectId Subject Identifier
     * @param classOrGroupId Class or group Identifier
     * @param startDate Replacement start date
     * @param endDate Replacement end date
     * @param enteredEndDate Share ressources end date
     * @param coTeaching is a co teaching or replacement
     * @param handler
     */
    void updateMultiTeaching(String id, String structureId, String mainTeacherId, JsonArray secondTeacher_id, String subjectId, JsonArray classOrGroupId, String startDate, String endDate, String enteredEndDate, Boolean coTeaching, Handler<Either<String, JsonObject>> handler);

    /**
     *
     * @param multiTeachingIds multi teaching identifier
     * @param handler
     */
    void deleteMultiTeaching(JsonArray multiTeachingIds, Handler<Either<String, JsonObject>> handler);

    /**
     *
     * @param structureId Structure Identifier
     * @param handler response
     */
    void getMultiTeaching( String structureId, Handler<Either<String,JsonArray>> handler);

    void getSubTeachers(String userId, String idStructure, Handler<Either<String, JsonArray>> handler);

    void getSubTeachersandCoTeachers(String userId, String idStructure, Handler<Either<String, JsonArray>> handler);

    void getCoTeachers(String userId, String idStructure, Handler<Either<String, JsonArray>> handler);
}
