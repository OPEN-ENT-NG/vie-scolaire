package fr.openent.viescolaire.service.impl;

import fr.openent.viescolaire.service.CommonCoursService;
import fr.openent.viescolaire.service.UtilsService;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.webutils.Either;

import fr.wseduc.webutils.Utils;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import java.util.concurrent.TimeUnit;

import static org.entcore.common.mongodb.MongoDbResult.*;

public class DefaultCommonCoursService implements CommonCoursService {
    protected static final Logger LOG = LoggerFactory.getLogger(DefaultPeriodeService.class);
    private static UtilsService utilsService= new DefaultUtilsService();
    private static final Course COURSE_TABLE = new Course();
    private static final String COURSES = "courses";
    private final EventBus eb;
    private static final JsonObject KEYS = new JsonObject().put(COURSE_TABLE._id, 1).put(COURSE_TABLE.structureId, 1).put(COURSE_TABLE.subjectId, 1)
            .put(COURSE_TABLE.roomLabels, 1).put(COURSE_TABLE.equipmentLabels, 1).put(COURSE_TABLE.teacherIds, 1).put(COURSE_TABLE.personnelIds, 1)
            .put(COURSE_TABLE.classes, 1).put(COURSE_TABLE.groups, 1).put(COURSE_TABLE.dayOfWeek, 1).put(COURSE_TABLE.startDate, 1).put(COURSE_TABLE.endDate, 1)
            .put(COURSE_TABLE.everyTwoWeek,1).put(COURSE_TABLE.manual,1);
    private static final String START_DATE_PATTERN = "T00:00Z";
    private static final String END_DATE_PATTERN = "T23.59Z";
    private static final String START_END_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public DefaultCommonCoursService(EventBus eb) {
        this.eb = eb;
    }

