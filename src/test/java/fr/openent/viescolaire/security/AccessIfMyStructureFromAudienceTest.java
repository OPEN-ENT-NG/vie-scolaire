package fr.openent.viescolaire.security;

import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.service.ClasseService;
import fr.openent.viescolaire.service.impl.DefaultClasseService;
import fr.wseduc.webutils.http.Binding;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.impl.HeadersAdaptor;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.entcore.common.user.UserInfos;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.util.ArrayList;
import java.util.List;


@RunWith(PowerMockRunner.class) //Using the PowerMock runner
@PowerMockRunnerDelegate(VertxUnitRunner.class) //And the Vertx runner
@PrepareForTest({AccessIfMyStructureFromAudience.class}) //Prepare the static class you want to test
public class AccessIfMyStructureFromAudienceTest {
    AccessIfMyStructureFromAudience access;
    HttpServerRequest request;
    Binding binding;
    MultiMap params;
    UserInfos user;
    List<String> structures;
    DefaultClasseService service;


    @Before
    public void setUp() throws Exception {
        params = Mockito.spy(new HeadersAdaptor(new DefaultHttpHeaders()));
        binding = Mockito.mock(Binding.class);
        request = Mockito.mock(HttpServerRequest.class);
        user = new UserInfos();
        structures = new ArrayList<>();
        service = Mockito.spy(new DefaultClasseService());
        PowerMockito.spy(DefaultClasseService.class);
        PowerMockito.whenNew(DefaultClasseService.class).withNoArguments().thenReturn(service);
        access = new AccessIfMyStructureFromAudience();
    }

    @Test
    public void testAuthorize(TestContext ctx){
        String audienceId = "11111";
        String classId = "aaaaa";
        JsonObject EtabInfStructureId = new JsonObject();
        EtabInfStructureId.put(Field.IDSTRUCTURE, "aaaaa");
        JsonArray etabInfos = new JsonArray();
        etabInfos.add(EtabInfStructureId);
        params.set(Field.AUDIENCEID, audienceId);
        Mockito.doReturn(params).when(request).params();
        Mockito.doReturn(Future.succeededFuture(classId)).when(service).getClasseIdFromAudience(Mockito.any());
        Mockito.doReturn(Future.succeededFuture(etabInfos)).when(service).getEtabClasses(Mockito.any());
        structures.add("aaaaa");
        user.setStructures(structures);
        Async async = ctx.async();
        access.authorize(request, binding, user, result -> {
            ctx.assertEquals(true, result);
            async.complete();
        });
        async.awaitSuccess(10000);
    }


    @Test
    public void testBadStructure(TestContext ctx){
        String audienceId = "11111";
        String classId = "aaaaa";
        JsonObject EtabInfStructureId = new JsonObject();
        EtabInfStructureId.put(Field.IDSTRUCTURE, "aaaaa");
        JsonArray etabInfos = new JsonArray();
        etabInfos.add(EtabInfStructureId);
        params.set(Field.AUDIENCEID, audienceId);
        Mockito.doReturn(params).when(request).params();
        Mockito.doReturn(Future.succeededFuture(classId)).when(service).getClasseIdFromAudience(Mockito.any());
        Mockito.doReturn(Future.succeededFuture(etabInfos)).when(service).getEtabClasses(Mockito.any());
        structures.add("azerty123");
        user.setStructures(structures);
        Async async = ctx.async();
        access.authorize(request, binding, user, result -> {
            ctx.assertEquals(false, result);
            async.complete();
        });
        async.awaitSuccess(10000);
    }


}
