package fr.openent.viescolaire.structure;

import fr.openent.viescolaire.db.DB;
import fr.openent.viescolaire.service.impl.StructureService;
import fr.wseduc.mongodb.MongoDb;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.sql.Sql;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;


@RunWith(VertxUnitRunner.class)
public class StructureServiceTest {

    Neo4j neo4j = Mockito.mock(Neo4j.class);

    private StructureService structureService = new StructureService();

    @Before
    public void setup() {
        DB.getInstance().init(neo4j, null, null);
        structureService = new StructureService();
    }

    @Test
    public void retrieveStructureInfo_should_use_correct_request(TestContext ctx) {
        String CORRECT_REQUEST = "MATCH (s:Structure {id: {id}}) RETURN s.id as id, s.name as name, s.UAI as UAI";
        String structureId1 = "structureId1";

        Mockito.doAnswer((Answer<Void>) invocation -> {
            String query = invocation.getArgument(0);
            JsonObject params = invocation.getArgument(1);
            ctx.assertEquals(query, CORRECT_REQUEST);
            ctx.assertEquals(params,  new JsonObject().put("id", structureId1));
            return null;
        }).when(DB.getInstance().neo4j()).execute(Mockito.anyString(), Mockito.any(JsonObject.class), Mockito.any(Handler.class));

        structureService.retrieveStructureInfo(structureId1, null);
    }
}
