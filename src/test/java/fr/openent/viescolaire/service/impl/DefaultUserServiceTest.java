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
public class DefaultUserServiceTest {

    private DefaultUserService defaultUserService;
    private final Neo4j neo4j = Neo4j.getInstance();
    private final Neo4jRest neo4jRest = mock(Neo4jRest.class);

    @Before
    public void setUp() throws NoSuchFieldException {
        this.defaultUserService = new DefaultUserService(Vertx.vertx().eventBus());
        FieldSetter.setField(neo4j, neo4j.getClass().getDeclaredField("database"), neo4jRest);
    }

    @Test
    public void testSearchEmptyField(TestContext ctx) {
        Async async = ctx.async();
        List<String> fields = Arrays.asList();
        this.defaultUserService.search("structureId", "userId", "query", fields, "profile", event -> {
            ctx.assertEquals(event.right().getValue().toString(), "[]");
            async.complete();
        });
    }

    @Test
    public void testSearchOneField(TestContext ctx) {
        Async async = ctx.async();
        List<String> fields = Arrays.asList("field1");
        String expectedQuery = "MATCH (u:User)-[:IN]->(:ProfileGroup)-[:DEPENDS]->(c:Class)-[:BELONGS]->(s:Structure {id:{structureId}})," +
                " (t:User {id: {userId}})-[:IN]->(:ProfileGroup)-[:DEPENDS]->(c) WHERE u.profiles = {profiles} AND" +
                " (toLower(u.displayName) CONTAINS {query} ) RETURN distinct u.id as id, (u.lastName + ' ' + u.firstName) as displayName," +
                " u.lastName as lastName, u.firstName as firstName, u.classes as idClasse, collect(c.name) as classesNames ORDER BY displayName;";
        String expectedParams = "{\"structureId\":\"structureId\",\"userId\":\"userId\",\"query\":\"query\",\"profiles\":[\"profile\"]}";

        Mockito.doAnswer((Answer<Void>) invocation -> {
            String queryResult = invocation.getArgument(0);
            JsonObject paramsResult = invocation.getArgument(1);
            ctx.assertEquals(queryResult, expectedQuery);
            ctx.assertEquals(paramsResult.toString(), expectedParams);
            async.complete();
            return null;
        }).when(neo4jRest).execute(Mockito.anyString(), Mockito.any(JsonObject.class), Mockito.any(Handler.class));

        this.defaultUserService.search("structureId", "userId", "query", fields, "profile", null);
    }

    @Test
    public void testSearchTwoField(TestContext ctx) {
        Async async = ctx.async();
        List<String> fields = Arrays.asList("field1", "field2");
        String expectedQuery = "MATCH (u:User)-[:IN]->(:ProfileGroup)-[:DEPENDS]->(c:Class)-[:BELONGS]->(s:Structure {id:{structureId}})," +
                " (t:User {id: {userId}})-[:IN]->(:ProfileGroup)-[:DEPENDS]->(c) WHERE u.profiles = {profiles} AND " +
                "(toLower(u.displayName) CONTAINS {query} OR toLower(u.displayName) CONTAINS {query} ) RETURN distinct u.id as id," +
                " (u.lastName + ' ' + u.firstName) as displayName, u.lastName as lastName, u.firstName as firstName, u.classes as idClasse," +
                " collect(c.name) as classesNames ORDER BY displayName;";
        String expectedParams = "{\"structureId\":\"structureId\",\"userId\":\"userId\",\"query\":\"query\",\"profiles\":[\"profile\"]}";

        Mockito.doAnswer((Answer<Void>) invocation -> {
            String queryResult = invocation.getArgument(0);
            JsonObject paramsResult = invocation.getArgument(1);
            ctx.assertEquals(queryResult, expectedQuery);
            ctx.assertEquals(paramsResult.toString(), expectedParams);
            async.complete();
            return null;
        }).when(neo4jRest).execute(Mockito.anyString(), Mockito.any(JsonObject.class), Mockito.any(Handler.class));

        this.defaultUserService.search("structureId", "userId", "query", fields, "profile", null);
    }

    @Test
    public void testSearchNullUserId(TestContext ctx) {
        Async async = ctx.async();
        List<String> fields = Arrays.asList("field1", "field2");
        String expectedQuery = "MATCH (u:User)-[:IN]->(p:ProfileGroup)-[:DEPENDS*]->(s:Structure) ,(p)-[:DEPENDS]->(c:Class) WHERE s.id" +
                " = {structureId} AND u.profiles = {profiles} AND (toLower(u.displayName) CONTAINS {query} OR toLower(u.displayName)" +
                " CONTAINS {query} ) RETURN distinct u.id as id, (u.lastName + ' ' + u.firstName) as displayName, u.lastName as lastName," +
                " u.firstName as firstName, u.classes as idClasse ,collect(c.name) as classesNames ORDER BY displayName;";
        String expectedParams = "{\"structureId\":\"structureId\",\"userId\":null,\"query\":\"query\",\"profiles\":[\"profile\"]}";

        Mockito.doAnswer((Answer<Void>) invocation -> {
            String queryResult = invocation.getArgument(0);
            JsonObject paramsResult = invocation.getArgument(1);
            ctx.assertEquals(queryResult, expectedQuery);
            ctx.assertEquals(paramsResult.toString(), expectedParams);
            async.complete();
            return null;
        }).when(neo4jRest).execute(Mockito.anyString(), Mockito.any(JsonObject.class), Mockito.any(Handler.class));

        this.defaultUserService.search("structureId", null, "query", fields, "profile", null);
    }
}
