/*
 * Copyright (c) Région Hauts-de-France, Département 77, CGI, 2016.
 *
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
 *
 */

package fr.openent.viescolaire.controller;

import fr.openent.viescolaire.service.ClasseService;
import fr.openent.viescolaire.service.CommonCoursService;
import fr.openent.viescolaire.service.CoursService;
import fr.openent.viescolaire.service.impl.DefaultClasseService;
import fr.openent.viescolaire.service.impl.DefaultCommonCoursService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.impl.DefaultCoursService;
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.request.RequestUtils;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.entcore.common.http.response.DefaultResponseHandler.*;

/**
 * Created by ledunoiss on 10/02/2016.
 */
public class CoursController extends ControllerHelper{

    private final CoursService coursService;
    private final ClasseService classeService;
    private final CommonCoursService commonCoursService;
    protected static final Logger log = LoggerFactory.getLogger(CoursController.class);

    public CoursController(){
        pathPrefix = Viescolaire.VSCO_PATHPREFIX;
        coursService = new DefaultCoursService();
        classeService = new DefaultClasseService();
        commonCoursService = new DefaultCommonCoursService(eb);
    }

    // TODO : Ajouter le filtre
    // TODO : MODIFIER L'URL POUR LA RENDRE CORRECTE
    @Get("/:idClasse/cours/:dateDebut/:dateFin")
    @ApiDoc("Recupere tous les cours d'une classe dans une période donnée.")
    @SecuredAction(value="", type= ActionType.AUTHENTICATED)
    public void getClasseCours(final HttpServerRequest request){
        String idClasse = request.params().get("idClasse");
        String dateDebut= request.params().get("dateDebut")+" 00:00:00";
        String dateFin= request.params().get("dateFin")+" 23:59:59";

        List<String> listIdClasse = new ArrayList<>();
        listIdClasse.add(idClasse);

        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

        coursService.getClasseCours(dateDebut, dateFin, listIdClasse, handler);
    }

    @Get("/common/courses/:structureId/:begin/:end")
    @ApiDoc("Get courses for a structure between two dates by optional teacher id and/or optional group name.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void listCoursesBetweenTwoDates(final HttpServerRequest request) {
        final String structureId = request.params().get("structureId");
        final List<String> teacherId = request.params().getAll("teacherId");
        final List<String> groupName = request.params().getAll("group");
        final String beginDate = request.params().get("begin");
        final String endDate = request.params().get("end");

        if (beginDate!=null && endDate != null &&
                beginDate.matches("\\d{4}-\\d{2}-\\d{2}") && endDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            commonCoursService.listCoursesBetweenTwoDatesFormatted(structureId, teacherId, groupName, beginDate, endDate, arrayResponseHandler(request));
        } else {
            badRequest(request, "timetable.invalid.dates");
        }
    }

    @Get("/cours/classes/:dateDebut/:dateFin")
    @ApiDoc("Recupere tous les cours d'une liste de classe dans une période donnée.")
    @SecuredAction(value="", type= ActionType.AUTHENTICATED)
    public void getClasseCoursListClasses(final HttpServerRequest request){
        String dateDebut= request.params().get("dateDebut")+" 00:00:00";
        String dateFin= request.params().get("dateFin")+" 23:59:59";

        List<String> listIdClasse = request.params().getAll("classeId");

        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

        coursService.getClasseCours(dateDebut, dateFin, listIdClasse, handler);
    }


    @Get("/:idClasse/cours/:dateDebut/:dateFin/time/:timeDb/:timeFn")
    @ApiDoc("Recupere tous les cours d'une classe dans une période donnée.")
    @SecuredAction(value="", type= ActionType.AUTHENTICATED)
    public void getClasseCoursByTime(final HttpServerRequest request){
        String idClasse = request.params().get("idClasse");
        String dateDebut= request.params().get("dateDebut")+' '+request.params().get("timeDb");
        String dateFin= request.params().get("dateFin")+' '+request.params().get("timeFn");

        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

        coursService.getClasseCoursBytime(dateDebut,dateFin, idClasse, handler);
    }
    // TODO : MODIFIER L'URL POUR LA RENDRE CORRECTE
    @Get("/enseignant/:userId/:structureId/cours/:dateDebut/:dateFin")
    @ApiDoc("Récupère tous les cours d'un utilisateur dans une période donnée.")
    @SecuredAction(value="", type= ActionType.AUTHENTICATED)
    public void getCoursByUserId(final HttpServerRequest request){
        String userId = request.params().get("userId");
        String structureId = request.params().get("structureId");
        String dateDebut= request.params().get("dateDebut")+" 00:00:00";
        String dateFin= request.params().get("dateFin")+" 00:00:00";

        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

        coursService.getCoursByUserId(dateDebut, dateFin, userId, structureId, handler);
    }

