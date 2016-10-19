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

package fr.openent.evaluations.service.impl;

import fr.openent.Viescolaire;
import fr.wseduc.webutils.Either;
import fr.openent.evaluations.bean.NoteDevoir;
import fr.openent.evaluations.service.UtilsService;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static org.entcore.common.sql.SqlResult.validResultHandler;
import static org.entcore.common.sql.SqlResult.validUniqueResultHandler;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public class DefaultUtilsService implements fr.openent.evaluations.service.UtilsService {
    private final Neo4j neo4j = Neo4j.getInstance();

    @Override
    public void listTypesDevoirsParEtablissement(String idEtablissement, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT type.* ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".type ")
                .append("WHERE type.id_etablissement = ? ");
        values.add(idEtablissement);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
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
    public JsonObject calculMoyenne(List<NoteDevoir> listeNoteDevoirs, Boolean statistiques, Integer diviseurM) {
        if(diviseurM == null){
            diviseurM = 20;
        }
        Double noteMin = new Double(0);
        Double noteMax = new Double(diviseurM);
        Double notes = new Double(0);
        Double diviseur = new Double(0);
        for (NoteDevoir noteDevoir : listeNoteDevoirs) {
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
            if (statistiques) {
                if (currNote > noteMin) {
                    noteMin = currNote;
                }
                if (currNote < noteMax) {
                    noteMax = currNote;
                }
            }
        }
        Double moyenne = (notes/diviseur)*diviseurM;
        DecimalFormat df = new DecimalFormat("##.##");
        moyenne = Double.parseDouble(df.format(moyenne).replace(",", "."));
        JsonObject r = new JsonObject().putNumber("moyenne", moyenne);
        if (statistiques) {
            r.putNumber("noteMin", noteMin).putNumber("noteMax", noteMax);
        }
        return r;
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
