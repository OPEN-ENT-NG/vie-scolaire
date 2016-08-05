package org.cgi.evaluations.service;

import fr.wseduc.webutils.Either;
import org.entcore.common.service.CrudService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public interface IEvalEnseignementService extends CrudService{
    /**
     * Récupération de tous les enseignements
     * @param handler handler portant le résultat de la requête
     */
    public void getEnseignements(Handler<Either<String, JsonArray>> handler);
}
