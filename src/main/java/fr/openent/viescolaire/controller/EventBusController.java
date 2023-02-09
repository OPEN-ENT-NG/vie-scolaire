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

package fr.openent.viescolaire.controller;

import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.model.Person.Student;
import fr.openent.viescolaire.service.*;
import fr.openent.viescolaire.service.impl.*;
import fr.wseduc.bus.BusAddress;
import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.BooleanUtils;
import org.entcore.common.bus.BusResponseHandler;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.request.JsonHttpServerRequest;
import org.entcore.common.user.UserInfos;

import java.util.*;
import java.util.stream.Collectors;

import static fr.openent.Viescolaire.FORADMIN;
import static fr.openent.Viescolaire.ID_STRUCTURE_KEY;

public class EventBusController extends ControllerHelper {

    private MultiTeachingService mutliTeachingService;
    private GroupeService groupeService;
    private ClasseService classeService;
    private UserService userService;
    private EleveService eleveService;
    private MatiereService matiereService;
    private PeriodeService periodeService;
    private PeriodeAnneeService periodeAnneeService;
    private EventService eventService;
    private UtilsService utilsService;
    private CommonCoursService commonCoursService;
    private TimeSlotService timeSlotService;
    private ServicesService servicesService;
    private ConfigController configController;
    private GroupingService groupingService;

    public EventBusController(ServiceFactory serviceFactory, JsonObject _config) {
        groupeService = new DefaultGroupeService();
        classeService = new DefaultClasseService();
        userService = new DefaultUserService(serviceFactory.getEventbus());
        eleveService = new DefaultEleveService();
        matiereService = new DefaultMatiereService(serviceFactory.getEventbus());
        periodeService = new DefaultPeriodeService();
        periodeAnneeService = new DefaultPeriodeAnneeService();
        eventService = new DefaultEventService();
        utilsService = new DefaultUtilsService();
        commonCoursService = new DefaultCommonCoursService(serviceFactory.getEventbus());
        timeSlotService = new DefaultTimeSlotService(serviceFactory);
        configController = new ConfigController(_config);
        servicesService = new DefaultServicesService();
        mutliTeachingService = new DefaultMultiTeachingService(serviceFactory.getEventbus());
        groupingService = new DefaultGroupingService(serviceFactory);
    }

    @BusAddress("viescolaire")
    public void getData(final Message<JsonObject> message) {
        final String action = message.body().getString("action");

        if (action == null) {
            log.warn("[@BusAddress](viescolaire) Invalid action.");
            message.reply(new JsonObject().put("status", "error")
                    .put("message", "Invalid action."));
            return;
        }

        String service = action.split("\\.")[0];
        String method = action.split("\\.")[1];

        switch (service) {
            case "groupe": {
                groupeBusService(method, message);
            }
            break;
            case "classe": {
                classeBusService(method, message);
            }
            break;
            case "user": {
                userBusService(method, message);
            }
            break;
            case "eleve": {
                eleveBusService(method, message);
            }
            break;
            case "matiere": {
                matiereBusService(method, message);
            }
            break;
            case "periode": {
                periodeBusService(method, message);
            }
            break;
            case "structure": {
                structureBusService(method, message);
            }
            break;
            case "event": {
                eventBusService(method, message);
            }
            break;
            case "course": {
                courseBusService(method, message);
            }
            break;
            case "timeslot": {
                timeslotBusService(method, message);
            }
            break;
            case "config": {
                configBusService(method, message);
            }
            break;
            case "grouping": {
                groupingService(method, message);
            }
            break;
            case "service": {
                serviceBusService(method, message);
            }
            break;
            case "multiTeaching": {
                mutliTeachingService(method, message);
            }
        }
    }

    private void groupingService(String method, Message<JsonObject> message) {
        JsonObject body = message.body();
        String structureId = body.getString(Field.STRUCTUREID);

        switch (method) {
            case "getGroupingStructure": {
                groupingService.listGrouping(structureId)
                        .onSuccess(groupingList -> getJsonArrayBusResultHandler(message).handle(new Either.Right<>(groupingList)))
                        .onFailure(error -> {
                            String messageError = String.format("[Viescolaire@%s::getGroupingStructure] Error when get grouping list %s",
                                    this.getClass().getSimpleName(), error.getMessage());
                            log.error(messageError);
                            getJsonArrayBusResultHandler(message).handle(new Either.Left<>(error.getMessage()));
                        });
            }
            break;
        }
    }

