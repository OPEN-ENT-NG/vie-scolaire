package fr.openent.viescolaire.security;

import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.service.impl.DefaultCommonCoursService;
import fr.wseduc.webutils.http.Binding;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.impl.headers.HeadersAdaptor;
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
@PrepareForTest({AccessStructureMyCourse.class})
public class AccessStructureMyCourseTest {
    AccessStructureMyCourse access;
    HttpServerRequest request;
    Binding binding;
    MultiMap params;
    UserInfos user;
    List<String> structures;
    DefaultCommonCoursService service;

    @Before
    public void setUp() throws Exception {
        params = Mockito.spy(new HeadersAdaptor(new DefaultHttpHeaders()));
        binding = Mockito.mock(Binding.class);
        request = Mockito.mock(HttpServerRequest.class);
        user = new UserInfos();
        structures = new ArrayList<>();
        service = Mockito.spy(new DefaultCommonCoursService());
        PowerMockito.spy(DefaultCommonCoursService.class);
        PowerMockito.whenNew(DefaultCommonCoursService.class).withNoArguments().thenReturn(service);
        access = new AccessStructureMyCourse();
    }

    @Test
    public void testAuthorize_should_retrieve_correct_param(TestContext ctx) {
        String idCourse = "11111";
        JsonObject course = new JsonObject();
        course.put(Field.STRUCTUREID, "aaaaa");
        params.set(Field.IDCOURSE, idCourse);
        Mockito.doReturn(params).when(request).params();
        Mockito.doReturn(Future.succeededFuture(course)).when(service).getCourse(Mockito.any());
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
    public void testBadStructure(TestContext ctx) {
        String idCourse = "11111";
        JsonObject course = new JsonObject();
        course.put(Field.STRUCTUREID, "aaaaa");
        params.set(Field.IDCOURSE, idCourse);
        Mockito.doReturn(params).when(request).params();
        Mockito.doReturn(Future.succeededFuture(course)).when(service).getCourse(Mockito.any());
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