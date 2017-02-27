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
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.entcore.common.sql.SqlResult.validResultHandler;


/**
 * Created by ledunoiss on 05/08/2016.
 */
public class DefaultUtilsService  implements fr.openent.evaluations.service.UtilsService {
    private final Neo4j neo4j = Neo4j.getInstance();

    @Override
    /**
     * Récupère la liste des professeurs remplaçants du titulaire
     * (si lien titulaire/remplaçant toujours actif à l'instant T)
     *
     * @param psIdTitulaire identifiant neo4j du titulaire
     * @param psIdEtablissement identifiant de l'établissement
     * @param handler handler portant le resultat de la requête : la liste des identifiants neo4j des rempacants
     */
    public void getRemplacants(String psIdTitulaire, String psIdEtablissement, Handler<Either<String, JsonArray>> handler) {

        //TODO Methode à tester (pas utilisée pour le moment)

        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT DISTINCT id_remplacant ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".rel_professeurs_remplacants ")
                .append("WHERE id_titulaire = ? ")
                .append("AND id_etablissement = ? ")
                .append("AND date_debut <= current_date ")
                .append("AND current_date <= date_fin ");

        values.add(psIdTitulaire);
        values.add(psIdEtablissement);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    /**
     * Récupère la liste des professeurs titulaires d'un remplaçant sur un établissement donné
     * (si lien titulaire/remplaçant toujours actif à l'instant T)
     *
     * @param psIdRemplacant identifiant neo4j du remplaçant
     * @param psIdEtablissement identifiant de l'établissement
     * @param handler handler portant le resultat de la requête : la liste des identifiants neo4j des titulaires
     */
    public void getTitulaires(String psIdRemplacant, String psIdEtablissement, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT DISTINCT id_titulaire ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".rel_professeurs_remplacants ")
                .append("WHERE id_remplacant = ? ")
                .append("AND id_etablissement = ? ")
                .append("AND date_debut <= current_date ")
                .append("AND current_date <= date_fin ");