    //
    @Get("/cours/:etabId/:eleveId/:dateDebut/:dateFin/time/:timeDb/:timeFn")
    @ApiDoc("Récupère tous les cours d'un eleve dans une période donnée.")
    @SecuredAction(value="", type= ActionType.AUTHENTICATED)
    public void getCoursByStudentId(final HttpServerRequest request){
        String eleveId = request.params().get("eleveId");
        String etabId = request.params().get("etabId");

        //chercher les classes/groupes
        classeService.getClasseEleve(etabId, eleveId, new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> event) {
                String dateDebut= request.params().get("dateDebut")+' '+request.params().get("timeDb") ;
                String dateFin= request.params().get("dateFin")+' '+request.params().get("timeFn");
                if(event.isRight()){
                    //get key
                    JsonObject obj = event.right().getValue().getJsonObject(0);
                    int size = obj.getJsonArray("Classes").size();
                    if(size > 0) {
                        Object[] Ids = new Object[size];
                        String[] IdsStr = new String[size];
                        try {
                            Ids = obj.getJsonArray("Classes").getList().toArray();
                        } catch (ClassCastException e) {
                            log.error("Cannot cast evenementId to Number on delete evenement : " + e);
                        }
                        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                        for(int i = 0; i < Ids.length; i++) {
                            IdsStr[i] = Ids[i].toString();
                        }

                        List<String> listIdClasse = Arrays.asList(IdsStr);

                        coursService.getClasseCours(dateDebut, dateFin, listIdClasse,  handler);
                    }else{
                        log.error("This Student has no Classes or Groups");
                    }
                }else{
                    leftToResponse(request, event.left());
                }
            }
        });
    }

    @Get("/cours")
    @ApiDoc("Recupere tous les cours en fonction de leur id")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getCours(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if (user != null) {
                    List<Long> idCours = new ArrayList<>();
                    for(String s : request.params().getAll("idCours")) {
                        idCours.add(Long.valueOf(s));
                    }
                    coursService.getCoursById(idCours, new Handler<Either<String, JsonArray>>() {
                        @Override
                        public void handle(Either<String, JsonArray> stringJsonArrayEither) {
                            if (stringJsonArrayEither.isRight()) {
                                Renders.renderJson(request, stringJsonArrayEither.right().getValue());
                            } else {
                                JsonObject error = new JsonObject()
                                        .put("error", stringJsonArrayEither.left().getValue());
                                log.error(stringJsonArrayEither.left().getValue());
                                Renders.renderJson(request, error, 400);
                            }
                        }
                    });
                } else {
                    unauthorized(request);
                }
            }
        });
    }

    @Post("/cours")
    @ApiDoc("Créé un cours.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void createCours(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                RequestUtils.bodyToJson(request, new Handler<JsonObject>() {
                    @Override
                    public void handle(JsonObject resource) {
                        String userId = user.getUserId();

                        String idEtablissement = resource.getString("idEtablissement");
                        String idMatiere = resource.getString("idMatiere");
                        String dateDebut = resource.getString("dateDebut"); //format YYYY-MM-DD HH:mm
                        String dateFin = resource.getString("dateFin"); //format YYYY-MM-DD HH:mm

                        List<String> listIdClasse = resource.getJsonArray("classeIds").getList();
                        List<String> listIdTeacher = resource.getJsonArray("teacherIds").getList();

                        Handler<Either<String, JsonObject>> handler = defaultResponseHandler(request);

                        coursService.createCours(userId, idEtablissement, idMatiere, dateDebut, dateFin, listIdClasse, listIdTeacher, handler);
                    }
                });
            }
        });
    }


}
