package fr.openent.viescolaire.service;

import fr.openent.viescolaire.model.*;
import fr.openent.viescolaire.model.InitForm.*;
import fr.openent.viescolaire.model.Person.*;
import io.vertx.core.*;
import io.vertx.core.json.*;
import org.entcore.common.user.*;

public interface InitService {

    Future<Void> launchInitWorker(UserInfos user, String structureId, InitFormModel form, JsonObject i18nParams);

    Future<InitTeachers> getTeachersStatus(String structureId);

    /**
     * Get initialization status
     * @param structureId structure id
     * @return true if initialized, false otherwise
     */
    Future<Boolean> getInitializationStatus(String structureId);

    Future<Void> initTimeSlots(String structureId, String structureName, User owner, InitFormTimetable timetable, String locale, String acceptLanguage);

}
