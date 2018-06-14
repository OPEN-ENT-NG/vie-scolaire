package fr.openent.viescolaire.service.impl;

import fr.openent.viescolaire.service.CommonCoursService;
import io.vertx.core.eventbus.EventBus;
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

public class DefaultCommonCoursService implements CommonCoursService {
    protected static final Logger LOG = LoggerFactory.getLogger(DefaultPeriodeService.class);
    private static final Course COURSE_TABLE = new Course();
    private static final String COURSES = "courses";
    private final EventBus eb;
    private static final JsonObject KEYS = new JsonObject().put(COURSE_TABLE._id, 1).put(COURSE_TABLE.structureId, 1).put(COURSE_TABLE.subjectId, 1)
            .put(COURSE_TABLE.roomLabels, 1).put(COURSE_TABLE.equipmentLabels, 1).put(COURSE_TABLE.teacherIds, 1).put(COURSE_TABLE.personnelIds, 1)
            .put(COURSE_TABLE.classes, 1).put(COURSE_TABLE.groups, 1).put(COURSE_TABLE.dayOfWeek, 1).put(COURSE_TABLE.startDate, 1).put(COURSE_TABLE.endDate, 1).put(COURSE_TABLE.everyTwoWeek,1);
    private static final String START_DATE_PATTERN = "T00:00Z";
    private static final String END_DATE_PATTERN = "T23.59Z";
    private static final String START_END_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String[] COLORS = {"cyan", "green", "orange", "pink", "yellow", "purple", "grey","orange","purple", "green", "yellow"};
    public DefaultCommonCoursService(EventBus eb) {
        this.eb = eb;
    }

    @Override
    public void listCoursesBetweenTwoDates(String structureId, List<String> teacherId, List<String>  groups, String begin, String end, Handler<Either<String,JsonArray>> handler){
        if (Utils.validationParamsNull(handler, structureId, begin, end)) return;
        final JsonObject query = new JsonObject();

        query.put("structureId", structureId);

        if (teacherId != null && !teacherId.isEmpty() ){
            query.put("teacherIds", teacherId);
        }

        final String startDate = begin + START_DATE_PATTERN;
        final String endDate = end + END_DATE_PATTERN;

        JsonObject betweenStart = new JsonObject();
        betweenStart.put("$lte", endDate);

        JsonObject betweenEnd = new JsonObject();
        betweenEnd.put("$gte", startDate);

        if (groups != null && !groups.isEmpty()) {
            JsonObject dateOperand =  new JsonObject()
                    .put("$and", new fr.wseduc.webutils.collections.JsonArray()
                            .add(new JsonObject().put(COURSE_TABLE.startDate ,betweenStart))
                            .add(new JsonObject().put(COURSE_TABLE.endDate ,betweenEnd)));
            JsonArray groupOperand = new fr.wseduc.webutils.collections.JsonArray();
            for(String group : groups){
                groupOperand.add(new JsonObject().put(COURSE_TABLE.classes, group))
                        .add(new JsonObject().put(COURSE_TABLE.groups, group));
            }

            JsonObject groupsOperand = new JsonObject()
                    .put("$or", groupOperand );
            query.put("$and", new fr.wseduc.webutils.collections.JsonArray().add(dateOperand).add(groupsOperand));
        } else {
            query.put("$and", new fr.wseduc.webutils.collections.JsonArray()
                    .add(new JsonObject().put(COURSE_TABLE.startDate, betweenStart))
                    .add(new JsonObject().put("endDate", betweenEnd)));
        }

        final JsonObject sort = new JsonObject().put(COURSE_TABLE.startDate, 1);

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
            JsonObject course =  courseOccurrence.getJsonObject(i);
            if(goodFormatDate(course.getString(COURSE_TABLE.startDate)) && goodFormatDate(course.getString(COURSE_TABLE.endDate)) ){
                Calendar startMoment = getCalendarDate(course.getString(COURSE_TABLE.startDate), handler);
                startMoment.set(Calendar.DAY_OF_WEEK,course.getInteger(COURSE_TABLE.dayOfWeek)+1);
                Calendar endMoment = getCalendarDate(course.getString(COURSE_TABLE.endDate), handler);
                course.put(COURSE_TABLE.startDate, startMoment.getTime().toInstant().toString());
                double numberWeek = Math.floor( daysBetween(startMoment, endMoment) / (double) 7 );
                if (numberWeek > 0) {
                    String endDateCombine = course.getString(COURSE_TABLE.startDate)
                            .replaceAll("T.*$", 'T' + course.getString(COURSE_TABLE.endDate).replaceAll("^.*T", ""));
                    endMoment = getCalendarDate(endDateCombine, handler);
                    for (int j = 0; j < numberWeek + 1; j++) {
                        JsonObject c = new JsonObject(formatCourse(course, startMoment, endMoment).toString());
                        result.add(c);
                        startMoment.add(Calendar.DATE, 7);
                        endMoment.add(Calendar.DATE, 7);
                    }
                } else {
                    JsonObject c = new JsonObject(formatCourse(course, startMoment, endMoment).toString());
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
        course.put("color",  getColor(occurence.getJsonArray("classes")));
        course.put("is_periodic",false);
        course.put("everyTwoWeek",false);
        course.put(COURSE_TABLE.startDate, start.getTime().toInstant().toString());
        course.put(COURSE_TABLE.endDate, end.getTime().toInstant().toString());
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

    private static String getColor(JsonArray classes) {
        byte[] bytes = classes.getString(0).getBytes();
        int number = 0;
        for (int i = 0; i < bytes.length ; i++){
            number += (int) bytes[i];
        }
        number = (int) Math.abs(Math.floor(Math.sin( (double) number) * 10 ) ) ;
        return  COLORS[number];
    }

    private Calendar getCalendarDate(String stringDate, Handler<Either<String,JsonArray>> handler){
        SimpleDateFormat formatter = new SimpleDateFormat(START_END_DATE_FORMAT);
        Date date =new Date() ;
        try {
            if(stringDate.matches(".*Z$")){
                stringDate = stringDate.replaceAll("[.]\\d*Z", "");
            }
            date = formatter.parse(stringDate);
        } catch (ParseException e) {
            LOG.error("Error formatting Date ");
            handler.handle(new Either.Left<>("Error formatting Date"));
        }
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(date);
        return startCalendar;
    }
}

 class Course {
     protected final String startDate = "startDate";
     protected final String _id = "_id";
     protected final String structureId = "structureId";
     protected final String subjectId = "subjectId";
     protected final String roomLabels = "roomLabels";
     protected final String equipmentLabels = " equipmentLabels";
     protected final String teacherIds = "teacherIds";
     protected final String personnelIds = "personnelIds";
     protected final String classes = "classes";
     protected final String groups = "groups";
     protected final String dayOfWeek = "dayOfWeek";
     protected final String endDate = "endDate";
     protected final String everyTwoWeek = "everyTwoWeek";
 }