package fr.openent.viescolaire.utils;

import fr.wseduc.webutils.Either;
import io.vertx.core.Future;
import io.vertx.core.Promise;

public class FormateFutureEvent {


    public static <T> void formate (Promise<T> promise, Either<String, T> event) {
        if(event.isLeft()) {
            String error = event.left().getValue();
            promise.fail(error);
        }
        else {
            promise.complete(event.right().getValue());
        }
    }

}


