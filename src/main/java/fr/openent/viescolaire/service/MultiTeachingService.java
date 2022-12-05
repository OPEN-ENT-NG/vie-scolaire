package fr.openent.viescolaire.service;

import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;


public interface MultiTeachingService {

    /**
     * @param structureId       Structure Identifier
     * @param mainTeacherId     Main teacher Identifier
     * @param secondTeacherIds  Co or Substitute teacher Identifiers
     * @param subjectId         Subject Identifier
     * @param classOrGroupIds   Class or group Identifiers
     * @param startDate         Replacement start date
     * @param endDate           Replacement end date
     * @param enteredEndDate    Share ressources end date
     * @param coTeaching        is a co teaching or replacement
     * @param handler           Function handler returning data
     * @param hasCompetences    is Competences module installed
     */
    void createMultiTeaching(String structureId, String mainTeacherId, JsonArray secondTeacherIds, String subjectId, JsonArray classOrGroupIds, String startDate, String endDate,
                             String enteredEndDate, Boolean coTeaching, Handler<Either<String, JsonArray>> handler, boolean hasCompetences);

    /**
     *  Delete multi teachings and their related courses substitutes
     * @param multiTeachingIds  multi teaching identifier list
     * @param handler           Function handler returning data
     */
    void deleteMultiTeaching(JsonArray multiTeachingIds, boolean hasCompetences,
                             Handler<Either<String, JsonObject>> handler);

    /**
     * Update multi teachings and their related courses substitutes
     * @param idsMultiTeachingToUpdate list of multiTeachings ids
     * @param secondTeacher            substitute identifier
     * @param startDate                start date of multiTeaching
     * @param endDate                  end date of multiTeaching
     * @param enteredEndDate
     * @param isVisible
     * @param hasCompetences           is Competences module installed
     * @param handler                  Function handler returning data
     */
    void updateMultiteaching(JsonArray idsMultiTeachingToUpdate, String secondTeacher, String startDate,
                             String endDate, String enteredEndDate, Boolean isVisible, boolean hasCompetences,
                             Handler<Either<String,JsonArray>> handler);

    void updateMultiTeachingVisibility(JsonArray groupsId, String structureId, String mainTeacherId,
                                       String secondTeacherId, String subjectId, Boolean isVisible,
                                       Handler<Either<String, JsonArray>> handler);

    /**
     * Fetch a multi teaching
     * @param structureId   Structure Identifier
     * @param handler       response
     */
    void getMultiTeaching(String structureId, Handler<Either<String,JsonArray>> handler);

    /**
     * Fetch list of multiTeachings
     * @param ids           multiTeaching identifier list
     * @param handler       Function handler returning data
     */
    void getMultiTeachings (JsonArray ids, Handler<Either<String, JsonArray>> handler);

    /**
     *
     * @param structureId structure id
     * @param groupIds classes ids
     * @param periodId periode
     * @param onlyVisible visible
     * @param handler response visible multiteachers on periode on classIds and on etablissement
     */
    void getMultiTeachers(String structureId, JsonArray groupIds, String periodId, Boolean onlyVisible,
                      Handler<Either<String, JsonArray>> handler);

    void getAllMultiTeachers(String structureId, JsonArray groupIds, Handler<Either<String, JsonArray>> handler);

    void getSubTeachers(String userId, String idStructure, Handler<Either<String, JsonArray>> handler);

    void getSubTeachersandCoTeachers(String userId, String idStructure, String subjectId, String groupId, Handler<Either<String, JsonArray>> handler);

    void getCoTeachers(String userId, String idStructure, Handler<Either<String, JsonArray>> handler);

    void getIdGroupsMutliTeaching(String userId, String idStructure, Handler<Either<String, JsonArray>> handler);
}
