package fr.openent.evaluations.service;

import fr.wseduc.webutils.Either;
import org.entcore.common.service.CrudService;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by vogelmt on 29/03/2017.
 */
public interface BFCService extends CrudService {
    /**
     * Créer un BFC pour un élève
     * @param bfc objet contenant les informations relative au BFC
     * @param user utilisateur
     * @param handler handler portant le résultat de la requête
     */
    public void createBFC(final JsonObject bfc, final UserInfos user, final Handler<Either<String, JsonObject>> handler);

    /**
     * Mise à jour d'un BFC pour un élève
     * @param data appreciation à mettre à jour
     * @param user utilisateur
     * @param handler handler portant le resultat de la requête
     */
    public void updateBFC(JsonObject data, UserInfos user, Handler<Either<String, JsonObject>> handler);

    /**
     * Suppression d'un BFC pour un élève
     * @param idBFC identifiant de la note
     * @param user user
     * @param handler handler portant le résultat de la requête
     */
    public void deleteBFC(Long idBFC, UserInfos user, Handler<Either<String, JsonObject>> handler);

    /**
     * Récupère les BFCs d'un élève pour chaque domaine
     * @param idEleve
     * @param idEtablissement
     * @param handler
     */
    public void getBFCsByEleve(String idEleve, String idEtablissement, Handler<Either<String,JsonArray>> handler);
}
