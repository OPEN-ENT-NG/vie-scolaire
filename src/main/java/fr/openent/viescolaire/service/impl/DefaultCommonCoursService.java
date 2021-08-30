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

import fr.openent.viescolaire.db.DBService;
import fr.openent.viescolaire.service.CommonCoursService;
import fr.openent.viescolaire.service.MatiereService;
import fr.openent.viescolaire.service.PeriodeAnneeService;
import fr.openent.viescolaire.service.UtilsService;
import fr.openent.viescolaire.utils.DateHelper;
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
import org.entcore.common.mongodb.MongoDbResult;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.entcore.common.mongodb.MongoDbResult.*;

public class DefaultCommonCoursService extends DBService implements CommonCoursService {
    protected static final Logger LOG = LoggerFactory.getLogger(fr.openent.viescolaire.service.impl.DefaultPeriodeService.class);
    private static UtilsService utilsService = new fr.openent.viescolaire.service.impl.DefaultUtilsService();
    private static final Course COURSE_TABLE = new Course();
    private final MatiereService matiereService;

    private static final String COURSES = "courses";
    public final static String EDT_SCHEMA = "edt";

    private final EventBus eb;
    private static final JsonObject KEYS = new JsonObject().put(COURSE_TABLE._id, 1).put(COURSE_TABLE.structureId, 1).put(COURSE_TABLE.subjectId, 1)
            .put(COURSE_TABLE.roomLabels, 1).put(COURSE_TABLE.equipmentLabels, 1).put(COURSE_TABLE.teacherIds, 1).put(COURSE_TABLE.personnelIds, 1)
            .put(COURSE_TABLE.classes, 1).put(COURSE_TABLE.groups, 1).put(COURSE_TABLE.dayOfWeek, 1).put(COURSE_TABLE.startDate, 1).put(COURSE_TABLE.endDate, 1)
            .put(COURSE_TABLE.everyTwoWeek, 1).put(COURSE_TABLE.manual, 1).put(COURSE_TABLE.exceptionnal, 1).put(COURSE_TABLE.author, 1).put(COURSE_TABLE.lastUser, 1)
            .put(COURSE_TABLE.created, 1).put(COURSE_TABLE.updated, 1).put(COURSE_TABLE.idStartSlot, 1).put(COURSE_TABLE.idEndSlot, 1).put(COURSE_TABLE.classesExternalIds, 1)
            .put(COURSE_TABLE.groupsExternalIds, 1).put(COURSE_TABLE.recurrence, 1).put(COURSE_TABLE.timetableSubjectId, 1);
    private static final String START_DATE_PATTERN = "T00:00Z";
    private static final String END_DATE_PATTERN = "T23.59Z";
    private static final String START_END_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private final Neo4j neo4j = Neo4j.getInstance();
    private final PeriodeAnneeService periodeAnneeService;


    public DefaultCommonCoursService(EventBus eb) {
        this.eb = eb;
        matiereService = new DefaultMatiereService(eb);
        periodeAnneeService = new DefaultPeriodeAnneeService();
    }

    public DefaultCommonCoursService() {
        this.eb = null;
        matiereService = new DefaultMatiereService();
        periodeAnneeService = new DefaultPeriodeAnneeService();
    }

