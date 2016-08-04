package org.cgi.absences.service;

import fr.wseduc.webutils.Either;
import org.entcore.common.service.CrudService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by ledunoiss on 25/02/2016.
 */
public interface IAbscEvenementService extends CrudService {

    /**
     * Met à jours l'évènement
     * @param pIIdEvenement identifiant de l'évènement.
     * @param pOEvenement objet contenant l'évènement.
     * @param handler handler portant le résultat de la requête.
     */
    public void updateEvenement(String pIIdEvenement, JsonObject pOEvenement, Handler<Either<String, JsonArray>> handler);

    /**
     * Créé un evenement en base de données
     * @param poEvenement l'objet JSON représentant l'évenement
     * @param handler handler portant le résultat de la requête.
     */
    public void createEvenement(JsonObject poEvenement, Handler<Either<String, JsonObject>> handler);

    /**
     * Met à jour un évenement.
     * @param poEvenement l'objet JSON représentant l'évenement.
     * @param handler portant le résultat de la requête.
     */
    public void updateEvenement(JsonObject poEvenement, Handler<Either<String, JsonObject>> handler);

    /**
     * Supprime un évenement à partir de son identifiant.
     * @param poEvenementId Identifiant de l'evenement à supprimer.
     * @param handler portant le résultat de la requête.
     */
    public void deleteEvenement(int poEvenementId, Handler<Either<String, JsonObject>> handler);

    /**
     * Récupère toutes les observations dans une période donnée.
     * @param psEtablissementId identifiant de l'établissement.
     * @param psDateDebut date de début de la période.
     * @param psDateFin date de fin de la période.
     * @param handler handler portant le résultat de la requête.
     */
    public void getObservations(String psEtablissementId, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler);

    /**
     * Recupère tous les évènements pour une classe donnée sur un cours donné.
     * @param psClasseId identifiant de la classe.
     * @param psCoursId identifiant du cours.
     * @param handler handler portant le résultat de la requête.
     */
    public void getEvenementClasseCours(String psClasseId, String psCoursId, Handler<Either<String, JsonArray>> handler);

    /**
     * Recupere les absences sur le cours précédent en fonction de l'identifiant de l'utilisateur, de l'identifiant
     * de la classe et qui n'est pas le cours donné.
     * @param psUserId identifiant de l'utilisateur.
     * @param psClasseId identifiant de la classe.
     * @param psCoursId identifiant du cours.
     * @param handler handler portant le résultat de la requête.
     */
    public void getAbsencesDernierCours(String psUserId, Integer psClasseId, Integer psCoursId, Handler<Either<String, JsonArray>> handler);

    /**
     * Recupere tous les évènements pour une classe donnée sur une période donnée
     * @param piClasseId identifiant de la classe (id postgresql).
     * @param psDateDebut date de début de la période.
     * @param psDateFin date de fin de la période.
     * @param handler handler portant le résultat de la requête.
     */
    public void getEvtClassePeriode(Integer piClasseId, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler);
}
