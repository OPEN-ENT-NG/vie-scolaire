package fr.openent.viescolaire.security;

import fr.openent.viescolaire.core.constants.Field;
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
public class AccessIfMyStructureParamServiceTest {
    AccessIfMyStructureParamService access;
    HttpServerRequest request;
    Binding binding;
    MultiMap map;
    List<String> structures;
    List<UserInfos.Action> actions;
    UserInfos.Action role1;
    MultiMap params;
    UserInfos user;

    @Before
    public void setUp() throws NoSuchFieldException {
        access = new AccessIfMyStructureParamService();
        request = Mockito.mock(HttpServerRequest.class);
        binding = Mockito.mock(Binding.class);
        params = Mockito.spy(new HeadersAdaptor(new DefaultHttpHeaders()));
        map = Mockito.spy(new HeadersAdaptor(new DefaultHttpHeaders()));
        structures = new ArrayList<>();
        user = new UserInfos();
        actions = new ArrayList<>();
        role1 = new UserInfos.Action();
    }

    @Test
    public void testAuthorizeStructureParamService(TestContext ctx){
        role1.setDisplayName(WorkflowActionUtils.PARAM_SERVICES_RIGHT);
        actions.add(role1);
        user.setAuthorizedActions(actions);
        params.set(Field.IDSTRUCTURE,"111111");
        Mockito.doReturn(params).when(request).params();
        structures.add("111111");
        user.setStructures(structures);
        access.authorize(request,binding,user,result -> {
            ctx.assertEquals(true, result);
        });
    }

    @Test
    public void testAuthorizeBadStrucureParamService(TestContext ctx){
        role1.setDisplayName(WorkflowActionUtils.PARAM_SERVICES_RIGHT);
        actions.add(role1);
        user.setAuthorizedActions(actions);
        params.set(Field.IDSTRUCTURE,"111111");
        Mockito.doReturn(params).when(request).params();
        structures.add("000000");
        user.setStructures(structures);
        access.authorize(request,binding,user,result -> {
            ctx.assertEquals(false, result);
        });
    }

    @Test
    public void testAuthorizeStructureNoParamService(TestContext ctx) {
        user.setAuthorizedActions(actions);
        params.set(Field.IDSTRUCTURE,"111111");
        Mockito.doReturn(params).when(request).params();
        structures.add("111111");
        user.setStructures(structures);
        access.authorize(request,binding,user,result -> {
            ctx.assertEquals(false, result);
        });
    }
}
