package fr.openent.viescolaire.helper;

import fr.openent.viescolaire.model.Person.Student;
import fr.openent.viescolaire.model.Person.User;
import fr.wseduc.webutils.http.Renders;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserHelper {
    private static final Logger log = LoggerFactory.getLogger(UserHelper.class);

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

    public static Future<List<UserInfos>> getUserInfosFromIds(EventBus eb, List<String> idList) {
        Promise<List<UserInfos>> promise = Promise.promise();

        List<Promise<UserInfos>> promiseList = new ArrayList<>();
        idList.forEach(userId -> {
            Promise<UserInfos> promiseUserInfo = Promise.promise();
            UserUtils.getUserInfos(eb, userId, promiseUserInfo::complete);
            promiseList.add(promiseUserInfo);
        });

        CompositeFuture.all(promiseList.stream().map(Promise::future).collect(Collectors.toList()))
                .onSuccess(result -> {
                    List<UserInfos> userInfosList = promiseList.stream()
                            .map(Promise::future)
                            .map(Future::result)
                            .collect(Collectors.toList());
                    promise.complete(userInfosList);
                })
                .onFailure(error -> {
                    String message = String.format("[Viescolaire@%s::getUserInfosFromIds] %s.", UserHelper.class.getSimpleName(), error.getMessage());
                    log.error(message);
                    promise.fail(error.getMessage());
                });

        return promise.future();
    }

}
