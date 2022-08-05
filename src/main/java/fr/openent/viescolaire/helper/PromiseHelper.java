package fr.openent.viescolaire.helper;

import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.logging.Logger;

public class PromiseHelper {
    public static void reject(Logger log, String messageToFormat, String className, AsyncResult<?> responseAsync, Promise<?> promise) {
        String message = String.format(messageToFormat, className, responseAsync.cause().getMessage());
        log.error(message);
        promise.fail(responseAsync.cause());
    }

    public static void reject(Logger log, String messageToFormat, String className, Throwable err, Promise<?> promise) {
        String message = String.format(messageToFormat, className, err.getMessage());
        log.error(message);
        promise.fail(err.getMessage());
    }
}
