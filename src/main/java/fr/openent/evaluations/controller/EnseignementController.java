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

package fr.openent.evaluations.controller;

import fr.openent.Viescolaire;
import fr.openent.evaluations.service.impl.DefaultCompetencesService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.openent.evaluations.service.impl.DefaultEnseignementService;
import org.entcore.common.controller.ControllerHelper;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.entcore.common.http.response.DefaultResponseHandler.leftToResponse;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public class EnseignementController extends ControllerHelper {


    /**
     * Déclaration des services
     */
    private final fr.openent.evaluations.service.EnseignementService enseignementService;
    private final fr.openent.evaluations.service.CompetencesService competencesService;

    public EnseignementController() {
        pathPrefix = Viescolaire.EVAL_PATHPREFIX;
        enseignementService = new DefaultEnseignementService(Viescolaire.EVALUATIONS_SCHEMA, Viescolaire.EVAL_ENSEIGNEMENTS_TABLE);
        competencesService = new DefaultCompetencesService(Viescolaire.EVALUATIONS_SCHEMA, Viescolaire.EVAL_COMPETENCES_TABLE);
    }

    /**
     * Récupère la liste des enseignements
     * @param request
     */
    @Get("/enseignements")
    @ApiDoc("Recupère la liste des enseignements")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getEnseignements(final HttpServerRequest request){
        final JsonObject _datas = new JsonObject();
        enseignementService.getEnseignements(new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> event) {
                if (event.right().isRight()) {
                    _datas.putArray("enseignements", event.right().getValue());
                    competencesService.getCompetencesByLevel("idtype = 1", new Handler<Either<String, JsonArray>>() {
                        @Override
                        public void handle(Either<String, JsonArray> eventCompetences_1) {
                            if (eventCompetences_1.right().isRight()) {
                                _datas.putArray("_competences_1", eventCompetences_1.right().getValue());
                                competencesService.getCompetencesByLevel("idtype = 2", new Handler<Either<String, JsonArray>>() {
                                    @Override
                                    public void handle(Either<String, JsonArray> eventCompetences_2) {
                                        if (eventCompetences_2.right().isRight()) {
                                            _datas.putArray("_competences_2", eventCompetences_2.right().getValue());
                                            JsonArray result = new JsonArray();
                                            JsonArray enseignements = _datas.getArray("enseignements");
                                            JsonArray _competences_1 =  _datas.getArray("_competences_1");
                                            JsonArray _competences_2 =  _datas.getArray("_competences_2");
                                            // Je boucle sur mes enseignements
                                            for (int i = 0; i < enseignements.size(); i++) {
                                                JsonObject enseignement = enseignements.get(i);
                                                Integer id = enseignement.getInteger("id");
                                                JsonArray enseignement_competences_l1 = new JsonArray();
                                                // Je boucle sur les competences de niveau 1
                                                for (int j = 0; j < _competences_1.size(); j++) {
                                                    JsonObject _competence_1 = _competences_1.get(j);
                                                    // Si la compétence est dans l'enseignement
                                                    if (_competence_1.getInteger("idenseignement") == id) {
                                                        Integer _competence_1_id = _competence_1.getInteger("id");
                                                        JsonArray _competence_1_competences_l2 = new JsonArray();
                                                        // Je boucle dans les competences de niveau 2
                                                        for (int g = 0; g < _competences_2.size(); g++) {
                                                            JsonObject _competence_2 = _competences_2.get(g);
                                                            // Si la competence de niveau 2 est dans la competence de niveau 1
                                                            if (_competence_2.getInteger("idparent") == _competence_1_id) {
                                                                // J'ajoute la compétence de niveau 2 dans la liste de compétences de la compétence de niveau 1
                                                                _competence_1_competences_l2.addObject(_competence_2);
                                                            }
                                                        }
                                                        // Dans la compétence de niveau 1, j'ajoute la liste de compétences de niveau 2
                                                        _competence_1.putArray("competences_2", _competence_1_competences_l2);
                                                        enseignement_competences_l1.addObject(_competence_1);
                                                    }
                                                }
                                                // Dans l'enseignement, j'ajoute la liste de ses compétences de niveau 1
                                                enseignement.putArray("competences_1", enseignement_competences_l1);
                                                result.addObject(enseignement);
                                            }
                                            request.response().putHeader("content-type", "application/json; charset=utf-8").end(result.toString());
                                        } else {
                                            leftToResponse(request, eventCompetences_2.left());
                                        }
                                    }
                                });
                            } else {
                                leftToResponse(request, eventCompetences_1.left());
                            }
                        }
                    });
                } else {
                    leftToResponse(request, event.left());
                }
            }
        });
    }
}
