package fr.openent.viescolaire.security;

import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.db.DB;
import fr.wseduc.webutils.http.Binding;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.impl.HeadersAdaptor;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.user.UserInfos;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.util.List;

@RunWith(PowerMockRunner.class) //Using the PowerMock runner
@PowerMockRunnerDelegate(VertxUnitRunner.class) //And the Vertx runner
@PrepareForTest({StructureAdminPersonnalTeacherFromGroup.class, Neo4j.class})
public class StructureAdminPersonnalTeacherFromGroupTest {
    StructureAdminPersonnalTeacherFromGroup access;
    HttpServerRequest request;
    Binding binding;
    MultiMap params;
    UserInfos user;
    Neo4j neo4j = Mockito.mock(Neo4j.class);
    String PROPER_QUERY;
    String groupId;
    JsonObject queryParamObject;

    @Before
    public void setUp() throws NoSuchFieldException {
        DB.getInstance().init(neo4j, null, null);
        request = Mockito.mock(HttpServerRequest.class);
        binding = Mockito.mock(Binding.class);
        params = Mockito.spy(new HeadersAdaptor(new DefaultHttpHeaders()));
        user = new UserInfos();
        access = new StructureAdminPersonnalTeacherFromGroup();
        PROPER_QUERY = "MATCH(s:Structure)<-[:DEPENDS]-(g:Group) WHERE g.id = {groupId} RETURN DISTINCT s.id as structureId";
        groupId = "groupId";
        queryParamObject = new JsonObject();
        queryParamObject.put(Field.GROUP_ID_CAMEL, groupId);
    }

    @Test
    public void authorize_should_use_proper_query(TestContext ctx) {
        params.set(Field.GROUP_ID_CAMEL, Field.GROUP_ID_CAMEL);
        Mockito.doReturn(params).when(request).params();
        Mockito.doAnswer((Answer<Void>) invocation -> {
            String query = invocation.getArgument(0);
            JsonObject queryParams = invocation.getArgument(1);
            ctx.assertEquals(query, PROPER_QUERY);
            ctx.assertEquals(queryParams,  queryParamObject);
            return null;
        }).when(neo4j).execute(Mockito.anyString(), Mockito.any(JsonObject.class), Mockito.any(Handler.class));
        access.authorize(request, binding, user, null);
    }
}
