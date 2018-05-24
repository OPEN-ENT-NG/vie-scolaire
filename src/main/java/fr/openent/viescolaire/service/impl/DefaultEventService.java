package fr.openent.viescolaire.service.impl;

import fr.openent.viescolaire.service.EventService;
import org.entcore.common.user.UserInfos;
import io.vertx.core.json.JsonObject;
import fr.wseduc.mongodb.MongoDb;

import java.sql.Timestamp;

/**
 * Created by ledunoiss on 02/05/2017.
 */
public class DefaultEventService implements EventService {

    private final String TRACE_COLLECTION = "vsco.events";
    private final MongoDb mongo;

    public DefaultEventService() {
        this.mongo = MongoDb.getInstance();
    }

    @Override
    public void add(UserInfos user, Number idRessource, JsonObject ressource, String event) {
        JsonObject trace = new JsonObject();
        trace.put("user", formatUser(user))
                .put("ressource_id", idRessource)
                .put("event", event)
                .put("ressource", ressource)
                .put("date", new Timestamp(System.currentTimeMillis()).toString());

        mongo.insert(TRACE_COLLECTION, trace);
    }

    @Override
    public void add(JsonObject formattedUser, Number idRessource, JsonObject ressource, String event) {
        JsonObject trace = new JsonObject();
        trace.put("user", formattedUser)
                .put("ressource_id", idRessource)
                .put("event", event)
                .put("ressource", ressource)
                .put("date", new Timestamp(System.currentTimeMillis()).toString());

        mongo.insert(TRACE_COLLECTION, trace);
    }

    private JsonObject formatUser (UserInfos user) {
        JsonObject u = new JsonObject();
        return  u.put("id", user.getUserId())
                .put("firstName", user.getFirstName())
                .put("lastName", user.getLastName())
                .put("type", user.getType());
    }
}
