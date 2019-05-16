/*
 * Copyright (c) Région Hauts-de-France, Département de la Seine-et-Marne, Région Nouvelle Aquitaine, Mairie de Paris, CGI, 2016.
 * This file is part of OPEN ENT NG. OPEN ENT NG is a versatile ENT Project based on the JVM and ENT Core Project.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 * For the sake of explanation, any module that communicate over native
 * Web protocols, such as HTTP, with OPEN ENT NG is outside the scope of this
 * license and could be license under its own terms. This is merely considered
 * normal use of OPEN ENT NG, and does not fall under the heading of "covered work".
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.CommonCoursService;
import fr.openent.viescolaire.service.UtilsService;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
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
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.sql.Sql;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import java.util.concurrent.TimeUnit;

import static org.entcore.common.mongodb.MongoDbResult.*;

public class DefaultCommonCoursService implements CommonCoursService {
    protected static final Logger LOG = LoggerFactory.getLogger(fr.openent.viescolaire.service.impl.DefaultPeriodeService.class);
    private static UtilsService utilsService= new fr.openent.viescolaire.service.impl.DefaultUtilsService();
    private static final Course COURSE_TABLE = new Course();
    private static final String COURSES = "courses";
    public final static String EDT_SCHEMA = "edt";
    private final EventBus eb;
    private static final JsonObject KEYS = new JsonObject().put(COURSE_TABLE._id, 1).put(COURSE_TABLE.structureId, 1).put(COURSE_TABLE.subjectId, 1)
            .put(COURSE_TABLE.roomLabels, 1).put(COURSE_TABLE.equipmentLabels, 1).put(COURSE_TABLE.teacherIds, 1).put(COURSE_TABLE.personnelIds, 1)
            .put(COURSE_TABLE.classes, 1).put(COURSE_TABLE.groups, 1).put(COURSE_TABLE.dayOfWeek, 1).put(COURSE_TABLE.startDate, 1).put(COURSE_TABLE.endDate, 1)
            .put(COURSE_TABLE.everyTwoWeek,1).put(COURSE_TABLE.manual,1).put(COURSE_TABLE.exceptionnal,1).put(COURSE_TABLE.author,1).put(COURSE_TABLE.lastUser,1)
            .put(COURSE_TABLE.created,1).put(COURSE_TABLE.updated,1).put(COURSE_TABLE.idStartSlot, 1).put(COURSE_TABLE.idEndSlot, 1);
    private static final String START_DATE_PATTERN = "T00:00Z";
    private static final String END_DATE_PATTERN = "T23.59Z";
    private static final String START_END_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private final Neo4j neo4j = Neo4j.getInstance();

    public DefaultCommonCoursService(EventBus eb) {
        this.eb = eb;
    }

    @Override
    public void listCoursesBetweenTwoDates(String structureId, List<String> teacherId, List<String>  groups, String begin, String end, Handler<Either<String,JsonArray>> handler){
        if (Utils.validationParamsNull(handler, structureId, begin, end)) return;
        final JsonObject query = new JsonObject();
        query.put("structureId", structureId)
                .put("deleted", new JsonObject().put("$exists", false));

        JsonArray $and = new JsonArray();
        String startDate = end + END_DATE_PATTERN;
        String endDate =  begin + START_DATE_PATTERN;
        JsonObject startFilter = new JsonObject().put("$lte", startDate);
        JsonObject endFilter = new JsonObject().put("$gte", endDate);
        $and.add(new JsonObject().put("startDate", startFilter))
                .add(new JsonObject().put("endDate", endFilter));
        if (!teacherId.isEmpty()) {
            JsonObject $in = new JsonObject()
                    .put("$in", new JsonArray(teacherId));
            $and.add(new JsonObject().put("teacherIds", $in));
        }

        if (!groups.isEmpty()) {
            JsonArray $or = new JsonArray();
            JsonObject filter = new JsonObject().put("$in", new JsonArray(groups));
            $or.add(new JsonObject().put("groups", filter))
                    .add(new JsonObject().put("classes", filter));

            $and.add(new JsonObject().put("$or", $or));
        }

        query.put("$and", $and);
        JsonObject sort = new JsonObject().put(COURSE_TABLE.startDate, 1);
        MongoDb.getInstance().find(COURSES, query, sort, KEYS, validResultsHandler(handler));
    }

    private JsonArray getGroupsFilterTable(List<String>  groups , List<String> teacherId) {
        JsonArray groupOperand = new fr.wseduc.webutils.collections.JsonArray();
        if (teacherId != null && !teacherId.isEmpty() ){
            for(String teacher : teacherId){
                groupOperand.add(new JsonObject().put(COURSE_TABLE.teacherIds, teacher));
            }
        }
        for(String group : groups){
            groupOperand.add(new JsonObject().put(COURSE_TABLE.classes, group))
                    .add(new JsonObject().put(COURSE_TABLE.groups, group));
        }

        return groupOperand ;
    }
    private JsonArray getTeachersFilterTable(List<String>  teachers) {
        JsonArray groupOperand = new fr.wseduc.webutils.collections.JsonArray();
        for(String teacher : teachers){
            groupOperand.add(new JsonObject().put(COURSE_TABLE.teacherIds, teacher));
        }
        return groupOperand ;
    }
    @Override
    public void getCoursesOccurences(String structureId, List<String> teacherId, List<String>  group, String begin, String end, final Handler<Either<String,JsonArray>> handler){

        Future<JsonArray> coursesFuture = Future.future();
        listCoursesBetweenTwoDates(structureId, teacherId, group, begin, end, response -> {
                    if (response.isRight()) {
                        coursesFuture.complete(response.right().getValue());
                    } else {
                        coursesFuture.fail("can't get courses from mongo");
                    }
                }
        );
        Future<JsonArray> classeFuture = Future.future();

        checkGroupFromClass(group,structureId,  response -> {
            if (response.isRight()) {
                classeFuture.complete(response.right().getValue());
            } else {
                classeFuture.fail("can't get courses from mongo");
            }

        }) ;
        //Exlusion part
        Future<JsonArray> exclusionsFuture = Future.future();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date queryStartDate = new Date();
        Date queryEndDate = new Date();
        try {
            queryStartDate = formatter.parse(begin + " 00:00:00");
            queryEndDate = formatter.parse(end + " 23:59:59");
        } catch (ParseException e) {
            exclusionsFuture.fail("can't parse init date to exclusions query");
        }
        final Date queryStart = queryStartDate, queryEnd = queryEndDate;
        String query = "SELECT to_char(start_date, 'YYYY-MM-DD HH24:MI:SS') as start_date, to_char(end_date, 'YYYY-MM-DD HH24:MI:SS') as end_date" +
                " FROM " + Viescolaire.VSCO_SCHEMA + "." + Viescolaire.VSCO_SETTING_PERIOD +
                " WHERE id_structure = ? AND code = 'EXCLUSION'";

        JsonArray params = new fr.wseduc.webutils.collections.JsonArray().add(structureId);
        Sql.getInstance().prepared(query, params, (res) -> {
            JsonArray exclusions;
            if (!res.isSend()) {
                exclusionsFuture.fail("can't get exclusions days from mongo");
            } else {
                exclusionsFuture.complete(res.body().getJsonArray("results"));
            }
        });

        CompositeFuture.all(coursesFuture,classeFuture, exclusionsFuture).setHandler(event -> {
            if (event.succeeded()) {
                handler.handle(new Either.Right<>(getOccurencesWithCourses(queryStart,classeFuture.result(),group,teacherId, queryEnd, coursesFuture.result(), exclusionsFuture.result(), handler)));

            } else {
                handler.handle(new Either.Left<>(event.cause().getMessage()));
            }
        });

    }

    private void checkGroupFromClass(List<String> group,String structureId,  Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        query.append("MATCH (u:User {profiles:['Student']})--(:ProfileGroup)--(c:Class)--(s:Structure) ")
                .append("WHERE s.id = {idStructure} and c.name IN {labelClasses} ")
                .append("WITH u, c MATCH (u)--(g) WHERE g:FunctionalGroup OR g:ManualGroup ")
                .append("RETURN  c.name as name_classe, COLLECT(DISTINCT g.name) AS name_groups");
        params.put("idStructure",structureId);
        params.put("labelClasses", new fr.wseduc.webutils.collections.JsonArray(group));

        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(handler));

    }

    public void getCourse(String idCourse, Handler<Either<String,JsonObject>> handler ) {
        final JsonObject query = new JsonObject();
        query.put(COURSE_TABLE._id, idCourse);
        MongoDb.getInstance().findOne(COURSES, query,KEYS, validResultHandler(handler));
    }
    private JsonArray getOccurencesWithCourses(Date queryStartDate,JsonArray arrayGroups, List<String>  group, List<String> teacherId, Date queryEndDate, JsonArray arrayCourses,JsonArray exclusions, Handler<Either<String,JsonArray>> handler) {
        JsonArray result = new JsonArray();

        boolean onlyOneClass = isOneClass(arrayGroups,teacherId,group);
        for(int i=0; i < arrayCourses.size() ; i++) {
            JsonObject course =  arrayCourses.getJsonObject(i);


            // Pour chaque course je vérifie si c'est le bon format de date.
            if(goodFormatDate(course.getString(COURSE_TABLE.startDate)) && goodFormatDate(course.getString(COURSE_TABLE.endDate))){
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
                        JsonObject c = new JsonObject(formatOccurence(course,onlyOneClass, startMoment, endMoment, true).toString());
                        if (periodOverlapPeriod(queryStartDate, queryEndDate, startMoment.getTime(), endMoment.getTime())&&(courseDoesntOverlapExclusionPeriods(exclusions, c))) {
                            result.add(c);
                        }
                        startMoment.add(Calendar.DATE, cadence);
                        endMoment.add(Calendar.DATE, cadence);
                    }
                } else {
                    JsonObject c = new JsonObject(formatOccurence(course,onlyOneClass, startMoment, endMoment, false).toString());
                    if (periodOverlapPeriod(queryStartDate, queryEndDate, startMoment.getTime(), endMoment.getTime())
                            &&(courseDoesntOverlapExclusionPeriods(exclusions, c))) {
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

    /**
     * Check if only one class and is group is called
     * @param arrayGroups
     * @param teacherId
     * @param groups
     * @return
     */
    private boolean isOneClass(JsonArray arrayGroups,List<String> teacherId, List<String> groups) {
        if (  !teacherId.isEmpty() || arrayGroups.size()>1){
            return false;
        }
        if(arrayGroups.size() == 0){
            return groups.size() == 1;
        }


        JsonObject result = arrayGroups.getJsonObject(0);
        String classe = result.getString("name_classe");
        JsonArray groupsFromNeo = result.getJsonArray("name_groups");


        for(int j = 0; j < groups.size(); j++){
            if(classe.equals(groups.get(j))){
                groups.remove(j);
            }
            for(int i = 0; i < groupsFromNeo.size();i++){

                if(groups.size()>0)
                    if(groups.get(j).equals(groupsFromNeo.getString(i))){
                        groups.remove(j);
                    }
            }
        }
        if(groups.size()==0){
           return  true;
        }else{
            return false;
        }
    }

    private static boolean periodOverlapPeriod(Date aStart, Date aEnd, Date bStart, Date bEnd){
        return aStart.before(bEnd) && bStart.before(aEnd);
    }
    private static boolean courseDoesntOverlapExclusionPeriods(JsonArray exclusions, JsonObject c){
        boolean canAdd = true;
        for(int i=0; i < exclusions.size(); i++){
            SimpleDateFormat DATE_FORMATTER= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date startExclusion = getDate(exclusions.getJsonArray(i).getString(0), DATE_FORMATTER) ;
            Date endExclusion = getDate( exclusions.getJsonArray(i).getString(1), DATE_FORMATTER) ;
            Date startOccurrence = getDate( c.getString(COURSE_TABLE.startDate) , DATE_FORMATTER) ;
            Date endOccurrence =  getDate(c.getString(COURSE_TABLE.endDate) , DATE_FORMATTER) ;
            if ( !( ( startOccurrence.before(startExclusion) && endOccurrence.before(startExclusion))
                    || ( startOccurrence.after(endExclusion) && endOccurrence.after(endExclusion)) )) {
                canAdd = false;
                break;
            }
        }
        return canAdd;
    }

    private static Integer getDayOfWeek (JsonObject course, Handler<Either<String,JsonArray>> handler){
        Integer dayOfWeek = null;
        try{
            dayOfWeek = Integer.parseInt(String.valueOf(course.getValue(COURSE_TABLE.dayOfWeek)));
        } catch (ClassCastException e) {
            LOG.error("Error formatting dayOfWeek ");
            handler.handle(new Either.Left<>("Error formatting dayOfWeek"));
        }
        dayOfWeek = dayOfWeek + 1 ;
        return dayOfWeek;
    }
    private static JsonObject formatOccurence(JsonObject course, boolean onlyOneGroup, Calendar start , Calendar end, boolean isRecurent) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.format(start.getTime());

        JsonObject occurence = new JsonObject(course.toString());
        occurence.put("is_recurrent", isRecurent);

        occurence.put("color",  !onlyOneGroup ?
                ( ( course.getJsonArray("classes").size() > 0)
                        ?  utilsService.getColor(course.getJsonArray("classes").getString(0))
                        :  utilsService.getColor(course.getJsonArray("groups").getString(0)))
                :   utilsService.getSubjectColor(course.getString("subjectId")));


        occurence.put("is_periodic",false);
        occurence.put(COURSE_TABLE.startDate, df.format(start.getTime()));
        occurence.put(COURSE_TABLE.endDate, df.format(end.getTime()));
        if(course.getString("subjectId").equals(Course.exceptionnalSubject)){
            occurence.put(COURSE_TABLE.exceptionnal, course.getString(COURSE_TABLE.exceptionnal));
        }
        occurence.put(COURSE_TABLE.updated, course.getString(COURSE_TABLE.updated));
        occurence.put(COURSE_TABLE.created, course.getString(COURSE_TABLE.created));
        occurence.put(COURSE_TABLE.author, course.getString(COURSE_TABLE.author));
        occurence.put(COURSE_TABLE.lastUser, course.getString(COURSE_TABLE.lastUser));

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
    public static Date getDate(String dateString, SimpleDateFormat dateFormat){
        Date date= new Date();
        try{
            date =  dateFormat.parse(dateString);
        } catch (ParseException e) {
            LOG.error("error when casting date: ", e);
        }
        return date ;
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
    public static String exceptionnalSubject = "exceptionnal Subject";
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
    protected final String exceptionnal = "exceptionnal";
    protected final String author = "author";
    protected final String created = "created";
    protected final String updated = "updated";
    protected final String lastUser = "lastUser";
    protected final String idStartSlot = "idStartSlot";
    protected final String idEndSlot = "idEndSlot";
}