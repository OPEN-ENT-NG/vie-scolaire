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

import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.ReferentielService;
import fr.openent.viescolaire.service.impl.DefaultReferentielService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import org.entcore.common.controller.ControllerHelper;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import java.util.Date;

/**
 * Created by ledunoiss on 02/11/2016.
 */
public class ReferentielController extends ControllerHelper {

    private final ReferentielService referentielService;

    public ReferentielController () {
        pathPrefix = Viescolaire.VSCO_PATHPREFIX;
        referentielService = new DefaultReferentielService();
        Logger log = LoggerFactory.getLogger(ReferentielController.class);
    }

    private void syncStructure(final HttpServerRequest request, final Handler<JsonObject> handler) {
        referentielService.syncStructure(request.params().getAll("externalid"), new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> event) {
                if (event.isRight()) {
                    JsonArray structures = event.right().getValue();
                    for (int i = 0; i < structures.size(); i++) {
                        JsonObject s = structures.get(i);
                        s = s.getObject("n").getObject("data");
                        referentielService.createStructure(s, new Handler<Either<String, JsonObject>>() {
                            @Override
                            public void handle(Either<String, JsonObject> event) {
                                if (event.isRight()) {
                                    JsonObject structure = event.right().getValue();
                                    handler.handle(structure);
                                } else {
                                    unauthorized(request);
                                }
                            }
                        });
                    }
                } else {
                    unauthorized(request);
                }
            }
        });
    }

    private void syncClasse(final HttpServerRequest request, final JsonObject structure, final Handler<Boolean> handler) {
        referentielService.syncClassesStructure(structure.getString("externalid"), new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> event) {
                if (event.isRight()) {
                    JsonArray classes = event.right().getValue();
                    JsonArray listeClasse = new JsonArray();
                    for (int i = 0; i < classes.size(); i++) {
                        JsonObject o = classes.get(i);
                        o = o .getObject("n").getObject("data");
                        listeClasse.addObject(o);
                    }
                    if (listeClasse.size() > 0) {
                        referentielService.createClasses(listeClasse, structure.getInteger("id"), 1, new Handler<Either<String, JsonObject>>() {
                            @Override
                            public void handle(Either<String, JsonObject> event) {
                                if (event.isRight()) {
                                    handler.handle(event.isRight());
                                } else {
                                    unauthorized(request);
                                }
                            }
                        });
                    }
                } else {
                    unauthorized(request);
                }
            }
        });
    }

    private void syncFunctionalGroups(final HttpServerRequest request, final JsonObject structure, final Handler<Boolean> handler) {
        referentielService.syncFunctionalsGroups(structure.getString("externalid"), new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> event) {
                if (event.isRight()) {
                    JsonArray classes = event.right().getValue();
                    JsonArray listeClasse = new JsonArray();
                    for (int i = 0; i < classes.size(); i++) {
                        JsonObject o = classes.get(i);
                        o = o .getObject("n").getObject("data");
                        listeClasse.addObject(o);
                    }
                    referentielService.createClasses(listeClasse, structure.getInteger("id"), 2, new Handler<Either<String, JsonObject>>() {
                        @Override
                        public void handle(Either<String, JsonObject> event) {
                            if (event.isRight()) {
                                handler.handle(event.isRight());
                            } else {
                                unauthorized(request);
                            }
                        }
                    });
                } else {
                    unauthorized(request);
                }
            }
        });
    }

    private void syncStudentsParents(final HttpServerRequest request, final JsonObject structure, final Handler<Boolean> handler) {
        referentielService.syncStudentsParents(structure.getString("externalid"), new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> event) {
                if (event.isRight()) {
                    JsonArray humans = event.right().getValue();
                    final JsonArray students = new JsonArray();
                    for (int i = 0; i < humans.size(); i++) {
                        JsonObject t = humans.get(i);
                        JsonObject eleve = t.getObject("eleve");
                        eleve = eleve.getObject("data");
                        JsonArray parents = new JsonArray();
                        JsonArray a = t.getArray("parents");
                        for (int y = 0; y < a.size(); y++) {
                            JsonObject o = a.get(y);
                            parents.addObject(o.getObject("data"));
                        }
                        eleve.putArray("parents", parents);
                        students.addObject(eleve);
                    }
                    referentielService.createStudentsParents(students, new Handler<Either<String, JsonObject>>() {
                        @Override
                        public void handle(Either<String, JsonObject> event) {
                            if (event.isRight()) {
                                event.right().getValue();
                                referentielService.linkStudentsParents(students, new Handler<Either<String, JsonObject>>() {
                                    @Override
                                    public void handle(Either<String, JsonObject> event) {
                                        if (event.isRight()) {
                                            handler.handle(event.isRight());
                                        } else {
                                            unauthorized(request);
                                        }
                                    }
                                });
                            } else {
                                unauthorized(request);
                            }
                        }
                    });
                } else {
                    unauthorized(request);
                }
            }
        });
    }

    private void syncPersonnels(final HttpServerRequest request, final JsonObject structure, final Handler<JsonArray> handler) {
        referentielService.syncPersonnels(structure.getString("externalid"), new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle(Either<String, JsonArray> event) {
                if (event.isRight()) {
                    JsonArray values = event.right().getValue();
                    JsonArray personnels = new JsonArray();
                    for (int i = 0; i < values.size(); i++) {
                        JsonObject personnel = values.get(i);
                        personnel = personnel.getObject("n");
                        personnels.addObject(personnel.getObject("data"));
                    }
                    referentielService.createPersonnels(personnels, structure.getString("externalid"), new Handler<Either<String, JsonObject>>() {
                        @Override
                        public void handle(Either<String, JsonObject> event) {
                            if (event.isRight()) {
                                referentielService.syncTeachers(structure.getString("externalid"), new Handler<Either<String, JsonArray>>() {
                                    @Override
                                    public void handle(Either<String, JsonArray> event) {
                                        if (event.isRight()) {
                                            JsonArray values = event.right().getValue();
                                            final JsonArray teachers = new JsonArray();
                                            for (int i = 0; i < values.size(); i++) {
                                                JsonObject teacher = values.get(i);
                                                teacher = teacher.getObject("n");
                                                teachers.addObject(teacher.getObject("data"));
                                            }
                                            referentielService.createTeachers(teachers, structure.getString("externalid"), new Handler<Either<String, JsonObject>>() {
                                                @Override
                                                public void handle(Either<String, JsonObject> event) {
                                                    if (event.isRight()) {
                                                        handler.handle(teachers);
                                                    } else {
                                                        unauthorized(request);
                                                    }
                                                }
                                            });
                                        } else {
                                            unauthorized(request);
                                        }
                                    }
                                });
                            } else {
                                unauthorized(request);
                            }
                        }
                    });
                } else {
                    unauthorized(request);
                }
            }
        });
    }

    private void syncMatieres (final HttpServerRequest request, final JsonArray teachers, final JsonObject structure, final Handler<Boolean> handler) {
        JsonArray matieresExtId = new JsonArray();
        for (int i = 0; i < teachers.size(); i++) {
            JsonObject teacher = teachers.get(i);
            if (teacher.containsField("classesFieldOfStudy")) {
                JsonArray classesFieldOfStudy = teacher.getArray("classesFieldOfStudy");
                for (int y = 0; y < classesFieldOfStudy.size(); y++) {
                    String fos = classesFieldOfStudy.get(y);
                    String fosExtId = fos.split("\\$")[2];
                    if (!matieresExtId.contains(fosExtId)) {
                        matieresExtId.add(fosExtId);
                    }
                }
            }
        }
        if (matieresExtId.size() > 0) {
            referentielService.syncMatieres(matieresExtId, new Handler<Either<String, JsonArray>>() {
                @Override
                public void handle(Either<String, JsonArray> event) {
                    if (event.isRight()) {
                        JsonArray matieresEnseignants = new JsonArray();
                        final JsonObject listeMatieres = new JsonObject();
                        JsonArray values = event.right().getValue();
                        for (int i = 0; i < values.size(); i++) {
                            JsonObject matiere = values.get(i);
                            matiere = matiere.getObject("n").getObject("data");
                            if (!listeMatieres.containsField("externalId")) {
                                listeMatieres.putObject(matiere.getString("externalId"), matiere);
                            }
                        }

                        for (int i = 0; i < teachers.size(); i++) {
                            JsonObject teacher = teachers.get(i);
                            if (teacher.containsField("classesFieldOfStudy")) {
                                JsonArray classesFieldOfStudy = teacher.getArray("classesFieldOfStudy");
                                for (int y = 0; y < classesFieldOfStudy.size(); y++) {
                                    String fos = classesFieldOfStudy.get(y);
                                    String classe = fos.split("\\$")[1];
                                    String fosExtId = fos.split("\\$")[2];
                                    JsonObject matiere = listeMatieres.getObject(fosExtId);
                                    if (matiere != null) {
                                        JsonObject o = new JsonObject();
                                        o.putString("classe", classe);
                                        o.putNumber("structureId", structure.getNumber("id"));
                                        o.putString("externalId", fosExtId);
                                        o.putString("userId", teacher.getString("id"));
                                        if (!matiere.containsField("name")) {
                                            matiere.getString("name");
                                        }
                                        o.putString("name", matiere.getString("name"));
                                        o.putString("fk4j_matiere_id", matiere.getString("id"));
                                        matieresEnseignants.addObject(o);
                                    }
                                }
                            }
                        }
                        referentielService.createMatiere(matieresEnseignants, new Handler<Either<String, JsonObject>>() {
                            @Override
                            public void handle(Either<String, JsonObject> event) {
                                if (event.isRight()) {
                                    handler.handle(event.isRight());
                                } else {
                                    unauthorized(request);
                                }
                            }
                        });
                    } else {
                        unauthorized(request);
                    }
                }
            });
        }
    }

    @Get("/sync/etablissement")
    @ApiDoc("Synchronise les établissements passés en paramètres")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void sync(final HttpServerRequest request) {
        if (!request.params().contains("externalid")) {
            unauthorized(request);
        }
        final Date startTime = new Date();
        syncStructure(request, new Handler<JsonObject>() {
            @Override
            public void handle(final JsonObject etab) {
                final JsonObject structure = etab;
                syncClasse(request, structure, new Handler<Boolean>() {
                    @Override
                    public void handle(Boolean b) {
                        if (b) {
                            syncFunctionalGroups(request, structure, new Handler<Boolean>() {
                                @Override
                                public void handle(Boolean b) {
                                    if (b) {
                                        syncStudentsParents(request, structure, new Handler<Boolean>() {
                                            @Override
                                            public void handle(Boolean b) {
                                                if (b) {
                                                    syncPersonnels(request, structure, new Handler<JsonArray>() {
                                                        @Override
                                                        public void handle(JsonArray teachers) {
                                                            syncMatieres(request, teachers, structure, new Handler<Boolean>() {
                                                                @Override
                                                                public void handle(Boolean b) {
                                                                    if (b) {
                                                                        request.response().putHeader("content-type", "application/json; charset=utf-8").end(
                                                                                new JsonObject().putNumber("status", 200)
                                                                                        .putString("time", String.valueOf(new Date().getTime() - startTime.getTime()))
                                                                                        .toString()
                                                                        );
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });

    }
}
