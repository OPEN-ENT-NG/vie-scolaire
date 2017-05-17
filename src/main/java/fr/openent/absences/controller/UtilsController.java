package fr.openent.absences.controller;

import fr.openent.Viescolaire;
import fr.openent.absences.service.UtilsService;
import fr.openent.absences.service.impl.DefaultUtilsService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Delete;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.request.RequestUtils;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.response.DefaultResponseHandler;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static fr.wseduc.webutils.http.response.DefaultResponseHandler.arrayResponseHandler;

public class UtilsController extends ControllerHelper {

    UtilsService utilsService;

    public UtilsController() {
        pathPrefix = Viescolaire.ABSC_PATHPREFIX;
        utilsService = new DefaultUtilsService();
    }

    @Get("/user/structures/actives")
    @ApiDoc("Retourne la liste des identifiants des structures actives de l'utilisateur")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getIdsStructuresActive(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if (user != null) {
                    utilsService.getActivesIDsStructures(user, arrayResponseHandler(request));
                } else {
                    unauthorized(request);
                }
            }
        });
    }

    /**
     * Retourne retourne le cycle de la classe
     *
     * @param request
     */
    @Post("/user/structures/actives")
    @ApiDoc("Retourne la liste des identifiants des structures actives de l'utilisateur")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void createStructureInactive(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
                    @Override
                    public void handle(JsonObject body) {
                        if(user != null && body.containsField("structureId")){
                            final String structureId = body.getString("structureId");
                            Handler<Either<String, JsonArray>> handler = DefaultResponseHandler.arrayResponseHandler(request);
                            utilsService.createActiveStructure(structureId, user, handler);
                        }else{
                            badRequest(request);
                        }
                    }
                });
            }
        });
    }

    @Delete("/user/structures/actives")
    @ApiDoc("Supprime une structure active.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void deleteEvenement(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if (user != null) {
                    final String structureId = request.params().get("structureId");
                    Handler<Either<String, JsonArray>> handler = DefaultResponseHandler.arrayResponseHandler(request);
                    utilsService.deleteActiveStructure(structureId, user, handler);
                } else {
                    unauthorized(request);
                }
            }
        });

    }
}