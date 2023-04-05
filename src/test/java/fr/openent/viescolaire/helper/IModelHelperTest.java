package fr.openent.viescolaire.helper;

import fr.openent.viescolaire.model.*;
import fr.wseduc.webutils.*;
import io.vertx.core.*;
import io.vertx.core.json.*;
import io.vertx.core.logging.*;
import io.vertx.ext.unit.*;
import io.vertx.ext.unit.junit.*;
import org.junit.*;
import org.junit.runner.*;
import org.reflections.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.*;

import static org.reflections.scanners.Scanners.SubTypes;

@RunWith(VertxUnitRunner.class)
public class IModelHelperTest {
    enum MyEnum {
        VALUE1,
        VALUE2,
        VALUE3
    }

    static class MyClass {
        public String id;
    }
    class MyIModel implements IModel<MyIModel> {
        public String id;
        public boolean isGood;
        public MyOtherIModel otherIModel;
        public MyClass myClass;
        public List<Integer> typeIdList;
        public List<MyOtherIModel> otherIModelList;
        public List<MyClass> myClassList;
        public List<List<JsonObject>> listList;
        public MyEnum myEnum;
        public List<MyEnum> myEnumList;
        public MyEnum nullValue = null;

        @Override
        public JsonObject toJson() {
            return IModelHelper.toJson(this, false, true);
        }

        @Override
        public boolean validate() {
            return false;
        }
    }

    static class MyOtherIModel implements IModel<MyOtherIModel> {
        public String myName;

        public MyOtherIModel() {
        }

        public MyOtherIModel(JsonObject jsonObject) {
            this.myName = jsonObject.getString("myName");
        }

        @Override
        public JsonObject toJson() {
            return IModelHelper.toJson(this, false, true);
        }

        @Override
        public boolean validate() {
            return false;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(IModelHelperTest.class);

    @Test
    public void testSubClassIModel(TestContext ctx) {
        Reflections reflections = new Reflections("fr.openent.viescolaire");
        List<Class<?>> ignoredClassList = Arrays.asList(MyIModel.class, MyOtherIModel.class);

        Set<Class<?>> subTypes =
                reflections.get(SubTypes.of(IModel.class).asClass());
        List<Class<?>> invalidModel = subTypes.stream()
                .filter(modelClass -> !ignoredClassList.contains(modelClass))
                .filter(modelClass -> {
                    Constructor<?> emptyConstructor = Arrays.stream(modelClass.getConstructors())
                            .filter(constructor -> constructor.getParameterTypes().length == 1
                                    && constructor.getParameterTypes()[0].equals(JsonObject.class))
                            .findFirst()
                            .orElse(null);
                    return emptyConstructor == null;
                }).collect(Collectors.toList());

        invalidModel.forEach(modelClass -> {
            String message = String.format("[Viescolaire@%s::testSubClassIModel]: The class %s must have public constructor with JsonObject parameter declared",
                    this.getClass().getSimpleName(), modelClass.getSimpleName());
            log.fatal(message);
        });

        ctx.assertTrue(invalidModel.isEmpty(), "One or more IModel don't have public constructor with JsonObject parameter declared. Check log above.");
    }

    @Test
    public void toJson(TestContext ctx) {
        MyOtherIModel otherIModel1 = new MyOtherIModel();
        otherIModel1.myName = "otherIModel1";
        MyOtherIModel otherIModel2 = new MyOtherIModel();
        otherIModel2.myName = "otherIModel2";
        MyOtherIModel otherIModel3 = new MyOtherIModel();
        otherIModel3.myName = "otherIModel3";

        MyClass myClass1 = new MyClass();
        myClass1.id = "myClass1";
        MyClass myClass2 = new MyClass();
        myClass2.id = "myClass2";
        MyClass myClass3 = new MyClass();
        myClass3.id = "myClass3";

        MyIModel iModel = new MyIModel();
        iModel.id = "id";
        iModel.isGood = true;
        iModel.typeIdList = Arrays.asList(1,2,3);
        iModel.otherIModel = otherIModel1;
        iModel.myClass = myClass1;
        iModel.otherIModelList = Arrays.asList(otherIModel2, otherIModel3);
        iModel.myClassList = Arrays.asList(myClass2, myClass3);

        iModel.listList = Arrays.asList(Arrays.asList(new JsonObject().put("uuid", "uuid1"), new JsonObject().put("uuid", "uuid2")),
                Arrays.asList(new JsonObject().put("uuid", "uuid3"), new JsonObject().put("uuid", "uuid4")),
                Arrays.asList(new JsonObject().put("uuid", "uuid5"), new JsonObject().put("uuid", "uuid6")));

        iModel.myEnum = MyEnum.VALUE1;
        iModel.myEnumList = Arrays.asList(MyEnum.VALUE2, MyEnum.VALUE3);

        String expected = "{\"id\":\"id\",\"is_good\":true,\"other_i_model\":{\"my_name\":\"otherIModel1\"}," +
                "\"type_id_list\":[1,2,3],\"other_i_model_list\":[{\"my_name\":\"otherIModel2\"},{\"my_name\":\"otherIModel3\"}]," +
                "\"my_class_list\":[],\"list_list\":[[{\"uuid\":\"uuid1\"},{\"uuid\":\"uuid2\"}],[{\"uuid\":\"uuid3\"}," +
                "{\"uuid\":\"uuid4\"}],[{\"uuid\":\"uuid5\"},{\"uuid\":\"uuid6\"}]],\"my_enum\":\"VALUE1\"," +
                "\"my_enum_list\":[\"VALUE2\",\"VALUE3\"],\"null_value\":null}";
        ctx.assertEquals(expected, iModel.toJson().toString());

        System.out.println();
    }

    @Test
    public void sqlUniqueResultToIModelTest(TestContext ctx) {
        Async async = ctx.async();
        Promise<MyOtherIModel> promise = Promise.promise();

        promise.future().onSuccess(myOtherIModel -> {
            ctx.assertEquals(myOtherIModel.myName, "test");
            async.complete();
        });

        final Handler<Either<String, JsonObject>> handler = IModelHelper.sqlUniqueResultToIModel(promise, MyOtherIModel.class);
        handler.handle(new Either.Right<>(new JsonObject("{\"myName\":\"test\"}")));

        async.awaitSuccess(1000);
    }

    @Test
    public void sqlResultToIModelTest(TestContext ctx) {
        Async async = ctx.async();
        Promise<List<MyOtherIModel>> promise = Promise.promise();

        promise.future().onSuccess(myOtherIModelList -> {
            ctx.assertEquals(myOtherIModelList.size(), 2);
            ctx.assertEquals(myOtherIModelList.get(0).myName, "test");
            ctx.assertEquals(myOtherIModelList.get(1).myName, "test2");
            async.complete();
        });

        final Handler<Either<String, JsonArray>> handler = IModelHelper.sqlResultToIModel(promise, MyOtherIModel.class);
        handler.handle(new Either.Right<>(new JsonArray("[{\"myName\":\"test\"}, {\"myName\":\"test2\"}]")));

        async.awaitSuccess(1000);
    }
}