    @Override
    public void listCoursesBetweenTwoDates(String structureId, List<String> teacherId, List<String> groupIds,
                                           List<String> groupExternalIds, List<String> groupNames, List<String> subjectIds,
                                           String begin, String end, String startTime, String endTime,
                                           boolean union, boolean crossDateFilter, String limitString,
                                           String offsetString, boolean descendingDate, Boolean searchTeacher,
                                           Handler<Either<String, JsonArray>> handler) {

        if (Utils.validationParamsNull(handler, structureId, begin, end)) return;
        final JsonObject query = new JsonObject();
        query.put("structureId", structureId)
                .put("deleted", new JsonObject().put("$exists", false));

        JsonArray $and = new JsonArray();
        query.put("$or", theoreticalFilter());
        String endDate = end + (endTime == null ? END_DATE_PATTERN : "T" + endTime + "Z");
        String startDate = begin + (startTime == null ? START_DATE_PATTERN : "T" + startTime + "Z");

        JsonObject startFilter = new JsonObject();
        JsonObject endFilter = new JsonObject();

        if (!crossDateFilter) {
            startFilter.put("$gte", startDate);
            endFilter.put("$lte", endDate);
        } else {
            startFilter.put("$lte", startDate);
            endFilter.put("$gte", endDate);
        }

        $and.add(new JsonObject().put("startDate", startFilter))
                .add(new JsonObject().put("endDate", endFilter));

        if (Boolean.TRUE.equals(searchTeacher))
            $and.add(new JsonObject().put("teacherIds", new JsonObject().put("$not", new JsonObject().put("$size", 0))));

        if (Boolean.FALSE.equals(searchTeacher))
            $and.add(new JsonObject().put("teacherIds", new JsonObject().put("$size", 0)));


        JsonObject filterGroupIds = (groupIds != null && !groupIds.isEmpty()) ?
                new JsonObject().put("$in", new JsonArray(groupIds)) :
                new JsonObject();
        JsonObject filterGroupExternalIds = (groupExternalIds != null && !groupExternalIds.isEmpty()) ?
                new JsonObject().put("$in", new JsonArray(groupExternalIds)) :
                new JsonObject();
        JsonObject filterGroupNames = (groupNames != null && !groupNames.isEmpty()) ?
                new JsonObject().put("$in", new JsonArray(groupNames)) :
                new JsonObject();

        JsonArray $or = new JsonArray();
        if (!filterGroupIds.isEmpty()) {
            $or.add(new JsonObject().put("groupsIds", filterGroupIds))
                    .add(new JsonObject().put("classesIds", filterGroupIds));
        }
        if (!filterGroupExternalIds.isEmpty()) {
            $or.add(new JsonObject().put("groupsExternalIds", filterGroupExternalIds))
                    .add(new JsonObject().put("classesExternalIds", filterGroupExternalIds));
        }
        if (groupNames != null && !groupNames.isEmpty()) {
            $or.add(new JsonObject().put("groups", filterGroupNames))
                    .add(new JsonObject().put("classes", filterGroupNames));
        }
        if (subjectIds != null && !subjectIds.isEmpty()) {
            JsonObject $in = new JsonObject().put("$in", new JsonArray(subjectIds));
            $and.add(new JsonObject().put("subjectId", $in));
        }

        if (!teacherId.isEmpty()) {
            JsonObject $in = new JsonObject().put("$in", new JsonArray(teacherId));
            if (union) $or.add(new JsonObject().put("teacherIds", $in)); //If we want an union of teachers and groups results
            else $and.add(new JsonObject().put("teacherIds", $in)); //If we want to intersect results
        }

        if (!$or.isEmpty()) $and.add(new JsonObject().put("$or", $or));

        query.put("$and", $and);

        // sort ascending/descending
        JsonObject sort = new JsonObject().put(COURSE_TABLE.startDate, descendingDate ? -1 : 1);

        JsonArray pipeline = new JsonArray()
                .add(match(query))
                .add(sort(sort));

        // filter with pagination area
        int limit, offset;
        offset = offsetString != null && !offsetString.equals("") ? Integer.parseInt(offsetString) : 0;
        limit = limitString != null && !limitString.equals("") ? Integer.parseInt(limitString) : -1;

        if (offsetString != null) {
            pipeline.add(skip(offset));
        }

        if (limitString != null) {
            pipeline.add(limit(limit));
        }

        pipeline.add(finalProject());

        JsonObject command = new JsonObject()
                .put("aggregate", COURSES)
                .put("allowDiskUse", true)
                .put("cursor", new JsonObject().put("batchSize", 2147483647))
                .put("pipeline", pipeline);

        mongoDb.command(command.toString(), MongoDbResult.validResultHandler(either -> {
            if (either.isLeft()) {
                String message = String.format("[Viescolaire@%s::listCoursesBetweenTwoDates] " +
                        "Failed to execute pipeline command search for courses", this.getClass().getSimpleName());
                LOG.error(message, either.left().getValue());
                handler.handle(new Either.Left<>(either.left().getValue()));
            } else {
                JsonArray result = either.right().getValue().getJsonObject("cursor", new JsonObject()).getJsonArray("firstBatch", new JsonArray());
                handler.handle(new Either.Right<>(result));
            }
        }));
    }

