package fr.openent.viescolaire.trombinoscope.model;

import fr.openent.viescolaire.model.Trombinoscope.ReportException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(VertxUnitRunner.class)
public class ReportExceptionModelTest {

    private final ReportException reportExceptionObject = new ReportException("test", "/test", Arrays.asList("3A", "3B"),
      Arrays.asList("Adam MILIEN", "Tom MATE"));


    @Test
    public void testReportExceptionNotNull(TestContext ctx) {
        ctx.assertNotNull(reportExceptionObject);
    }

    @Test
    public void testReportExceptionHasContentWithObject(TestContext ctx) {
        boolean isNotEmpty = !reportExceptionObject.message().isEmpty() &&
                !reportExceptionObject.path().isEmpty() &&
                !reportExceptionObject.audiencesConcerned().isEmpty() &&
                !reportExceptionObject.studentsConcerned().isEmpty();
        ctx.assertTrue(isNotEmpty);
    }

    @Test
    public void testReportExceptionToJson(TestContext ctx) {
        JsonObject reportJSON1 = reportExceptionObject.toJSON();
        JsonObject reportJSON2 = new JsonObject()
                .put("message", "test")
                .put("path", "/test")
                .put("audiencesConcerned", new JsonArray().add("3A").add("3B"))
                .put("studentsConcerned", new JsonArray().add("Adam MILIEN").add("Tom MATE"));

        JsonObject reportJSON3 = new JsonObject()
                .put("message", "test")
                .put("path", "/test")
                .put("audiencesConcerned", new JsonArray().add("3C").add("3D"))
                .put("studentsConcerned", new JsonArray().add("Olivier MILIEN").add("Tom MATE"));

        ctx.assertEquals(reportJSON1, reportJSON2);
        ctx.assertNotEquals(reportJSON1, reportJSON3);
    }

}
