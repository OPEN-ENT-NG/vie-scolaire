package org.cgi.absences.service;

import fr.wseduc.webutils.Either;
import org.entcore.common.service.CrudService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.impl.Json;

/**
 * Created by ledunoiss on 22/02/2016.
 */
public interface IAbscAppelService extends CrudService {

    /**
     * Recupére tous les appels effectués sur un établissement dans une période donnée
     * @param psIdEtablissement identifiant de l'établissement.
     * @param psDateDebut date de début de la période.
     * @param psDateFin date de fin de la période.
     * @param handler handler portant le résultat de la requête.
     */
    public void getAppelPeriode(String psIdEtablissement, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler);

    /**
     * Recupere tous les appels non effectues sur un établissement
     * @param psIdEtablissement identifiant de l'établissement.
     * @param psDateDebut date de début de la période.
     * @param psDateFin date de fin de la période.
     * @param handler handler portant le résultat de la requête.
     */
    public void getAppelsNonEffectues(String psIdEtablissement, String psDateDebut,  String psDateFin, Handler<Either<String, JsonArray>> handler);

    /**
     * Créé un appel.
     * @param poPersonnelId identifiant de l'enseignant/CPE.
     * @param poCourId identifiant du cours.
     * @param poEtatAppelId identifiant de l'état de l'appel souhaité.
     * @param poJustificatifAppelId identifiant du justificatif (null si pas de justificatif)
     * @param handler handler portant le résultat de la requête.
     */
    public void createAppel(Integer poPersonnelId, Integer poCourId, Integer poEtatAppelId,
                            Integer poJustificatifAppelId, Handler<Either<String, JsonObject>> handler);

    /**
     * Met à jour un appel.
     * @param poAppelId identifiant de l'appel.
     * @param poPersonnelId identifiant de l'enseignant/CPE.
     * @param poCourId identifiant du cours.
     * @param poEtatAppelId identifiant de l'état de l'appel souhaité.
     * @param poJustificatifAppelId identifiant du justificatif (null si pas de justificatif)
     * @param handler handler portant le résultat de la requête.
     */
    public void updateAppel(Integer poAppelId, Integer poPersonnelId, Integer poCourId,
                            Integer poEtatAppelId, Integer poJustificatifAppelId, Handler<Either<String, JsonObject>> handler);

    /**
     * Recupere un appel grâce à l'identifiant d'un cours.
     * @param poCoursId identifiant d'un cours.
     * @param handler handler portant le résultat de la requête.
     */
    public void getAppelCours(Integer poCoursId, Handler<Either<String, JsonArray>> handler);
}
