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
     * @param hanlder handler portant le résultat de la requête.
     */
    public void updateEvenement(String pIIdEvenement, JsonObject pOEvenement, Handler<Either<String, JsonArray>> handler);
}
