package fr.openent.viescolaire.trombinoscope;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.db.DB;
import fr.openent.viescolaire.service.TrombinoscopeService;
import fr.openent.viescolaire.service.impl.DefaultTrombinoscopeFailureService;
import fr.openent.viescolaire.service.impl.DefaultTrombinoscopeService;
import fr.openent.viescolaire.service.impl.StructureService;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.neo4j.Neo4j;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.entcore.common.sql.Sql;
import org.entcore.common.storage.Storage;
import org.entcore.common.storage.StorageFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.reflect.Whitebox;


import java.util.Arrays;


@RunWith(VertxUnitRunner.class)
public class TrombinoscopeTest {

    Sql sql = Mockito.mock(Sql.class);
    private Vertx vertx;
    private Storage storage;

    private static final Logger log = LoggerFactory.getLogger(TrombinoscopeTest.class);

    private TrombinoscopeService trombinoscopeService;

    String structureId = "structure1";
    String studentId = "student1";
    String pictureId = "picture1";

    @Before
    public void setUp(TestContext context) {
        /* Server mocked settings */
        vertx = Vertx.vertx();
        vertx.exceptionHandler(context.exceptionHandler());
        storage = new StorageFactory(vertx).getStorage();

        DB.getInstance().init(null, sql, null);

        /* Service(s) to test */
        DefaultTrombinoscopeFailureService failureService = new DefaultTrombinoscopeFailureService(storage);
        this.trombinoscopeService = new DefaultTrombinoscopeService(vertx.fileSystem(), storage, failureService);

    }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testGetSetting(TestContext ctx) {
        Mockito.doAnswer((Answer<Void>) invocation -> {
            JsonArray params = invocation.getArgument(1);
            ctx.assertEquals(params, new JsonArray().add(structureId));
            return null;
        }).when(sql).prepared(Mockito.anyString(), Mockito.any(JsonArray.class), Mockito.any(Handler.class));

        trombinoscopeService.getSetting(structureId,null);
    }

    @Test
    public void testSetSettingTrue(TestContext ctx) {
        Mockito.doAnswer((Answer<Void>) invocation -> {
            JsonArray params = invocation.getArgument(1);

            ctx.assertEquals(params, new JsonArray(Arrays.asList(structureId, true, true)));
            return null;
        }).when(sql).prepared(Mockito.anyString(), Mockito.any(JsonArray.class), Mockito.any(Handler.class));

        trombinoscopeService.setSetting(structureId, true, null);
    }

    @Test
    public void testSetSettingFalse(TestContext ctx) {
        Mockito.doAnswer((Answer<Void>) invocation -> {
            JsonArray params = invocation.getArgument(1);

            ctx.assertEquals(params, new JsonArray(Arrays.asList(structureId, false, false)));
            return null;
        }).when(sql).prepared(Mockito.anyString(), Mockito.any(JsonArray.class), Mockito.any(Handler.class));

        trombinoscopeService.setSetting(structureId, false, null);
    }


    @Test
    public void testGetTrombinoscopes(TestContext ctx) {
        Mockito.doAnswer((Answer<Void>) invocation -> {
            JsonArray params = invocation.getArgument(1);

            ctx.assertEquals(params, new JsonArray(Arrays.asList(structureId, studentId)));
            return null;
        }).when(sql).prepared(Mockito.anyString(), Mockito.any(JsonArray.class), Mockito.any(Handler.class));

        trombinoscopeService.get(structureId, studentId, null);
    }

    @Test
    public void testSaveTrombinoscope(TestContext ctx) {
        Mockito.doAnswer((Answer<Void>) invocation -> {
            JsonArray params = invocation.getArgument(1);

            ctx.assertEquals(params, new JsonArray(Arrays.asList(structureId, studentId, pictureId, pictureId)));
            return null;
        }).when(sql).prepared(Mockito.anyString(), Mockito.any(JsonArray.class), Mockito.any(Handler.class));

        try {
            Whitebox.invokeMethod(trombinoscopeService, "saveTrombinoscope",
                    structureId, studentId, pictureId, Promise.promise().future());
        } catch (Exception e) {
            ctx.assertFalse(e.getMessage().isEmpty());
        }
    }

    @Test
    public void testDeleteTrombinoscope(TestContext ctx) {
        String CORRECT_QUERY = "DELETE FROM " + Viescolaire.VSCO_SCHEMA + ".trombinoscope " +
        " WHERE structure_id = ? AND student_id = ? ";

        Mockito.doAnswer((Answer<Void>) invocation -> {
            String query = invocation.getArgument(0);
            JsonArray params = invocation.getArgument(1);
            ctx.assertEquals(CORRECT_QUERY, query);
            ctx.assertEquals(params, new JsonArray(Arrays.asList(structureId, studentId)));
            return null;
        }).when(sql).prepared(Mockito.anyString(), Mockito.any(JsonArray.class), Mockito.any(Handler.class));

        try {
            Whitebox.invokeMethod(trombinoscopeService, "deleteTrombinoscope", structureId, studentId);
        } catch (Exception e) {
            ctx.assertFalse(e.getMessage().isEmpty());
        }
    }
}
