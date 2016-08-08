package org.cgi.evaluations.service;

import fr.wseduc.webutils.Either;
import org.entcore.common.service.CrudService;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public interface IEvalCompetenceNoteService extends CrudService {

    /**
     * Créer une compétenceNote
     * @param competenceNote objet contenant les informations relatives à la compétenceNote
     * @param user utilisateur courant
     * @param handler handler portant le résultat de la requête
     */
    public void createCompetenceNote(JsonObject competenceNote, UserInfos user, Handler<Either<String, JsonObject>> handler);

    /**
     * Met à jour une compétence note
     * @param id identifiant de la compétence note à mettre à jour
     * @param competenceNote objet contenant les informations relatives à la compétenceNote
     * @param user utilisateur courant
     * @param handler handler portant le résultat de la requête
     */
    public void updateCompetenceNote(String id, JsonObject competenceNote, UserInfos user, Handler<Either<String, JsonObject>> handler);

    /**
     * Supprimer une compétence Note
     * @param id identifiant de la compétence note à supprimer
     * @param handler handler portant le résultat de la requête
     */
    public void deleteCompetenceNote(String id, Handler<Either<String, JsonObject>> handler);

    /**
     * Recupère toutes les notes des compétences pour un devoir donné et un élève donné
     * @param idDevoir identifiant du devoir
     * @param idEleve identifiant de l'élève
     * @param handler handler portant le résultat de la requête
     */
    public void getCompetencesNotes(Integer idDevoir, String idEleve, Handler<Either<String, JsonArray>> handler);
}
