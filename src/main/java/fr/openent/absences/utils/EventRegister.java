package fr.openent.absences.utils;

import fr.openent.viescolaire.service.EventService;
import fr.openent.viescolaire.service.impl.DefaultEventService;
import fr.wseduc.webutils.Either;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by ledunoiss on 11/05/2017.
 */
public class EventRegister {

    private EventService eventService = new DefaultEventService();

    public Handler<Either<String, JsonObject>> getEventRegisterHandler (final HttpServerRequest request, final UserInfos user, final JsonObject object, final String eventName) {
        return new Handler<Either<String, JsonObject>>() {
            @Override
            public void handle(Either<String, JsonObject> event) {
                if (event.isRight()) {
                    ControllerHelper.renderJson(request, event.right().getValue());
                    Number idRessources = event.right().getValue().containsField("id")
                            ? event.right().getValue().getNumber("id")
                            : object.getNumber("id");
                    eventService.add(user,
                            idRessources,
                            object,
                            eventName);
                } else {
                    ControllerHelper.badRequest(request);
                }
            }
        };
    }
}
