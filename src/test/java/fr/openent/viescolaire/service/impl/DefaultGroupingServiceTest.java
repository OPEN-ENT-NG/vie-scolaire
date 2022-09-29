package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.service.ServiceFactory;
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
import java.util.Collections;

import static org.mockito.Mockito.mock;


@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(VertxUnitRunner.class)
@PrepareForTest(DefaultGroupingService.class)
public class DefaultGroupingServiceTest {
    private Vertx vertx;
    private final String address = "fr.openent.Viescolaire";
    private DefaultGroupingService defaultGroupingService;
    private final Neo4j neo4j = Neo4j.getInstance();
    private final Neo4jRest neo4jRest = mock(Neo4jRest.class);
    private final String tableGrouping = Viescolaire.VSCO_SCHEMA + "." + Viescolaire.GROUPING_TABLE;
    private final String TABLE_REL = Viescolaire.VSCO_SCHEMA + "." + Viescolaire.REL_GROUPING_CLASS_TABLE;

    @Before
    public void setUp() throws NoSuchFieldException {
        this.vertx = Vertx.vertx();
        this.defaultGroupingService = PowerMockito.spy(new DefaultGroupingService(new ServiceFactory(vertx.eventBus())));
        Sql.getInstance().init(vertx.eventBus(), address);
        FieldSetter.setField(neo4j, neo4j.getClass().getDeclaredField("database"), neo4jRest);
    }

    @Test
    public void testListGrouping(TestContext ctx) throws Exception {
        Async async = ctx.async(2);

        JsonArray result = new JsonArray().add(3).add(4);
        Future<JsonArray> futureGetGroupings = Future.succeededFuture(result);
        Future<JsonArray> futureGenerateGroupings = Future.succeededFuture(result);
        Future<JsonArray> futureFail = Future.failedFuture("error");
        PowerMockito.doAnswer(invocation -> futureGetGroupings)
                .when(this.defaultGroupingService, "getGroupingsByStructure", "structureId1");
        PowerMockito.doAnswer(invocation -> futureFail)
                .when(this.defaultGroupingService, "getGroupingsByStructure", "structureId2");
        PowerMockito.doAnswer(invocation -> futureGenerateGroupings)
                .when(this.defaultGroupingService, "generateGroupingsList", result);

        this.defaultGroupingService.listGrouping("structureId1").onComplete(event -> {
            ctx.assertTrue(event.succeeded() && event.result().toString().equals(result.toString()));
            async.countDown();
        });

        this.defaultGroupingService.listGrouping("structureId2").onComplete(event -> {
            ctx.assertTrue(event.failed() && "error".equals(event.cause().getMessage()));
            async.countDown();
        });

        async.awaitSuccess(10000);
    }

    @Test
    public void TestCreateGrouping(TestContext ctx) {
        Async async = ctx.async();
        String queryExpected = "INSERT INTO " + tableGrouping + "(id, name, structure_id) VALUES(?, ?, ?)";
        //tests variables
        String groupingTestId = "test_id";
        String structureTestId = "1";

        vertx.eventBus().consumer(address, message -> {
            JsonObject body = (JsonObject) message.body();
            ctx.assertEquals(Field.PREPARED, body.getString(Field.ACTION));
            ctx.assertEquals(queryExpected, body.getString(Field.STATEMENT));
            JsonArray args = body.getJsonArray(Field.VALUES);
            args.remove(0);
            ctx.assertEquals(new JsonArray(Arrays.asList(groupingTestId, structureTestId)).toString(), args.toString());
            async.complete();
        });
        defaultGroupingService.createGrouping(groupingTestId, structureTestId);
    }

    @Test
    public void TestUpdateGrouping(TestContext ctx) {
        Async async = ctx.async();
        String queryExpected = "UPDATE " + tableGrouping + " SET name = ? WHERE id = ?";

        //tests variables
        String groupingTestId = "test_id";
        String nameTest = "name_test";

        vertx.eventBus().consumer(address, message -> {
            JsonObject body = (JsonObject) message.body();
            ctx.assertEquals(Field.PREPARED, body.getString(Field.ACTION));
            ctx.assertEquals(queryExpected, body.getString(Field.STATEMENT));
            JsonArray args = body.getJsonArray(Field.VALUES);
            ctx.assertEquals(new JsonArray(Arrays.asList(nameTest, groupingTestId)).toString(), args.toString());
            async.complete();
        });
        defaultGroupingService.updateGrouping(groupingTestId, nameTest);
    }

    @Test
    public void TestAddGrouping(TestContext ctx) {
        Async async = ctx.async();
        String queryExpected = "INSERT INTO " + TABLE_REL +"(grouping_id, student_division_id)  VALUES(?, ?)";

        //tests variables
        String groupingTestId = "grouping_id";
        String studentDivisionId = "student_division_id";

        PowerMockito.doAnswer(answer -> Future.succeededFuture(Boolean.TRUE)).when(defaultGroupingService).groupOrClassExist(studentDivisionId);

        vertx.eventBus().consumer(address, message -> {
            JsonObject body = (JsonObject) message.body();
            ctx.assertEquals(Field.PREPARED, body.getString(Field.ACTION));
            ctx.assertEquals(queryExpected, body.getString(Field.STATEMENT));
            JsonArray args = body.getJsonArray(Field.VALUES);
            ctx.assertEquals(new JsonArray(Arrays.asList(groupingTestId,
                    studentDivisionId))
                    .toString(), args.toString());
            async.complete();
        });
        defaultGroupingService.addToGrouping(groupingTestId, studentDivisionId);
    }

    @Test
    public void TestDeleteGrouping(TestContext ctx) {
        Async async = ctx.async();
        String queryExpected = "DELETE FROM " + tableGrouping + " WHERE " + tableGrouping + ".id = ?";
        String groupingTestId = "grouping_id";

        vertx.eventBus().consumer(address, message -> {
            JsonObject body = (JsonObject) message.body();
            ctx.assertEquals(Field.PREPARED, body.getString(Field.ACTION));
            ctx.assertEquals(queryExpected, body.getString(Field.STATEMENT));
            JsonArray args = body.getJsonArray(Field.VALUES);
            ctx.assertEquals(new JsonArray(Collections.singletonList(groupingTestId)).toString(), args.toString());
            async.complete();
        });
        defaultGroupingService.deleteGrouping(groupingTestId);
    }

    @Test
    public void TestDeleteGroupingAudience(TestContext ctx) {
        Async async = ctx.async();
        String queryExpected = "DELETE FROM " + TABLE_REL + " WHERE " + "grouping_id = ? AND " + "student_division_id = ?";
        String groupingTestId = "grouping_id";
        String studentDivisionId = "student_division_id";

        vertx.eventBus().consumer(address, message -> {
            JsonObject body = (JsonObject) message.body();
            ctx.assertEquals(Field.PREPARED, body.getString(Field.ACTION));
            ctx.assertEquals(queryExpected, body.getString(Field.STATEMENT));
            JsonArray args = body.getJsonArray(Field.VALUES);
            ctx.assertEquals(new JsonArray(Arrays.asList(groupingTestId,
                    studentDivisionId)).toString(), args.toString());
            async.complete();
        });
        defaultGroupingService.deleteGroupingAudience(groupingTestId,studentDivisionId);
    }

}
