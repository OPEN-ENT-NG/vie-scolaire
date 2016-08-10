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

package org.cgi.evaluations.service.impl;

import fr.wseduc.webutils.Either;
import org.cgi.evaluations.bean.CEvalNoteDevoir;
import org.cgi.evaluations.service.IEvalUtilsService;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

import static org.entcore.common.sql.SqlResult.validResultHandler;
import static org.entcore.common.sql.SqlResult.validUniqueResultHandler;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public class CEvalUtilsServiceImpl implements IEvalUtilsService {
    private final Neo4j neo4j = Neo4j.getInstance();

    @Override
    public void listTypesDevoirsParEtablissement(String idEtablissement, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT type.* ")
                .append("FROM notes.type ")
                .append("WHERE type.idetablissement = ? ");
        values.add(idEtablissement);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void listPeriodesParEtablissement(String idEtablissement, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT periode.* ")
                .append("FROM notes.periode ")
                .append("WHERE periode.idetablissement = ? ")
                .append("ORDER BY periode.dateDebut ASC");
        values.add(idEtablissement);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
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
    public void listSousMatieres(String id, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT sousmatiere.id ,typesousmatiere.libelle ")
                .append("FROM notes.sousmatiere, notes.typesousmatiere ")
                .append("WHERE sousmatiere.id_typesousmatiere = typesousmatiere.id ")
                .append("AND sousmatiere.id_matiere = ? ");

        values.add(id);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void listMatieres(String id, Handler<Either<String, JsonArray>> result) {
        String query = "MATCH (u:User {id:{id}}) return u.classesFieldOfStudy";
        neo4j.execute(query.toString(), new JsonObject().putString("id", id), Neo4jResult.validResultHandler(result));
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
    public void getInfoEleve(String id, Handler<Either<String, JsonObject>> result) {
        StringBuilder query = new StringBuilder();

        query.append("MATCH (u:`User` {id: {id}}) ")
                .append("OPTIONAL MATCH ")
                .append("(n:`UserBook` {userid : {id}}) ")
                .append("OPTIONAL MATCH (c:`Class`) WHERE c.externalId in u.classes ")
                .append("RETURN u,n,c");
        neo4j.execute(query.toString(), new JsonObject().putString("id", id), Neo4jResult.validUniqueResultHandler(result));
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

    @Override
    public void getEnfants(String id, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        //query.append("MATCH (m:`User` {id: {id}})-[:COMMUNIQUE_DIRECT]->(n:`User`) RETURN n");
        query.append("MATCH (m:`User` {id: {id}})-[:COMMUNIQUE_DIRECT]->(n:`User`)-[:ADMINISTRATIVE_ATTACHMENT]->(s:`Structure`) RETURN n.id,n.displayName, n.classes , s.id");
        neo4j.execute(query.toString(), new JsonObject().putString("id", id), Neo4jResult.validResultHandler(handler));
    }
    /**
     * Fonction de calcul générique de la moyenne
     * @param listeNoteDevoirs : contient une liste de NoteDevoir.
     * Dans le cas ou les objets seraient des moyennes, toutes les propriétés ramener sur devront
     * être à false.
     *
     * @param diviseurM : diviseur de la moyenne. Par défaut, cette valeur est égale à 20 (optionnel).
     **/
    @Override
    public Double calculMoyenne(List<CEvalNoteDevoir> listeNoteDevoirs, Integer diviseurM) {
        if(diviseurM == null){
            diviseurM = 20;
        }
        Double notes = new Double(0);
        Double diviseur = new Double(0);

        for (CEvalNoteDevoir noteDevoir : listeNoteDevoirs) {

            Double currNote = noteDevoir.getNote();
            Double currCoefficient = noteDevoir.getCoefficient();
            Integer currDiviseur = noteDevoir.getDiviseur();

            if(noteDevoir.getRamenerSur()){

                if(currNote != null){
                    notes = notes + ((currNote * currCoefficient) * (new Double(diviseurM)/new Double(currDiviseur)));

                }
                diviseur = diviseur + (diviseurM*currCoefficient);
            }else{
                if(currNote != null){
                    notes = notes + (currNote * currCoefficient);
                }
                diviseur = diviseur + (currDiviseur * currCoefficient);
            }
        }
        Double moyenne = (notes/diviseur)*diviseurM;

        return moyenne;
    }

    /**
     * Recupere un periode sous sa representation en BDD
     * @param idPeriode identifiant de la periode
     * @param handler handler comportant le resultat
     */
    @Override
    public void getPeriode(Integer idPeriode, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT periode.* ")
                .append("FROM notes.periode ")
                .append("WHERE periode.id = ? ");
        values.add(idPeriode);

        Sql.getInstance().prepared(query.toString(), values, validUniqueResultHandler(handler));
    }

    /**
     * Recupere un établissemnt sous sa representation en BDD
     * @param id identifiant de l'etablissement
     * @param handler handler comportant le resultat
     */
    @Override
    public void getStructure(String id, Handler<Either<String, JsonObject>> handler) {
        String query = "match (s:`Structure`) where s.id = {id} return s";
        neo4j.execute(query, new JsonObject().putString("id", id), Neo4jResult.validUniqueResultHandler(handler));
    }
}
