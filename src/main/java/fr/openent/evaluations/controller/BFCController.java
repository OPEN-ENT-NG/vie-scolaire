package fr.openent.evaluations.controller;

import fr.openent.Viescolaire;
import fr.openent.evaluations.security.AccessBFCFilter;
import fr.openent.evaluations.service.*;
import fr.openent.evaluations.service.impl.*;
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
import org.vertx.java.core.json.JsonArray;
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
    private final EnseignementComplementService enseignementComplement;
    private final NiveauEnseignementComplementService niveauEnseignementComplement;

    public BFCController() {
        pathPrefix = Viescolaire.EVAL_PATHPREFIX;
        bfcService = new DefaultBFCService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_BFC_TABLE);
        syntheseService = new DefaultBfcSyntheseService(Viescolaire.EVAL_SCHEMA, Viescolaire.EVAL_BFC_SYNTHESE_TABLE);
        utilsService = new DefaultUtilsService();
        classeService = new DefaultClasseService();
        enseignementComplement = new DefaultEnseignementComplementService(Viescolaire.EVAL_SCHEMA,Viescolaire.EVAL_ENSEIGNEMENT_COMPLEMENT);
        niveauEnseignementComplement = new DefaultNiveauEnseignementComplementService(Viescolaire.EVAL_SCHEMA,Viescolaire.EVAL_ELEVE_ENSEIGNEMENT_COMPLEMENT);
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

    //La Synthèse du Bilan de fin de cycle

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
                    log.debug("id_cycle : "+idCycle.right().getValue());
                    if (idCycle.isRight()) {
                        syntheseService.getBfcSyntheseByEleve(idEleve, idCycle.right().getValue(), defaultResponseHandler(request));
                    } else {
                        log.info("idCycle not found");
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
    //Les enseignements de complément pour le cycle 4 seulement

    @Get("/ListEnseignementComplement")
    @ApiDoc("Récupère la liste des enseignements ")
    @SecuredAction(value="",type = ActionType.AUTHENTICATED)
    public void getEnseignementsDeComplement(final  HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos userInfos) {
                if(userInfos!=null){
                    Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                    enseignementComplement.getEnseignementsComplement(handler);
                }else{
                    Renders.unauthorized(request);
                }
            }
        });
    }
    @Post("/CreateNiveauEnsCpl")
    @ApiDoc("crée l'enseignement de complement pour un élève")
    @SecuredAction(value="",type=ActionType.AUTHENTICATED)
    public void createNiveauEnsCpl(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos userInfos) {
                if(userInfos!=null && userInfos.getFunctions().containsKey("ENS")){
                    RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_NIVEAUENSCPL_CREATE, new Handler<JsonObject>() {
                        @Override
                        public void handle(JsonObject data) {
                            niveauEnseignementComplement.createEnsCplByELeve(data,userInfos,notEmptyResponseHandler(request));
                        }
                    });
                }else{
                    Renders.unauthorized(request);
                }
            }
        });
    }

    @Put("/UpdateNiveauEnsCpl")
    @ApiDoc("met à jour niveau d'enseignement complément")
    @SecuredAction(value="",type=ActionType.AUTHENTICATED)
    public void updateNiveauEnsCpl(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos userInfos) {
                if(userInfos!=null && userInfos.getFunctions().containsKey("ENS")){
                    RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_NIVEAUENSCPL_CREATE, new Handler<JsonObject>() {
                        @Override
                        public void handle(JsonObject data) {
                            final Integer id = Integer.parseInt(request.params().get("id"));

                            niveauEnseignementComplement.updateEnsCpl(id,data,defaultResponseHandler(request));
                        }
                    });
                }else{
                    Renders.unauthorized(request);
                }
            }
        });
    }

    @Get("/GetNiveauEnsCpl")
    @ApiDoc("Récupère l'enseignement de complément pour un élève")
    @SecuredAction(value="", type=ActionType.AUTHENTICATED)
    public void getNiveauEnsCplByEleve(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos userInfos) {
                if(userInfos!=null)  {
                    final String id = request.params().get("idEleve");
                    niveauEnseignementComplement.getNiveauEnsCplByEleve(id,defaultResponseHandler(request));
                }
            }
        });
    }



}
