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
import fr.openent.viescolaire.service.CoursService;
import fr.openent.viescolaire.service.impl.DefaultClasseService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.impl.DefaultCoursService;
import fr.wseduc.webutils.http.Renders;
import org.entcore.common.controller.ControllerHelper;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Objects;

import static org.entcore.common.http.response.DefaultResponseHandler.*;

/**
 * Created by ledunoiss on 10/02/2016.
 */
public class CoursController extends ControllerHelper{
    private final CoursService coursService;
    private final ClasseService classeService;
    public CoursController(){
        pathPrefix = Viescolaire.VSCO_PATHPREFIX;
        coursService = new DefaultCoursService();
        classeService = new DefaultClasseService();
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

        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

        coursService.getClasseCours(dateDebut, dateFin, idClasse, handler);
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
    @Get("/enseignant/:userId/cours/:dateDebut/:dateFin")
    @ApiDoc("Récupère tous les cours d'un utilisateur dans une période donnée.")
    @SecuredAction(value="", type= ActionType.AUTHENTICATED)
    public void getCoursByUserId(final HttpServerRequest request){
        String userId = request.params().get("userId");
        String dateDebut= request.params().get("dateDebut")+" 00:00:00";
        String dateFin= request.params().get("dateFin")+" 00:00:00";

        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

        coursService.getCoursByUserId(dateDebut, dateFin, userId, handler);
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
                    JsonObject obj = event.right().getValue().get(0);
                    int size = obj.getArray("Classes").size();
                    if(size > 0) {
                        Object[] Ids = new Object[size];
                        String[] IdsStr = new String[size];
                        try {
                            Ids = obj.getArray("Classes").toArray();
                        } catch (ClassCastException e) {
                            log.error("Cannot cast evenementId to Number on delete evenement : " + e);
                        }
                        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                        for(int i = 0; i < Ids.length; i++) {
                            IdsStr[i] = Ids[i].toString();
                        }
                        coursService.getCoursByStudentId(dateDebut, dateFin, IdsStr,  handler);
                    }else{
                        log.error("This Student has no Classes or Groups");
                    }
                }else{
                    leftToResponse(request, event.left());
                }
            }
        });


    }
}
