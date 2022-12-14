package fr.openent.viescolaire.multiteaching;
import fr.openent.Viescolaire;
import fr.openent.viescolaire.db.DB;
import fr.openent.viescolaire.service.*;
import fr.openent.viescolaire.service.impl.*;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.entcore.common.sql.Sql;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import static fr.openent.Viescolaire.VSCO_SCHEMA;


@RunWith(VertxUnitRunner.class)
public class MultiTeachingServiceTest {

    Sql sql = Mockito.mock(Sql.class);
    private MultiTeachingService multiTeachingService;


    @Before
    public void setUp() {
        DB.getInstance().init(null, sql, null);
        this.multiTeachingService = new DefaultMultiTeachingService();
    }

    @Test
    public void getMultiTeaching_should_use_proper_query(TestContext ctx) {
        String PROPER_QUERY = "SELECT * FROM "+ VSCO_SCHEMA + "." + Viescolaire.VSCO_MULTI_TEACHING_TABLE + " " +
                "WHERE structure_id = ? AND deleted_date IS NULL;";


        String structureId = "structureId";

        Mockito.doAnswer((Answer<Void>) invocation -> {
            String query = invocation.getArgument(0);
            JsonArray params = invocation.getArgument(1);
            ctx.assertEquals(query, PROPER_QUERY);
            ctx.assertEquals(params,  new JsonArray().add(structureId));
            return null;
        }).when(sql).prepared(Mockito.anyString(), Mockito.any(JsonArray.class), Mockito.any(Handler.class));


        multiTeachingService.getMultiTeaching(structureId, null);
    }

    @Test
    public void getMultiTeachings_should_use_proper_query(TestContext ctx) {

        JsonArray ids  = new JsonArray();
        ids.add("id");


        String PROPER_QUERY = "SELECT * FROM "+ VSCO_SCHEMA + "." + Viescolaire.VSCO_MULTI_TEACHING_TABLE + " " +
                "WHERE id IN " + Sql.listPrepared(ids.getList()) + " AND deleted_date IS NULL;";

        Mockito.doAnswer((Answer<Void>) invocation -> {
            String query = invocation.getArgument(0);
            JsonArray params = invocation.getArgument(1);
            ctx.assertEquals(query, PROPER_QUERY);
            ctx.assertEquals(params,  ids);
            return null;
        }).when(sql).prepared(Mockito.anyString(), Mockito.any(JsonArray.class), Mockito.any(Handler.class));


        multiTeachingService.getMultiTeachings(ids, null);
    }
}
