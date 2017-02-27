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

        query.append("MATCH (u:`User` {id:{userId}}),(f:`FieldOfStudy`) WHERE f.externalId in u.fieldOfStudy return f");
        values.putString("userId", userId);

        neo4j.execute(query.toString(), values, Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void listMatieres(String id, JsonArray poTitulairesIdList, Handler<Either<String, JsonArray>> result) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        if(poTitulairesIdList == null || poTitulairesIdList.size() == 0) {
            params.putString("id", id);
            query.append("MATCH (u:User {id:{id}}) ");
        } else{
            query.append("MATCH (u:User) WHERE u.id IN {userIdList} AND u.classesFieldOfStudy IS NOT null ");

            JsonArray oUserIdList = new JsonArray();
            oUserIdList.add(id);

            for (Object oTitulaire:poTitulairesIdList) {
                String sIdTitulaire = ((JsonObject)oTitulaire).getString("id_titulaire");
                oUserIdList.add(sIdTitulaire);
            }

            params.putArray("userIdList", oUserIdList);
        }
        query.append("return u.classesFieldOfStudy,u.groupsFieldOfStudy");

        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(result));
    }

    @Override
    public void getCorrespondanceMatieres(JsonArray codeMatieres, JsonArray codeEtablissement, Handler<Either<String, JsonArray>> result) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();
        params.putArray("listeMatieres", codeMatieres);
        params.putArray("listeEtablissements", codeEtablissement);
        query.append("MATCH (n:`FieldOfStudy`) WHERE n.externalId in {listeMatieres} RETURN n")
                .append(" UNION ")
                .append("MATCH (n:`Structure`) WHERE n.externalId in {listeEtablissements} RETURN n");
        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(result));
    }

    @Override
    public void getMatiere(List<String> ids, Handler<Either<String, JsonArray>> result) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();
        JsonArray matieresListe = new JsonArray();
        for(int i = 0 ; i < ids.size(); i++){
            matieresListe.addString(ids.get(i).toString());
        }
        params.putArray("ids", matieresListe);
        query.append("MATCH (n:`FieldOfStudy`) WHERE n.id in {ids} RETURN n");
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
        query.toString();
        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(result));
    }
}
