package fr.openent.viescolaire.helper;

import fr.openent.viescolaire.core.constants.*;
import fr.wseduc.webutils.*;
import io.vertx.core.*;
import io.vertx.core.eventbus.*;
import io.vertx.core.json.*;

public class MessageResponseHandler {

    private MessageResponseHandler() {
    }

    public static Handler<AsyncResult<Message<JsonObject>>> messageJsonArrayHandler(Handler<Either<String, JsonArray>> handler) {
        return event -> {
            if (event.succeeded() && Field.OK.equals(event.result().body().getString(Field.STATUS))) {
                handler.handle(new Either.Right<>(event.result().body().getJsonArray(Field.RESULT, event.result().body().getJsonArray(Field.RESULTS))));
            } else {
                if (event.failed()) {
                    handler.handle(new Either.Left<>(event.cause().getMessage()));
                    return;
                }
                handler.handle(new Either.Left<>(event.result().body().getString(Field.MESSAGE)));
            }
        };
    }

    public static Handler<AsyncResult<Message<JsonObject>>> messageJsonObjectHandler(Handler<Either<String, JsonObject>> handler) {
        return event -> {
            if (event.succeeded() && Field.OK.equals(event.result().body().getString(Field.STATUS))) {
                if (!event.result().body().containsKey(Field.RESULT))
                    handler.handle(new Either.Right<>(event.result().body()));
                else
                    handler.handle(new Either.Right<>(event.result().body().getJsonObject(Field.RESULT)));
            } else {
                if (event.failed()) {
                    handler.handle(new Either.Left<>(event.cause().getMessage()));
                    return;
                }
                handler.handle(new Either.Left<>(event.result().body().getString(Field.MESSAGE)));
            }
        };
    }
}