    private JsonObject finalProject() {
        JsonObject project = new JsonObject()
                .put("_id", 1)
                .put("structureId", 1)
                .put("subjectId", 1)
                .put("timetableSubjectId", 1)
                .put("created", 1)
                .put("teacherIds", 1)
                .put("classes", 1)
                .put("classesExternalIds", 1)
                .put("groups", 1)
                .put("groupsExternalIds", 1)
                .put("periodWeek", 1)
                .put("theoretical", 1)
                .put("recurrence", 1)
                .put("idStartSlot", 1)
                .put("idEndSlot", 1)
                .put("roomLabels", 1)
                .put("startCourse", 1)
                .put("endCourse", 1)
                .put("dayOfWeek", 1)
                .put("color", 1)
                .put("author", 1)
                .put("exceptionnal", 1)
                .put("manual", 1)
                .put("updated", 1)
                .put("lastUser", 1)
                .put("startDate", dateToString("startDate"))
                .put("endDate", dateToString("endDate"));

        return new JsonObject()
                .put("$project", project);
    }

    private JsonObject dateToString(String field) {
        JsonObject type = new JsonObject()
                .put("$type", String.format("$%s", field));

        JsonArray ifArray = new JsonArray()
                .add(type)
                .add("string");

        JsonObject dateToString = new JsonObject()
                .put("format", "%Y-%m-%dT%H:%M:%S")
                .put("date", String.format("$%s", field));

        JsonObject elseObject = new JsonObject()
                .put("$dateToString", dateToString);

        JsonObject cond = new JsonObject()
                .put("if", ifArray)
                .put("then", String.format("$%s", field))
                .put("else", elseObject);

        return new JsonObject()
                .put("$cond", cond);
    }

    private JsonObject match(JsonObject filter) {
        return new JsonObject()
                .put("$match", filter);
    }

    private JsonObject sort(JsonObject filter) {
        return new JsonObject()
                .put("$sort", filter);
    }

    private JsonObject skip(Integer number) {
        return new JsonObject()
                .put("$skip", number);
    }

    private JsonObject limit(Integer number) {
        return new JsonObject()
                .put("$limit", number);
    }

    private JsonArray theoreticalFilter() {
        JsonArray filter = new JsonArray();
        JsonObject falseValue = new JsonObject().put("theoretical", false);
        JsonObject existsValue = new JsonObject().put("theoretical", new JsonObject().put("$exists", false));
        return filter.add(existsValue).add(falseValue);
    }

    @Override
    public void getCoursesOccurences(String structureId, List<String> teacherId, List<String> group, String begin, String end, String startTime, String endTime,
                                     boolean union, boolean crossDateFilter, final Handler<Either<String, JsonArray>> handler) {
        this.getCoursesOccurences(structureId, teacherId, group, begin, end, startTime, endTime, union, crossDateFilter,
                null, null, false, null, handler);
    }

    @Override
    public void getCoursesOccurences(String structureId, List<String> teacherId, List<String> group, String begin, String end, String startTime, String endTime,
                                     boolean union, boolean crossDateFilter, String limit, String offset, boolean descendingDate,
                                     Boolean searchTeacher, final Handler<Either<String, JsonArray>> handler) {
        this.getCoursesOccurences(structureId, teacherId, null, null, group, begin, end,
                startTime, endTime, union, crossDateFilter, limit, offset, descendingDate, searchTeacher, handler);
    }

