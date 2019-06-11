package fr.openent.viescolaire.controller;

import fr.openent.viescolaire.service.TimeSlotService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.I18n;
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.request.RequestUtils;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.response.DefaultResponseHandler;

import static fr.openent.Viescolaire.DIRECTORY_ADDRESS;
import static fr.wseduc.webutils.Utils.handlerToAsyncHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;

public class TimeSlotController extends ControllerHelper {

    private TimeSlotService timeSlotService;

    private static final I18n i18n = I18n.getInstance();

    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @Get("/time-slots")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
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
                            boolean found = false; int i = 0;
                            while (!found && i < slotProfiles.size()) {
                                if (slotProfiles.getJsonObject(i).getString("_id").equals(id)) {
                                    found = true;
                                    slotProfiles.getJsonObject(i).put("default", true);

                                    renderJson(request, slotProfiles);
                                    return;
                                }
                                i++;
                            }
                            renderJson(request, slotProfiles);
                        }
                    });
                }
                else {
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
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    @ApiDoc("Save a profile slot")
    public void saveTimeProfil(final HttpServerRequest request) {
        RequestUtils.bodyToJson(request,
                timeSlot -> {
                    timeSlotService.saveTimeProfil(timeSlot, arrayResponseHandler(request));
                });
    }


    @Get("/structures/:id/time-slot")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    @ApiDoc("Retrieve default structure time slot")
    public void getDefaultStructureTimeSlot(final HttpServerRequest request) {
        String structureId = request.getParam("id");

        timeSlotService.getSlotProfiles(structureId, either -> {
            if (either.isLeft()) {
                log.error("[Viescolaire@TimeSlotController] Failed to retrieve default structure time slot", either.left().getValue());
                renderError(request, new JsonObject().put("error", either.left().getValue()));
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

}
