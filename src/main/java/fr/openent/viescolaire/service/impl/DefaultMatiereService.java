/*
 * Copyright (c) Région Hauts-de-France, Département de la Seine-et-Marne, Région Nouvelle Aquitaine, Mairie de Paris, CGI, 2016.
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
 */

package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.ClasseService;
import fr.openent.viescolaire.service.MatiereService;
import fr.openent.viescolaire.service.SousMatiereService;
import fr.openent.viescolaire.service.UtilsService;
import fr.openent.viescolaire.utils.FormateFutureEvent;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.I18n;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.service.impl.SqlCrudService;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.sql.SqlResult;

import java.util.*;
import java.util.stream.Collectors;

import static fr.openent.Viescolaire.*;
import static fr.wseduc.webutils.Utils.handlerToAsyncHandler;

/**
 * Created by ledunoiss on 18/10/2016.
 */
public class DefaultMatiereService extends SqlCrudService implements MatiereService {

    private final Neo4j neo4j = Neo4j.getInstance();

    private UtilsService utilsService;
    private SousMatiereService sousMatiereService;
    private ClasseService classeService;
    private EventBus eb;
    private String subjectLibelleTable = VSCO_SCHEMA + "." + VSCO_MATIERE_LIBELLE_TABLE;
    private String modelSubjectLibelleTable = VSCO_SCHEMA + "." + VSCO_MODEL_MATIERE_LIBELLE_TABLE;