    @Override
    public void getCoursesOccurences(String structureId, List<String> teacherId, List<String> groupIds, List<String> groupExternalIds,
                                     List<String> group, String begin, String end, String startTime, String endTime,
                                     boolean union, boolean crossDateFilter, String limit, String offset,
                                     boolean descendingDate, Boolean searchTeacher, Handler<Either<String, JsonArray>> handler) {
        Future<JsonArray> coursesFuture = Future.future();
        Future<JsonArray> classeFuture = Future.future();
        Future<JsonArray> exclusionPeriodsFuture = Future.future();

        getCoursesBetweenTwoDates(structureId, teacherId, groupIds, groupExternalIds, group, begin, end, startTime, endTime, union,
                crossDateFilter, limit, offset, descendingDate, searchTeacher, coursesFuture);

        checkGroupFromClass(groupIds, groupExternalIds, group, structureId, response -> {
            if (response.isRight()) {
                classeFuture.complete(response.right().getValue());
            } else {
                classeFuture.fail("[Viescolaire@DefaultCommonCoursService::getCoursesOccurences] Can't get courses from mongo");
            }
        });

        periodeAnneeService.listExclusion(structureId, resExlusions -> {
            if (resExlusions.isLeft()) {
                exclusionPeriodsFuture.fail("[Viescolaire@DefaultCommonCoursService::getCoursesOccurences] Can't get exclusion periods. "
                        + resExlusions.left().getValue());
                return;
            }
            exclusionPeriodsFuture.complete(resExlusions.right().getValue());
        });

        CompositeFuture.all(coursesFuture, classeFuture, exclusionPeriodsFuture).setHandler(event -> {
            if (event.succeeded()) {
                JsonArray results = new JsonArray();
                for (Object o : coursesFuture.result()) {
                    JsonObject course = (JsonObject) o;
                    if (isCourseInsideExcludedPeriods(exclusionPeriodsFuture.result(), course)) continue;
                    boolean onlyOneClass = isOneClass(classeFuture.result(), teacherId, group);
                    Calendar startMoment = getCalendarDate(course.getString(COURSE_TABLE.startDate), handler);
                    Calendar endMoment = getCalendarDate(course.getString(COURSE_TABLE.endDate), handler);
                    results.add(formatOccurence(course, onlyOneClass, startMoment, endMoment));
                }
                handler.handle(new Either.Right<>(results));
            } else {
                handler.handle(new Either.Left<>(event.cause().getMessage()));
            }
        });
    }

    private void getCoursesBetweenTwoDates(String structureId, List<String> teacherId, List<String> groupIds,
                                           List<String> groupExternalIds, List<String> groupNames, String begin,
                                           String end, String startTime, String endTime, boolean union,
                                           boolean crossDateFilter, String limit, String offset,
                                           boolean descendingDate, Boolean searchTeacher, Future<JsonArray> coursesFuture) {
        listCoursesBetweenTwoDates(structureId, teacherId, groupIds, groupExternalIds, groupNames, null, begin, end,
                startTime, endTime, union, crossDateFilter, limit, offset, descendingDate, searchTeacher,
                response -> {
                    if (response.isLeft()) {
                        LOG.error("[Viescolaire@DefaultCommonCoursService::getCoursesBetweenTwoDates] " +
                                "failed to list courses from mongoDb");
                        coursesFuture.fail(response.left().getValue());
                    } else {
                        JsonArray courses = response.right().getValue();

                        setCoursesSubjects(response.right().getValue(), res -> {
                            if (res.isLeft()) {
                                coursesFuture.fail(res.left().getValue());
                            } else {
                                coursesFuture.complete(courses);
                            }
                        });
                    }
                }
        );
    }

    private void setCoursesSubjects(JsonArray courses, Handler<Either<String, JsonArray>> handler) {
        JsonArray subjectIds = getSubjectAndTimetableSubjectIds(courses);

        matiereService.getSubjectsAndTimetableSubjects(subjectIds, subjectsAsync -> {
            if (subjectsAsync.isLeft()) {
                String message = "[Viescolaire@DefaultCommonCoursService::setCoursesSubjects] " +
                        "Failed to retrieve subjects and/or timetableSubject";
                LOG.error(message);
                handler.handle(new Either.Left<>(subjectsAsync.left().getValue()));
            } else {
                setSubjectToCourse(courses, subjectsAsync.right().getValue());
                handler.handle(new Either.Right<>(courses));
            }
        });
    }

    private static boolean isCourseInsideExcludedPeriods(JsonArray exclusions, JsonObject course) {
        boolean isExcluded = false;
        for (Object o : exclusions) {
            JsonObject exclusion = (JsonObject) o;
            SimpleDateFormat DATE_FORMATTER = DateHelper.DATE_FORMATTER;
            Date startExclusion = getDate(exclusion.getString("start_date"), DATE_FORMATTER);
            Date endExclusion = getDate(exclusion.getString("end_date"), DATE_FORMATTER);
            Date startCourse = getDate(course.getString(COURSE_TABLE.startDate), DATE_FORMATTER);
            Date endCourse = getDate(course.getString(COURSE_TABLE.endDate), DATE_FORMATTER);
            if (startCourse.before(endExclusion) && startExclusion.before(endCourse)) {
                isExcluded = true;
                break;
            }
        }
        return isExcluded;
    }

