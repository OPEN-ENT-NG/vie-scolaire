package fr.openent.viescolaire.service.impl;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jRest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;

@RunWith(VertxUnitRunner.class)
public class DefaultGroupeServiceTest {
    private final Neo4j neo4j = Neo4j.getInstance();
    private final Neo4jRest neo4jRest = mock(Neo4jRest.class);
    private Vertx vertx;
    private DefaultGroupeService defaultGroupeService;

    @Before
    public void setUp() throws NoSuchFieldException {
        vertx = Vertx.vertx();
        FieldSetter.setField(neo4j, neo4j.getClass().getDeclaredField("database"), neo4jRest);
        this.defaultGroupeService = Mockito.spy(new DefaultGroupeService());
    }

    @Test
    public void testSearch(TestContext ctx) {
        Async async = ctx.async();
        List<String> fields = Arrays.asList("name", "id", "student");

        String expectedQuery = "MATCH (u:User {id:{userId}})-[:IN]->(:ProfileGroup)-[:DEPENDS]->(g: Class)-[:BELONGS]->(s:Structure {id:{structureId}})" +
                " WHERE toLower(g.name) CONTAINS {query} OR toLower(g.id) CONTAINS {query} OR toLower(g.name) CONTAINS {query}" +
                " RETURN g.id as id, g.name as name ORDER BY g.name UNION MATCH (u:User" +
                " {profiles:['Student']})--(:ProfileGroup)--(c:Class)--(:ProfileGroup)--(t:User {id:{userId}}) WITH u, c" +
                " MATCH (u)--(g)-[:DEPENDS]->(s:Structure {id:{structureId}}) WHERE (g:FunctionalGroup) AND toLower(g.name)" +
                " CONTAINS {query} OR toLower(g.id) CONTAINS {query} OR toLower(g.name) CONTAINS {query} RETURN DISTINCT" +
                " g.id as id, g.name as name ORDER BY g.name";
        String expectedResult = "{\"structureId\":\"structureId\",\"userId\":\"userId\",\"query\":\"query\"}";
        Mockito.doAnswer((Answer<Void>) invocation -> {
            String queryResult = invocation.getArgument(0);
            JsonObject paramsResult = invocation.getArgument(1);
            ctx.assertEquals(queryResult, expectedQuery);
            ctx.assertEquals(paramsResult.toString(), expectedResult);
            async.complete();
            return null;
        }).when(neo4jRest).execute(Mockito.anyString(), Mockito.any(JsonObject.class), Mockito.any(Handler.class));

        this.defaultGroupeService.search("structureId", "userId", "query", fields, null);
    }

    @Test
    public void testGetNameOfGroupClass(TestContext ctx) {
        Async async = ctx.async();

        String expectedQuery = "MATCH (c:`Class`) WHERE c.id IN {idsAudience} RETURN c.id as id,  c.name as name UNION " +
                "MATCH (g:`FunctionalGroup`) WHERE g.id IN {idsAudience} return g.id as id, g.name as name UNION MATCH " +
                "(g:`ManualGroup`) WHERE g.id IN {idsAudience} return g.id as id, g.name as name ";
        String expectedResult = "{\"idsAudience\":[\"id1\",\"id2\"]}";
        Mockito.doAnswer((Answer<Void>) invocation -> {
            String queryResult = invocation.getArgument(0);
            JsonObject paramsResult = invocation.getArgument(1);
            ctx.assertEquals(queryResult, expectedQuery);
            ctx.assertEquals(paramsResult.toString(), expectedResult);
            async.complete();
            return null;
        }).when(neo4jRest).execute(Mockito.anyString(), Mockito.any(JsonObject.class), Mockito.any(Handler.class));
        this.defaultGroupeService.getNameOfGroupClass(Arrays.asList("id1", "id2"));
    }

    @Test
    public void testIsGroupExist(TestContext ctx) {
        Async async = ctx.async();

        String expectedQuery = "MATCH (g:`Group` {id: {groupeId}}) WITH COUNT(g) > 0 as node_exists RETURN node_exists";
        String expectedResult = "{\"groupId\":\"id1\"}";
        Mockito.doAnswer((Answer<Void>) invocation -> {
            String queryResult = invocation.getArgument(0);
            JsonObject paramsResult = invocation.getArgument(1);
            ctx.assertEquals(queryResult, expectedQuery);
            ctx.assertEquals(paramsResult.toString(), expectedResult);
            async.complete();
            return null;
        }).when(neo4jRest).execute(Mockito.anyString(), Mockito.any(JsonObject.class), Mockito.any(Handler.class));
        this.defaultGroupeService.isGroupExist("id1");
    }
}
