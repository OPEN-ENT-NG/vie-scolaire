package org.cgi.absences.service;

import fr.wseduc.webutils.Either;
import org.entcore.common.service.CrudService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
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
     * @param handler handler portant le résultat de la requête.
     */
    public void getAppelsNonEffectues(String psIdEtablissement, String psDateDebut, Handler<Either<String, JsonArray>> handler);
}
