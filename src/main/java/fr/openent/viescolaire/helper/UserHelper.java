package fr.openent.viescolaire.helper;

import fr.openent.viescolaire.model.Person.Student;
import fr.openent.viescolaire.model.Person.User;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;

import java.util.ArrayList;
import java.util.List;

public class UserHelper {

    /**
     * Convert JsonArray into User list
     *
     * @param userArray JsonArray user
     * @return new list of user
     */
    public static List<User> toUserList(JsonArray userArray) {
        List<User> userList = new ArrayList<>();
        for (Object o : userArray) {
            if (!(o instanceof JsonObject)) continue;
            User user = new User((JsonObject) o);
            userList.add(user);
        }
        return userList;
    }

    /**
     * Convert JsonArray into student list
     *
     * @param array JsonArray response
     * @return new list of events
     */
    public static List<Student> toStudentList(JsonArray array) {
        List<Student> studentList = new ArrayList<>();
        for (Object o : array) {
            if (!(o instanceof JsonObject)) continue;
            Student student = new Student((JsonObject) o);
            studentList.add(student);
        }
        return studentList;
    }

    /**
     * Convert List User into User JsonArray
     *
     * @param userList User list
     * @return new JsonArray of user
     */
    public static JsonArray toJsonArray(List<User> userList) {
        JsonArray userArray = new JsonArray();
        for (User user : userList) {
            userArray.add(user.toJSON());
        }
        return userArray;
    }

    public static Future<UserInfos> getUserInfos(EventBus eb, HttpServerRequest request) {
        Promise<UserInfos> promise = Promise.promise();
        UserUtils.getUserInfos(eb, request, promise::complete);
        return promise.future();
    }
}
