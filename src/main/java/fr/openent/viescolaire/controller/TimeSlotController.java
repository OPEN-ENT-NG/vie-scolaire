package fr.openent.viescolaire.controller;

import fr.openent.viescolaire.core.constants.Actions;
import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.helper.UserHelper;
import fr.openent.viescolaire.security.*;
import fr.openent.viescolaire.service.ClasseService;
import fr.openent.viescolaire.service.ServiceFactory;
import fr.openent.viescolaire.service.TimeSlotService;
import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.I18n;
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.request.RequestUtils;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.http.filter.Trace;
import org.entcore.common.http.response.DefaultResponseHandler;

import java.util.HashMap;
import java.util.Map;

import static fr.openent.Viescolaire.DIRECTORY_ADDRESS;
import static fr.wseduc.webutils.Utils.handlerToAsyncHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;

public class TimeSlotController extends ControllerHelper {

    private static final I18n i18n = I18n.getInstance();
    private final TimeSlotService timeSlotService;
    private final ClasseService classeService;

    public TimeSlotController(ServiceFactory serviceFactory) {
        this.timeSlotService = serviceFactory.timeSlotService();
        this.classeService = serviceFactory.classeService();
    }

    @Get("/time-slots")
    @SecuredAction(value = WorkflowActionUtils.TIME_SLOTS_READ, type = ActionType.WORKFLOW)
    public void getSlotProfilesByStructure(final HttpServerRequest request) {
        final String structureId = request.params().get("structureId");
        if (structureId == null) {
            String errorMessage = i18n.translate(
                    "directory.slot.bad.request.invalid.structure",
                    Renders.getHost(request),
                    I18n.acceptLanguage(request));
            badRequest(request, errorMessage);
            return;
        }
        JsonObject action = new JsonObject()
                .put("action", "list-slotprofiles")
                .put("structureId", structureId);

        Handler<Either<String, JsonArray>> handler = DefaultResponseHandler.arrayResponseHandler(request);
        eb.send(DIRECTORY_ADDRESS, action, handlerToAsyncHandler(new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> message) {
                JsonObject body = message.body();
                if ("ok".equals(body.getString("status"))) {
                    JsonArray slotProfiles = body.getJsonArray("result");
                    timeSlotService.getSlotProfiles(structureId, event -> {
                        if (event.isLeft()) {
                            log.error(event.left().getValue());
                            renderError(request);
                        } else {
                            JsonArray slots = event.right().getValue();
                            if (slots.isEmpty()) {
                                renderJson(request, slotProfiles);
                                return;
                            }

                            String id = slots.getJsonObject(0).getString("id");
                            String endHalfDay = slots.getJsonObject(0).getString("end_of_half_day");
                            for (int i = 0; i < slotProfiles.size(); i++) {
                                if (slotProfiles.getJsonObject(i).getString("_id").equals(id)) {
                                    slotProfiles.getJsonObject(i).put("end_of_half_day", endHalfDay);
                                    slotProfiles.getJsonObject(i).put("default", true);
                                    renderJson(request, slotProfiles);
                                    return;
                                }
                            }
                            renderJson(request, slotProfiles);
                        }
                    });
                } else {
                    badRequest(request);
                }
            }
        }));
    }

    /**
     * Sauvegarder un profil de plage horaire avec les données passées en POST
     *
     * @param request
     */

    @Post("/time-slots")
    @SecuredAction(value = WorkflowActionUtils.TIME_SLOTS_MANAGE, type = ActionType.WORKFLOW)
    @ApiDoc("Save a profile slot")
    public void saveTimeProfil(final HttpServerRequest request) {
        RequestUtils.bodyToJson(request,
                timeSlot -> {
                    timeSlotService.saveTimeProfil(timeSlot, arrayResponseHandler(request));
                });
    }

    @Get("/structures/:id/time-slot")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AccessIfMyStructureCheckIdParam.class)
    @ApiDoc("Retrieve default structure time slot")
    public void getDefaultStructureTimeSlot(final HttpServerRequest request) {
        String structureId = request.getParam("id");

        timeSlotService.getSlotProfiles(structureId, either -> {
            if (either.isLeft()) {
                log.error("[Viescolaire@TimeSlotController] Failed to retrieve default structure time slot", either.left().getValue());
                renderError(request, new JsonObject().put(Field.ERROR, either.left().getValue()));
                return;
            }

            JsonArray slots = either.right().getValue();
            if (slots.isEmpty()) {
                renderJson(request, new JsonObject());
                return;
            }

            JsonObject setting = slots.getJsonObject(0);
            timeSlotService.getDefaultTimeSlot(setting.getString("id"), defaultResponseHandler(request));
        });
    }

    /**
     * @param request
     * @queryParam {structureId} mandatory
     */
    @Put("/time-slots")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AdminRightStructure.class)
    @ApiDoc("Update forgotten notebook")
    public void update(final HttpServerRequest request) {
        if (!request.params().contains(Field.ID)) {
            badRequest(request);
            return;
        }
        String timeSlotId = request.getParam(Field.ID);
        RequestUtils.bodyToJson(request, timeSlotBody -> {
            if (!timeSlotBody.containsKey(Field.TIME) && !timeSlotBody.containsKey(Field.STRUCTUREID)) {
                badRequest(request);
                return;
            }
            String time = timeSlotBody.getString(Field.TIME);
            String structureId = timeSlotBody.getString(Field.STRUCTUREID);
            timeSlotService.updateEndOfHalfDay(timeSlotId, time, structureId, DefaultResponseHandler.defaultResponseHandler(request));
        });
    }

    /**
     * Get timeslot from audience
     */
    @Get("/timeslot/audience/:audienceId")
    @ApiDoc("Get timeslot from audience")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getAudienceTimeslot(final HttpServerRequest request) {
        String audienceId = request.getParam(Field.AUDIENCEID);

        if (audienceId != null && !audienceId.isEmpty()) {
            Map<String, Object> params = new HashMap<>();
            classeService.getClasseIdFromAudience(audienceId)
                    .compose(classId -> {
                        params.put(Field.CLASSID, classId);
                        if (classId.isEmpty()) {
                            badRequest(request, Field.AUDIENCE_CLASS_NOT_FOUND);
                            params.put(Field.ISRESPONSE, Boolean.TRUE);
                            return Future.failedFuture("");
                        }
                        return classeService.getEtabClasses(classId);
                    })
                    .compose(etabInfo -> {
                        if (etabInfo.isEmpty()) {
                            badRequest(request, Field.STRUCTURE_TIMESLOT_NOT_FOUND);
                            params.put(Field.ISRESPONSE, Boolean.TRUE);
                            return Future.failedFuture("");
                        }
                        String classStructureId = etabInfo.getJsonObject(0).getString(Field.IDSTRUCTURE);
                        params.put(Field.STRUCTUREID, classStructureId);
                        return UserHelper.getUserInfos(eb, request);
                    })
                    .compose(userInfo -> {
                        if (userInfo.getStructures().contains(params.get(Field.STRUCTUREID))) {
                            return timeSlotService.getTimeSlot((String) params.get(Field.CLASSID), (String) params.get(Field.STRUCTUREID));
                        }
                        unauthorized(request, Field.USER_NOT_IN_AUDIENCE_STRUCTURE);
                        params.put(Field.ISRESPONSE, Boolean.TRUE);
                        return Future.failedFuture("");
                    })
                    .onSuccess(jsonObject -> renderJson(request, jsonObject))
                    .onFailure(err -> {
                        if (!params.containsKey(Field.ISRESPONSE)) {
                            log.error("[Viescolaire@TimeSlotController] Failed to retrieve timeslot from audience", err.getMessage());
                            renderError(request, new JsonObject().put(Field.ERROR, err.getMessage()));
                        }
                    });
        } else {
            badRequest(request);
        }
    }

    /**
     * Get class from timeslot
     */
    @Get("/timeslot/:timeslotId")
    @ApiDoc("Get class from timeslot")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(TimeSlotsRead.class)
    public void getAllClassFromTimeslot(final HttpServerRequest request) {
        String timeslotId = request.getParam(Field.TIMESLOTID);

        if (timeslotId != null && !timeslotId.isEmpty()) {
            Map<String, Object> params = new HashMap<>();
            timeSlotService.getDefaultTimeSlot(timeslotId)
                    .compose(timeslotInfo -> {
                        if (timeslotInfo.getString(Field.SCHOOLID, null) == null) {
                            badRequest(request, Field.TIMESLOT_NOT_FOUND);
                            params.put(Field.ISRESPONSE, Boolean.TRUE);
                            return Future.failedFuture("");
                        }
                        String structureId = timeslotInfo.getString(Field.SCHOOLID);
                        params.put(Field.SCHOOLID, structureId);
                        return UserHelper.getUserInfos(eb, request);
                    })
                    .compose(userInfos -> {
                        if (userInfos.getStructures().contains(params.get(Field.SCHOOLID))) {
                            return classeService.getClassIdFromTimeslot(timeslotId);
                        }
                        unauthorized(request, Field.USER_NOT_IN_TIMESLOT_STRUCTURE);
                        params.put(Field.ISRESPONSE, Boolean.TRUE);
                        return Future.failedFuture("");
                    })
                    .onSuccess(jsonObject -> renderJson(request, jsonObject))
                    .onFailure(err -> {
                        if (!params.containsKey(Field.ISRESPONSE)) {
                            log.error("[Viescolaire@TimeSlotController] Failed to retrieve class from timeslot", err.getMessage());
                            renderError(request, new JsonObject().put(Field.ERROR, err.getMessage()));
                        }
                    });
        } else {
            badRequest(request);
        }
    }

    /**
     * Create or update class/timeslot associations
     */
    @Post("/timeslot/audience")
    @ApiDoc("Create or update class/timeslot associations")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(TimeSlotsManage.class)
    public void createOrUpdateClassTimeslot(final HttpServerRequest request) {
        RequestUtils.bodyToJson(request, pathPrefix + "rel_time_slot_class_create", body -> {
            String classId = body.getString(Field.CLASS_ID);
            String timeslotId = body.getString(Field.TIMESLOT_ID);

            Map<String, Object> params = new HashMap<>();
            classeService.getClasseInfo(classId)
                    .compose(classInfo -> {
                        if (classInfo.isEmpty() || !classInfo.getJsonObject(Field.CLASS_SHORT, new JsonObject())
                                .getJsonObject(Field.METADATA, new JsonObject())
                                .getJsonArray(Field.LABELS, new JsonArray())
                                .getList()
                                .contains(Field.CLASS)) {
                            badRequest(request, Field.CLASS_NOT_FOUND);
                            params.put(Field.ISRESPONSE, Boolean.TRUE);
                            return Future.failedFuture("");
                        }
                        return classeService.getEtabClasses(classId);
                    })
                    .compose(etabInfo -> {
                        if (etabInfo.isEmpty()) {
                            badRequest(request, Field.STRUCTURE_TIMESLOT_NOT_FOUND);
                            params.put(Field.ISRESPONSE, Boolean.TRUE);
                            return Future.failedFuture("");
                        }
                        String classStructureId = etabInfo.getJsonObject(0).getString(Field.IDSTRUCTURE);
                        params.put(Field.STRUCTUREID, classStructureId);
                        return timeSlotService.getStructureFromTimeSlot(timeslotId);
                    })
                    .compose(timeslotStructureId -> {
                        if (timeslotStructureId.isEmpty()) {
                            badRequest(request, Field.TIMESLOT_NOT_FOUND);
                            params.put(Field.ISRESPONSE, Boolean.TRUE);
                            Future.failedFuture("");
                        }
                        params.put(Field.TIMESLOTSTRUCTUREID, timeslotStructureId);
                        return UserHelper.getUserInfos(eb, request);
                    })
                    .compose(userInfos -> {
                        if (userInfos.getStructures().contains(params.get(Field.TIMESLOTSTRUCTUREID)) &&
                                params.get(Field.TIMESLOTSTRUCTUREID).equals(params.get(Field.STRUCTUREID))) {
                            return timeSlotService.setTimeSlotFromAudience(classId, timeslotId);
                        }
                        unauthorized(request, Field.USER_TIMESLOT_CLASS_NOT_SAME_STRUCTURE);
                        params.put(Field.ISRESPONSE, Boolean.TRUE);
                        return Future.failedFuture("");
                    })
                    .onSuccess(res -> noContent(request))
                    .onFailure(err -> {
                        if (!params.containsKey(Field.ISRESPONSE)) {
                            log.error("[Viescolaire@TimeSlotController] Failed to set timeslot to a class", err.getMessage());
                            renderError(request, new JsonObject().put(Field.ERROR, err.getMessage()));
                        }
                    });
        });
    }

    /**
     * Delete class/timeslot associations for a class
     */
    @Delete("/timeslot/audience/:classId")
    @ApiDoc("Delete class/timeslot associations for a class")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(TimeSlotsManage.class)
    @Trace(Actions.VIESCOLAIRE_TIMESLOT_CLASS_ASSOCIATION_DELETE)
    public void deleteClassTimeslot(final HttpServerRequest request) {
        String classId = request.getParam(Field.CLASSID);

        if (classId != null && !classId.isEmpty()) {
            Map<String, Object> params = new HashMap<>();
            classeService.getClasseInfo(classId)
                    .compose(classInfo -> {
                        if (classInfo.isEmpty() || !classInfo.getJsonObject(Field.CLASS_SHORT)
                                .getJsonObject(Field.METADATA)
                                .getJsonArray(Field.LABELS)
                                .getList()
                                .contains(Field.CLASS)) {
                            badRequest(request, Field.CLASS_NOT_FOUND);
                            params.put(Field.ISRESPONSE, Boolean.TRUE);
                            Future.failedFuture("");
                        }
                        return classeService.getEtabClasses(classId);
                    })
                    .compose(etabInfo -> {
                        if (etabInfo.isEmpty()) {
                            badRequest(request, Field.STRUCTURE_TIMESLOT_NOT_FOUND);
                            params.put(Field.ISRESPONSE, Boolean.TRUE);
                            return Future.failedFuture("");
                        }
                        String classStructureId = etabInfo.getJsonObject(0).getString(Field.IDSTRUCTURE);
                        params.put(Field.STRUCTUREID, classStructureId);
                        return UserHelper.getUserInfos(eb, request);
                    })
                    .compose(userInfos -> {
                        if (userInfos.getStructures().contains(params.get(Field.STRUCTUREID))) {
                            return timeSlotService.deleteTimeSlotFromClass(classId);
                        }
                        unauthorized(request, Field.USER_NOT_IN_AUDIENCE_STRUCTURE);
                        params.put(Field.ISRESPONSE, Boolean.TRUE);
                        return Future.failedFuture("");
                    })
                    .onSuccess(res -> noContent(request))
                    .onFailure(err -> {
                        if (!params.containsKey(Field.ISRESPONSE)) {
                            log.error("[Viescolaire@TimeSlotController] Failed to delete timeslot/class association from class", err.getMessage());
                            renderError(request, new JsonObject().put(Field.ERROR, err.getMessage()));
                        }
                    });
        } else {
            badRequest(request);
        }
    }

    /**
     * Delete all class/timeslot associations from a timeslot
     */
    @Delete("/timeslot/:timeslotId")
    @ApiDoc("Delete all class/timeslot associations from a timeslot")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(TimeSlotsManage.class)
    @Trace(Actions.VIESCOLAIRE_TIMESLOT_CLASS_ASSOCIATION_DELETE_MULTIPLE)
    public void deleteAllAudienceFromTimeslot(final HttpServerRequest request) {
        String timeslotId = request.getParam(Field.TIMESLOTID);

        if (timeslotId != null && !timeslotId.isEmpty()) {
            Map<String, Object> params = new HashMap<>();
            timeSlotService.getDefaultTimeSlot(timeslotId)
                    .compose(timeslotInfo -> {
                        if (timeslotInfo.getString(Field.SCHOOLID, null) == null) {
                            badRequest(request, Field.TIMESLOT_NOT_FOUND);
                            params.put(Field.ISRESPONSE, Boolean.TRUE);
                            return Future.failedFuture("");
                        }
                        String structureId = timeslotInfo.getString(Field.SCHOOLID);
                        params.put(Field.STRUCTUREID, structureId);
                        return UserHelper.getUserInfos(eb, request);
                    })
                    .compose(userInfos -> {
                        if (userInfos.getStructures().contains(params.get(Field.STRUCTUREID))) {
                            return timeSlotService.deleteTimeSlotFromTimeslot(timeslotId);
                        }
                        unauthorized(request, Field.USER_NOT_IN_TIMESLOT_STRUCTURE);
                        params.put(Field.ISRESPONSE, Boolean.TRUE);
                        return Future.failedFuture("");
                    })
                    .onSuccess(res -> noContent(request))
                    .onFailure(err -> {
                        if (!params.containsKey(Field.ISRESPONSE)) {
                            log.error("[Viescolaire@TimeSlotController] Failed to delete timeslot/class association from timeslot", err.getMessage());
                            renderError(request, new JsonObject().put(Field.ERROR, err.getMessage()));
                        }
                    });
        } else {
            badRequest(request);
        }
    }
}