    private JsonArray getSubjectAndTimetableSubjectIds(JsonArray courses) {
        JsonArray subjectIds = new JsonArray();
        if (courses != null) {
            for (int i = 0; i < courses.size(); i++) {
                JsonObject course = courses.getJsonObject(i);
                if (course.containsKey("subjectId")
                        && !subjectIds.contains(course.getString("subjectId"))
                        && course.getString("subjectId") != null) {
                    subjectIds.add(course.getString("subjectId"));
                }
                if (course.containsKey("timetableSubjectId")
                        && !subjectIds.contains(course.getString("timetableSubjectId"))
                        && course.getString("timetableSubjectId") != null) {
                    subjectIds.add(course.getString("timetableSubjectId"));
                }
            }
        }
        return subjectIds;
    }

    @SuppressWarnings("unchecked")
    private void setSubjectToCourse(JsonArray courses, JsonArray subjectsResult) {
        Map<String, JsonObject> subjectsMap = ((List<JsonObject>) subjectsResult.getList())
                .stream()
                .collect(Collectors.toMap(subject -> subject.getString("id"), Function.identity()));

        ((List<JsonObject>) courses.getList()).forEach(course -> {
                    course.put("subject", subjectsMap.getOrDefault
                            (course.getString("subjectId",
                                    course.getString("timetableSubjectId")), new JsonObject()));

                    //if subjectId exists but does not match any subject, use timetableSubjectId
                    if (course.getJsonObject("subject").isEmpty()) {
                        course.put("subject", subjectsMap.getOrDefault
                                (course.getString("timetableSubjectId"), new JsonObject()));
                    }
                }
        );
    }

