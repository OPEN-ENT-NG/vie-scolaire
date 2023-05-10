package fr.openent.viescolaire.service;

import fr.openent.viescolaire.model.*;
import fr.openent.viescolaire.model.InitForm.*;
import fr.openent.viescolaire.model.Person.*;
import fr.openent.viescolaire.model.SlotProfile.*;
import io.vertx.core.*;
import io.vertx.core.json.*;
import org.entcore.common.user.*;

import java.util.*;

public interface InitService {

    Future<Void> launchInitWorker(UserInfos user, String structureId, InitFormModel form, JsonObject i18nParams);

    Future<InitTeachers> getTeachersStatus(String structureId);

    /**
     * Get initialization status
     * @param structureId structure id
     * @return true if initialized, false otherwise
     */
    Future<Boolean> getInitializationStatus(String structureId);

    /**
     * Set initialization status for a structure
     * @param structureId structure id
     * @param status initialization status
     */
    Future<Void> setInitializationStatus(String structureId, boolean status);

    Future<SlotProfile> initTimeSlots(String structureId, String structureName, User owner, InitFormTimetable timetable, String locale, String acceptLanguage);

    Future<SubjectModel> initSubject(String structureId, SubjectModel subject);

    Future<JsonObject> initServices(String structureId, SubjectModel subject);

    Future<JsonObject> initExclusionPeriod(String structureId, String zone);

    Future<JsonObject> initCourses(String structureId, String subjectId, String startDate, String endDate,
                                   InitFormTimetable timetable, List<Timeslot> timeslots, String userId);

    Future<Void> resetInit(String structureId);
}