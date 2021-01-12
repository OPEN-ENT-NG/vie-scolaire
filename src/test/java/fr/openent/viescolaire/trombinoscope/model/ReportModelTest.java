package fr.openent.viescolaire.trombinoscope.model;

import fr.openent.viescolaire.model.Trombinoscope.ReportException;
import fr.openent.viescolaire.model.Trombinoscope.TrombinoscopeFailure;
import fr.openent.viescolaire.model.Trombinoscope.TrombinoscopeReport;
import fr.wseduc.webutils.data.FileResolver;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(VertxUnitRunner.class)
public class ReportModelTest {

    private Vertx vertx;
    private EventBus eb;

    @Before
    public void setUp(TestContext context) {
        /* Server mocked settings */
        vertx = Vertx.vertx();
        vertx.exceptionHandler(context.exceptionHandler());
    }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testReportNotNull(TestContext ctx) {
        TrombinoscopeReport report = new TrombinoscopeReport(vertx, "fr");
        ctx.assertNotNull(report);
    }

    @Test
    public void testReportHasContentWithObject(TestContext ctx) {
        TrombinoscopeReport report = new TrombinoscopeReport(vertx, "fr");
        report.setUai("TEST01");
        report.setStructureId("0981735e-4d91-4e86-9c5d-5e43445095d7");
        report.addReport(new ReportException("TEST","/test"));
        boolean isNotEmpty =
                !report.getUai().isEmpty() &&
                !report.getStructureId().isEmpty() &&
                !report.getReports().isEmpty();
        ctx.assertTrue(isNotEmpty);
    }

    @Test
    public void testShouldGenerateReport(TestContext ctx) {
        TrombinoscopeReport report = new TrombinoscopeReport(vertx, "fr");
        report.setUai("TEST01");
        report.setStructureId("0981735e-4d91-4e86-9c5d-5e43445095d7");

        report.start();
        report.addReport(new ReportException("TEST","/test"));
        report.end();

        FileResolver.getInstance().setBasePath("./");

        report.generate(rep -> ctx.assertFalse(rep.failed()));
    }
}
