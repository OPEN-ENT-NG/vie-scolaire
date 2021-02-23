package fr.openent.viescolaire.memento;
import fr.openent.Viescolaire;
import fr.openent.viescolaire.db.DB;
import fr.openent.viescolaire.service.MementoService;
import fr.openent.viescolaire.service.impl.DefaultMementoService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
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
import org.powermock.reflect.Whitebox;

import java.util.Arrays;


@RunWith(VertxUnitRunner.class)
public class MementoServiceTest {

    Neo4j neo4j = Mockito.mock(Neo4j.class);
    Sql sql = Mockito.mock(Sql.class);
    private MementoService mementoService;


    @Before
    public void setUp() {
        DB.getInstance().init(neo4j, sql, null);
        this.mementoService = new DefaultMementoService();
    }

    @Test
    public void postComment_should_use_proper_query(TestContext ctx) throws Exception {
       String PROPER_QUERY = "INSERT INTO " + Viescolaire.MEMENTO_SCHEMA + ".comments (student, owner, comment) VALUES (?, ?, ?) " +
                "ON CONFLICT ON CONSTRAINT uniq_comment DO UPDATE SET comment = ? WHERE comments.owner = ? AND comments.student = ?;";
        String studentId = "studentId";
        String owner = "owner";
        String comment = "comment";

        Mockito.doAnswer((Answer<Void>) invocation -> {
            String query = invocation.getArgument(0);
            JsonArray params = invocation.getArgument(1);
            ctx.assertEquals(query, PROPER_QUERY);
            ctx.assertEquals(params,  new JsonArray(Arrays.asList(studentId, owner, comment, comment, owner, studentId)));
            return null;
        }).when(sql).prepared(Mockito.anyString(), Mockito.any(JsonArray.class), Mockito.any(Handler.class));

        mementoService.postComment(studentId, owner, comment, null);
    }

    @Test
    public void getStudentComment_should_use_proper_query(TestContext ctx) throws Exception {
        String PROPER_QUERY = "SELECT comment FROM " + Viescolaire.MEMENTO_SCHEMA + ".comments WHERE owner = ? AND student = ?;";

        String studentId = "studentId";
        String owner = "owner";

        Mockito.doAnswer((Answer<Void>) invocation -> {
            String query = invocation.getArgument(0);
            JsonArray params = invocation.getArgument(1);
            ctx.assertEquals(query, PROPER_QUERY);
            ctx.assertEquals(params,  new JsonArray(Arrays.asList(owner, studentId)));
            return null;
        }).when(sql).prepared(Mockito.anyString(), Mockito.any(JsonArray.class), Mockito.any(Handler.class));

        Whitebox.invokeMethod(mementoService, "getStudentComment", studentId, owner, null);
    }

    @Test
    public void retrieveStudentClasses_should_use_proper_query(TestContext ctx) throws Exception {
        String PROPER_QUERY = "MATCH (u:User {id:{id}})-[:IN]->(:ProfileGroup)-[:DEPENDS]->(g:Class)" +
                "RETURN collect(g.name) AS classes, collect(g.id) as id";

        String studentId = "studentId";

        Mockito.doAnswer((Answer<Void>) invocation -> {
            String query = invocation.getArgument(0);
            JsonObject params = invocation.getArgument(1);
            ctx.assertEquals(query, PROPER_QUERY);
            ctx.assertEquals(params,  new JsonObject().put("id", studentId));
            return null;
        }).when(DB.getInstance().neo4j()).execute(Mockito.anyString(), Mockito.any(JsonObject.class), Mockito.any(Handler.class));

        Whitebox.invokeMethod(mementoService, "retrieveStudentClasses", studentId, null);
    }

    @Test
    public void retrieveStudentAndItsFunctionalGroups_should_use_proper_query(TestContext ctx) throws Exception {
        String PROPER_QUERY = "MATCH(u:User {id:{id}})" +
                "OPTIONAL MATCH (u)-[:IN]-(g:FunctionalGroup) " +
                "RETURN u.id as id, u.lastName + ' ' + u.firstName AS name, u.birthDate as birth_date, " +
                "u.accommodation as accommodation, u.transport as transport, collect(g.name) as groups";
        String studentId = "studentId";

        Mockito.doAnswer((Answer<Void>) invocation -> {
            String query = invocation.getArgument(0);
            JsonObject params = invocation.getArgument(1);
            ctx.assertEquals(query, PROPER_QUERY);
            ctx.assertEquals(params,  new JsonObject().put("id", studentId));
            return null;
        }).when(DB.getInstance().neo4j()).execute(Mockito.anyString(), Mockito.any(JsonObject.class), Mockito.any(Handler.class));

        Whitebox.invokeMethod(mementoService, "retrieveStudentAndItsFunctionalGroups", studentId, null);
    }

    @Test
    public void getAllRelatives_should_use_proper_query(TestContext ctx) throws Exception {
        String PROPER_QUERY = "MATCH(:User {id:{id}})-[:RELATED]->(u:User) " +
                "RETURN u.id as id, u.lastName + ' ' + u.firstName AS name, u.title AS title, " +
                "CASE WHEN u.mobilePhone is null THEN u.mobile ELSE u.mobilePhone[0] END AS mobile, " +
                "u.homePhone AS phone, u.address + ' ' + u.zipCode + ' ' + u.city AS address, u.email as email, " +
                "NOT(HAS(u.activationCode)) AS activated";

        String studentId = "studentId";

        Mockito.doAnswer((Answer<Void>) invocation -> {
            String query = invocation.getArgument(0);
            JsonObject params = invocation.getArgument(1);
            ctx.assertEquals(query, PROPER_QUERY);
            ctx.assertEquals(params,  new JsonObject().put("id", studentId));
            return null;
        }).when(DB.getInstance().neo4j()).execute(Mockito.anyString(), Mockito.any(JsonObject.class), Mockito.any(Handler.class));


        Whitebox.invokeMethod(mementoService, "getAllRelatives", studentId, null);
    }

    @Test
    public void updateRelativePriorities_should_use_proper_query(TestContext ctx) {
        String PROPER_QUERY = "MATCH(u:User {id:{id}}) SET u.primaryRelatives = {relativeIds} RETURN u.primaryRelatives AS primaryRelatives";
        String studentId = "studentId";
        JsonArray relativeIds = new JsonArray().add("relativeId");

        Mockito.doAnswer((Answer<Void>) invocation -> {
            String query = invocation.getArgument(0);
            JsonObject params = invocation.getArgument(1);
            ctx.assertEquals(query, PROPER_QUERY);
            ctx.assertEquals(params,  new JsonObject().put("id", studentId).put("relativeIds", relativeIds));
            return null;
        }).when(DB.getInstance().neo4j()).execute(Mockito.anyString(), Mockito.any(JsonObject.class), Mockito.any(Handler.class));

        mementoService.updateRelativePriorities(studentId, relativeIds, null);
    }
}
