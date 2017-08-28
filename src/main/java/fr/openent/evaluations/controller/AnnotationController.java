package fr.openent.evaluations.controller;

import fr.openent.Viescolaire;
import fr.openent.evaluations.security.AccessAnnotationFilter;
import fr.openent.evaluations.security.CreateAnnotationWorkflow;
import fr.openent.evaluations.service.AnnotationService;
import fr.openent.evaluations.service.impl.DefaultAnnotationService;
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
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;

/**
 * Created by vogelmt on 21/08/2017.
 */
public class AnnotationController extends ControllerHelper {


    /**
     * Déclaration des services
     */
    private final AnnotationService annotationService;


    public AnnotationController() {
        pathPrefix = Viescolaire.EVAL_PATHPREFIX;
        annotationService = new DefaultAnnotationService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_ANNOTATIONS);
    }


    /**
     * Récupère les annotations de l'établissement
     * @param request
     */
    @Get("/annotations")
    @ApiDoc("Récupère les annotations de l'établissement")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getAnnotations(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>(){
            @Override
            public void handle(final UserInfos user) {
                if (user != null && null != request.params().get("idEtablissement")) {
                    final Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                    annotationService.listAnnotations(request.params().get("idEtablissement"), handler);
                } else {
                    badRequest(request);
                }
            }
        });
    }

    /**
     * Créer une annotation avec les données passées en POST
     * @param request
     */
    @Post("/annotation")
    @ApiDoc("Créer une annotation sur un devoir")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
	@ResourceFilter(CreateAnnotationWorkflow.class)
    public void create(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null){
                    RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_ANNOTATION_UPDATE, new Handler<JsonObject>() {
                        @Override
                        public void handle(JsonObject annotation) {
                            annotationService.createAnnotationDevoir(annotation.getLong("id_devoir"),Long.parseLong(annotation.getString("id_annotation")),annotation.getString("id_eleve"),defaultResponseHandler(request) );
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
     * Modifie une annontation avec les données passées en paramètre
     * @param request
     */
    @Put("/annotation")
    @ApiDoc("Modifie une annotation sur un devoir")
    @SecuredAction(value = "", type= ActionType.RESOURCE)
    @ResourceFilter(AccessAnnotationFilter.class)
    public void update(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null){
                    RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_ANNOTATION_UPDATE, new Handler<JsonObject>() {
                        @Override
                        public void handle(JsonObject annotation) {
                            annotationService.updateAnnotationDevoir(annotation.getLong("id_devoir"),annotation.getLong("id_annotation"),annotation.getString("id_eleve"),defaultResponseHandler(request) );
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
     * Supprime l'annotation avec les données passées en paramètre
     * @param request
     */
    @Delete("/annotation")
    @ApiDoc("Supprimer une annotation donnée")
    @SecuredAction(value = "", type= ActionType.RESOURCE)
    @ResourceFilter(AccessAnnotationFilter.class)
    public void delete(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if(user != null){
                    annotationService.deleteAnnotation(Long.valueOf(request.params().get("idDevoir")),request.params().get("idEleve"),defaultResponseHandler(request) );
                }else {
                    log.debug("User not found in session.");
                    Renders.unauthorized(request);
                }
            }
        });
    }
}