    public DefaultMatiereService(EventBus eb) {
        super(VSCO_SCHEMA, Viescolaire.VSCO_MATIERE_TABLE);
        this.eb = eb;
        utilsService = new DefaultUtilsService();
        sousMatiereService = new DefaultSousMatiereService();
        classeService = new DefaultClasseService();
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
            returndata = "RETURN s.id as idEtablissement, sub.id as id, sub.code as externalId, sub.source as source, " +
                    "sub.label as name, sub.externalId as external_id_subject ORDER BY name ";
        }
        String query = "MATCH (sub:Subject)-[sj:SUBJECT]->(s:Structure {id: {idStructure}}) " +
                returndata;
        JsonObject values = new JsonObject().put("idStructure", idStructure);
        neo4j.execute(query, values, Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void listMatieres(String structureId , JsonArray aIdEnseignant, JsonArray aIdMatiere, JsonArray aIdGroupe,Handler<Either<String, JsonArray>> result) {

        String query = "MATCH (s:Structure)<-[:SUBJECT]-(sub:Subject)<-[r:TEACHES]-(u:User) ";
        String returnValue = " WITH r.classes + r.groups as libelleClasses, s, u, sub " +
                "MATCH (s)--(c) WHERE (c:Class OR c:FunctionalGroup OR c:ManualGroup) AND c.externalId IN libelleClasses " +
                "RETURN u.id as idEnseignant, s.id as idEtablissement, sub.id as id, sub.code as externalId, sub.label as name, libelleClasses, COLLECT(c.id) as idClasses";
        String condition = "WHERE s.id = {structureId}";
        JsonObject params = new JsonObject().put("structureId", structureId);

        if(aIdEnseignant != null && aIdEnseignant.size() != 0) {
            condition += " AND u.id IN {userIdList}";
            params.put("userIdList", aIdEnseignant);
        }

        if(aIdMatiere != null && aIdMatiere.size() != 0) {
            condition += " AND sub.id IN {subjectIdList}";
            params.put("subjectIdList", aIdMatiere);
        }

        if(aIdGroupe != null && aIdGroupe.size() != 0) {
            condition += " AND c.id IN {groupeIdList}";
            params.put("groupeIdList", aIdGroupe);
        }

        params.put("structureId", structureId);

        neo4j.execute(query + condition + returnValue, params, Neo4jResult.validResultHandler(result));
    }

    @Override
    public void listAllMatieres(String structureId, String idEnseignant, Boolean onlyId, Handler<Either<String, JsonArray>> handler) {
        utilsService.getTitulaires(idEnseignant, structureId, eventRemplacants -> {
            if (eventRemplacants.isRight()) {
                JsonArray aIdEnseignant = eventRemplacants.right().getValue();
                aIdEnseignant.add(idEnseignant);

                listMatieres(structureId, aIdEnseignant, null, null, checkOverwrite(structureId, aIdEnseignant, event -> {
                    if (event.isRight()) {
                        final JsonArray resultats = event.right().getValue();
                        if (resultats.size() > 0) {

                            final List<String> ids = new ArrayList<>();

                            for (Object res : resultats) {
                                ids.add(((JsonObject) res).getString("id"));
                            }
                            if(onlyId) {
                                handler.handle(new Either.Right<>(new JsonArray(ids)));
                            } else {
                                addSousMatiere(ids, resultats,handler);
                            }
                        } else {
                            listMatieresEtabWithSousMatiere(structureId,onlyId,handler);
                        }
                    } else {
                        handler.handle(event.left());
                    }
                }));

            } else {
                handler.handle(eventRemplacants.left());
            }
        });
    }

    private void addSousMatiere(List<String> ids, JsonArray resultats, Handler<Either<String, JsonArray>> handler){

        sousMatiereService.getSousMatiereById(ids.toArray(new String[0]),
                event_ssmatiere -> {
                    if (event_ssmatiere.right().isRight()) {
                        JsonArray finalresponse = new fr.wseduc.webutils.collections.JsonArray();
                        JsonArray res = event_ssmatiere.right().getValue();
                        for (int i = 0; i < resultats.size(); i++) {
                            JsonObject matiere = resultats.getJsonObject(i);
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
                        handler.handle(new Either.Right<>(finalresponse));
                    } else {
                        handler.handle(event_ssmatiere.left());
                    }
                });
    }

    public void listMatieresEtabWithSousMatiere(String structureId,
                                                Boolean onlyId,
                                                Handler<Either<String, JsonArray>> handler ){

        listMatieresEtab(structureId, onlyId, event2 -> {
            if (event2.isRight()) {

                if(onlyId) {
                    handler.handle(event2.right());
                } else {
                    JsonArray matieresEtab = event2.right().getValue();
                    if(matieresEtab.size() > 0){
                        final List<String> ids = new ArrayList<>();

                        for (Object res : matieresEtab) {
                            ids.add(((JsonObject) res).getString("id"));
                        }
                        addSousMatiere(ids,matieresEtab,handler);
                    }else{
                        handler.handle(new Either.Left("no subject"));
                    }

                }
            }else{
                handler.handle(event2.left());
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

    private Handler<Either<String, JsonArray>> checkOverwrite(String idStructure, JsonArray aIdEnseignant, Handler<Either<String, JsonArray>> handler) {

        return matieres -> {
            if(matieres.isRight() && matieres.right().getValue().size() > 0) {
                Set<Service> aMatieres = new HashSet<>();
                aMatieres.addAll(matieres.right().getValue().stream().map(serv -> new Service((JsonObject) serv)).collect(Collectors.toList()));

                JsonObject action = new JsonObject()
                        .put("action", "service.getServices")
                        .put("idStructure", idStructure)
                        .put("aIdEnseignant", aIdEnseignant);

                eb.send(Viescolaire.COMPETENCES_BUS_ADDRESS, action,
                        handlerToAsyncHandler(message -> {
                            JsonObject body = message.body();

                            if ("ok".equals(body.getString("status"))) {
                                Set<Service> aServices = new HashSet<>();
                                aServices.addAll(body.getJsonArray("results").stream().map(serv -> new Service((JsonObject) serv)).collect(Collectors.toList()));

                                Set idClasses = new HashSet();
                                utilsService.pluck(matieres.right().getValue(), "idClasses").forEach(array -> idClasses.addAll(((JsonArray) array).getList()));
                                idClasses.addAll(utilsService.pluck(body.getJsonArray("results"), "id_groupe"));

                                Set idMatieres = new HashSet<>();
                                idMatieres.addAll(utilsService.pluck(matieres.right().getValue(), "id"));
                                idMatieres.addAll(utilsService.pluck(body.getJsonArray("results"), "id_matiere"));

                                classeService.getClassesInfo(new JsonArray(new ArrayList(idClasses)), classesEvent -> {
                                    if (classesEvent.isRight()) {
                                        JsonArray classes = classesEvent.right().getValue();

                                        getMatieres(new JsonArray(new ArrayList(idMatieres)), matieresEvent -> {
                                            if (matieresEvent.isRight()) {
                                                JsonArray newMatieres = matieresEvent.right().getValue();

                                                for (Service oService : aServices) {

                                                    Service matiereFound = (Service) utilsService.find(aMatieres, mat -> oService.equals((Service) mat));
                                                    if(matiereFound != null) {
                                                        if(oService.evaluable) {
                                                            matiereFound.addClasses(oService.idClasses);
                                                        } else {
                                                            matiereFound.rmClasses(oService.idClasses);
                                                        }
                                                    } else {
                                                        aMatieres.add(oService);
                                                    }
                                                }

                                                handler.handle(new Either.Right<>(new JsonArray(new ArrayList(aMatieres.parallelStream().filter(oMat -> !oMat.idClasses.isEmpty()).map(oMat -> {
                                                    oMat.fillMissingValues(newMatieres, classes);
                                                    return oMat.toJson();
                                                }).collect(Collectors.toList())))));
                                            } else {
                                                handler.handle(matieresEvent.left());
                                            }
                                        });
                                    } else {
                                        handler.handle(classesEvent.left());
                                    }
                                });
                            }
                        })
                );

            } else if (matieres.isRight()) {
                handler.handle(matieres.right());
            } else {
                handler.handle(matieres.left());
            }
        };
    }

    private class Service {

        public String idMatiere, idEnseignant, idEtablissement, name, externalId, modalite;
        public HashSet idClasses, libelleClasses;
        public boolean evaluable;

        public Service (JsonObject matiere) {
            if(matiere.containsKey("id") && matiere.containsKey("idEnseignant") && matiere.containsKey("idClasses")) {
                this.idMatiere = matiere.getString("id");
                this.idEnseignant = matiere.getString("idEnseignant");
                this.idEtablissement = matiere.getString("idEtablissement");
                this.name = matiere.getString(NAME);
                this.externalId = matiere.getString("externalId");
                this.idClasses = matiere.containsKey("idClasses")
                        ? new HashSet(matiere.getJsonArray("idClasses").getList())
                        : new HashSet();
                this.libelleClasses = matiere.containsKey("libelleClasses")
                        ? new HashSet(matiere.getJsonArray("libelleClasses").getList())
                        : new HashSet();
            } else {
                this.idMatiere = matiere.getString("id_matiere");
                this.idEnseignant = matiere.getString("id_enseignant");
                this.idEtablissement = matiere.getString("id_etablissement");
                this.evaluable = matiere.getBoolean("evaluable");
                this.modalite = matiere.getString("modalite");
                this.idClasses = new HashSet();
                this.idClasses.add(matiere.getString("id_groupe"));
                this.libelleClasses = new HashSet();
            }
        }

        public void addClasses(Set<String> classes) {
            for(String s : classes) {
                this.idClasses.add(s);
            }
        }

        public void rmClasses(Set<String> classes) {
            for (String s : classes) {
                this.idClasses.remove(s);
            }
        }

        public void fillMissingValues(JsonArray matieres, JsonArray classes) {
            JsonArray classeToKeep = utilsService.filter(classes, classe -> this.idClasses.contains(((JsonObject) classe).getString("id")));
            this.libelleClasses = new HashSet(utilsService.pluck(classeToKeep, EXTERNAL_ID_KEY));

            JsonObject matiere = utilsService.findWhere(matieres, new JsonObject().put("id", this.idMatiere));
            this.name = matiere.getString(NAME);
            this.externalId = matiere.getString(EXTERNAL_ID_KEY);
        }

        public boolean equals (Service s) {
            return s.idMatiere.equals(this.idMatiere) && s.idEnseignant.equals(this.idEnseignant);
        }

        public int hashCode () {
            return Objects.hash(this.idMatiere, this.idEnseignant);
        }

        public boolean isValid() {
            return this.idEnseignant != null && this.idMatiere != null && this.idClasses.size() > 0 && this.libelleClasses.size() > 0;
        }

        public JsonObject toJson() {
            return new JsonObject()
                    .put("id", this.idMatiere)
                    .put(EXTERNAL_ID_KEY, this.externalId)
                    .put(ID_ETABLISSEMENT_KEY, this.idEtablissement)
                    .put("libelleClasses", new JsonArray(new ArrayList(this.libelleClasses)))
                    .put(NAME, this.name);
            }
    }
}
