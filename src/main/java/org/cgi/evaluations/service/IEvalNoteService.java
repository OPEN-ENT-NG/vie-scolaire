package org.cgi.evaluations.service;

import fr.wseduc.webutils.Either;
import org.entcore.common.service.CrudService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public interface IEvalNoteService extends CrudService {
    /**
     * Recupère la liste des Notes en fonction d'un identifiant de devoir donné.
     * @param devoirId identifiant du devoir
     * @param handler handler portant le resultat de la requête
     */
    public void listNotesParDevoir(Integer devoirId, Handler<Either<String, JsonArray>> handler);

    /**
     * Recupere la note d'un élève pour un devoir.
     *
     * @param idDevoir l'identifiant du devoir
     * @param idEleve l'identifiant de l'élève
     * @param handler handler portant le résultat de la requête
     */
    public void getNoteParDevoirEtParEleve(Integer idDevoir, String idEleve, Handler<Either<String, JsonArray>> handler);

    /**
     * Mise à jour d'une note
     * @param data Note à mettre à jour
     * @param handler handler portant le resultat de la requête
     */
    public void updateNote(JsonObject data, Handler<Either<String, JsonObject>> handler);

    /**
     * Suppression d'un note en bdd
     * @param idNote identifiant de la note
     * @param handler handler portant le résultat de la requête
     */
    public void deleteNote(Integer idNote, Handler<Either<String, JsonObject>> handler);

    /**
     * Récupération des Notes pour le widget
     * @param userId identifiant de l'utilisateur
     * @param handler handler portant le résultat de la requête
     */
    public void getWidgetNotes(String userId, Handler<Either<String, JsonArray>> handler);

    /**
     * Récupération des toutes les notes par devoir des élèves
     * @param userId identifiant de l'utilisateur
     * @param etablissementId identifiant de l'établissement
     * @param classeId identifiant de la classe
     * @param matiereId identifiant de la matière
     * @param periodeId identifiant de la periode
     * @param handler handler portant le résultat de la requête
     */
    public void getNoteElevePeriode(String userId, String etablissementId, String classeId, String matiereId, Integer periodeId, Handler<Either<String, JsonArray>> handler);
}
