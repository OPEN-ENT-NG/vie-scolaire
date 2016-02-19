package org.cgi.viescolaire.service;

import fr.wseduc.webutils.Either;
import org.entcore.common.service.CrudService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;

/**
 * Created by ledunoiss on 19/02/2016.
 */
public interface IVscoPersonnelService extends CrudService {
    /**
     * Récupère tous les enseignants de l'établissement en fonction de son id Neo4j.
     * @param idEtablissement Identifiant de l'établissement.
     * @param handler Handler portant le résultat de la requête.
     */
    public void getEnseignantEtablissement(String idEtablissement, Handler<Either<String, JsonArray>> handler);
}
