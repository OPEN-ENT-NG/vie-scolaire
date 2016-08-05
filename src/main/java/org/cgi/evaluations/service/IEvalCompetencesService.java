package org.cgi.evaluations.service;

import fr.wseduc.webutils.Either;
import org.entcore.common.service.CrudService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public interface IEvalCompetencesService extends CrudService {
    /**
     * Récupération des compétences
     * @param handler handler portant le résultat de la requête
     */
    public void getCompetences(Handler<Either<String, JsonArray>> handler);

    /**
     * Setter des compétences pour un devoir donné
     * @param devoirId Id du devoir (Integer)
     * @param values Objet contenant les compétences (JsonObject)
     * @param handler handler portant le résultat de la requête
     */
    public void setDevoirCompetences(Integer devoirId, JsonArray values, Handler<Either<String, JsonObject>> handler);

    /**
     * Getter : Récupération des compétences pour un devoir donné
     * @param devoirId id du Devoir (Integer)
     * @param handler handler portant le résultat de la requête
     */
    public void getDevoirCompetences(Integer devoirId, Handler<Either<String, JsonArray>> handler);

    /**
     * Getter : Récupération des compétences sélectionné sur le dernier devoir créé par
     * l'utilisateur
     * @param userId identifiant de l'utilisateur connecté
     * @param handler handler portant le résultat de la requête
     */
    public void getLastCompetencesDevoir(String userId, Handler<Either<String, JsonArray>> handler);

    /**
     * Getter : Récupération des connaissances liées à une compétence
     * @param skillId Id de la compétence (Integer)
     * @param handler Handler portant le résultat de la requête
     */
    public void getSousCompetences(Integer skillId, Handler<Either<String, JsonArray>> handler);

    /**
     * Getter : Récupération des compétences liées à un module d'enseignement
     * @param teachingId Id de l'enseignement
     * @param handler Handler portant le résultat de la requête
     */
    public void getCompetencesEnseignement(Integer teachingId, Handler<Either<String, JsonArray>> handler);
}
