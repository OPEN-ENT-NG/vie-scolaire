package org.cgi.absences.service;

import fr.wseduc.webutils.Either;
import org.entcore.common.service.CrudService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;

/**
 * Created by ledunoiss on 23/02/2016.
 */
public interface IAbscMotifService extends CrudService {

    /**
     * Recupere tous les motifs d'absences en fonction de l'id de l'établissement.
     * @param psIdEtablissement identifiant de l'établissement.
     * @param handler handler portant le résultat de la requête.
     */
    public void getAbscMotifsEtbablissement(String psIdEtablissement, Handler<Either<String, JsonArray>> handler);

    /**
     * Recupere tous les justificatifs d'appels en fonction de l'id de l'établissement
     * @param psIdEtablissement identifiant de l'établissement
     * @param handler handler portant le résultat de la requête
     */
    public void getAbscJustificatifsEtablissement(String psIdEtablissement, Handler<Either<String, JsonArray>> handler);

}
