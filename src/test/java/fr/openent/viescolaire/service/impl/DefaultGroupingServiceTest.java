package fr.openent.viescolaire.service.impl;

import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.utils.DateHelper;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jRest;
import org.entcore.common.sql.Sql;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.reflection.FieldSetter;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.util.Arrays;

import static org.mockito.Mockito.*;


@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(VertxUnitRunner.class)
@PrepareForTest(DateHelper.class)
public class DefaultGroupingServiceTest {
    private Vertx vertx;
    private final String address = "fr.openent.Viescolaire";
    private DefaultGroupingService defaultGroupingService;
    private final Neo4j neo4j = Neo4j.getInstance();
    private final Neo4jRest neo4jRest = mock(Neo4jRest.class);
    @Before
    public void setUp() throws NoSuchFieldException {
        vertx = Vertx.vertx();
        defaultGroupingService = PowerMockito.spy(new DefaultGroupingService());
        Sql.getInstance().init(vertx.eventBus(), address);
        FieldSetter.setField(neo4j, neo4j.getClass().getDeclaredField("database"), neo4jRest);

    }
    @Test
    public void TestCreateGrouping(TestContext ctx) {
        Async async = ctx.async();
        String queryExpected = "INSERT INTO viesco.grouping(id, name, structure_id, created_at, updated_at) VALUES(?, ?, ?, ?, ?)";
        String date = DateHelper.getCurrentDate(DateHelper.MONGO_FORMAT);
        //tests variables
        String groupingTestId = "test_id";
        String structureTestId = "1";

        PowerMockito.mockStatic(DateHelper.class);
        PowerMockito.when(DateHelper.getCurrentDate(DateHelper.MONGO_FORMAT)).thenReturn(date);

        vertx.eventBus().consumer(address, message -> {
            JsonObject body = (JsonObject) message.body();
            ctx.assertEquals(Field.PREPARED, body.getString(Field.ACTION));
            ctx.assertEquals(queryExpected, body.getString(Field.STATEMENT));
            JsonArray args = body.getJsonArray(Field.VALUES);
            args.remove(0);
            ctx.assertEquals(new JsonArray(Arrays.asList(groupingTestId, structureTestId, date, date)).toString(), args.toString());
            async.complete();
        });
        defaultGroupingService.createGrouping(groupingTestId, structureTestId);
    }

    @Test
    public void TestUpdateGrouping(TestContext ctx) {
        Async async = ctx.async();
        String queryExpected = "UPDATE viesco.grouping SET name = '?' AND updated_at = '?' WHERE id = ?";
        String date = DateHelper.getCurrentDate(DateHelper.MONGO_FORMAT);
        PowerMockito.mockStatic(DateHelper.class);
        PowerMockito.when(DateHelper.getCurrentDate(DateHelper.MONGO_FORMAT)).thenReturn(date);

        //tests variables
        String groupingTestId = "test_id";
        String nameTest = "name_test";

        vertx.eventBus().consumer(address, message -> {
            JsonObject body = (JsonObject) message.body();
            ctx.assertEquals(Field.PREPARED, body.getString(Field.ACTION));
            ctx.assertEquals(queryExpected, body.getString(Field.STATEMENT));
            JsonArray args = body.getJsonArray(Field.VALUES);
            ctx.assertEquals(new JsonArray(Arrays.asList(nameTest, DateHelper.getCurrentDate(DateHelper.MONGO_FORMAT), groupingTestId)).toString(), args.toString());
            async.complete();
        });
        defaultGroupingService.updateGrouping(groupingTestId, nameTest);
    }

    @Test
    public void TestAddGrouping(TestContext ctx) {
        Async async = ctx.async();
        String queryExpected = "INSERT INTO viesco.rel_grouping_class(grouping_id, class_id, group_id, created_at, updated_at) VALUES(?, ?, ?, ?, ?)";
        String date = DateHelper.getCurrentDate(DateHelper.MONGO_FORMAT);
        PowerMockito.mockStatic(DateHelper.class);
        PowerMockito.when(DateHelper.getCurrentDate(DateHelper.MONGO_FORMAT)).thenReturn(date);

        //tests variables
        String groupingTestId = "grouping_id";
        String classTestId = "class_id";
        String groupTestId = "group_id";

        PowerMockito.doAnswer(answer -> Future.succeededFuture(Boolean.TRUE)).when(defaultGroupingService).groupAndClassExist(classTestId, groupTestId);

        vertx.eventBus().consumer(address, message -> {
            JsonObject body = (JsonObject) message.body();
            ctx.assertEquals(Field.PREPARED, body.getString(Field.ACTION));
            ctx.assertEquals(queryExpected, body.getString(Field.STATEMENT));
            JsonArray args = body.getJsonArray(Field.VALUES);
            ctx.assertEquals(new JsonArray(Arrays.asList(groupingTestId,
                    classTestId,
                    groupTestId,
                    DateHelper.getCurrentDate(DateHelper.MONGO_FORMAT),
                    DateHelper.getCurrentDate(DateHelper.MONGO_FORMAT)))
                    .toString(), args.toString());
            async.complete();
        });
        defaultGroupingService.addToGrouping(groupingTestId, groupTestId, classTestId);
    }

}
