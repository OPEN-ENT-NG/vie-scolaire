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

package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.MatiereService;
import fr.openent.viescolaire.service.SousMatiereService;
import fr.openent.viescolaire.service.UtilsService;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Renders;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.service.impl.SqlCrudService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.leftToResponse;

/**
 * Created by ledunoiss on 18/10/2016.
 */
public class DefaultMatiereService extends SqlCrudService implements MatiereService {

    private final Neo4j neo4j = Neo4j.getInstance();

    private UtilsService utilsService;
    private SousMatiereService sousMatiereService;

    public DefaultMatiereService () {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.VSCO_MATIERE_TABLE);
        utilsService = new DefaultUtilsService();
        sousMatiereService = new DefaultSousMatiereService();
    }

    @Override
    public void listMatieresEleve(String userId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject values = new JsonObject();

        query.append("MATCH (u:`User` {id:{userId}}),(s:Structure)<-[:SUBJECT]-(f:Subject)")
                .append(" WHERE f.code in u.fieldOfStudy and s.externalId in u.structures")
                .append(" return f.id as id, f.code as externalId, f.label as name");
        values.put("userId", userId);

        neo4j.execute(query.toString(), values, Neo4jResult.validResultHandler(handler));
    }
    @Override
    public void listMatieresEtab(String idStructure, Boolean onlyId, Handler<Either<String, JsonArray>> handler){
        String returndata;
        if (onlyId) {
            returndata = "RETURN collect(sub.id) as res ";
        }
        else {
            returndata = "RETURN s.id as idEtablissement, sub.id as id, sub.code as externalId, sub.label as name";
        }
        String query = "MATCH (sub:Subject)-[sj:SUBJECT]->(s:Structure {id: {idStructure}}) " +
                returndata;
        JsonObject values = new JsonObject().put("idStructure", idStructure);
        neo4j.execute(query, values, Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void listMatieres(String structureId , JsonArray aIdEnseignant, Handler<Either<String, JsonArray>> result) {
        String query = "MATCH (s:Structure {id : {structureId}})<-[:SUBJECT]-(sub:Subject)<-[r:TEACHES]-(u:User) " +
                "WHERE u.id IN {userIdList} RETURN u.id as idEnseignant, s.id as idEtablissement, sub.id as id, " +
                "sub.code as externalId, sub.label as name, r.classes as libelleClasses, r.groups as libelleGroupes";
        JsonObject params = new JsonObject().put("userIdList", aIdEnseignant).put("structureId", structureId);

        params.put("structureId", structureId);

        neo4j.execute(query, params, Neo4jResult.validResultHandler(result));
    }

    @Override
    public void listAllMatieres(String structureId, String idEnseignant, Boolean onlyId, Handler<Either<String, JsonArray>> handler) {
        utilsService.getTitulaires(idEnseignant, structureId, new Handler<Either<String, JsonArray>>() {
            @Override
            public void handle (Either < String, JsonArray > eventRemplacants) {
                if (eventRemplacants.isRight()) {
                    JsonArray aIdEnseignant = eventRemplacants.right().getValue();
                    aIdEnseignant.add(idEnseignant);

                    listMatieres(structureId, aIdEnseignant, new Handler<Either<String, JsonArray>>() {
                        @Override
                        public void handle(Either<String, JsonArray> event) {
                            if (event.isRight()) {
                                final JsonArray resultats = event.right().getValue();
                                if (resultats.size() > 0) {

                                    final List<String> ids = new ArrayList<String>();

                                    final JsonArray reponseJA = new fr.wseduc.webutils.collections.JsonArray();
                                    JsonArray libelleGroups, libelleClasses;
                                    for (Object res : resultats) {
                                        final JsonObject r = (JsonObject) res;
                                        libelleGroups = r.getJsonArray("libelleGroupes");
                                        libelleClasses = r.getJsonArray("libelleClasses");
                                        libelleGroups = libelleGroups == null ? new fr.wseduc.webutils.collections.JsonArray() : libelleGroups;
                                        libelleClasses = libelleClasses == null ? new fr.wseduc.webutils.collections.JsonArray() : libelleClasses;
                                        r.put("libelleClasses", utilsService.saUnion(libelleClasses, libelleGroups));
                                        r.remove("libelleGroupes");
                                        reponseJA.add(r);
                                        ids.add(r.getString("id"));
                                    }
                                    sousMatiereService.getSousMatiereById(ids.toArray(new String[0]),
                                            new Handler<Either<String, JsonArray>>() {
                                                @Override
                                                public void handle(Either<String, JsonArray> event_ssmatiere) {
                                                    if (event_ssmatiere.right().isRight()) {
                                                        JsonArray finalresponse = new fr.wseduc.webutils.collections.JsonArray();
                                                        JsonArray res = event_ssmatiere.right().getValue();
                                                        for (int i = 0; i < reponseJA.size(); i++) {
                                                            JsonObject matiere = reponseJA.getJsonObject(i);
                                                            String id = matiere.getString("id");
                                                            JsonArray ssms = new fr.wseduc.webutils.collections.JsonArray();
                                                            for (int j = 0; j < res.size(); j++) {
                                                                JsonObject ssm = res.getJsonObject(j);
                                                                if (ssm.getString("id_matiere").equals(id)) {
                                                                    ssms.add(ssm);
                                                                }
                                                            }
                                                            matiere.put("sous_matieres", ssms);
                                                            finalresponse.add(matiere);
                                                        }
                                                        handler.handle(new Either.Right<>(onlyId ? new JsonArray(ids) : finalresponse));
                                                    } else {
                                                        handler.handle(event_ssmatiere.left());
                                                    }
                                                }
                                            });
                                } else {
                                    listMatieresEtab(structureId, onlyId, handler);
                                }
                            } else {
                                handler.handle(event.left());
                            }
                        }
                    });

                } else {
                    handler.handle(eventRemplacants.left());
                }
            }
        });
    }


    @Override
    public void getEnseignantsMatieres(ArrayList<String> classesFieldOfStudy, Handler<Either<String, JsonArray>> result) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        query.append("MATCH (n:`User`) WHERE ");
        for(int i = 0; i < classesFieldOfStudy.size(); i++){
            query.append("{id")
                    .append(i)
                    .append("} in n.classesFieldOfStudy ");
            params.put("id"+i, classesFieldOfStudy.get(i));
            if(i != classesFieldOfStudy.size()-1){
                query.append("OR ");
            }
        }
        query.append("RETURN n");
        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(result));
    }

    @Override
    public void getMatieres(JsonArray idMatieres, Handler<Either<String, JsonArray>> result) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        query.append("MATCH (f:Subject) WHERE f.id IN {idMatieres} ")
                .append("RETURN f.id as id, f.code as externalId, f.label as name, f as data ");
        params.put("idMatieres", idMatieres);
        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(result));
    }

    @Override
    public void getMatiere(String idMatiere, Handler<Either<String, JsonObject>> result) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        query.append("  MATCH (n:Subject {id: {idMatiere}}) RETURN n ");
        params.put("idMatiere", idMatiere);
        neo4j.execute(query.toString(), params, Neo4jResult.validUniqueResultHandler(result));
    }
}
