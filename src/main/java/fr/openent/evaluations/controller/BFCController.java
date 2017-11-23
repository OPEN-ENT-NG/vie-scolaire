package fr.openent.evaluations.controller;

import fr.openent.Viescolaire;
import fr.openent.evaluations.security.AccessBFCFilter;
import fr.openent.evaluations.service.BFCService;
import fr.openent.evaluations.service.BfcSyntheseService;
import fr.openent.evaluations.service.UtilsService;
import fr.openent.evaluations.service.impl.DefaultBFCService;
import fr.openent.evaluations.service.impl.DefaultBfcSyntheseService;
import fr.openent.evaluations.service.impl.DefaultUtilsService;
import fr.openent.viescolaire.service.ClasseService;
import fr.openent.viescolaire.service.impl.DefaultClasseService;
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
import org.vertx.java.core.json.JsonObject;

import static org.entcore.common.http.response.DefaultResponseHandler.*;

/**
 * Created by vogelmt on 29/03/2017.
 */
public class BFCController extends ControllerHelper {
    /**
     * Déclaration des services
     */
    private final BFCService bfcService;
    private final BfcSyntheseService syntheseService;
    private final UtilsService utilsService;
    private final ClasseService classeService;

    public BFCController() {
        pathPrefix = Viescolaire.EVAL_PATHPREFIX;
        bfcService = new DefaultBFCService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_BFC_TABLE);
        syntheseService = new DefaultBfcSyntheseService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_BFC_SYNTHESE_TABLE);
        utilsService = new DefaultUtilsService();
        classeService = new DefaultClasseService();
    }


    /**
     * Créer un BFC avec les données passées en POST
     *
     * @param request
     */
    @Post("/bfc")
    @ApiDoc("Créer un BFC")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AccessBFCFilter.class)
    public void create(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if (user != null) {
                    RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_BFC_CREATE, new Handler<JsonObject>() {
                        @Override
                        public void handle(JsonObject resource) {
                            bfcService.createBFC(resource, user, notEmptyResponseHandler(request));
                        }
                    });
                } else {
                    log.debug("User not found in session.");
                    Renders.unauthorized(request);
                }
            }
        });
    }


    /**
     * Modifie un BFC avec les données passées en PUT
     *
     * @param request
     */
    @Put("/bfc")
    @ApiDoc("Modifie un BFC")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AccessBFCFilter.class)
    public void update(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if (user != null) {
                    RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_BFC_UPDATE, new Handler<JsonObject>() {
                        @Override
                        public void handle(JsonObject resource) {
                            bfcService.updateBFC(resource, user, defaultResponseHandler(request));
                        }
                    });
                } else {
                    log.debug("User not found in session.");
                    Renders.unauthorized(request);
                }
            }
        });
    }

    /**
     * Supprime l'appreciation passée en paramètre
     *
     * @param request
     */
    @Delete("/bfc")
    @ApiDoc("Supprimer un bfc donnée")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AccessBFCFilter.class)
    public void delete(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if (user != null) {

                    Long idBFC;
                    try {
                        idBFC = Long.parseLong(request.params().get("id"));
                    } catch (NumberFormatException e) {
                        log.error("Error : idAppreciation must be a long object", e);
                        badRequest(request, e.getMessage());
                        return;
                    }

                    bfcService.deleteBFC(idBFC, user, defaultResponseHandler(request));
                } else {
                    unauthorized(request);
                }
            }
        });
    }

    @Get("/bfc/eleve/:idEleve")
    @ApiDoc("Retourne les bfcs notes pour un élève.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getBFCsEleve(final HttpServerRequest request) {
        if (request.params().contains("idEleve")
                && request.params().contains("idEtablissement")) {
            String idEleve = request.params().get("idEleve");
            String idEtablissement = request.params().get("idEtablissement");
            bfcService.getBFCsByEleve(new String[]{idEleve}, idEtablissement, null, arrayResponseHandler(request));
        } else {
            Renders.badRequest(request, "Invalid parameters");
        }
    }

    /**
     * Créer une Synthese avec les données passées en POST
     *
     * @param request
     */
    @Post("/BfcSynthese")
    @ApiDoc("Créer une Synthese du BFC")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AccessBFCFilter.class)
    public void createSynthese(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if (user != null) {
                    RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_BFCSYNTHESE_CREATE, new Handler<JsonObject>() {
                        @Override
                        public void handle(final JsonObject synthese) {
                            syntheseService.getIdCycleWithIdEleve(synthese.getString("id_eleve"), new Handler<Either<String, Integer>>() {
                                @Override
                                public void handle(Either<String, Integer> idCycle) {
                                    System.out.println("id du cycle create: "+idCycle.right().getValue());
                                    log.debug("id_cycle : "+idCycle.right().getValue());
                                    if (idCycle.isRight()) {
                                        JsonObject syntheseCycle = new JsonObject()
                                                .putString("id_eleve", synthese.getString("id_eleve"))
                                                .putString("owner", user.getUserId())
                                                .putNumber("id_cycle", idCycle.right().getValue())
                                                .putString("texte", synthese.getString("texte"));
                                        syntheseService.createBfcSynthese(syntheseCycle, user, notEmptyResponseHandler(request));
                                    } else {
                                        log.debug("idCycle not found");
                                        Renders.badRequest(request);
                                    }
                                }
                            } );
                        }
                    });
                } else {
                    log.debug("User not found in session.");
                    Renders.unauthorized(request);
                }
            }
        });
    }


    @Get("/BfcSynthese")
    @ApiDoc("récupére une Synthese du BFC pour un élève")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getSynthese(final HttpServerRequest request) {

        if (request.params().contains("idEleve")) {
            final String idEleve = request.params().get("idEleve");
            syntheseService.getIdCycleWithIdEleve(idEleve, new Handler<Either<String, Integer>>() {
                @Override
                public void handle(Either<String, Integer> idCycle) {
                    System.out.println("id du cycle get : "+idCycle.right().getValue());
                    log.debug("id_cycle : "+idCycle.right().getValue());
                    if (idCycle.isRight()) {
                        syntheseService.getBfcSyntheseByEleve(idEleve, idCycle.right().getValue(), defaultResponseHandler(request));
                    } else {
                        log.debug("idCycle not found");
                        Renders.badRequest(request);
                    }
                }
            });
        } else {
            log.debug("idEleve not found");
            Renders.badRequest(request);
        }
    }

    @Put("/BfcSynthese")
    @ApiDoc("Met à jour la synthèse du bilan de compétence pour un élève")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AccessBFCFilter.class)
    public void updateSynthese(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos userInfos) {
                if(userInfos != null){
                    if(request.params().contains("id")){
                        System.out.println("id du bfcSynthese update : "+request.params().get("id"));
                        RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_BFCSYNTHESE_CREATE, new Handler<JsonObject>() {
                            @Override
                            public void handle(JsonObject synthese) {
                                syntheseService.updateBfcSynthese(request.params().get("id"), synthese, notEmptyResponseHandler(request));
                            }
                        });
                    }else{
                        log.debug("idbfcSynthese not found");
                        Renders.badRequest(request);
                    }

                }
            }
        });
    }
}
