package fr.openent.viescolaire.service;

import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public interface CommonCoursService {
    void listCoursesBetweenTwoDates(String structureId, List<String> teacherId, List<String>  group, String begin, String end, Handler<Either<String,JsonArray>> handler);

    void getCoursesOccurences(String structureId, List<String> teacherId, List<String>  group, String begin, String end, Handler<Either<String,JsonArray>> handler);

    void getCourse(String idCourse, Handler<Either<String,JsonObject>> handler );

}
