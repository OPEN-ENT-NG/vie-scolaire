package fr.openent.viescolaire.service.impl;

import fr.openent.viescolaire.service.EventService;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.json.JsonObject;
import fr.wseduc.mongodb.MongoDb;

/**
 * Created by ledunoiss on 02/05/2017.
 */
public class DefaultEventService implements EventService {

    private final String TRACE_COLLECTION;
    private final MongoDb mongo;

    public DefaultEventService(final String collection) {
        this.TRACE_COLLECTION = collection;
        this.mongo = MongoDb.getInstance();
    }

    @Override
    public void add(UserInfos user, Long idRessource, JsonObject ressource, String event) {
        JsonObject trace = new JsonObject();
        trace.putObject("user", formatUser(user))
                .putNumber("ressource_id", idRessource)
                .putString("event", event)
                .putObject("ressource", ressource);

        mongo.insert(TRACE_COLLECTION, trace);
    }

    private JsonObject formatUser (UserInfos user) {
        JsonObject u = new JsonObject();
        return  u.putString("id", user.getUserId())
                .putString("firstName", user.getFirstName())
                .putString("lastName", user.getLastName())
                .putString("type", user.getType());
    }
}
