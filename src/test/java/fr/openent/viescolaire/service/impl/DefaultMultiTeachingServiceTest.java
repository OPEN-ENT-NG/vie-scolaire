package fr.openent.viescolaire.service.impl;

import fr.openent.viescolaire.db.DB;
import fr.openent.viescolaire.model.MultiTeaching;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.eventbus.ResultMessage;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.impl.EventBusImpl;
import io.vertx.core.impl.VertxImpl;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.core.Handler;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.entcore.common.sql.Sql;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.FieldSetter;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.powermock.reflect.Whitebox;

import java.util.Arrays;

import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class) //Using the PowerMock runner
@PowerMockRunnerDelegate(VertxUnitRunner.class) //And the Vertx runner
@PrepareForTest({DefaultMultiTeachingService.class, EventBus.class}) //Prepare the class we want to test
public class DefaultMultiTeachingServiceTest {

    private Vertx vertx;
    private final Sql sql = mock(Sql.class);
    private DefaultMultiTeachingService multiTeachingService;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
        DB.getInstance().init(null, sql, null);

        this.multiTeachingService = PowerMockito.spy(new DefaultMultiTeachingService(vertx.eventBus()));
    }

    @Test
    public void testCreateMultiTeaching(TestContext ctx) throws Exception {
        Async async = ctx.async();
        String structureId = "structureId";
        String mainTeacherId = "mainTeacherId";
        JsonArray secondTeacherIds = new JsonArray(Arrays.asList("secondTeacherId1", "secondTeacherId2"));
        String subjectId = "subjectId";
        JsonArray classOrGroupIds = new JsonArray(Arrays.asList("classId1", "groupId1", "classId2"));
        String startDate = "startDate";
        String endDate = "endDate";
        String enteredEndDate = "enteredEndDate";
        Boolean coTeaching = true;
        Handler<Either<String, JsonArray>> handler = event -> {
        };

        String expectedQuery = "WITH insert AS ( " +
                "INSERT INTO viesco.multi_teaching (structure_id, main_teacher_id,  second_teacher_id, subject_id, class_or_group_id," +
                " is_coteaching, start_date, end_date, entered_end_date) " +
                "VALUES ( ?, ?, ?, ?, ?, ?, to_timestamp( ?, 'YYYY-MM-DD'), to_timestamp( ?, 'YYYY-MM-DD'), to_timestamp( ?, 'YYYY-MM-DD') )," +
                "( ?, ?, ?, ?, ?, ?, to_timestamp( ?, 'YYYY-MM-DD'), to_timestamp( ?, 'YYYY-MM-DD'), to_timestamp( ?, 'YYYY-MM-DD') )," +
                "( ?, ?, ?, ?, ?, ?, to_timestamp( ?, 'YYYY-MM-DD'), to_timestamp( ?, 'YYYY-MM-DD'), to_timestamp( ?, 'YYYY-MM-DD') )," +
                "( ?, ?, ?, ?, ?, ?, to_timestamp( ?, 'YYYY-MM-DD'), to_timestamp( ?, 'YYYY-MM-DD'), to_timestamp( ?, 'YYYY-MM-DD') )," +
                "( ?, ?, ?, ?, ?, ?, to_timestamp( ?, 'YYYY-MM-DD'), to_timestamp( ?, 'YYYY-MM-DD'), to_timestamp( ?, 'YYYY-MM-DD') )," +
                "( ?, ?, ?, ?, ?, ?, to_timestamp( ?, 'YYYY-MM-DD'), to_timestamp( ?, 'YYYY-MM-DD'), to_timestamp( ?, 'YYYY-MM-DD') )" +
                "RETURNING * ) " +
                "SELECT second_teacher_id FROM insert " +
                "UNION " +
                "SELECT DISTINCT second_teacher_id FROM viesco.multi_teaching " +
                "WHERE structure_id = ? AND main_teacher_id = ? AND subject_id = ? AND class_or_group_id IN (?,?,?) AND deleted_date IS NULL ";
        JsonArray expectedValues = new JsonArray(Arrays.asList(
                "structureId", "mainTeacherId", "secondTeacherId1", "subjectId", "classId1", true, "startDate", "endDate", "enteredEndDate",
                "structureId", "mainTeacherId", "secondTeacherId1", "subjectId", "groupId1", true, "startDate", "endDate", "enteredEndDate",
                "structureId", "mainTeacherId", "secondTeacherId1", "subjectId", "classId2", true, "startDate", "endDate", "enteredEndDate",
                "structureId", "mainTeacherId", "secondTeacherId2", "subjectId", "classId1", true, "startDate", "endDate", "enteredEndDate",
                "structureId", "mainTeacherId", "secondTeacherId2", "subjectId", "groupId1", true, "startDate", "endDate", "enteredEndDate",
                "structureId", "mainTeacherId", "secondTeacherId2", "subjectId", "classId2", true, "startDate", "endDate", "enteredEndDate",
                "structureId", "mainTeacherId", "subjectId", "classId1", "groupId1", "classId2"));

        PowerMockito.doAnswer(invocation -> {
            MultiTeaching multiTeaching = invocation.getArgument(0);
            ctx.assertEquals(structureId, multiTeaching.getStructureId());
            ctx.assertEquals(startDate, multiTeaching.getStartDate());
            ctx.assertEquals(endDate, multiTeaching.getEndDate());
            ctx.assertEquals(subjectId, multiTeaching.getSubjectId());
            ctx.assertEquals(mainTeacherId, multiTeaching.getMainTeacherId());
            ctx.assertEquals(secondTeacherIds, invocation.getArgument(1));
            ctx.assertEquals(classOrGroupIds, invocation.getArgument(2));
            ctx.assertEquals(handler, invocation.getArgument(3));
            ctx.assertTrue(invocation.getArgument(4));
            ctx.assertEquals(expectedQuery, invocation.getArgument(5));
            JsonArray values = invocation.getArgument(6);
            ctx.assertEquals(expectedValues, values);
            async.complete();
            return null;
        }).when(this.multiTeachingService, "handleCreationSqlResponse", Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.any(), Mockito.any());

        this.multiTeachingService.createMultiTeaching(structureId, mainTeacherId, secondTeacherIds, subjectId, classOrGroupIds, startDate, endDate,
                enteredEndDate, coTeaching, handler, true);
    }

    @Test
    public void testHandleCreationSqlResponse(TestContext ctx) throws Exception {
        Async async = ctx.async(3);
        JsonArray sqlResult = new JsonArray(Arrays.asList(1, 2, 3));
        JsonArray prepareResult = new JsonArray(Arrays.asList(4, 5, 6));
        PowerMockito.doReturn(Future.succeededFuture(prepareResult)).when(this.multiTeachingService, "prepareSendIdsToShare", Mockito.any(), Mockito.any(), Mockito.any());
        PowerMockito.doReturn(Future.succeededFuture(prepareResult)).when(this.multiTeachingService, "addCourseSubstitutes", Mockito.any(), Mockito.any(), Mockito.any());

        Mockito.doAnswer(invocation -> {
            Handler<Message<JsonObject>> handler = invocation.getArgument(2);
            handler.handle(new ResultMessage(new JsonObject().put("results", sqlResult)));
            return null;
        }).when(sql).prepared(Mockito.any(), Mockito.any(), Mockito.any(Handler.class));

        MultiTeaching multiTeaching = new MultiTeaching();
        multiTeaching.setStartDate("");
        multiTeaching.setEndDate("");
        JsonArray secondTeacherIds = new JsonArray();
        JsonArray classOrGroupIds = new JsonArray();
        Handler<Either<String, JsonArray>> handler = event -> {
            try {
                PowerMockito.verifyPrivate(this.multiTeachingService, Mockito.times(1)).invoke("prepareSendIdsToShare", Mockito.any(), Mockito.any(), Mockito.any());
                PowerMockito.verifyPrivate(this.multiTeachingService, Mockito.times(1)).invoke("addCourseSubstitutes", Mockito.any(), Mockito.any(), Mockito.any());
            } catch (Exception e) {
                ctx.fail(e);
            }
            ctx.assertEquals(event.right().getValue(), prepareResult);
            async.countDown();
        };
        String query = "";
        JsonArray values = new JsonArray();

        Whitebox.invokeMethod(this.multiTeachingService, "handleCreationSqlResponse", multiTeaching, secondTeacherIds, classOrGroupIds,
                handler, true, query, values);

        handler = event -> {
            try {
                PowerMockito.verifyPrivate(this.multiTeachingService, Mockito.times(1)).invoke("prepareSendIdsToShare", Mockito.any(), Mockito.any(), Mockito.any());
                PowerMockito.verifyPrivate(this.multiTeachingService, Mockito.times(2)).invoke("addCourseSubstitutes", Mockito.any(), Mockito.any(), Mockito.any());
            } catch (Exception e) {
                ctx.fail(e);
            }
            ctx.assertEquals(event.right().getValue(), sqlResult);
            async.countDown();
        };

        Whitebox.invokeMethod(this.multiTeachingService, "handleCreationSqlResponse", multiTeaching, secondTeacherIds, classOrGroupIds,
                handler, false, query, values);


        multiTeaching.setStartDate(null);
        multiTeaching.setEndDate(null);
        handler = event -> {
            try {
                PowerMockito.verifyPrivate(this.multiTeachingService, Mockito.times(1)).invoke("prepareSendIdsToShare", Mockito.any(), Mockito.any(), Mockito.any());
                PowerMockito.verifyPrivate(this.multiTeachingService, Mockito.times(2)).invoke("addCourseSubstitutes", Mockito.any(), Mockito.any(), Mockito.any());
            } catch (Exception e) {
                ctx.fail(e);
            }
            ctx.assertEquals(event.right().getValue(), sqlResult);
            async.countDown();
        };

        Whitebox.invokeMethod(this.multiTeachingService, "handleCreationSqlResponse", multiTeaching, secondTeacherIds, classOrGroupIds,
                handler, false, query, values);
    }

    @Test
    public void testPrepareSendIdsToShare(TestContext ctx) throws Exception {
        Async async = ctx.async();
        String expected = "[[\"otherTeacherId1\",\"mainTeacherId\",\"subjectId\",\"classId1\",\"structureId\"]," +
                "[\"otherTeacherId1\",\"mainTeacherId\",\"subjectId\",\"groupId1\",\"structureId\"]," +
                "[\"otherTeacherId1\",\"mainTeacherId\",\"subjectId\",\"classId2\",\"structureId\"]," +
                "[\"otherTeacherId2\",\"mainTeacherId\",\"subjectId\",\"classId1\",\"structureId\"]," +
                "[\"otherTeacherId2\",\"mainTeacherId\",\"subjectId\",\"groupId1\",\"structureId\"]," +
                "[\"otherTeacherId2\",\"mainTeacherId\",\"subjectId\",\"classId2\",\"structureId\"]," +
                "[\"otherTeacherId2\",\"otherTeacherId1\",\"subjectId\",\"classId1\",\"structureId\"]," +
                "[\"otherTeacherId2\",\"otherTeacherId1\",\"subjectId\",\"groupId1\",\"structureId\"]," +
                "[\"otherTeacherId2\",\"otherTeacherId1\",\"subjectId\",\"classId2\",\"structureId\"]]";
        PowerMockito.doAnswer(invocation -> {
            JsonArray res = invocation.getArgument(0);
            ctx.assertEquals(res.toString(), expected);
            async.complete();
            return null;
        }).when(this.multiTeachingService, "sendIdsToShare", Mockito.any(), Mockito.any());

        MultiTeaching multiTeaching = new MultiTeaching();
        multiTeaching.setMainTeacherId("mainTeacherId");
        multiTeaching.setSubjectId("subjectId");
        multiTeaching.setStructureId("structureId");
        JsonArray classOrGroupIds = new JsonArray(Arrays.asList("classId1", "groupId1", "classId2"));
        JsonArray otherTeacher = new JsonArray(Arrays.asList(new JsonArray(Arrays.asList("otherTeacherId1")), new JsonArray(Arrays.asList("otherTeacherId2"))));
        Whitebox.invokeMethod(this.multiTeachingService, "prepareSendIdsToShare", multiTeaching, classOrGroupIds, otherTeacher);
    }
}
