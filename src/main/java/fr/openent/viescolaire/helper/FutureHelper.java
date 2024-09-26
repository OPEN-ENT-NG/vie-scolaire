package fr.openent.viescolaire.helper;

import fr.wseduc.webutils.Either;
import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.List;

public class FutureHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(FutureHelper.class);

    private FutureHelper() {
    }

    /**
     * @deprecated  Replaced by {@link #handlerEitherPromise(Promise)}
     */
    public static Handler<Either<String, JsonArray>> handlerJsonArray(Promise<JsonArray> promise) {
        return event -> {
            if (event.isRight()) {
                promise.complete(event.right().getValue());
            } else {
                LOGGER.error(event.left().getValue());
                promise.fail(event.left().getValue());
            }
        };
    }



    /**
     * @deprecated  Replaced by {@link #handlerEitherPromise(Promise)}
     */
    public static Handler<Either<String, JsonObject>> handlerJsonObject(Promise<JsonObject> promise) {
        return event -> {
            if (event.isRight()) {
                promise.complete(event.right().getValue());
            } else {
                LOGGER.error(event.left().getValue());
                promise.fail(event.left().getValue());
            }
        };
    }

    public static <L, R> Handler<Either<L, R>> handlerEitherPromise(Promise<R> promise) {
        return event -> {
            if (event.isRight()) {
                promise.complete(event.right().getValue());
            } else {
                String errormessage = event.left().getValue() != null ? event.left().getValue().toString() : "";
                String message = String.format("[Viescolaire@%s::handlerEitherPromise]: %s",
                        FutureHelper.class.getSimpleName(), errormessage);
                LOGGER.error(message);
                promise.fail(errormessage);
            }
        };
    }

    public static <L, R> Handler<Either<L, R>> handlerEitherPromise(Promise<R> promise, String logs) {
        return event -> {
            if (event.isRight()) {
                promise.complete(event.right().getValue());
            } else {
                LOGGER.error(String.format("%s %s ", logs, event.left().getValue()));
                promise.fail(event.left().getValue().toString());
            }
        };
    }



    public static <T> Handler<AsyncResult<T>> promiseHandler(Promise<T> promise) {
        return event -> {
            if (event.failed()) {
                LOGGER.error(event.cause().getMessage());
                promise.fail(event.cause().getMessage());
                return;
            }
            promise.complete(event.result());
        };
    }

}
