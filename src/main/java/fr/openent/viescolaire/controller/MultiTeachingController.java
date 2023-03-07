package fr.openent.viescolaire.controller;

import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.security.AccessIfMyStructureParamService;
import fr.openent.viescolaire.security.AccessStructureAdminRightParamService;
import fr.openent.viescolaire.security.StructureRight;
import fr.openent.viescolaire.service.MultiTeachingService;
import fr.openent.viescolaire.service.impl.DefaultMultiTeachingService;
import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.request.RequestUtils;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.user.UserUtils;

import static java.util.Objects.isNull;
import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;

public class MultiTeachingController extends ControllerHelper {

    /**
     * Déclaration des services
     */
    private final MultiTeachingService multiTeachingService;

    public MultiTeachingController(EventBus eb) {
        this.multiTeachingService = new DefaultMultiTeachingService(eb);
    }

    /**
     * @param request
     */
    @Post("/multiteaching/create")
    @ApiDoc("add a new teacher to service, can be co-teaching or substitute")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AccessIfMyStructureParamService.class)
    public void addTeacher(final HttpServerRequest request) {
        RequestUtils.bodyToJson(request, pathPrefix + "multiteaching_create", params -> {

            String structureId = params.getString(Field.STRUCTURE_ID);
            String mainTeacherId = params.getString(Field.MAIN_TEACHER_ID);
            JsonArray secondTeacherIds = params.getJsonArray(Field.SECOND_TEACHER_IDS);
            String subjectId = params.getString(Field.SUBJECT_ID);
            JsonArray classOrGroupIds = params.getJsonArray(Field.CLASS_OR_GROUP_IDS);
            String startDate = params.containsKey(Field.START_DATE) ? params.getString(Field.START_DATE) : null;
            String endDate = params.containsKey(Field.END_DATE) ? params.getString(Field.END_DATE) : null;
            String enteredDndDate = params.containsKey(Field.ENTERED_END_DATE) ? params.getString(Field.ENTERED_END_DATE) : null;
            Boolean coTeaching = params.getBoolean(Field.CO_TEACHING, false);

            multiTeachingService.createMultiTeaching(structureId, mainTeacherId, secondTeacherIds, subjectId, classOrGroupIds,
                        startDate, endDate, enteredDndDate, coTeaching, arrayResponseHandler(request), hasCompetence());

        });
    }

    @Put("/multiteaching/update_visibility")
    @ApiDoc("Mets à jour la visibilité d'un co-enseignant / remplaçant")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AccessIfMyStructureParamService.class)
    public void updateVisibility(final HttpServerRequest request) {
        RequestUtils.bodyToJson(request,  params -> {
            JsonArray groupsId = params.getJsonArray(Field.CLASS_OR_GROUP_IDS);
            String structureId = params.getString(Field.STRUCTURE_ID);
            String mainTeacherId = params.getString(Field.MAIN_TEACHER_ID);
            String secondTeacherId = params.getString(Field.SECOND_TEACHER_IDS);
            String subjectId = params.getString(Field.SUBJECT_ID);
            Boolean isVisible = params.getBoolean(Field.IS_VISIBLE);

            multiTeachingService.updateMultiTeachingVisibility(groupsId, structureId, mainTeacherId,
                    secondTeacherId, subjectId, isVisible, event -> {
                        if(event.isRight()){
                            renderJson(request, event.right().getValue());
                        }
                        else {
                            log.info(event.left().getValue());
                        }
                    });
        });
    }

    @Put("/multiteaching/update")
    @ApiDoc("update a co-teaching or substitute teacher in a service")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AccessIfMyStructureParamService.class)
    public void updateTeacher(final HttpServerRequest request) {
        RequestUtils.bodyToJson(request, pathPrefix + "multiteaching_update", params -> {
            String secondTeacherId = params.getJsonArray(Field.SECOND_TEACHER_IDS).getString(0);
            String startDate = params.containsKey(Field.START_DATE) ? params.getString(Field.START_DATE) : null;
            String endDate = params.containsKey(Field.END_DATE) ? params.getString(Field.END_DATE) : null;
            String enteredDndDate = params.containsKey(Field.ENTERED_END_DATE) ? params.getString(Field.ENTERED_END_DATE) : null;
            Boolean isVisible = params.getBoolean(Field.IS_VISIBLE, true);
            JsonArray multiTeachingIdsToUpdate = params.getJsonArray(Field.IDS_MULTITEACHINGTOUPDATE);
            JsonArray multiTeachingIdsToDelete = params.getJsonArray(Field.IDS_MULTITEACHINGTODELETE);

            Promise<JsonObject> promiseToDelete = Promise.promise();

            if (!multiTeachingIdsToDelete.isEmpty()) {
                multiTeachingService.deleteMultiTeaching(multiTeachingIdsToDelete, hasCompetence(), deleteResponse -> {
                    if (deleteResponse.isRight()) {
                        promiseToDelete.complete(deleteResponse.right().getValue());
                    } else {
                        promiseToDelete.fail(deleteResponse.left().getValue());
                    }
                });
            } else {
                promiseToDelete.complete(new JsonObject());
            }

            Promise<JsonArray> promiseToUpdate = Promise.promise();
            Handler<Either<String, JsonArray>> getHandlerToUpdate = updateResponse -> {
                if (updateResponse.isRight()) {
                    promiseToUpdate.complete(updateResponse.right().getValue());
                } else {
                    promiseToUpdate.fail(updateResponse.left().getValue());
                }
            };
            multiTeachingService.updateMultiteaching(multiTeachingIdsToUpdate, secondTeacherId,
                    startDate, endDate, enteredDndDate, isVisible, hasCompetence(), getHandlerToUpdate);


            CompositeFuture.all(promiseToDelete.future(), promiseToUpdate.future())
                    .onFailure(fail -> {
                        badRequest(request, fail.getMessage());
                        log.error(String.format("[Vie-scolaire@%s::updateTeacher] " +
                                        "failed to update multiteaching : %s", this.getClass().getSimpleName(),
                                fail.getMessage()));
                    })
                    .onSuccess(ar -> {
                        JsonObject response = new JsonObject();
                        response.put(Field.DELETE, promiseToDelete.future().result())
                                .put(Field.UPDATE, promiseToUpdate.future().result());
                        Renders.renderJson(request, response);
                    });

        });
    }


    private boolean hasCompetence() {
        JsonObject services = config.getJsonObject(Field.SERVICES);
        return isNull(services) || services.getBoolean(Field.COMPETENCES);
    }


    @Get("/mainteachers/:idStructure")
    @ApiDoc("Retourne tous les types de devoir par etablissement")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(StructureRight.class)
    public void viewTittulaires(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, user -> {
            multiTeachingService.getSubTeachers(user.getUserId(), request.getParam(Field.IDSTRUCTURE), event -> {
                log.info(event.right().getValue());
            });
        });
    }

    @Put("/multiteaching/delete")
    @ApiDoc("delete a co-teaching or substitute teacher in a service")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AccessStructureAdminRightParamService.class)
    public void deleteTeacher(final HttpServerRequest request) {
        RequestUtils.bodyToJson(request, entries -> {
            if (entries.containsKey(Field.IDS) && entries.getJsonArray(Field.IDS).size() > 0) {
                JsonArray multiTeachingIds = entries.getJsonArray(Field.IDS);
                multiTeachingService.deleteMultiTeaching(multiTeachingIds, hasCompetence(), either -> {
                    if (either.isLeft()) {
                        log.error(String.format("[Vie-scolaire@%s::deleteTeacher] " +
                                "failed to delete multiteaching : %s", this.getClass().getSimpleName(),
                                either.left().getValue()));
                        renderError(request);
                    } else {
                        renderJson(request, either.right().getValue());
                    }
                });

            } else {
                badRequest(request);
            }
        });

    }
}
