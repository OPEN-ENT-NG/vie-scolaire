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

import fr.openent.viescolaire.service.CommonCoursService;
import fr.openent.viescolaire.service.UtilsService;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
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


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.*;

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
            .put(COURSE_TABLE.created,1).put(COURSE_TABLE.updated,1).put(COURSE_TABLE.idStartSlot, 1).put(COURSE_TABLE.idEndSlot, 1).put(COURSE_TABLE.classesExternalIds, 1)
            .put(COURSE_TABLE.groupsExternalIds, 1).put(COURSE_TABLE.reccurrence, 1);
    private static final String START_DATE_PATTERN = "T00:00Z";
    private static final String END_DATE_PATTERN = "T23.59Z";
    private static final String START_END_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private final Neo4j neo4j = Neo4j.getInstance();

    public DefaultCommonCoursService(EventBus eb) {
        this.eb = eb;
    }

    @Override
    public void listCoursesBetweenTwoDates(String structureId, List<String> teacherId, List<String>  groups, String begin, String end, boolean union, Handler<Either<String,JsonArray>> handler){
        if (Utils.validationParamsNull(handler, structureId, begin, end)) return;
        final JsonObject query = new JsonObject();
        query.put("structureId", structureId)
                .put("deleted", new JsonObject().put("$exists", false));

        JsonArray $and = new JsonArray();
        query.put("$or", theoreticalFilter());
        String startDate = end + END_DATE_PATTERN;
        String endDate =  begin + START_DATE_PATTERN;
        JsonObject startFilter = new JsonObject().put("$lte", startDate);
        JsonObject endFilter = new JsonObject().put("$gte", endDate);
        $and.add(new JsonObject().put("startDate", startFilter))
                .add(new JsonObject().put("endDate", endFilter));



        //If we want an union of teachers and groups results
        if(union) {
            if(!groups.isEmpty() || !teacherId.isEmpty()) {
                JsonArray $or = new JsonArray();
                if (!groups.isEmpty()) {
                    JsonObject filter = new JsonObject().put("$in", new JsonArray(groups));
                    $or.add(new JsonObject().put("groups", filter))
                            .add(new JsonObject().put("classes", filter));
                }
                if (!teacherId.isEmpty()) {
                    JsonObject $in = new JsonObject().put("$in", new JsonArray(teacherId));
                    $or.add(new JsonObject().put("teacherIds", $in));
                }
                $and.add(new JsonObject().put("$or", $or));
            }
        }


        //If we want to intersect results
        else {
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
        }

        query.put("$and", $and);
        JsonObject sort = new JsonObject().put(COURSE_TABLE.startDate, 1);
        MongoDb.getInstance().find(COURSES, query, sort, KEYS, validResultsHandler(handler));
    }

    private JsonArray theoreticalFilter() {
        JsonArray filter = new JsonArray();
        JsonObject falseValue = new JsonObject().put("theoretical", false);
        JsonObject existsValue = new JsonObject().put("theoretical", new JsonObject().put("$exists", false));
        return filter.add(existsValue).add(falseValue);
    }

    @Override
    public void getCoursesOccurences(String structureId, List<String> teacherId, List<String>  group, String begin, String end, boolean union, final Handler<Either<String,JsonArray>> handler){
        Future<JsonArray> coursesFuture = Future.future();
        listCoursesBetweenTwoDates(structureId, teacherId, group, begin, end, union, response -> {
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
        });

        CompositeFuture.all(coursesFuture,classeFuture).setHandler(event -> {
            if (event.succeeded()) {
                JsonArray results = new JsonArray();
                for(Object o : coursesFuture.result()) {
                    JsonObject course = (JsonObject) o;
                    boolean onlyOneClass = isOneClass(classeFuture.result(),teacherId,group);
                    Calendar startMoment = getCalendarDate(course.getString(COURSE_TABLE.startDate), handler);
                    Calendar endMoment = getCalendarDate(course.getString(COURSE_TABLE.endDate), handler);
                    results.add(formatOccurence(course,onlyOneClass, startMoment, endMoment));
                }
                handler.handle(new Either.Right<>(results));
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

    /**
     * Check if only one class and is group is called
     * @param arrayGroups   groups
     * @param teacherId     list of teacherId
     * @param groups        list of group
     * @return boolean
     */
    private boolean isOneClass(JsonArray arrayGroups,List<String> teacherId, List<String> groups) {
        if (!teacherId.isEmpty() || arrayGroups.size() > 1){
            return false;
        }
        if(arrayGroups.isEmpty()){
            return groups.size() == 1;
        }

        int count = arrayGroups.size();
        JsonObject result = arrayGroups.getJsonObject(0);
        List<String> groupsFromNeo = result.getJsonArray("name_groups").getList();
        String classe = result.getString("name_classe");
        if (groups.contains(classe)) count--;
        for (String group: groupsFromNeo) {
            if (groups.contains(group)) count--;
        }

        return count == 0;
    }

    private static JsonObject formatOccurence(JsonObject course, boolean onlyOneGroup, Calendar start , Calendar end) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.format(start.getTime());

        JsonObject occurence = new JsonObject(course.toString());

        String color = "";
        if(!onlyOneGroup) {
            if( course.getJsonArray("classes") != null && course.getJsonArray("classes").size() > 0){
                color = utilsService.getColor(course.getJsonArray("classes").getString(0));
            }
            else if(course.getJsonArray("groups") != null && course.getJsonArray("groups").size() > 0){
                color = utilsService.getColor(course.getJsonArray("groups").getString(0));
            }
        }
        if(color.isEmpty() && course.containsKey("subjectId")) {
            color = utilsService.getSubjectColor(course.getString("subjectId"));
        }
        occurence.put("color", color);

        occurence.put("is_periodic",false);
        occurence.put(COURSE_TABLE.startDate, df.format(start.getTime()));
        occurence.put(COURSE_TABLE.endDate, df.format(end.getTime()));
        occurence.put(COURSE_TABLE.startCourse, df.format(start.getTime()));
        occurence.put(COURSE_TABLE.endCourse, df.format(end.getTime()));
        if(course.getString("subjectId", "").equals(Course.exceptionnalSubject)){
            occurence.put(COURSE_TABLE.exceptionnal, course.getString(COURSE_TABLE.exceptionnal));
        }
        return occurence;
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
    protected final String startCourse = "startCourse";
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
    protected final String endCourse = "endCourse";
    protected final String everyTwoWeek = "everyTwoWeek";
    protected final String manual = "manual";
    protected final String exceptionnal = "exceptionnal";
    protected final String author = "author";
    protected final String created = "created";
    protected final String updated = "updated";
    protected final String lastUser = "lastUser";
    protected final String idStartSlot = "idStartSlot";
    protected final String idEndSlot = "idEndSlot";
    final String classesExternalIds = "classesExternalIds";
    final String groupsExternalIds = "groupsExternalIds";
    final String reccurrence = "reccurrence";
}