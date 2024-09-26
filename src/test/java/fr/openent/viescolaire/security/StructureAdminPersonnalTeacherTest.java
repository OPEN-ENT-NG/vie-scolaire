package fr.openent.viescolaire.security;

import fr.wseduc.webutils.http.Binding;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.impl.headers.HeadersAdaptor;
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
public class StructureAdminPersonnalTeacherTest {

    StructureAdminPersonnalTeacher access;
    HttpServerRequest request;
    Binding binding;
    UserInfos user = new UserInfos();
    MultiMap map;
    List<String> structures;
    List<UserInfos.Action> actions;
    UserInfos.Action role1;
    @Before
    public void setUp() throws NoSuchFieldException {
        access = new StructureAdminPersonnalTeacher();
        request = Mockito.mock(HttpServerRequest.class);
        binding = Mockito.mock(Binding.class);
        map = Mockito.spy(new HeadersAdaptor(new DefaultHttpHeaders()));
        structures = new ArrayList<>();
        user = new UserInfos();
        actions = new ArrayList<>();
        role1 = new UserInfos.Action();
    }

    @Test
    public void testAuthorizePersonnel(TestContext ctx) {
        role1.setDisplayName(WorkflowActionUtils.ADMIN_RIGHT);
        actions.add(role1);
        user.setAuthorizedActions(actions);
        map.set("idEtablissement", "9af51dc6-ead0-4edb-8978-da14a3e9f49a");
        Mockito.doReturn(map).when(request).params();
        user.setType("Personnel");
        structures.add("9af51dc6-ead0-4edb-8978-da14a3e9f49a");
        user.setStructures(structures);
        access.authorize(request, binding, user, result -> {
            ctx.assertEquals(true, result);
        });
    }

    @Test
    public void testAuthorizeTeacher(TestContext ctx) {
        role1.setDisplayName(WorkflowActionUtils.ADMIN_RIGHT);
        actions.add(role1);
        user.setAuthorizedActions(actions);
        map.set("idEtablissement", "9af51dc6-ead0-4edb-8978-da14a3e9f49a");
        Mockito.doReturn(map).when(request).params();
        user.setType("Teacher");
        structures.add("9af51dc6-ead0-4edb-8978-da14a3e9f49a");
        user.setStructures(structures);
        access.authorize(request, binding, user, result -> {
            ctx.assertEquals(true, result);
        });
    }

    @Test
    public void testAuthorizeWrongStructure(TestContext ctx) {
        map.set("idEtablissement", "9af51dc6-ead0-4edb-8978-da14a3e9f49a");
        Mockito.doReturn(map).when(request).params();
        structures.add("testWrong");
        user.setStructures(structures);
        access.authorize(request, binding, user, result -> {
            ctx.assertEquals(false, result);
        });
    }

    @Test
    public void testAuthorizeWrongType(TestContext ctx) {
        user.setAuthorizedActions(actions);
        map.set("idEtablissement", "9af51dc6-ead0-4edb-8978-da14a3e9f49a");
        Mockito.doReturn(map).when(request).params();
        user.setType("Student");
        structures.add("9af51dc6-ead0-4edb-8978-da14a3e9f49a");
        user.setStructures(structures);
        access.authorize(request, binding, user, result -> {
            ctx.assertEquals(false, result);
        });
    }
}