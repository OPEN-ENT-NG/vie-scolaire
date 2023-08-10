package fr.openent.viescolaire.service.impl;

import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.model.InitForm.InitFormHolidays;
import fr.openent.viescolaire.service.*;
import fr.wseduc.mongodb.*;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.*;
import io.vertx.ext.unit.*;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.entcore.common.neo4j.*;
import org.entcore.common.sql.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.stubbing.*;
import org.powermock.reflect.*;

import static fr.openent.Viescolaire.EDT_ADDRESS;

@RunWith(VertxUnitRunner.class)
public class DefaultInitServiceTest {

    Neo4j neo4j = Mockito.mock(Neo4j.class);
    Sql sql = Mockito.mock(Sql.class);

    MongoDb mongoDb = Mockito.mock(MongoDb.class);

    private InitService initService;

    private static final String STRUCTURE_ID = "structureId";

    private EventBus eb;

    @Before
    public void setUp() {
        //Mock event bus with spy
        this.eb = Mockito.spy(Vertx.vertx().eventBus());
        ServiceFactory serviceFactory = new ServiceFactory(this.eb, sql, neo4j, mongoDb, new JsonObject());
        this.initService = new DefaultInitService(serviceFactory);
    }

    @Test
    public void testGetTeachersStatus(TestContext ctx) throws Exception {
        Mockito.doAnswer((Answer<Void>) invocation -> {
            JsonObject params = invocation.getArgument(1);

            ctx.assertEquals(params, new JsonObject().put("structureId", STRUCTURE_ID));
            return null;
        }).when(neo4j).execute(Mockito.anyString(), Mockito.any(JsonObject.class), Mockito.any(Handler.class));

        Whitebox.invokeMethod(initService, "getTeachersStatus", STRUCTURE_ID);
    }

    @Test
    public void testGetInitializationStatus(TestContext ctx) throws Exception {
        Mockito.doAnswer((Answer<Void>) invocation -> {
            JsonArray params = invocation.getArgument(1);

            ctx.assertEquals(params, new JsonArray().add(STRUCTURE_ID));
            return null;
        }).when(sql).prepared(Mockito.anyString(), Mockito.any(JsonArray.class), Mockito.any(Handler.class));

        Whitebox.invokeMethod(initService, "getInitializationStatus", STRUCTURE_ID);
    }

    @Test
    public void testInitExclusionPeriod_withOtherSystem(TestContext ctx) throws Exception {
        InitFormHolidays holidaysForm = Mockito.mock(InitFormHolidays.class);
        Mockito.when(holidaysForm.getSystem()).thenReturn(Field.OTHER);

        //Check if eb was called
        Mockito.verify(eb, Mockito.never()).request(Mockito.anyString(), Mockito.any(JsonObject.class), Mockito.any(Handler.class));
        Mockito.verifyNoMoreInteractions(eb);

        Future<JsonObject> result = Whitebox.invokeMethod(initService, "initExclusionPeriod", STRUCTURE_ID, holidaysForm);

        result.onComplete(res -> {
            ctx.assertTrue(res.succeeded());
            ctx.assertNull(res.result());
        });
    }

    @Test
    public void testInitExclusionPeriod_withInvalidZone(TestContext ctx) throws Exception {
        InitFormHolidays holidaysForm = Mockito.mock(InitFormHolidays.class);
        Mockito.when(holidaysForm.getSystem()).thenReturn(Field.FRENCH);
        Mockito.when(holidaysForm.getZone()).thenReturn("D");

        //Check if eb was called
        Mockito.verify(eb, Mockito.never()).request(Mockito.anyString(), Mockito.any(JsonObject.class), Mockito.any(Handler.class));
        Mockito.verifyNoMoreInteractions(eb);

        Future<JsonObject> result = Whitebox.invokeMethod(initService, "initExclusionPeriod", STRUCTURE_ID, holidaysForm);

        result.onComplete(res -> {
            ctx.assertTrue(res.succeeded());
            ctx.assertNull(res.result());
        });
    }

    @Test
    public void testInitExclusionPeriod_withValideZoneAndSystem(TestContext ctx) throws Exception {
        Async async = ctx.async();
        InitFormHolidays holidaysForm = Mockito.mock(InitFormHolidays.class);
        Mockito.when(holidaysForm.getSystem()).thenReturn(Field.FRENCH);
        Mockito.when(holidaysForm.getZone()).thenReturn("A");

        Mockito.doAnswer((Answer<Void>) invocation -> {
            JsonObject action = invocation.getArgument(1);
            ctx.assertEquals(action.getString(Field.ACTION), "init");
            ctx.assertEquals(action.getString(Field.STRUCTUREID), STRUCTURE_ID);
            ctx.assertEquals(action.getString(Field.ZONE), "A");
            ctx.assertFalse(action.getBoolean(Field.INITSCHOOLYEAR));
            async.complete();
            return null;
        }).when(this.eb).request(Mockito.eq(EDT_ADDRESS), Mockito.any(JsonObject.class), Mockito.any(Handler.class));

        Whitebox.invokeMethod(initService, "initExclusionPeriod", STRUCTURE_ID, holidaysForm);

        async.awaitSuccess(10000);
    }

}
