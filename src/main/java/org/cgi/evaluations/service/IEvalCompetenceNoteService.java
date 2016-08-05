package org.cgi.evaluations.service;

import fr.wseduc.webutils.Either;
import org.entcore.common.service.CrudService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public interface IEvalCompetenceNoteService extends CrudService {

    /**
     * Recupère toutes les notes des compétences pour un devoir donné et un élève donné
     * @param idDevoir identifiant du devoir
     * @param idEleve identifiant de l'élève
     * @param handler handler portant le résultat de la requête
     */
    public void getCompetencesNotes(Integer idDevoir, String idEleve, Handler<Either<String, JsonArray>> handler);
}
