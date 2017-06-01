package fr.openent.evaluations.controller;

import fr.openent.Viescolaire;
import fr.openent.evaluations.security.*;
import fr.openent.evaluations.service.AppreciationService;
import fr.openent.evaluations.service.impl.DefaultAppreciationService;
import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.request.RequestUtils;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;

/**
 * Created by anabah on 01/03/2017.
 */
public class AppreciationController extends ControllerHelper {




    /**
     * Déclaration des services
     */
    private final AppreciationService appreciationService;

    public AppreciationController() {
        pathPrefix = Viescolaire.EVAL_PATHPREFIX;
        appreciationService = new DefaultAppreciationService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_APPRECIATIONS_TABLE);
    }




    /**
     * Créer une appreciation avec les données passées en POST
     * @param request
     */
    @Post("/appreciation")
    @ApiDoc("Créer une appreciation")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
	@ResourceFilter(CreateEvaluationWorkflow.class)
    public void create(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null){
                    RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_APPRECIATIONS_CREATE, new Handler<JsonObject>() {
                        @Override
                        public void handle(JsonObject resource) {
                            appreciationService.createAppreciation(resource, user, notEmptyResponseHandler(request));
                        }
                    });
                }else {
                    log.debug("User not found in session.");
                    Renders.unauthorized(request);
                }
            }
        });
    }

    /**
     * Modifie une appreciation avec les données passées en PUT
     * @param request
     */
    @Put("/appreciation")
    @ApiDoc("Modifie une appreciation")
    @SecuredAction(value = "", type= ActionType.RESOURCE)
    @ResourceFilter(AccessAppreciationFilter.class)
    public void update(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null){
                    RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_APPRECIATIONS_UPDATE, new Handler<JsonObject>() {
                        @Override
                        public void handle(JsonObject resource) {
                            appreciationService.updateAppreciation(resource, user, defaultResponseHandler(request));
                        }
                    });
                }else {
                    log.debug("User not found in session.");
                    Renders.unauthorized(request);
                }
            }
        });
    }
    /**
     * Supprime l'appreciation passée en paramètre
     * @param request
     */
    @Delete("/appreciation")
    @ApiDoc("Supprimer une appréciation donnée")
    @SecuredAction(value = "", type= ActionType.RESOURCE)
    @ResourceFilter(AccessAppreciationFilter.class)
    public void deleteAppreciationDevoir(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null){

                    Long idAppreciation;
                    try {
                        idAppreciation = Long.parseLong(request.params().get("idAppreciation"));
                    } catch(NumberFormatException e) {
                        log.error("Error : idAppreciation must be a long object", e);
                        badRequest(request, e.getMessage());
                        return;
                    }

                    appreciationService.deleteAppreciation(idAppreciation, user, defaultResponseHandler(request));
                }else{
                    unauthorized(request);
                }
            }
        });
    }



}

