package fr.openent.viescolaire.service;

import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;


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
     * @param multiTeachingIds multi teaching identifier
     * @param handler
     */
    void deleteMultiTeaching(JsonArray multiTeachingIds, boolean hasCompetences,EventBus eb, Handler<Either<String, JsonObject>> handler);

    void updateMultiteaching(JsonArray ids_multiTeachingToUpdate, String second_teacher, String startDate,
                             String endDate, String enteredEndDate, Boolean isVisible,
                             EventBus eb, boolean hasCompetences, Handler<Either<String,JsonArray>> handler);

    void updateMultiTeachingVisibility(JsonArray groupsId, String structureId, String mainTeacherId,
                                       String secondTeacherId, String subjectId, Boolean isVisible,
                                       Handler<Either<String, JsonArray>> handler);

    /**
     *
     * @param structureId Structure Identifier
     * @param handler response
     */
    void getMultiTeaching(String structureId, Handler<Either<String,JsonArray>> handler);
    
    void getMultiTeachersByClass(String structureId, String classId, String periodId, Boolean onlyVisible,
                                 Handler<Either<String,JsonArray>> handler);

    /**
     *
     * @param structureId struture id
     * @param groupIds classes ids
     * @param periodId periode
     * @param onlyVisible visible
     * @param handler response visible multiteachers on periode on classIds and on etablissement
     */
    void getMultiTeachers(String structureId, JsonArray groupIds, String periodId, Boolean onlyVisible,
                      Handler<Either<String, JsonArray>> handler);

    void getSubTeachers(String userId, String idStructure, Handler<Either<String, JsonArray>> handler);

    void getSubTeachersandCoTeachers(String userId, String idStructure, String subjectId, String groupId, Handler<Either<String, JsonArray>> handler);

    void getCoTeachers(String userId, String idStructure, Handler<Either<String, JsonArray>> handler);

    void getIdGroupsMutliTeaching(String userId, String idStructure, Handler<Either<String, JsonArray>> handler);
}