    @Override
    public void listCoursesBetweenTwoDates(String structureId, List<String> teacherId, List<String>  groups, String begin, String end, Handler<Either<String,JsonArray>> handler){
        if (Utils.validationParamsNull(handler, structureId, begin, end)) return;
        final JsonObject query = new JsonObject();

        query.put("structureId", structureId);

        if (teacherId != null && !teacherId.isEmpty() ){
            query.put("$or", getTeachersFilterTable(teacherId));
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
            JsonObject groupsOperand = getGroupsFilterTable( groups);
            query.put("$and", new fr.wseduc.webutils.collections.JsonArray().add(dateOperand).add(groupsOperand));
        } else {
            query.put("$and", new fr.wseduc.webutils.collections.JsonArray()
                    .add(new JsonObject().put(COURSE_TABLE.startDate, betweenStart))
                    .add(new JsonObject().put(COURSE_TABLE.endDate, betweenEnd)));
        }

        final JsonObject sort = new JsonObject().put(COURSE_TABLE.startDate, 1);

        MongoDb.getInstance().find(COURSES, query, sort, KEYS, validResultsHandler(handler));
    }
    private JsonObject getGroupsFilterTable(List<String>  groups) {
        JsonArray groupOperand = new fr.wseduc.webutils.collections.JsonArray();
        for(String group : groups){
            groupOperand.add(new JsonObject().put(COURSE_TABLE.classes, group))
                    .add(new JsonObject().put(COURSE_TABLE.groups, group));
        }

        return  new JsonObject().put("$or", groupOperand );
    }
    private JsonArray getTeachersFilterTable(List<String>  teachers) {
        JsonArray groupOperand = new fr.wseduc.webutils.collections.JsonArray();
        for(String teacher : teachers){
            groupOperand.add(new JsonObject().put(COURSE_TABLE.teacherIds, teacher));
        }
        return groupOperand ;
    }
    @Override
    public void getCoursesOccurences(String structureId, List<String> teacherId, List<String>  group, String begin, String end, Handler<Either<String,JsonArray>> handler){
        listCoursesBetweenTwoDates(structureId, teacherId, group, begin, end, response -> {
                    if (response.isRight()) {
                        JsonArray arrayCourses = response.right().getValue();

                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date queryStartDate = new Date();
                        Date queryEndDate = new Date();
                        try {
                            queryStartDate = formatter.parse(begin + " 00:00:00");
                            queryEndDate = formatter.parse(end + " 23:59:59");
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        handler.handle(new Either.Right<>(getOccurencesWithCourses(queryStartDate, queryEndDate, arrayCourses, handler)));
                    } else {
                        LOG.error("can't get courses from mongo");
                        handler.handle(new Either.Left<>("can't get courses from mongo"));
                    }
                }
        );
    }
    public void getCourse(String idCourse, Handler<Either<String,JsonObject>> handler ) {
        final JsonObject query = new JsonObject();
        query.put(COURSE_TABLE._id, idCourse);
        MongoDb.getInstance().findOne(COURSES, query,KEYS, validResultHandler(handler));
    }
    private JsonArray getOccurencesWithCourses(Date queryStartDate, Date queryEndDate, JsonArray arrayCourses, Handler<Either<String,JsonArray>> handler) {
        JsonArray result = new JsonArray();
        for(int i=0; i < arrayCourses.size() ; i++) {
            JsonObject course =  arrayCourses.getJsonObject(i);

            // Pour chaque course je vérifie si c'est le bon format de date.
            if(goodFormatDate(course.getString(COURSE_TABLE.startDate)) && goodFormatDate(course.getString(COURSE_TABLE.endDate)) ){
                course.put("startCourse",course.getString(COURSE_TABLE.startDate))
                        .put("endCourse",course.getString(COURSE_TABLE.endDate));

                Calendar startMoment = getCalendarDate( course.getString(COURSE_TABLE.startDate), handler);
                startMoment.set(Calendar.DAY_OF_WEEK,getDayOfWeek(course, handler));
                Calendar endMoment = getCalendarDate(course.getString(COURSE_TABLE.endDate), handler);
                course.put(COURSE_TABLE.startDate, startMoment.getTime().toInstant().toString());
                double numberWeek = Math.floor( daysBetween(startMoment, endMoment) / (double) 7 );
                if (numberWeek > 0) {

                    String endDateCombine = course.getString(COURSE_TABLE.startDate)
                            .replaceAll("T.*$", 'T' + course.getString(COURSE_TABLE.endDate).replaceAll("^.*T", ""));
                    endMoment = getCalendarDate(endDateCombine, handler);
                    int cadence = course.containsKey(COURSE_TABLE.everyTwoWeek) && course.getBoolean(COURSE_TABLE.everyTwoWeek)  ? 14 : 7 ;
                    for (int j = 0; j < numberWeek + 1; j++) {
                        JsonObject c = new JsonObject(formatOccurence(course, startMoment, endMoment, true).toString());
                        if (periodOverlapPeriod(queryStartDate, queryEndDate, startMoment.getTime(), endMoment.getTime())) {
                            result.add(c);
                        }
                        startMoment.add(Calendar.DATE, cadence);
                        endMoment.add(Calendar.DATE, cadence);
                    }
                } else {
                    JsonObject c = new JsonObject(formatOccurence(course, startMoment, endMoment, false).toString());

                    if (periodOverlapPeriod(queryStartDate, queryEndDate, startMoment.getTime(), endMoment.getTime())) {
                        result.add(c);
                    }
                }
            }else {
                LOG.error("Error bad data format Date ");
                handler.handle(new Either.Left<>("Error bad data format Date "));
            }
        }
        return result;
    }

    private static boolean periodOverlapPeriod(Date aStart, Date aEnd, Date bStart, Date bEnd){
        return aStart.before(bEnd) && bStart.before(aEnd);
    }

    private static Integer getDayOfWeek (JsonObject course, Handler<Either<String,JsonArray>> handler){
        Integer dayOfWeek = null;
        try{
            dayOfWeek = course.getInteger(COURSE_TABLE.dayOfWeek) ;
        } catch (ClassCastException e) {
            LOG.error("Error formatting dayOfWeek ");
            handler.handle(new Either.Left<>("Error formatting dayOfWeek"));
        }
        dayOfWeek = dayOfWeek + 1 ;
        return dayOfWeek;
    }
    private static JsonObject formatOccurence(JsonObject course, Calendar start , Calendar end, boolean isRecurent) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.format(start.getTime());

        JsonObject occurence = new JsonObject(course.toString());
        occurence.put("is_recurrent", isRecurent);
        occurence.put("color", utilsService.getColor(course.getJsonArray("classes").getString(0)));
        occurence.put("is_periodic",false);
        occurence.put(COURSE_TABLE.startDate, df.format(start.getTime()));
        occurence.put(COURSE_TABLE.endDate, df.format(end.getTime()));
        return occurence;
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
        Date date = new Date() ;
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
    protected final String manual = "manual";
}