    private void mutliTeachingService(String method, Message<JsonObject> message) {
        JsonObject body = message.body();
        String structureId = body.getString("structureId");
        switch (method) {
            case "getIdMultiTeachers": {
                String teacherId = body.getString("userId");
                String subjectId = body.getString("subjectId");
                String groupId = body.getString("groupId");
                mutliTeachingService.getSubTeachersandCoTeachers(teacherId, structureId, subjectId,
                        groupId, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getMultiTeachersByClass": {
                String groupId = body.getString("groupId");
                String periodId = body.getString("periodId");
                mutliTeachingService.getMultiTeachers(structureId, new JsonArray().add(groupId), periodId,
                        true, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getMultiteachers": {
                JsonArray groupIds = body.getJsonArray("groupIds");
                String periodId = body.getString("periodId");
                mutliTeachingService.getMultiTeachers(structureId, groupIds, periodId, true,
                        getJsonArrayBusResultHandler(message));
            }
            break;
            case "getAllMultiteachers": {
                JsonArray groupIds = body.getJsonArray("groupIds");
                mutliTeachingService.getAllMultiTeachers(structureId, groupIds, getJsonArrayBusResultHandler(message));
            }
            break;
            default: {
                message.reply(getErrorReply("Method not found"));
            }
        }
    }

    private void timeslotBusService(String method, Message<JsonObject> message) {
        JsonObject body = message.body();
        String structureId = body.getString("structureId");
        switch (method) {
            case "getSlotProfiles": {
                timeSlotService.getSlotProfiles(structureId, event -> {
                    if (event.isLeft()) {
                        message.reply(getErrorReply(event.left().getValue()));
                    } else {
                        if (event.right().getValue().size() == 0) {
                            message.reply(new JsonObject()
                                    .put("status", "ok")
                                    .put("result", new JsonObject()));
                            return;
                        }
                        String slotProfile = event.right().getValue().getJsonObject(0).getString("id");
                        JsonObject action = new JsonObject()
                                .put("action", "list-slots")
                                .put("slotProfileId", slotProfile);
                        eb.send("directory", action, directoryMessage -> {
                            String status = ((JsonObject) directoryMessage.result().body()).getString("status");
                            if ("error".equals(status)) {
                                message.reply(getErrorReply(directoryMessage.cause().getMessage()));
                            } else {
                                JsonObject timeslot = ((JsonObject) directoryMessage.result().body()).getJsonObject("result");
                                JsonArray slots = timeslot.getJsonArray("slots", new JsonArray());
                                List<JsonObject> sortedSlots = ((List<JsonObject>) slots.getList());
                                sortedSlots.sort((Comparator) (o, t1) -> ((JsonObject) o).getString("startHour").compareTo(((JsonObject) t1).getString("startHour")));
                                timeslot.put("slots", new JsonArray(sortedSlots));
                                JsonObject result = new JsonObject()
                                        .put("status", "ok")
                                        .put("result", timeslot);
                                message.reply(result);
                            }
                        });
                    }
                });
            }
            break;
            case "getDefaultSlots": {
                timeSlotService.getDefaultSlots(structureId, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getSlotProfileSettings": {
                timeSlotService.getSlotProfileSetting(structureId, getJsonObjectBusResultHandler(message));
            }
            break;
            case "getAudienceTimeslot": {
                JsonArray jsonAudienceId = body.getJsonArray(Field.AUDIENCEIDS, new JsonArray());
                List<String> audienceIds = (List<String>) jsonAudienceId.getList();
                List<String> timeslotsId = new ArrayList<>();
                Map<String, String> audiencesIdToClassId = new HashMap<>();
                Map<String, String> classIdToTimeslotId = new HashMap<>();
                Map<String, JsonObject> timeslotIdToTimeSlot = new HashMap<>();
                Map<String, String> params = new HashMap<>();

                //récupération des classes des audiences
                classeService.getClassesFromAudiences(audienceIds)
                        .compose(classes -> {
                            List<String> classeIds = new ArrayList<>();
                            ((List<JsonObject>) classes.getList())
                                    .forEach(classe -> {
                                        classeIds.add(classe.getString(Field.ID_CLASSES));
                                        audiencesIdToClassId.put(classe.getString(Field.ID_AUDIENCE), classe.getString(Field.ID_CLASSES));
                                    });
                            //récupération des assotiation classTimeslot
                            return timeSlotService.getSlotProfilesFromClasses(classeIds);
                        })
                        .compose(arrayTimeslotsId -> {
                            ((List<JsonObject>) arrayTimeslotsId.getList())
                                    .forEach(jsonObject -> {
                                        timeslotsId.add(jsonObject.getString(Field.ID_TIME_SLOT));
                                        classIdToTimeslotId.put(jsonObject.getString(Field.ID_CLASS), jsonObject.getString(Field.ID_TIME_SLOT));
                                    });
                            //récupération du timeslotId de la structure
                            return timeSlotService.getSlotProfiles(structureId);
                        })
                        .compose(structureTimeslot -> {
                            String structureTimeslotId = structureTimeslot.getString(Field.ID, "");
                            params.put(Field.TIMESLOTSTRUCTUREID, structureTimeslotId);
                            timeslotsId.add(structureTimeslotId);

                            //récupération des timeslot a partir de leur id
                            return timeSlotService.getMultipleTimeSlot(timeslotsId);
                        })
                        .onSuccess(arrayTimeSlot -> {
                            List<JsonObject> timeslots = new ArrayList<>();
                            ((List<JsonObject>) arrayTimeSlot.getList())
                                    .forEach(timeslot -> {
                                        timeslots.add(timeslot);
                                        timeslotIdToTimeSlot.put(timeslot.getString(Field._ID), timeslot);
                                    });

                            getJsonArrayBusResultHandler(message).handle(new Either.Right<>(new JsonArray(
                                    audienceIds.stream().map(audienceId -> {
                                        String classId = audiencesIdToClassId.getOrDefault(audienceId, "");
                                        String timeslotId = classIdToTimeslotId.getOrDefault(classId, "");
                                        JsonObject timeslotAudience = timeslotIdToTimeSlot.getOrDefault(timeslotId, new JsonObject());
                                        // if the audience has no class, or the class is not associated with a timeslot,
                                        // or the timeslot no longer exists, then we return the timeslot of its structure instead of audience
                                        if ((classId.isEmpty() || timeslotId.isEmpty() || timeslotAudience.isEmpty()) &&
                                                timeslotIdToTimeSlot.containsKey(params.get(Field.TIMESLOTSTRUCTUREID))) {
                                            return timeslotIdToTimeSlot.get(params.get(Field.TIMESLOTSTRUCTUREID)).copy().put(Field.AUDIENCEID, audienceId);
                                        }

                                        // we go in this case if this audience has its own timeslot
                                        return timeslotAudience.copy().put(Field.AUDIENCEID, audienceId);
                                    }).collect(Collectors.toList())
                            )));
                        })
                        .onFailure(err -> getJsonObjectBusResultHandler(message).handle(new Either.Left<>(err.getMessage())));
            }
            break;
            case "getTimeslotFromStudentIds": {
                List<Student> studentList = body.getJsonArray(Field.STUDENT_ID_LIST).stream()
                        .map(String.class::cast)
                        .map(Student::new)
                        .collect(Collectors.toList());

                this.timeSlotService.getTimeslotFromStudentId(studentList)
                        .onSuccess(mapStudentTimeslotModel -> {
                            JsonObject resultStudentTimeslot = new JsonObject();
                            mapStudentTimeslotModel.forEach((student, timeslotModel) ->
                                    resultStudentTimeslot.put(student.getId(), timeslotModel.toJson()));
                            getJsonObjectBusResultHandler(message).handle(new Either.Right<>(resultStudentTimeslot));
                        })
                        .onFailure(error -> {
                            String messageError = String.format("[Viescolaire@%s::getTimeslotFromStudentId] Error when get grouping list %s",
                                    this.getClass().getSimpleName(), error.getMessage());
                            log.error(messageError);
                            getJsonObjectBusResultHandler(message).handle(new Either.Left<>(messageError));
                        });
            }
        }
    }

    private void configBusService(String method, Message<JsonObject> message) {
        if ("generale".equals(method)) {
            message.reply(new JsonObject()
                    .put("status", "ok")
                    .put("result", configController.getConfig()));
        }
    }

    @SuppressWarnings("unchecked")
    private void courseBusService(String method, Message<JsonObject> message) {
        switch (method) {
            case "getCoursesOccurences": {
                JsonObject body = message.body();
                String structureId = body.getString("structureId");
                List<String> teacherId = body.getJsonArray("teacherId", new JsonArray()).getList();
                List<String> groupIds = body.getJsonArray("groupIds", new JsonArray()).getList();
                List<String> groupExternalIds = body.getJsonArray("groupExternalIds", new JsonArray()).getList();
                List<String> groupName = body.getJsonArray("group", new JsonArray()).getList();
                String beginDate = body.getString("begin");
                String endDate = body.getString("end");
                String startTime = body.getString("startTime");
                String endTime = body.getString("endTime");
                boolean crossDateFilter = Boolean.parseBoolean(body.getString("crossDateFilter"));
                boolean union = Boolean.parseBoolean(body.getString("union"));
                String limit = body.getString("limit");
                String offset = body.getString("offset");
                boolean descendingDate = Boolean.parseBoolean(body.getString("descendingDate"));
                Boolean searchTeacher = BooleanUtils.toBooleanObject(body.getString("searchTeacher")); // use this method to keep null status

                if (beginDate != null && endDate != null &&
                        beginDate.matches("\\d{4}-\\d{2}-\\d{2}") && endDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    commonCoursService.getCoursesOccurences(structureId, teacherId, groupIds, groupExternalIds,
                            groupName, beginDate, endDate, startTime, endTime, union, crossDateFilter, limit, offset, descendingDate, searchTeacher,
                            getJsonArrayBusResultHandler(message));
                }
                break;
            }
            case "getCoursesByIds":
                JsonObject body = message.body();
                commonCoursService.getCoursesByIds(body.getJsonArray("courseIds").getList(), BusResponseHandler.busArrayHandler(message));
                break;
        }
    }

    private void eventBusService(String method, Message<JsonObject> message) {
        switch (method) {
            case "add": {
                JsonObject formattedUser = message.body().getJsonObject("user");
                Number idRessource = message.body().getLong("idRessource");
                JsonObject ressource = message.body().getJsonObject("ressource");
                String event = message.body().getString("event");

                eventService.add(formattedUser, idRessource, ressource, event);
            }
            break;
        }
    }

    @SuppressWarnings("unchecked")
    private void groupeBusService(String method, Message<JsonObject> message) {
        switch (method) {
            case "listUsersByGroupeEnseignementId": {
                String groupeEnseignementId = message.body().getString("groupEnseignementId");
                String profile = message.body().getString("profile");
                Long idPeriode = message.body().getLong("idPeriode");
                groupeService.listUsersByGroupeEnseignementId(groupeEnseignementId, profile,
                        idPeriode, getJsonArrayBusResultHandler(message));
            }
            break;
            case "listGroupesEnseignementsByUserId": {
                String userId = message.body().getString("userId");
                groupeService.listGroupesEnseignementsByUserId(userId, getJsonArrayBusResultHandler(message));
            }
            break;
            case "search": {
                String query = message.body().getString(Field.Q);
                List<String> fields = message.body().getJsonArray(Field.FIELDS).getList();
                String structureId = message.body().getString(Field.STRUCTUREID);
                String userId = message.body().getString(Field.USERID);
                groupeService.search(structureId, userId, query, fields, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getGroupsTypes": {
                JsonArray groupsIds;
                if (message.body().containsKey("groupsIds")) {
                    groupsIds = message.body().getJsonArray("groupsIds");
                    groupeService.getTypesOfGroup(groupsIds, getJsonArrayBusResultHandler(message));
                }
            }
            break;
            default: {
                message.reply(getErrorReply("Method not found"));
            }
        }
    }

    private void classeBusService(String method, Message<JsonObject> message) {
        switch (method) {
            case "getNbElevesGroupe": {
                JsonArray idGroupes = message.body().getJsonArray("idGroupes");
                classeService.getNbElevesGroupe(idGroupes, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getEtabClasses": {
                String[] idClasses = convertJsonArrayToStringArray(message.body().getJsonArray("idClasses"));
                classeService.getEtabClasses(idClasses, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getClasseEtablissement": {
                String idEtablissement = message.body().getString("idEtablissement");
                classeService.getClasseEtablissement(idEtablissement, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getElevesClasses": {
                String[] idClasses = convertJsonArrayToStringArray(message.body().getJsonArray("idClasses"));
                Long idPeriode = message.body().getLong("idPeriode");
                classeService.getElevesClasses(idClasses, idPeriode, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getEleveClasse": {
                String idClasse = message.body().getString("idClasse");
                Long idPeriode = message.body().getLong("idPeriode");
                classeService.getEleveClasse(idClasse, idPeriode, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getClasseInfo": {
                String idClasse = message.body().getString("idClasse");
                classeService.getClasseInfo(idClasse, getJsonObjectBusResultHandler(message));
            }
            break;
            case "getClassesInfo": {
                JsonArray idClasses = message.body().getJsonArray("idClasses");
                classeService.getClassesInfo(idClasses, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getClasseIdByEleve": {
                String idEleve = message.body().getString("idEleve");
                classeService.getClasseIdByEleve(idEleve, getJsonObjectBusResultHandler(message));
            }
            break;
            case "listClasses": {
                String idEtablissement = message.body().getString(ID_STRUCTURE_KEY);
                boolean forAdmin = false;
                if (message.body().getBoolean(FORADMIN) != null) {
                    forAdmin = message.body().getBoolean(FORADMIN);
                }
                classeService.listClasses(idEtablissement, true, null, null, forAdmin,
                        getJsonArrayBusResultHandler(message), false);
            }
            break;
            case "listAllGroupes": {
                String idEtablissement = message.body().getString(ID_STRUCTURE_KEY);
                boolean forAdmin = false;
                if (message.body().getBoolean(FORADMIN) != null) {
                    forAdmin = message.body().getBoolean(FORADMIN);
                }
                classeService.listClasses(idEtablissement, null, null, null, forAdmin,
                        getJsonArrayBusResultHandler(message), false);
            }
            break;
            case "listAllGroupesByIds": {
                String idStructure = message.body().getString(ID_STRUCTURE_KEY);
                JsonArray idClassesAndGroups = message.body().getJsonArray("idClassesAndGroups");
                boolean forAdmin = false;
                if (message.body().getBoolean(FORADMIN) != null) {
                    forAdmin = message.body().getBoolean(FORADMIN);
                }
                classeService.listClasses(idStructure, null, null, idClassesAndGroups, forAdmin,
                        getJsonArrayBusResultHandler(message), false);
            }
            break;
            case "getGroupesClasse": {
                String[] idClasses = convertJsonArrayToStringArray(message.body().getJsonArray("idClasses"));
                classeService.getGroupeClasse(idClasses, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getEvaluableGroupsClasses" : {
                String[] idClasses = convertJsonArrayToStringArray(message.body().getJsonArray(Field.ID_CLASSES));
                classeService.getEvaluableGroupsClasses(idClasses, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getHeadTeachersClasse": {
                String idClasse = message.body().getString("idClasse");
                classeService.getHeadTeachers(idClasse, getJsonArrayBusResultHandler(message));
            }
            break;
            default: {
                message.reply(getErrorReply("Method not found"));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void userBusService(String method, Message<JsonObject> message) {
        switch (method) {
            case "getActivesStructure": {
                JsonArray structures = message.body().getJsonArray("structures");
                String module = message.body().getString("module");
                UserInfos user = new UserInfos();
                user.setStructures(structures.getList());
                userService.getActivesIDsStructures(user, module, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getMoyenne": {
                String idEleve = message.body().getString("idEleve");
                Long[] idDevoirs = convertJsonArrayToLongArray(message.body().getJsonArray("idDevoirs"));
                userService.getMoyenne(idEleve, idDevoirs, getJsonObjectBusResultHandler(message));
            }
            break;
            case "getUAI": {
                String idEtabl = message.body().getString("idEtabl");
                userService.getUAI(idEtabl, getJsonObjectBusResultHandler(message));
            }
            break;
            case "getUsers": {
                List<String> idsUsers = message.body().getJsonArray("idUsers").getList();
                userService.getUsers(idsUsers, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getUsersByTypeClassAndStructure": {
                String structureId = message.body().getString(Field.STRUCTUREID);
                String profile = message.body().getString(Field.PROFILE);
                userService.list(structureId, profile, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getElevesRelatives": {
                List<String> idsClass = message.body().getJsonArray("idsClass").getList();
                userService.getElevesRelatives(idsClass, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getCodeDomaine": {
                String idClass = message.body().getString("idClass");
                userService.getCodeDomaine(idClass, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getResponsablesDirection": {
                String idStructure = message.body().getString(ID_STRUCTURE_KEY);
                userService.getResponsablesDirection(idStructure, getJsonArrayBusResultHandler(message));
            }
            break;
            case "search": {
                String query = message.body().getString(Field.Q);
                List<String> fields = message.body().getJsonArray(Field.FIELDS).getList();
                String profile = message.body().getString(Field.PROFILE);
                String structureId = message.body().getString(Field.STRUCTUREID);
                String userId = message.body().getString(Field.USERID);
                userService.search(structureId, userId, query, fields, profile, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getAllElevesWithTheirRelatives": {
                String idStructure = message.body().getString((ID_STRUCTURE_KEY));
                List<String> idsClass = message.body().getJsonArray("idsClass").getList();
                List<String> idsDletedElevePostgres = message.body().getJsonArray("idsDeletedStudent").getList();
                userService.getAllElevesWithTheirRelatives(idStructure, idsClass, idsDletedElevePostgres, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getDeletedTeachers": {
                List<String> idsTeacher = message.body().getJsonArray("idsTeacher").getList();
                userService.getDeletedTeachers(idsTeacher, getJsonArrayBusResultHandler(message));
            }
            break;
            default: {
                message.reply(getErrorReply("Method not found"));
            }
        }
    }


    private void serviceBusService(String method, Message<JsonObject> message) {
        switch (method) {
            case "getServices": {
                String idStructure = message.body().getString("idStructure");
                JsonArray aIdEnseignant = message.body().getJsonArray("aIdEnseignant");
                JsonArray aIdMatiere = message.body().getJsonArray("aIdMatiere");
                JsonArray aIdGroupe = message.body().getJsonArray("aIdGroupe");
                Boolean bEvaluable = message.body().getBoolean("evaluable");

                JsonObject oService = new JsonObject();

                if (aIdGroupe != null) {
                    oService.put("id_groupe", aIdGroupe);
                }

                if (aIdEnseignant != null) {
                    oService.put("id_enseignant", aIdEnseignant);
                }

                if (aIdMatiere != null) {
                    oService.put("id_matiere", aIdMatiere);
                }

                oService.put("evaluable", bEvaluable);

                servicesService.getServicesSQL(idStructure, oService, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getDefaultServices": {
                if (message.body().getValue("idEtablissement") == null) {
                    message.reply(getErrorReply("Error : idEtablissement should be provided."));
                } else {
                    String structureId = message.body().getString("idEtablissement");

                    JsonArray idsEnseignant = message.body().getJsonArray("idsEnseignant", null);
                    JsonArray idsMatiere = message.body().getJsonArray("idsMatiere", null);
                    JsonArray idsGroupe = message.body().getJsonArray("idsGroupe", null);
                    JsonObject filters = message.body().getJsonObject("filters", null);
                    Boolean evaluable = true; // select evaluable services
                    Boolean noEvaluable = true; // select no evaluable services
                    Boolean classes = true; // select services with type class
                    Boolean groups = true; // select services with type group
                    Boolean manualGroups = true; //select services with type manual
                    Boolean compressed = false; // select service subject-teacher-[all class or/and groups]
                    if (filters != null) {
                        if (filters.containsKey("evaluable")) evaluable = filters.getBoolean("evaluable");
                        if (filters.containsKey("noEvaluable")) noEvaluable = filters.getBoolean("noEvaluable");
                        if (filters.containsKey("classes")) classes = filters.getBoolean("classes");
                        if (filters.containsKey("groups")) groups = filters.getBoolean("groups");
                        if (filters.containsKey("manualGroups")) manualGroups = filters.getBoolean("manualGroups");
                        if (filters.containsKey("compressed")) compressed = filters.getBoolean("compressed");
                    }

                    JsonObject oService = new JsonObject();

                    if (idsGroupe != null) {
                        oService.put("id_groupe", idsGroupe);
                    }
                    if (idsMatiere != null) {
                        oService.put("id_matiere", idsMatiere);
                    }
                    if (idsEnseignant != null) {
                        oService.put("id_enseignant", idsEnseignant);
                    }
                    servicesService.getAllServices(structureId, evaluable, noEvaluable, classes, groups, manualGroups,
                            compressed, oService, getJsonArrayBusResultHandler(message));
                }
            }
            break;
            default: {
                message.reply(getErrorReply("Method not found"));
            }
        }
    }

    private void eleveBusService(String method, Message<JsonObject> message) {
        switch (method) {
            case "getUsers": {
                JsonArray idUsers = message.body().getJsonArray("idUsers");
                eleveService.getUsers(idUsers, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getInfoEleve": {
                String[] idEleves = convertJsonArrayToStringArray(message.body().getJsonArray("idEleves"));
                String idEtablissement = message.body().getString("idEtablissement");
                eleveService.getInfoEleve(idEleves, idEtablissement, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getCycle": {
                String idClasse = message.body().getString("idClasse");
                eleveService.getCycle(idClasse, getJsonArrayBusResultHandler(message));
            }
            break;
            case "isEvaluableOnPeriode": {
                String idEleve = message.body().getString("idEleve");
                Long idPeriode = message.body().getLong("idPeriode");
                String idEtablissement = message.body().getString("idEtablissement");
                eleveService.isEvaluableOnPeriode(idEleve, idPeriode, idEtablissement,
                        getJsonArrayBusResultHandler(message));
            }
            break;
            case "getResponsables": {
                String idEleve = message.body().getString("idEleve");
                eleveService.getResponsable(idEleve, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getEleveClasse": {
                String idClass = message.body().getString("idClass");
                eleveService.getEleveClasse(idClass, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getGroups": {
                String idEleve = message.body().getString("idEleve");
                eleveService.getGroups(idEleve, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getDeletedStudentByPeriodeByClass": {
                String idClass = message.body().getString("idClass");
                String beginningPeriode = message.body().getString("beginningPeriode");
                eleveService.getDeletedStudentByPeriodeByClass(idClass, beginningPeriode, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getAnnotations": {
                String idEleve = message.body().getString("idEleve");
                Long idPeriode = message.body().getLong("idPeriode");
                String idMatiere = message.body().containsKey("idMatiere") ? message.body().getString("idMatiere") : null;
                eleveService.getAnnotations(idEleve, idPeriode, null, idMatiere, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getCompetences": {
                String idEleve = message.body().getString("idEleve");
                Long idPeriode = message.body().getLong("idPeriode");
                String idMatiere = message.body().containsKey("idMatiere") ? message.body().getString("idMatiere") : null;
                JsonArray idGroups = message.body().getJsonArray("idGroups");
                eleveService.getCompetences(idEleve, idPeriode, idGroups, null, idMatiere,
                        getJsonArrayBusResultHandler(message));
            }
            break;
            case "getPrimaryRelatives": {
                JsonArray studentIds = message.body().getJsonArray("studentIds");
                eleveService.getPrimaryRelatives(studentIds, getJsonArrayBusResultHandler(message));
            }
            break;
            default: {
                message.reply(getErrorReply("Method not found"));
            }
        }
    }

    private void structureBusService(String method, Message<JsonObject> message) {
        switch (method) {
            case "getStructuresActives": {
                String module = message.body().getString("module");
                userService.getActivesIDsStructures(module, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getStructure": {
                String idStructure = message.body().getString(ID_STRUCTURE_KEY);
                utilsService.getStructure(idStructure, getJsonObjectBusResultHandler(message));
            }
            break;
            case "getAllStructures": {
                utilsService.getStructures(getJsonArrayBusResultHandler(message));
            }
            break;
            default: {
                message.reply(getErrorReply("Method not found"));
            }
        }
    }

    private void matiereBusService(String method, final Message<JsonObject> message) {
        switch (method) {
            case "getSubjectsAndTimetableSubjects": {
                String structureId = message.body().getString("structureId");
                if (structureId != null) {
                    matiereService.getSubjectsAndTimetableSubjects(structureId, getJsonArrayBusResultHandler(message));
                    return;
                }
                JsonArray idMatieres = message.body().getJsonArray("idMatieres");
                matiereService.getSubjectsAndTimetableSubjects(idMatieres, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getMatieres": {
                JsonArray idMatieres = message.body().getJsonArray("idMatieres");
                matiereService.getMatieres(idMatieres, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getMatiere": {
                String idMatiere = message.body().getString("idMatiere");
                matiereService.getMatiere(idMatiere, getJsonObjectBusResultHandler(message));
            }
            break;
            case "getMatieresForUser": {
                final String userType = message.body().getString("userType");
                final String idEnseignant = message.body().getString("idUser");
                final String idStructure = message.body().getString(ID_STRUCTURE_KEY);
                final Boolean onlyId = message.body().containsKey("onlyId") ? message.body().getBoolean("onlyId") : false;
                if ("Personnel".equals(userType)) {
                    matiereService.listMatieresEtabWithSousMatiere(idStructure, onlyId, getJsonArrayBusResultHandler(message));
                } else {
                    matiereService.listAllMatieres(idStructure, idEnseignant, onlyId, getJsonArrayBusResultHandler(message));
                }
            }
            break;
            case "getAllMatieresEnseignants": {
                final String idStructure = message.body().getString(ID_STRUCTURE_KEY);
                matiereService.listMatieres(idStructure, null, null, null, getJsonArrayBusResultHandler(message));
            }
            break;
            case "listMatieresEtab": {
                final String idStructure = message.body().getString(ID_STRUCTURE_KEY);
                final Boolean onlyId = message.body().getBoolean("onlyId");
                matiereService.listMatieresEtab(idStructure, onlyId, getJsonArrayBusResultHandler(message));
            }
            break;
            default: {
                message.reply(getErrorReply("Method not found"));
            }
        }
    }


    private void periodeBusService(String method, final Message<JsonObject> message) {
        switch (method) {
            case "getLibellePeriode": {
                if (message.body().containsKey("ordre")) {
                    Integer type = message.body().getInteger("type");
                    Integer ordre = message.body().getInteger("ordre");
                    JsonObject request = message.body().getJsonObject("request");

                    String libelle = periodeService.getLibellePeriode(type, ordre, new JsonHttpServerRequest(request));
                    message.reply(new JsonObject()
                            .put("status", "ok")
                            .put("result", libelle));
                } else {
                    Integer idType = message.body().getInteger("idType") == null
                            ? 0
                            : message.body().getInteger("idType");
                    JsonObject request = message.body().getJsonObject("request");
                    periodeService.getLibellePeriode(Long.parseLong(idType.toString()),
                            new JsonHttpServerRequest(request), new Handler<Either<String, String>>() {
                                @Override
                                public void handle(Either<String, String> res) {
                                    if (res.isRight()) {
                                        message.reply(new JsonObject()
                                                .put("status", "ok")
                                                .put("result", res.right().getValue()));
                                    } else {
                                        message.reply(getErrorReply(res.left().getValue()));
                                    }
                                }
                            });
                }
            }
            break;
            case "getPeriodes": {
                List<String> l = message.body().getJsonArray("idGroupes").getList();
                String[] idGroupes = l.toArray(new String[l.size()]);
                String idEtablissement = message.body().getString("idEtablissement");

                JsonObject request = message.body().getJsonObject("request");
                periodeService.getPeriodes(idEtablissement, idGroupes,
                        new Handler<Either<String, JsonArray>>() {
                            @Override
                            public void handle(Either<String, JsonArray> res) {
                                if (res.isRight()) {
                                    message.reply(new JsonObject()
                                            .put("status", "ok")
                                            .put("result", res.right().getValue()));
                                } else {
                                    message.reply(getErrorReply(res.left().getValue()));
                                }
                            }
                        });
            }
            break;
            case "getDatesDtFnAnneeByClasse": {
                List<String> idClasses = message.body().getJsonArray("idClasses").getList();
                String idEtablissement = message.body().getString("idEtablissement");
                periodeService.getDatesDtFnAnneeByClasse(idEtablissement, idClasses, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getExclusionDays": {
                String idEtablissement = message.body().getString("idEtablissement");
                periodeAnneeService.listExclusion(idEtablissement, getJsonArrayBusResultHandler(message));
            }
            break;
            case "getSchoolYearPeriod":
                String structureId = message.body().getString("structureId");
                periodeAnneeService.getPeriodeAnnee(structureId, BusResponseHandler.busResponseHandler(message));
                break;
            case "getPeriodesClasses": {
                String idEtablissement = message.body().getString("idEtablissement");
                String[] idsClasse = convertJsonArrayToStringArray(message.body().getJsonArray("idsClasse"));
                Long idPeriode = message.body().getLong("idPeriode");
                periodeService.getPeriodesClasses(idEtablissement, idsClasse, idPeriode, getJsonArrayBusResultHandler(message));
            }
            break;
            default: {
                message.reply(getErrorReply("Method not found"));
            }
        }
    }

    private Handler<Either<String, JsonArray>> getJsonArrayBusResultHandler(final Message<JsonObject> message) {
        return new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> result) {
                if (result.isRight()) {
                    message.reply(new JsonObject()
                            .put("status", "ok")
                            .put("results", result.right().getValue()));
                } else {
                    message.reply(getErrorReply(result.left().getValue()));
                }
            }
        };
    }

    private Handler<Either<String, JsonObject>> getJsonObjectBusResultHandler(final Message<JsonObject> message) {
        return new Handler<Either<String, JsonObject>>() {
            @Override
            public void handle(Either<String, JsonObject> result) {
                if (result.isRight()) {
                    message.reply(new JsonObject()
                            .put("status", "ok")
                            .put("result", result.right().getValue()));
                } else {
                    message.reply(getErrorReply(result.left().getValue()));
                }
            }
        };
    }

    private JsonObject getErrorReply(String message) {
        return new JsonObject()
                .put("status", "error")
                .put("message", message);
    }

    private String[] convertJsonArrayToStringArray(JsonArray list) {
        String[] objects = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            objects[i] = list.getString(i);
        }

        return objects;
    }

    private Long[] convertJsonArrayToLongArray(JsonArray list) {
        Long[] objects = new Long[list.size()];
        for (int i = 0; i < list.size(); i++) {
            objects[i] = list.getLong(i);
        }

        return objects;
    }

    ;
}
