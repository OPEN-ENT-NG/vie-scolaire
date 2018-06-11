package fr.openent.viescolaire.service;

import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;

import java.util.List;

public interface MongoCoursService {
    void listCoursesBetweenTwoDates(String structureId, List<String> teacherId, List<String>  group, String begin, String end, Handler<Either<String,JsonArray>> handler);

    void listCoursesBetweenTwoDatesFormatted(String structureId, List<String> teacherId, List<String>  group, String begin, String end, Handler<Either<String,JsonArray>> handler);
}
