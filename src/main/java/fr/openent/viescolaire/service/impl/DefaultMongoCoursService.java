package fr.openent.viescolaire.service.impl;

import fr.openent.viescolaire.service.MongoCoursService;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.webutils.Either;

import fr.wseduc.webutils.Utils;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.entcore.common.mongodb.MongoDbResult.*;

public class DefaultMongoCoursService implements MongoCoursService {
    protected static final Logger LOG = LoggerFactory.getLogger(DefaultPeriodeService.class);
    private static final String COURSES = "courses";
    private final EventBus eb;
    private static final JsonObject KEYS = new JsonObject().put("_id", 1).put("structureId", 1).put("subjectId", 1)
            .put("roomLabels", 1).put("equipmentLabels", 1).put("teacherIds", 1).put("personnelIds", 1)
            .put("classes", 1).put("groups", 1).put("dayOfWeek", 1).put("startDate", 1).put("endDate", 1)
            .put("subjectId", 1).put("roomLabels", 1);
    private static final String START_DATE_PATTERN = "T00:00Z";
    private static final String END_DATE_PATTERN = "T23.59Z";
    private static final String START_END_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String[] colors = {"cyan", "green", "orange", "pink", "yellow", "purple", "grey"};
    public DefaultMongoCoursService(EventBus eb) {
        this.eb = eb;
    }

    @Override
    public void listCoursesBetweenTwoDates(String structureId, List<String> teacherId, List<String>  groups, String begin, String end, Handler<Either<String,JsonArray>> handler){
        if (Utils.validationParamsNull(handler, structureId, begin, end)) return;
        final JsonObject query = new JsonObject();

        query.put("structureId", structureId);

        if (teacherId != null && teacherId.size() != 0){
            query.put("teacherIds", teacherId);
        }

        final String startDate = begin + START_DATE_PATTERN;
        final String endDate = end + END_DATE_PATTERN;

        JsonObject betweenStart = new JsonObject();
        betweenStart.put("$lte", endDate);

        JsonObject betweenEnd = new JsonObject();
        betweenEnd.put("$gte", startDate);

        if (groups != null && groups.size() != 0) {
            JsonObject dateOperand =  new JsonObject()
                    .put("$and", new fr.wseduc.webutils.collections.JsonArray()
                            .add(new JsonObject().put("startDate" ,betweenStart))
                            .add(new JsonObject().put("endDate" ,betweenEnd)));
            JsonArray groupOperand = new fr.wseduc.webutils.collections.JsonArray();
            for(String group : groups){
                groupOperand.add(new JsonObject().put("classes", group))
                        .add(new JsonObject().put("groups", group));
            }

            JsonObject groupsOperand = new JsonObject()
                    .put("$or", groupOperand );
            query.put("$and", new fr.wseduc.webutils.collections.JsonArray().add(dateOperand).add(groupsOperand));
        } else {
            query.put("$and", new fr.wseduc.webutils.collections.JsonArray()
                    .add(new JsonObject().put("startDate", betweenStart))
                    .add(new JsonObject().put("endDate", betweenEnd)));
        }

        final JsonObject sort = new JsonObject().put("startDate", 1);

        MongoDb.getInstance().find(COURSES, query, sort, KEYS, validResultsHandler(handler));
    }
    @Override
    public void listCoursesBetweenTwoDatesFormatted(String structureId, List<String> teacherId, List<String>  group, String begin, String end, Handler<Either<String,JsonArray>> handler){
        listCoursesBetweenTwoDates(structureId, teacherId, group, begin, end, courseOccurrenceObject -> {
                    if (courseOccurrenceObject.isRight()) {
                        JsonArray courseOccurrence = courseOccurrenceObject.right().getValue();
                        handler.handle(new Either.Right<>(formatCourses(courseOccurrence, handler)));
                    } else {
                        LOG.error("can't get courses from mongo");
                        handler.handle(new Either.Left<>("can't get courses from mongo"));
                    }
                }
        );
    }

    private JsonArray formatCourses(JsonArray courseOccurrence,  Handler<Either<String,JsonArray>> handler) {
        JsonArray result = new JsonArray();
        for(int i=0; i < courseOccurrence.size() ; i++) {
            JsonObject occurence =  courseOccurrence.getJsonObject(i);
            if(goodFormatDate(occurence.getString("startDate")) && goodFormatDate(occurence.getString("endDate")) ){
                Calendar startMoment = getCalendarDate(occurence.getString("startDate"), handler);
                startMoment.set(Calendar.DAY_OF_WEEK,occurence.getInteger("dayOfWeek")+1);
                Calendar endMoment = getCalendarDate(occurence.getString("endDate"), handler);
                occurence.put("startDate", startMoment.getTime().toInstant().toString());
                double numberWeek = Math.floor( daysBetween(startMoment, endMoment) / (double) 7 );
                if (numberWeek > 0) {
                    String endDateCombine = occurence.getString("startDate")
                            .replaceAll("T.*$", 'T' + occurence.getString("endDate").replaceAll("^.*T", ""));
                    endMoment = getCalendarDate(endDateCombine, handler);
                    for (int j = 0; j < numberWeek + 1; j++) {
                        JsonObject c = new JsonObject(formatCourse(occurence, startMoment, endMoment).toString());
                        result.add(c);
                        startMoment.add(Calendar.DATE, 7);
                        endMoment.add(Calendar.DATE, 7);
                    }
                } else {
                    JsonObject c = new JsonObject(formatCourse(occurence, startMoment, endMoment).toString());
                    result.add(c);
                }
            }else {
                LOG.error("Error bad data format Date ");
                handler.handle(new Either.Left<>("Error bad data format Date "));
            }
        }
        return result;
    }
    private static JsonObject formatCourse(JsonObject occurence, Calendar start , Calendar end) {
        JsonObject course = new JsonObject(occurence.toString());
        course.put("color", colors[(int) Math.floor(Math.random() * colors.length)] );
        course.put("is_periodic",false);
        course.put("everyTwoWeek",false);
        course.put("startDate", start.getTime().toInstant().toString());
        course.put("endDate", end.getTime().toInstant().toString());
        return course;
    }
    private static long daysBetween(Calendar startDate, Calendar endDate) {
        long end = endDate.getTimeInMillis();
        long start = startDate.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toDays(Math.abs(end - start));
    }
    private boolean goodFormatDate(String date){
        return date.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*");
    }

    private Calendar getCalendarDate(String stringDate, Handler<Either<String,JsonArray>> handler){
        SimpleDateFormat formatter = new SimpleDateFormat(START_END_DATE_FORMAT);
        Date Date =new Date() ;
        try {
            if(stringDate.matches(".*Z$")){
                stringDate = stringDate.replaceAll("[.]\\d*Z", "");
            }
            Date = formatter.parse(stringDate);
        } catch (ParseException e) {
            LOG.error("Error formatting Date ");
            handler.handle(new Either.Left<>("Error formatting Date"));
        }
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(Date);
        return startCalendar;
    }
}
