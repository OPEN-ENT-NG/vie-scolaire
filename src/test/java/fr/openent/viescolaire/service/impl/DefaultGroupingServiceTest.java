package fr.openent.viescolaire.service.impl;

import fr.openent.viescolaire.utils.DateHelper;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.entcore.common.sql.Sql;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.powermock.reflect.Whitebox;

import java.util.Arrays;

import static org.mockito.Mockito.*;


@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(VertxUnitRunner.class)
@PrepareForTest(DateHelper.class)
public class DefaultGroupingServiceTest {
    private Vertx vertx;
    private DefaultGroupingService defaultGroupingService = new DefaultGroupingService();

    @Before
    public void setUp() throws NoSuchFieldException {
        vertx = Vertx.vertx();
        Sql.getInstance().init(vertx.eventBus(), "fr.openent.Viescolaire");

    }
    @Test
    public void TestCreateGrouping(TestContext ctx) throws Exception {
        Async async = ctx.async();
        String queryExpected = "INSERT INTO viesco.grouping(id, name, structure_id, created_at, updated_at) VALUES(?, ?, ?, ?, ?)";
        String date = DateHelper.getCurrentDate(DateHelper.MONGO_FORMAT);
        PowerMockito.mockStatic(DateHelper.class);
        PowerMockito.when(DateHelper.getCurrentDate(DateHelper.MONGO_FORMAT)).thenReturn(date);
        vertx.eventBus().consumer("fr.openent.Viescolaire", message -> {
            JsonObject body = (JsonObject) message.body();
            ctx.assertEquals("prepared", body.getString("action"));
            ctx.assertEquals(queryExpected, body.getString("statement"));
            JsonArray args = body.getJsonArray("values");
            args.remove(0);
            ctx.assertEquals(new JsonArray(Arrays.asList("test", "1", date, date)).toString(), args.toString());
            async.complete();
        });
defaultGroupingService.createGrouping("test", "1");
    }

}
