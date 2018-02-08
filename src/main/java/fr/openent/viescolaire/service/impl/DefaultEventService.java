package fr.openent.viescolaire.service.impl;

import fr.openent.viescolaire.service.EventService;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.json.JsonObject;
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
        trace.putObject("user", formatUser(user))
                .putNumber("ressource_id", idRessource)
                .putString("event", event)
                .putObject("ressource", ressource)
                .putString("date", new Timestamp(System.currentTimeMillis()).toString());

        mongo.insert(TRACE_COLLECTION, trace);
    }

    @Override
    public void add(JsonObject formattedUser, Number idRessource, JsonObject ressource, String event) {
        JsonObject trace = new JsonObject();
        trace.putObject("user", formattedUser)
                .putNumber("ressource_id", idRessource)
                .putString("event", event)
                .putObject("ressource", ressource)
                .putString("date", new Timestamp(System.currentTimeMillis()).toString());

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
