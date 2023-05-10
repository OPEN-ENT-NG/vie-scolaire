package fr.openent.viescolaire.service.impl;

import fr.openent.viescolaire.model.SlotModel;
import fr.openent.viescolaire.model.TimeslotModel;
import fr.openent.viescolaire.service.ServiceFactory;
import fr.wseduc.webutils.eventbus.ResultMessage;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.entcore.common.sql.Sql;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.powermock.reflect.Whitebox;

import java.util.Arrays;
import java.util.List;

@RunWith(PowerMockRunner.class) //Using the PowerMock runner
@PowerMockRunnerDelegate(VertxUnitRunner.class) //And the Vertx runner
@PrepareForTest({Sql.class}) //Prepare the static class you want to mock
public class DefaultTimeSlotServiceTest {

    private Vertx vertx;
    private DefaultTimeSlotService defaultTimeSlotService;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        Sql.getInstance().init(vertx.eventBus(), "fr.openent.viescolaire");
        this.defaultTimeSlotService = new DefaultTimeSlotService(new ServiceFactory(vertx.eventBus(), null,
                null, null, new JsonObject()));
        this.defaultTimeSlotService = Mockito.spy(this.defaultTimeSlotService);
    }

    @Test
    public void testInsertSlot(TestContext ctx) throws Exception {
        JsonArray slots = new JsonArray().add(
                new JsonObject().put("id", "id1").put("structureId", "structureId1").put("name", "name1").put("startHour", "startHour1").put("endHour", "endHour1")
        ).add(
                new JsonObject().put("id", "id2").put("structureId", "structureId2").put("name", "name2").put("startHour", "startHour2").put("endHour", "endHour2")
        ).add(
                new JsonObject().put("id", "id3").put("structureId", "structureId3").put("name", "name3").put("startHour", "startHour3").put("endHour", "endHour3")
        );
        JsonObject result = Whitebox.invokeMethod(this.defaultTimeSlotService, "insertSlot", slots);
        String expected = "{\"action\":\"prepared\",\"statement\":\"INSERT INTO viesco.slots(id, structure_id, name, start_hour, end_hour) VALUES (?, ?, ?, ?, ?),(?, ?, ?, ?, ?),(?, ?, ?, ?, ?)\",\"values\":[\"id1\",\"structureId1\",\"name1\",\"startHour1\",\"endHour1\",\"id2\",\"structureId2\",\"name2\",\"startHour2\",\"endHour2\",\"id3\",\"structureId3\",\"name3\",\"startHour3\",\"endHour3\"]}";
        ctx.assertEquals(expected, result.toString());
    }

    @Test
    public void testGetTimeslotIdFromClasses(TestContext ctx) {
        Async async = ctx.async();
        List<String> idsClass = Arrays.asList("idClass1", "idClass2");
        JsonArray jsonArray = new JsonArray("[{\"id_class\": \"idClass1\", \"id_time_slot\": \"idTimeSlot1\"}," +
                " {\"id_class\": \"idClass2\", \"id_time_slot\": \"idTimeSlot2\"}]");
        Future<JsonArray> jsonArrayFuture = Future.succeededFuture(jsonArray);


        Mockito.doReturn(jsonArrayFuture).when(this.defaultTimeSlotService).getSlotProfilesFromClasses(idsClass);

        this.defaultTimeSlotService.getTimeslotIdFromClasses(idsClass).onSuccess(res -> {
            ctx.assertEquals(res.size(), 2);
            ctx.assertEquals(res.getOrDefault("idClass1", ""), "idTimeSlot1");
            ctx.assertEquals(res.getOrDefault("idClass2", ""), "idTimeSlot2");
            async.complete();
        });

        async.awaitSuccess(10000);
    }

    @Test
    public void testGetSlotProfileSetting(TestContext ctx) {
        Async async = ctx.async();
        List<String> structureIdList = Arrays.asList("structureId1", "structureId2");

        Sql sql = Mockito.spy(Sql.getInstance());
        PowerMockito.spy(Sql.class);
        PowerMockito.when(Sql.getInstance()).thenReturn(sql);

        String query = "SELECT * FROM viesco.time_slots WHERE id_structure IN (?,?)";
        JsonArray params = new JsonArray("[\"structureId1\",\"structureId2\"]");

        JsonArray sqlResult = new JsonArray("[[\"structureId1\", \"idTimeslot1\"], [\"structureId2\", \"idTimeslot2\"]]");
        JsonArray fields = new JsonArray("[\"id_structure\", \"id\"]");

        Mockito.doAnswer(invocation -> {
            ctx.assertEquals(query, invocation.getArgument(0));
            ctx.assertEquals(params.toString(), invocation.getArgument(1).toString());
            ((Handler<Message<JsonObject>>) invocation.getArgument(2)).handle(new ResultMessage(new JsonObject().put("results", sqlResult)).put("fields", fields));
            return null;
        }).when(sql).prepared(Mockito.any(), Mockito.any(), Mockito.any());

        this.defaultTimeSlotService.getSlotProfileSetting(structureIdList)
                .onSuccess(event -> {
                    ctx.assertEquals(event.size(), 2);
                    ctx.assertEquals(event.getOrDefault("structureId1", ""), "idTimeslot1");
                    ctx.assertEquals(event.getOrDefault("structureId2", ""), "idTimeslot2");
                    async.complete();
                });

        async.awaitSuccess(10000);
    }

    @Test
    public void testGetTimeSlotFromId(TestContext ctx) {
        Async async = ctx.async();
        List<String> timeslotId = Arrays.asList("timeslotId1", "timeslotId2");

        List<SlotModel> slotModelList = Arrays.asList(new SlotModel().setEndHour("09:30").setStartHour("08h30").setId("slotId").setName("slotName"));
        TimeslotModel timeslotModel = new TimeslotModel().setId("timeslotID").setName("timeslotName").setStructureId("structureId").setSlots(slotModelList);
        JsonArray jsonArray = new JsonArray(Arrays.asList(timeslotModel.toJson()));
        Future<JsonArray> jsonArrayFuture = Future.succeededFuture(jsonArray);

        Mockito.doReturn(jsonArrayFuture).when(this.defaultTimeSlotService).getMultipleTimeSlot(timeslotId);

        this.defaultTimeSlotService.getTimeSlotFromId(timeslotId).onSuccess(event -> {
            ctx.assertEquals(event.size(), 1);
            ctx.assertEquals(event.get(0).toJson().toString(), timeslotModel.toJson().toString());
            async.complete();
        });

        async.awaitSuccess(10000);
    }
}
