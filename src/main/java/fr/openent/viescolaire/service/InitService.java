package fr.openent.viescolaire.service;

import fr.openent.viescolaire.model.*;
import io.vertx.core.*;

public interface InitService {

    Future<InitTeachers> getTeachersStatus(String structureId);

    /**
     * Get initialization status
     * @param structureId structure id
     * @return true if initialized, false otherwise
     */
    Future<Boolean> getInitializationStatus(String structureId);

}
