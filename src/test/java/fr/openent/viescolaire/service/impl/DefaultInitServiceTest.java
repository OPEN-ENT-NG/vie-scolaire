package fr.openent.viescolaire.service.impl;

import fr.openent.viescolaire.service.*;
import fr.wseduc.mongodb.*;
import io.vertx.core.*;
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

@RunWith(VertxUnitRunner.class)
public class DefaultInitServiceTest {

    Neo4j neo4j = Mockito.mock(Neo4j.class);
    Sql sql = Mockito.mock(Sql.class);

    MongoDb mongoDb = Mockito.mock(MongoDb.class);

    private InitService initService;

    private static final String STRUCTURE_ID = "structureId";

    @Before
    public void setUp() {
        ServiceFactory serviceFactory = new ServiceFactory(Vertx.vertx().eventBus(), sql, neo4j, mongoDb, new JsonObject());
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
}
