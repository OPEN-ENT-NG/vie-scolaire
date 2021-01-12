package fr.openent.viescolaire.trombinoscope.model;

import fr.openent.viescolaire.model.Trombinoscope.TrombinoscopeFailure;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.mock;

@RunWith(VertxUnitRunner.class)
public class FailureModelTest {

    private final JsonObject failureJsonObject_1 = new JsonObject()
            .put("id", 5161)
            .put("path", "/test")
            .put("message", "test")
            .put("structure_id", "0981735e-4d91-4e86-9c5d-5e43445095d7")
            .put("picture_id", "73cecfb1b4b3")
            .put("created_at", "2021-08-01 17:15:00");

    @Test
    public void testFailureNotNull(TestContext ctx) {
        JsonObject failureObjectMock = mock(JsonObject.class);
        TrombinoscopeFailure failure = new TrombinoscopeFailure(failureObjectMock);
        ctx.assertNotNull(failure);
    }

    @Test
    public void testFailureHasContentWithObject(TestContext ctx) {
        TrombinoscopeFailure failure = new TrombinoscopeFailure(failureJsonObject_1);
        boolean isNotEmpty = (failure.getId() != null) &&
                !failure.getPath().isEmpty() &&
                !failure.getMessage().isEmpty() &&
                !failure.getStructureId().isEmpty() &&
                !failure.getPictureId().isEmpty() &&
                !failure.getCreatedAt().isEmpty();
        ctx.assertTrue(isNotEmpty);
    }
}
