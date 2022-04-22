package fr.openent.viescolaire.service.impl;

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
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;

@RunWith(VertxUnitRunner.class)
public class DefaultTimeSlotServiceTest {

    private Vertx vertx;
    private DefaultTimeSlotService defaultTimeSlotService;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        Sql.getInstance().init(vertx.eventBus(), "fr.openent.viescolaire");
        this.defaultTimeSlotService = new DefaultTimeSlotService();
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
}
