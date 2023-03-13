package fr.openent.viescolaire.security;

import fr.wseduc.webutils.http.Binding;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.impl.HeadersAdaptor;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.entcore.common.user.UserInfos;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

@RunWith(VertxUnitRunner.class)
public class AdminRightStructureTest {
    AdminRightStructure access;
    HttpServerRequest request;
    Binding binding;
    MultiMap params;
    List<String> structures;
    List<UserInfos.Action> roles;
    UserInfos user;
    UserInfos.Action role;


    @Before
    public void setUp() throws NoSuchFieldException {
        params = Mockito.spy(new HeadersAdaptor(new DefaultHttpHeaders()));
        binding = Mockito.mock(Binding.class);
        request = Mockito.mock(HttpServerRequest.class);
        structures = new ArrayList<>();
        roles = new ArrayList<>();
        user = new UserInfos();
        role = new UserInfos.Action();
        access = new AdminRightStructure();
    }

    @Test
    public void testAuthorize(TestContext ctx) {
        role.setDisplayName(WorkflowActionUtils.ADMIN_RIGHT);
        roles.add(role);
        user.setAuthorizedActions(roles);
        params.set("structureId", "a1a1a1");
        Mockito.doReturn(params).when(request).params();
        structures.add("a1a1a1");
        user.setStructures(structures);
        Async async = ctx.async();
        access.authorize(request, binding, user, result -> {
            ctx.assertEquals(true, result);
            async.complete();
        });
        async.awaitSuccess(10000);
    }


    @Test
    public void testAuthorizeBadStructure(TestContext ctx){
        role.setDisplayName(WorkflowActionUtils.ADMIN_RIGHT);
        roles.add(role);
        user.setAuthorizedActions(roles);
        params.set("structureId", "9af51dc6-ead0-4edb-8978-da14a3e9f49a");
        Mockito.doReturn(params).when(request).params();
        structures.add("azerty");
        user.setStructures(structures);
        Async async = ctx.async();
        access.authorize(request, binding, user, result -> {
            ctx.assertEquals(false, result);
            async.complete();
        });
        async.awaitSuccess(10000);
    }

    @Test
    public void testAuthorizeBadRight(TestContext ctx){
        role.setDisplayName(WorkflowActionUtils.COMPETENCE_ACCESS);
        roles.add(role);
        user.setAuthorizedActions(roles);
        params.set("structureId", "azerty1");
        Mockito.doReturn(params).when(request).params();
        structures.add("azerty1");
        user.setStructures(structures);
        Async async = ctx.async();
        access.authorize(request, binding, user, result -> {
            ctx.assertEquals(false, result);
            async.complete();
        });
        async.awaitSuccess(10000);
    }

    @Test
    public void testAuthorizeBadRightBadStructure(TestContext ctx){
        role.setDisplayName(WorkflowActionUtils.COMPETENCE_ACCESS);
        roles.add(role);
        user.setAuthorizedActions(roles);
        params.set("structureId", "azert");
        Mockito.doReturn(params).when(request).params();
        structures.add("azerty1");
        user.setStructures(structures);
        Async async = ctx.async();
        access.authorize(request, binding, user, result -> {
            ctx.assertEquals(false, result);
            async.complete();
        });
        async.awaitSuccess(10000);
    }
}
