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
import fr.wseduc.webutils.Either;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ledunoiss on 18/10/2016.
 */
public class DefaultMatiereService extends SqlCrudService implements MatiereService {

    private final Neo4j neo4j = Neo4j.getInstance();

    public DefaultMatiereService () {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.VSCO_MATIERE_TABLE);
    }

    @Override
    public void listMatieresEleve(String userId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject values = new JsonObject();

        query.append("MATCH (u:`User` {id:{userId}}),(s:Structure)<-[:SUBJECT]-(f:Subject)")
        .append(" WHERE f.code in u.fieldOfStudy and s.externalId in u.structures")
        .append(" return f.id as id, f.code as externalId, f.label as name");
        values.putString("userId", userId);

        neo4j.execute(query.toString(), values, Neo4jResult.validResultHandler(handler));
    }
    @Override
    public void listMatieresEtab(String idStructure, UserInfos user,  Handler<Either<String, JsonArray>> handler){
        String query = "MATCH (sub:Subject)-[sj:SUBJECT]->(s:Structure {id: {idStructure}}) " +
				"RETURN s.id as idEtablissement, sub.id as id, sub.code as externalId, sub.label as name";
        JsonObject values = new JsonObject().putString("idStructure", idStructure);
        neo4j.execute(query, values, Neo4jResult.validResultHandler(handler));
    }
    @Override
    public void listMatieres(String structureId , String id, JsonArray poTitulairesIdList, Handler<Either<String, JsonArray>> result) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        query.append("MATCH (s:Structure)<-[:SUBJECT]-(sub:Subject)<-[r:TEACHES]-");
        final String returnQuery = " return s.id as idEtablissement, sub.id as id, sub.code as externalId, sub.label as name, r.classes as libelleClasses, r.groups as libelleGroupes";

        if(poTitulairesIdList == null || poTitulairesIdList.size() == 0) {
            params.putString("id", id);
            query.append("(u:User{id:{id}}) where s.id = {structureId}");
        } else{
            query.append("(u:User) WHERE u.id IN {userIdList} AND s.id = {structureId} ");

            JsonArray oUserIdList = new JsonArray();
            oUserIdList.add(id);

            for (Object oTitulaire:poTitulairesIdList) {
                String sIdTitulaire = ((JsonObject)oTitulaire).getString("id_titulaire");
                oUserIdList.add(sIdTitulaire);
            }

            params.putArray("userIdList", oUserIdList);
        }
        params.putString("structureId", structureId);
        query.append(returnQuery);

        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(result));
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
            params.putString("id"+i, classesFieldOfStudy.get(i));
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
        params.putArray("idMatieres", idMatieres);
        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(result));
    }



    @Override
    public void getMatiere(String idMatiere, Handler<Either<String, JsonObject>> result) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        query.append("  MATCH (n:Subject {id: {idMatiere}}) RETURN n ");
        params.putString("idMatiere", idMatiere);
        neo4j.execute(query.toString(), params, Neo4jResult.validUniqueResultHandler(result));
    }
}
