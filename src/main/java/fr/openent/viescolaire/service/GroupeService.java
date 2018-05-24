package fr.openent.viescolaire.service;

import fr.wseduc.webutils.Either;
import org.entcore.common.service.CrudService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;

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
     * Récupère les classes des groupes passes en parametre
     *
     * @param idGroupes Identifiant des groupes
     * @param handler  Handler portant le résultat de la requête
     */
    public void getClasseGroupe(String[] idGroupes, Handler<Either<String, JsonArray>> handler);

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
