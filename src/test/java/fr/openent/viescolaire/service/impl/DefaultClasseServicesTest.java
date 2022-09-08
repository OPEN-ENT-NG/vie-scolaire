package fr.openent.viescolaire.service.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(VertxUnitRunner.class)
public class DefaultClasseServicesTest {
    private DefaultClasseService classeService;

    @Before
    public void setUp() {
        this.classeService = Mockito.spy(DefaultClasseService.class);
    }

    @Test
    public void testIsClassExist(TestContext ctx) {
        Async async = ctx.async(3);
        Future<JsonObject> findFuture = Future.succeededFuture(new JsonObject().put("value", "value"));
        Future<JsonObject> notFindFuture = Future.succeededFuture(new JsonObject());
        Future<JsonObject> failFuture = Future.failedFuture("error");
        Mockito.doReturn(findFuture).when(this.classeService).getClasseInfo("id1");
        Mockito.doReturn(notFindFuture).when(this.classeService).getClasseInfo("id2");
        Mockito.doReturn(failFuture).when(this.classeService).getClasseInfo("id3");

        //Test class found
        this.classeService.isClassExist("id1").onComplete(event -> {
            ctx.assertTrue(event.succeeded() && event.result());
            async.countDown();
        });

        //Test class not found
        this.classeService.isClassExist("id2").onComplete(event -> {
            ctx.assertTrue(event.succeeded() && !event.result());
            async.countDown();
        });

        //Test fail method
        this.classeService.isClassExist("id3").onComplete(event -> {
            ctx.assertTrue(event.failed() && "error".equals(event.cause().getMessage()));
            async.countDown();
        });
        async.awaitSuccess(10000);
    }

}
