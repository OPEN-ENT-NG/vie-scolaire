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

package fr.openent.viescolaire.service;

import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public interface CommonCoursService {

    /**
     * fetch courses in mongoDB
     *
     * @param structureId       structure identifier
     * @param teacherId         teacher identifier list
     * @param group             Event type list
     * @param begin             start date begin
     * @param end               end date begin
     * @param startTime         start time begin
     * @param endTime           end time begin
     * @param union             union filter way mode$or for OR and $and for and
     * @param limit             limit filter
     * @param offset            offset filter (limit should be included)
     * @param descendingDate    for descending we set TRUE (-1) date else we keep (FALSE) ascending (default is 1)
     * @param handler           Function handler returning data
     */
    void listCoursesBetweenTwoDates(String structureId, List<String> teacherId, List<Long> groupIds,
                                    List<String> groupExternalIds, List<String> groupNames, String begin, String end,
                                    String startTime, String endTime, boolean union, boolean crossDateFilter,
                                    String limit, String offset, boolean descendingDate, Boolean searchTeacher,
                                    Handler<Either<String,JsonArray>> handler);

    /**
     * Get courses occurences
     *
     * @param structureId       structure identifier
     * @param teacherId         teacher identifier list
     * @param group             Event type list
     * @param begin             start date begin
     * @param end               end date begin
     * @param startTime         start time begin
     * @param endTime           end time begin
     * @param union             union filter way mode$or for OR and $and for and
     * @param crossDateFilter   cross date filter (true : get courses beginning < start date and finishing end date)
     * @param handler           Function handler returning data
     */
    void getCoursesOccurences(String structureId, List<String> teacherId, List<String> group, String begin, String end,
                              String startTime, String endTime, boolean union, boolean crossDateFilter,
                              Handler<Either<String,JsonArray>> handler);

    /**
     * Get courses occurences (pagination/limit/offset included)
     *
     * @param structureId       structure identifier
     * @param teacherId         teacher identifier list
     * @param group             Event type list
     * @param begin             start date begin
     * @param end               end date begin
     * @param startTime         start time begin
     * @param endTime           end time begin
     * @param union             union filter way mode$or for OR and $and for and
     * @param limit             limit filter
     * @param offset            offset filter (limit should be included)
     * @param descendingDate    for descending we set TRUE (-1) date else we keep (FALSE) ascending (default is 1)
     * @param handler           Function handler returning data
     */
    void getCoursesOccurences(String structureId, List<String> teacherId, List<String> group, String begin, String end,
                              String startTime, String endTime, boolean union, boolean crossDateFilter, String limit, String offset,
                              boolean descendingDate, Boolean searchTeacher, Handler<Either<String,JsonArray>> handler);

    void getCoursesOccurences(String structureId, List<String> teacherId, List<Long> groupIds, List<String> groupExternalIds,
                              List<String> group, String begin, String end, String startTime, String endTime,
                              boolean union, boolean crossDateFilter, String limit, String offset,
                              boolean descendingDate, Boolean searchTeacher, Handler<Either<String,JsonArray>> handler);

    void getCourse(String idCourse, Handler<Either<String,JsonObject>> handler);

    void getCoursesByIds(List<String> courseIds, Handler<Either<String,JsonArray>> handler);

}
