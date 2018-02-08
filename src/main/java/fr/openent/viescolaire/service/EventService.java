package fr.openent.viescolaire.service;

import org.entcore.common.user.UserInfos;
import org.vertx.java.core.json.JsonObject;

public interface EventService {

    /**
     * Ajouter une trace d'un évènement dans la collection MongoDB
     * @param user utilisateur courant
     * @param idRessource identifiant de la ressource impactée
     * @param ressource ressource impactée
     * @param event Evènement
     */
    public void add(UserInfos user, Number idRessource, JsonObject ressource, String event);

    /**
     * Ajouter une trace d'un évènement dans la collection MongoDB
     * @param user objet contenant les champs id, firstName, lastName, type
     * @param idRessource identifiant de la ressource impactée
     * @param ressource ressource impactée
     * @param event Evènement
     */
    public void add(JsonObject user, Number idRessource, JsonObject ressource, String event);
}
