package fr.openent.viescolaire.controller;

import fr.openent.viescolaire.service.MultiTeachingService;
import fr.openent.viescolaire.service.impl.DefaultMultiTeachingService;
import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.request.RequestUtils;
import io.vertx.core.*;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;

import static java.util.Objects.isNull;
import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;

public class MultiTeachingController extends ControllerHelper {

    /**
     * Déclaration des services
     */
    private final MultiTeachingService multiTeachingService;

    public MultiTeachingController() {
        this.multiTeachingService = new DefaultMultiTeachingService(eb);
    }

    /**
     * @param request
     */
    @Post("/multiteaching/create")
    @ApiDoc("add a new teacher to service, can be co-teaching or substitute")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void addTeacher(final HttpServerRequest request) {
        RequestUtils.bodyToJson(request, pathPrefix + "multiteaching_create", params -> {

            String structureId = params.getString("structure_id");
            String mainTeacherId = params.getString("main_teacher_id");
            JsonArray secondTeacherIds = params.getJsonArray("second_teacher_ids");
            String subjectId = params.getString("subject_id");
            JsonArray classOrGroupIds = params.getJsonArray("class_or_group_ids");
            String startDate = params.containsKey("start_date") ? params.getString("start_date") : null;
            String endDate = params.containsKey("end_date") ? params.getString("end_date") : null;
            String enteredDndDate = params.containsKey("entered_end_date") ? params.getString("entered_end_date") : null;
            Boolean coTeaching = params.getBoolean("co_teaching", false);

            multiTeachingService.createMultiTeaching(structureId, mainTeacherId, secondTeacherIds, subjectId, classOrGroupIds,
                        startDate, endDate, enteredDndDate, coTeaching, arrayResponseHandler(request), hasCompetence());

        });
    }

    @Put("/multiteaching/update_visibility")
    @ApiDoc("Mets à jour la visibilité d'un co-enseignant / remplaçant")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void updateVisibility(final HttpServerRequest request) {
        RequestUtils.bodyToJson(request,  params -> {
            JsonArray groupsId = params.getJsonArray("class_or_group_ids");
            String structureId = params.getString("structure_id");
            String mainTeacherId = params.getString("main_teacher_id");
            String secondTeacherId = params.getString("second_teacher_ids");
            String subjectId = params.getString("subject_id");
            Boolean isVisible = params.getBoolean("is_visible");

            multiTeachingService.updateMultiTeachingVisibility(groupsId, structureId, mainTeacherId,
                    secondTeacherId, subjectId, isVisible, new Handler<Either<String, JsonArray>>() {
                        @Override
                        public void handle(Either<String, JsonArray> event) {
                            if(event.isRight()){
                                renderJson(request, event.right().getValue());
                            }
                            else {
                                log.info(event.left().getValue());
                            }
                        }
                    });
        });
    }

    @Put("/multiteaching/update")
    @ApiDoc("update a co-teaching or substitute teacher in a service")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void updateTeacher(final HttpServerRequest request) {
        RequestUtils.bodyToJson(request, pathPrefix + "multiteaching_update", params -> {
            String secondTeacherId = params.getJsonArray("second_teacher_ids").getString(0);
            String startDate = params.containsKey("start_date") ? params.getString("start_date") : null;
            String endDate = params.containsKey("end_date") ? params.getString("end_date") : null;
            String enteredDndDate = params.containsKey("entered_end_date") ? params.getString("entered_end_date") : null;
            Boolean isVisible = params.getBoolean("is_visible", true);
            JsonArray multiTeachingIdsToUpdate = params.getJsonArray("ids_multiTeachingToUpdate");
            JsonArray multiTeachingIdsToDelete = params.getJsonArray("ids_multiTeachingToDelete");

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
                        response.put("delete", promiseToDelete.future().result())
                                .put("update", promiseToUpdate.future().result());
                        Renders.renderJson(request, response);
                    });

        });
    }


    private boolean hasCompetence() {
        JsonObject services = config.getJsonObject("services");
        return isNull(services) || services.getBoolean("competences");
    }


    @Get("/mainteachers/:idStructure")
    @ApiDoc("Retourne tous les types de devoir par etablissement")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void viewTittulaires(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                multiTeachingService.getSubTeachers(user.getUserId(), request.getParam("idStructure"), new Handler<Either<String, JsonArray>>() {
                    @Override
                    public void handle(Either<String, JsonArray> event) {
                        log.info(event.right().getValue());
                    }
                });
            }
        });
    }
    @Get("/coteachers/:idStructure")
    @ApiDoc("Retourne tous les types de devoir par etablissement")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void viewcoteachers(final HttpServerRequest request) {
//        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
//            @Override
//            public void handle(final UserInfos user) {
//                multiTeachingService.getSubTeachersandCoTeachers(user.getUserId(), request.getParam("idStructure"), new Handler<Either<String, JsonArray>>() {
//                    @Override
//                    public void handle(Either<String, JsonArray> event) {
//                        log.info(event.right().getValue());
//                    }
//                });
//            }
//        });
        multiTeachingService.createMultiTeaching("","",new JsonArray(),"",
                new JsonArray(),"","","",false,  arrayResponseHandler(request),  hasCompetence());
    }

    @Put("/multiteaching/delete")
    @ApiDoc("delete a co-teaching or substitute teacher in a service")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void deleteTeacher(final HttpServerRequest request) {

        RequestUtils.bodyToJson(request, entries -> {
            if (entries.containsKey("ids") && entries.getJsonArray("ids").size() > 0) {
                JsonArray multiTeachingIds = entries.getJsonArray("ids");
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

    /**
     * @param request
     * @response jsonObject {id:String, name:String}
     */
    @Get("/multi-teaching/active-teacher/:idTeacher/teacher-id")
    @ApiDoc("found teacher name with id for multiteaching")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void get(final HttpServerRequest request) {
        String idTeacher = request.params().get("idTeacher");
        if(idTeacher.equals("43512bf6-8f0d-4a78-ae3d-a32f8e48a1d8")){
            request.response().setStatusCode(200).end(new JsonObject().put("id", "43512bf6-8f0d-4a78-ae3d-a32f8e48a1d8").put("name","julien").toString());
        }
        System.out.println(idTeacher);
        badRequest(request);
    }
}
