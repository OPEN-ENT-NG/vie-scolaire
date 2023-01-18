package fr.openent.viescolaire.security;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.model.Person.User;
import fr.wseduc.webutils.http.Binding;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.impl.HeadersAdaptor;
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
public class AccesCompetencesVieScoStructureFilterTest {
    AccesCompetencesVieScoStructureFilter access;
    HttpServerRequest request;
    Binding binding;
    UserInfos user;
    List<UserInfos.Action> actions;
    UserInfos.Action role1;
    UserInfos.Action role2;
    MultiMap params;
    List structures;
    List<String> groupsId;

    @Before
    public void setUp(){
        access = new AccesCompetencesVieScoStructureFilter();
        request = Mockito.mock(HttpServerRequest.class);
        binding = Mockito.mock(Binding.class);
        user = new UserInfos();
        actions = new ArrayList<>();
        role1 = new UserInfos.Action();
        role2 = new UserInfos.Action();
        params = Mockito.spy(new HeadersAdaptor(new DefaultHttpHeaders()));
        structures = new ArrayList<String>();
        groupsId = new ArrayList<>();
    }

    @Test
    public void testAccessCompetanceAndViescoRightStructDelete(TestContext ctx){

        Mockito.doReturn(HttpMethod.DELETE).when(request).method();
        Mockito.doReturn(params).when(request).params();
        role1.setDisplayName(WorkflowActionUtils.ADMIN_RIGHT);
        role2.setDisplayName(WorkflowActionUtils.COMPETENCE_ACCESS);
        actions.add(role1);
        actions.add(role2);
        user.setAuthorizedActions(actions);
        params.set(Viescolaire.ID_ETABLISSEMENT_KEY,"1");
        structures.add("1");
        user.setStructures(structures);
        user.setGroupsIds(groupsId);

        access.authorize(request,binding,user,result -> {
            ctx.assertEquals(true,result);
        });
    }

    @Test
    public void testAccessCompetanceAndViescoWrongStructDelete(TestContext ctx){
        Mockito.doReturn(HttpMethod.DELETE).when(request).method();
        Mockito.doReturn(params).when(request).params();
        role1.setDisplayName(WorkflowActionUtils.ADMIN_RIGHT);
        role2.setDisplayName(WorkflowActionUtils.COMPETENCE_ACCESS);
        actions.add(role1);
        actions.add(role2);
        user.setAuthorizedActions(actions);
        params.set(Viescolaire.ID_ETABLISSEMENT_KEY,"1");
        structures.add("0");
        user.setStructures(structures);
        user.setGroupsIds(groupsId);

        access.authorize(request,binding,user,result -> {
            ctx.assertEquals(false,result);
        });
    }

    @Test
    public void testNotAccessCompetanceAndViescoRightStruct(TestContext ctx) {
        Mockito.doReturn(HttpMethod.DELETE).when(request).method();
        Mockito.doReturn(params).when(request).params();
        role2.setDisplayName(WorkflowActionUtils.COMPETENCE_ACCESS);
        actions.add(role2);
        user.setAuthorizedActions(actions);
        params.set(Viescolaire.ID_ETABLISSEMENT_KEY,"1");
        structures.add("1");
        user.setStructures(structures);
        user.setGroupsIds(groupsId);

        access.authorize(request,binding,user,result -> {
            ctx.assertEquals(false,result);
        });
    }

    @Test
    public void testNotAccessCompetanceAndViescoWrongStruct(TestContext ctx) {
        Mockito.doReturn(HttpMethod.DELETE).when(request).method();
        Mockito.doReturn(params).when(request).params();
        role2.setDisplayName(WorkflowActionUtils.ADMIN_RIGHT);
        actions.add(role2);
        user.setAuthorizedActions(actions);
        params.set(Viescolaire.ID_ETABLISSEMENT_KEY,"1");
        structures.add("0");
        user.setStructures(structures);
        user.setGroupsIds(groupsId);

        access.authorize(request,binding,user,result -> {
            ctx.assertEquals(false,result);
        });
    }

    //TODO Test for filter with post http method
    @Test
    public void testAccessCompetanceAndViescoRightStructPost(TestContext ctx){
//        Mockito.doReturn(params).when(request).bodyHandler(Mockito.any(Handler.class));
    }
}
