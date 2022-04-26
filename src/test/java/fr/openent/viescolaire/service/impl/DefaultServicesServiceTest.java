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

import java.util.Arrays;

@RunWith(VertxUnitRunner.class)
public class DefaultServicesServiceTest {
    private Vertx vertx;
    private DefaultServicesService defaultServicesService;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        Sql.getInstance().init(vertx.eventBus(), "fr.openent.viescolaire");
        this.defaultServicesService = new DefaultServicesService();
    }

    @Test
    public void TestGetServicesSQL(TestContext ctx) {
        Async async = ctx.async();

        String expectedQuery = "SELECT * FROM viesco.services WHERE id_etablissement = ? AND id_enseignant IN (?,?,?) AND is_visible = ?";
        JsonArray expectedParams = new JsonArray(Arrays.asList("idEtablissement",21,22,23,false));

        vertx.eventBus().consumer("fr.openent.viescolaire", message -> {
            JsonObject body = (JsonObject) message.body();
            ctx.assertEquals("prepared", body.getString("action"));
            ctx.assertEquals(expectedQuery, body.getString("statement"));
            ctx.assertEquals(expectedParams.toString(), body.getJsonArray("values").toString());
            async.complete();
        });

        JsonObject oService = new JsonObject()
                .put("id_enseignant", new JsonArray(Arrays.asList(21,22,23)))
                .put("is_visible", false)
                .put("otherKey", "otherValue");
        defaultServicesService.getServicesSQL("idEtablissement", oService, null);
    }
}