        values.add(psIdRemplacant);
        values.add(psIdEtablissement);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }


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
        Double noteMax = new Double(0);
        Double noteMin = new Double(diviseurM);
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
                if (currNote > noteMax) {
                    noteMax = currNote;
                }
                if (currNote < noteMin) {
                    noteMin = currNote;
                }
            }
        }
        Double moyenne = (notes/diviseur)*diviseurM;
        DecimalFormat df = new DecimalFormat("##.##");
        moyenne = Double.parseDouble(df.format(moyenne).replace(",", "."));
        JsonObject r = new JsonObject().putNumber("moyenne", moyenne);
        if (statistiques) {
            r.putNumber("noteMax", noteMax).putNumber("noteMin", noteMin);
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

    @Override
    public void list(String structureId, String classId, String groupId,
                          JsonArray expectedProfiles, String filterActivated, String nameFilter,
                          UserInfos userInfos, Handler<Either<String, JsonArray>> results) {
        JsonObject params = new JsonObject();
        String filter = "";
        String filterProfile = "WHERE 1=1 ";
        String optionalMatch =
                "OPTIONAL MATCH u-[:IN]->(:ProfileGroup)-[:DEPENDS]->(class:Class)-[:BELONGS]->(s) " +
                        "OPTIONAL MATCH u-[:RELATED]->(parent: User) " +
                        "OPTIONAL MATCH (child: User)-[:RELATED]->u " +
                        "OPTIONAL MATCH u-[rf:HAS_FUNCTION]->fg-[:CONTAINS_FUNCTION*0..1]->(f:Function) ";
        if (expectedProfiles != null && expectedProfiles.size() > 0) {
            filterProfile += "AND p.name IN {expectedProfiles} ";
            params.putArray("expectedProfiles", expectedProfiles);
        }
        if (classId != null && !classId.trim().isEmpty()) {
            filter = "(n:Class {id : {classId}})<-[:DEPENDS]-(g:ProfileGroup)<-[:IN]-";
            params.putString("classId", classId);
        } else if (structureId != null && !structureId.trim().isEmpty()) {
            filter = "(n:Structure {id : {structureId}})<-[:DEPENDS]-(g:ProfileGroup)<-[:IN]-";
            params.putString("structureId", structureId);
        } else if (groupId != null && !groupId.trim().isEmpty()) {
            filter = "(n:Group {id : {groupId}})<-[:IN]-";
            params.putString("groupId", groupId);
        }
        String condition = "";
        String functionMatch = "WITH u MATCH (s:Structure)<-[:DEPENDS]-(pg:ProfileGroup)-[:HAS_PROFILE]->(p:Profile), u-[:IN]->pg ";
        /*if (!userInfos.getFunctions().containsKey(SUPER_ADMIN) &&
                !userInfos.getFunctions().containsKey(ADMIN_LOCAL) &&
                !userInfos.getFunctions().containsKey(CLASS_ADMIN)) {
            results.handle(new Either.Left<String, JsonArray>("forbidden"));
            return;
        } else if (userInfos.getFunctions().containsKey(ADMIN_LOCAL)) {
            UserInfos.Function f = userInfos.getFunctions().get(ADMIN_LOCAL);
            List<String> scope = f.getScope();
            if (scope != null && !scope.isEmpty()) {
                condition = "AND s.id IN {scope} ";
                params.putArray("scope", new JsonArray(scope.toArray()));
            }
        } else if(userInfos.getFunctions().containsKey(CLASS_ADMIN)){
            UserInfos.Function f = userInfos.getFunctions().get(CLASS_ADMIN);
            List<String> scope = f.getScope();
            if (scope != null && !scope.isEmpty()) {
                functionMatch = "WITH u MATCH (c:Class)<-[:DEPENDS]-(cpg:ProfileGroup)-[:DEPENDS]->(pg:ProfileGroup)-[:HAS_PROFILE]->(p:Profile), u-[:IN]->pg ";
                condition = "AND c.id IN {scope} ";
                params.putArray("scope", new JsonArray(scope.toArray()));
            }
        }*/
        if(nameFilter != null && !nameFilter.trim().isEmpty()){
            condition += "AND u.displayName =~ {regex}  ";
            params.putString("regex", "(?i)^.*?" + Pattern.quote(nameFilter.trim()) + ".*?$");
        }
        if(filterActivated != null){
            if("inactive".equals(filterActivated)){
                condition += "AND NOT(u.activationCode IS NULL)  ";
            } else if("active".equals(filterActivated)){
                condition += "AND u.activationCode IS NULL ";
            }
        }

        String query =
                "MATCH " + filter + "(u:User) " +
                        functionMatch + filterProfile + condition + optionalMatch +
                        "RETURN DISTINCT u.id as id, p.name as type, u.externalId as externalId, " +
                        "u.activationCode as code, u.login as login, u.firstName as firstName, " +
                        "u.lastName as lastName, u.displayName as displayName, u.source as source, u.attachmentId as attachmentId, " +
                        "u.birthDate as birthDate, " +
                        "extract(function IN u.functions | last(split(function, \"$\"))) as aafFunctions, " +
                        "collect(distinct {id: s.id, name: s.name}) as structures, " +
                        "collect(distinct {id: class.id, name: class.name}) as allClasses, " +
                        "collect(distinct [f.externalId, rf.scope]) as functions, " +
                        "CASE WHEN parent IS NULL THEN [] ELSE collect(distinct {id: parent.id, firstName: parent.firstName, lastName: parent.lastName}) END as parents, " +
                        "CASE WHEN child IS NULL THEN [] ELSE collect(distinct {id: child.id, firstName: child.firstName, lastName: child.lastName, attachmentId : child.attachmentId }) END as children, " +
                        "HEAD(COLLECT(distinct parent.externalId)) as parent1ExternalId, " + // Hack for GEPI export
                        "HEAD(TAIL(COLLECT(distinct parent.externalId))) as parent2ExternalId " + // Hack for GEPI export
                        "ORDER BY type DESC, displayName ASC ";
        neo4j.execute(query, params,  Neo4jResult.validResultHandler(results));
    }



    /**
     * Récupère le cycle de la classe dans la relation classe_cycle
     * @param idClasse List Identifiant de classe.
     * @param handler Handler portant le résultat de la requête.
     */
    @Override
    public void getCycle(List<String> idClasse, Handler<Either<String, JsonArray>> handler){
        StringBuilder query =new StringBuilder();
        JsonArray params = new JsonArray();

        query.append("SELECT id_groupe, id_cycle ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".rel_groupe_cycle ")
                .append("WHERE id_groupe IN (");

        Integer classNbr = 0;
        for(String id :  idClasse){
            classNbr++;
            params.addString(id);
        }
        for(Integer j=0; j<classNbr-1; j++ ){
            query.append("? , ");
        }
        if(classNbr>0){
            query.append("?)");
        }
        else{
            query.append(")");
        }
        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));
    }

}