    private void checkGroupFromClass(List<String> groupIds, List<String> groupExternalIds, List<String> group, String structureId,
                                     Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        query.append("MATCH (u:User {profiles:['Student']})--(:ProfileGroup)--(c:Class)--(s:Structure) ")
                .append("WHERE s.id = {idStructure} ")
                .append("AND (c.name IN {labelClasses} OR c.externalId IN {classesExternalIds} ")
                .append("OR c.id IN {classesIds}) ")
                .append("WITH u, c MATCH (u)--(g) WHERE g:FunctionalGroup OR g:ManualGroup ")
                .append("RETURN  c.name as name_classe, COLLECT(DISTINCT g.name) AS name_groups");
        params.put("idStructure", structureId);

        params.put("classesIds", (groupIds != null && !groupIds.isEmpty()) ? new JsonArray(groupIds) : new JsonArray());
        params.put("classesExternalIds", (groupExternalIds != null && !groupExternalIds.isEmpty()) ?
                new JsonArray(groupExternalIds) : new JsonArray());
        params.put("labelClasses", (group != null && !group.isEmpty()) ? new JsonArray(group) : new JsonArray());
        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(handler));

    }

    public void getCourse(String idCourse, Handler<Either<String, JsonObject>> handler) {
        final JsonObject query = new JsonObject();
        query.put(COURSE_TABLE._id, idCourse);
        MongoDb.getInstance().findOne(COURSES, query, KEYS, validResultHandler(handler));
    }

    @Override
    public void addCoursesTeachers(List<String> courseIds, List<String> teacherIds, Handler<Either<String, JsonObject>> handler) {

        JsonObject selectIds = new JsonObject()
                .put("_id", new JsonObject().put("$in", new JsonArray(courseIds)));

        JsonObject updatedCourse = new JsonObject()
                .put("$addToSet", new JsonObject().put("teacherIds", new JsonObject().put("$each", teacherIds)));

        mongoDb.update(COURSES, selectIds, updatedCourse, false, true, validResultHandler(handler));
    }

    @Override
    public void removeCoursesTeachers(List<String> courseIds, String teacherId, Handler<Either<String, JsonObject>> handler) {

        JsonObject selectIds = new JsonObject()
                .put("_id", new JsonObject().put("$in", new JsonArray(courseIds)));

        JsonObject updatedCourse = new JsonObject()
                .put("$pull", new JsonObject().put("teacherIds", teacherId));

        mongoDb.update(COURSES, selectIds, updatedCourse, false, true, validResultHandler(handler));
    }

    public void getCoursesByIds(List<String> courseIds, Handler<Either<String, JsonArray>> handler) {
        final JsonObject query = new JsonObject();
        query.put(COURSE_TABLE._id, new JsonObject().put("$in", new JsonArray(courseIds)));
        mongoDb.find(COURSES, query, new JsonObject(), KEYS, res -> {
            Either<String, JsonObject> either = MongoDbResult.validResult(res);
            if (either.isLeft()) {
                String message = "[Viescolaire@DefaultCommonCoursService::getCoursesByIds] " +
                        "Error fetching courses by ids";
                handler.handle(new Either.Left<>(message));
            } else {
                JsonArray courses = res.body().getJsonArray("results");
                setCoursesSubjects(courses, subjectRes -> {
                    if (subjectRes.isLeft()) {
                        handler.handle(new Either.Left<>(subjectRes.left().getValue()));
                    } else {
                        handler.handle(new Either.Right<>(courses));
                    }
                });
            }
        });
    }

    /**
     * Check if only one class and his group is called
     *
     * @param arrayGroups groups
     * @param teacherId   list of teacherId
     * @param groups      list of group
     * @return boolean
     */
    private boolean isOneClass(JsonArray arrayGroups, List<String> teacherId, List<String> groups) {
        if (!teacherId.isEmpty() || arrayGroups.size() > 1) {
            return false;
        }
        if (arrayGroups.isEmpty()) {
            return groups.size() == 1;
        }

        return true;
    }

    private static JsonObject formatOccurence(JsonObject course, boolean onlyOneGroup, Calendar start, Calendar end) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.format(start.getTime());

        JsonObject occurence = new JsonObject(course.toString());

        String color = "";
        if (!onlyOneGroup) {
            if (course.getJsonArray("classes") != null && course.getJsonArray("classes").size() > 0) {
                color = utilsService.getColor(course.getJsonArray("classes").getString(0));
            } else if (course.getJsonArray("groups") != null && course.getJsonArray("groups").size() > 0) {
                color = utilsService.getColor(course.getJsonArray("groups").getString(0));
            }
        }

        if (color.isEmpty() && course.containsKey("subjectId") && course.getString("subjectId") != null) {
            color = utilsService.getColor(course.getString("subjectId"));
        } else if (color.isEmpty() && course.containsKey("timetableSubjectId") && course.getString("timetableSubjectId") != null) {
            color = utilsService.getColor(course.getString("timetableSubjectId"));
        } else if (color.isEmpty() && course.containsKey("exceptionnal") && course.getString("exceptionnal") != null) {
            color = utilsService.getColor(course.getString("exceptionnal"));
        }

        occurence.put("color", color);

        occurence.put("is_periodic", false);
        occurence.put(COURSE_TABLE.startDate, df.format(start.getTime()));
        occurence.put(COURSE_TABLE.endDate, df.format(end.getTime()));
        occurence.put(COURSE_TABLE.startCourse, df.format(start.getTime()));
        occurence.put(COURSE_TABLE.endCourse, df.format(end.getTime()));
        if (course.getString("subjectId") == null && course.containsKey(COURSE_TABLE.exceptionnal)) {
            occurence.put(COURSE_TABLE.exceptionnal, course.getString(COURSE_TABLE.exceptionnal));
        }
        return occurence;
    }

    public static Date getDate(String dateString, SimpleDateFormat dateFormat) {
        Date date = new Date();
        try {
            date = dateFormat.parse(dateString);
        } catch (ParseException e) {
            String message = "[Viescolaire@DefaultCommonCoursService::getDate] Error " +
                    "formatting Date: '" + e.getMessage() + "', will continue proceeding";
            LOG.error(message);
        }
        return date;
    }

    private Calendar getCalendarDate(String stringDate, Handler<Either<String, JsonArray>> handler) {
        SimpleDateFormat formatter = new SimpleDateFormat(START_END_DATE_FORMAT);
        Date date = new Date();
        try {
            if (stringDate.matches(".*Z$")) {
                stringDate = stringDate.replaceAll("[.]\\d*Z", "");
            }
            date = formatter.parse(stringDate);
        } catch (ParseException e) {
            String message = "[Viescolaire@DefaultCommonCoursService::getCalendarDate] Error " +
                    "formatting Date: '" + e.getMessage() + "', will continue proceeding";
            LOG.error(message);
        }
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(date);
        return startCalendar;
    }

}

class Course {
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
    final String recurrence = "recurrence";
    final String timetableSubjectId = "timetableSubjectId";
}