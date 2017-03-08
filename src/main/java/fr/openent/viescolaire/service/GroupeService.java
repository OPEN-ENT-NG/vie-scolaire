package fr.openent.viescolaire.service;

import fr.wseduc.webutils.Either;
import org.entcore.common.service.CrudService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;

/**
 * Created by vogelmt on 13/02/2017.
 */
public interface GroupeService extends CrudService {

    /**
     * Liste les groupes d'enseognement pour un élève donné
     * @param userId identifiant de l'utilisateur
     * @param handler handler portant le résultat de la requête
     */
    public void listGroupesEnseignementsByUserId(String userId, Handler<Either<String, JsonArray>> handler);

    /**
     * Liste les élèves pour un groupe d'ensignement donné
     * @param groupeEnseignementId
     * @param handler
     */
    public void listUsersByGroupeEnseignementId(String groupeEnseignementId,String profile, Handler<Either<String, JsonArray>> handler);

    /**
     * get name of classe or groupe
     * @param idGroupe id of classe or groupe
     * @param handler
     */
    public void getNameOfGroupeClasse(String idGroupe, Handler<Either<String, JsonArray>> handler);
}
