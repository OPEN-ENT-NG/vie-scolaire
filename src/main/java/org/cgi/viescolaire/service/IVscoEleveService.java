package org.cgi.viescolaire.service;

import fr.wseduc.webutils.Either;
import org.entcore.common.service.CrudService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;

/**
 * Created by ledunoiss on 10/02/2016.
 */
public interface IVscoEleveService extends CrudService {

    /**
     * Récupération des élèves d'une classes en fonction de son identifiant
     * @param pSIdClasse Identifiant de la classe
     * @param handler Handler de retour
     */
    public void getEleveClasse(String pSIdClasse, Handler<Either<String, JsonArray>> handler);
}
