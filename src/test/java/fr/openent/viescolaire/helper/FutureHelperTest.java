package fr.openent.viescolaire.helper;

import fr.openent.viescolaire.core.constants.Field;
import fr.wseduc.webutils.Either;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class FutureHelperTest {

    @Test
    public void testHandlerEitherPromise(TestContext ctx) {
        Async async = ctx.async(3);
        String errorMessage = "errorMessage";
        JsonObject jsonError = new JsonObject().put(Field.ERROR, errorMessage);
        Promise<Boolean> promise1 = Promise.promise();
        Promise<Boolean> promise2 = Promise.promise();
        Promise<Boolean> promise3 = Promise.promise();

        //Test Fail with string object
        promise1.future().onFailure(error -> {
            ctx.assertEquals(error.getMessage(), errorMessage);
            async.countDown();
        }).onSuccess(res -> ctx.fail());

        FutureHelper.handlerEitherPromise(promise1).handle(new Either.Left<>(errorMessage));

        //Test Fail with JsonObject
        promise2.future().onFailure(error -> {
            ctx.assertEquals(error.getMessage(), jsonError.toString());
            async.countDown();
        }).onSuccess(res -> ctx.fail());

        FutureHelper.handlerEitherPromise(promise2).handle(new Either.Left<>(jsonError));

        //Test success response
        promise3.future().onSuccess(res -> {
            ctx.assertTrue(Boolean.TRUE.equals(res));
            async.countDown();
        }).onFailure(res -> ctx.fail());

        FutureHelper.handlerEitherPromise(promise3).handle(new Either.Right<>(true));

        async.awaitSuccess(10000);
    }
}
