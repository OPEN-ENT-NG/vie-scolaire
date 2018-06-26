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
import fr.openent.viescolaire.service.EleveService;
import fr.openent.viescolaire.service.UtilsService;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.Utils;
import io.vertx.core.eventbus.Message;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;

import static org.entcore.common.sql.SqlResult.validResultHandler;

/**
 * Created by ledunoiss on 10/02/2016.
 */
public class DefaultEleveService extends SqlCrudService implements EleveService {

    public DefaultEleveService() {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.VSCO_ELEVE_TABLE);
    }

    private final Neo4j neo4j = Neo4j.getInstance();

    @Override
    public void getEleveClasse(String pSIdClasse,Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        query.append("Match (c:Class{id: {idClasse} }) with c ")
                .append( "MATCH (u:User{profiles :['Student']}) where c.externalId IN u.classes  ")
                .append( "RETURN u.id as id, u.firstName as firstName, u.lastName as lastName,  u.level as level, u.classes as classes ORDER BY lastName");

        neo4j.execute(query.toString(), new JsonObject().put("idClasse", pSIdClasse), Neo4jResult.validResultHandler(handler));

    }

    @Override
    public void getEvenements(String psIdEleve, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();

        query.append("SELECT evenement.* ")
                .append("FROM "+ Viescolaire.ABSC_SCHEMA +".evenement, "+ Viescolaire.VSCO_SCHEMA +".cours, "+ Viescolaire.ABSC_SCHEMA +".appel ")
                .append("WHERE evenement.id_eleve = ? ")
                .append("AND evenement.id_appel = appel.id ")
                .append("AND appel.id_cours = cours.id ")
                .append("AND to_date(?, 'DD-MM-YYYY') < cours.timestamp_dt ")
                .append("AND cours.timestamp_fn < to_date(?, 'DD-MM-YYYY')");

        values.add(new Integer(psIdEleve));
        values.add(psDateDebut);
        values.add(psDateFin);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getEleve(String idEtab,  Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        query.append(" MATCH (u:User)-[r:ADMINISTRATIVE_ATTACHMENT]->(s:Structure) where u.profiles= [\"Student\"] and s.id = {idEtab}  with u ")
                .append("Optional MATCH (g:FunctionalGroup)<-[IN]-(u) ")
                .append("RETURN u.id as id, u.firstName as firstName, u.lastName as lastName, u.birthDate as birthDate, u.level as level, collect(g.id) as groupesId, u.classes as classesId  ");
        neo4j.execute(query.toString(), new JsonObject().put("idEtab", idEtab), Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void getEleves(String idEtab,  Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        query.append(" MATCH (f:FunctionalGroup)<-[i:IN]-(u:User)-[r:ADMINISTRATIVE_ATTACHMENT]->(s:Structure) where u.profiles= [\"Student\"] and s.id = {idEtab}  with u, collect(f.id) as f ")
                .append("Match (c:Class) where c.externalId IN u.classes ")
                .append("RETURN u.id as id, u.firstName as firstName, u.lastName as lastName,  u.level as level, collect(c.id) as classes ,f as groupes");
        neo4j.execute(query.toString(), new JsonObject().put("idEtab", idEtab), Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void getResponsables(String idEleve, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        query.append("MATCH (u:User {profiles: ['Student']})-[:RELATED]->(r:User {profiles: ['Relative']}) ")
                .append("WHERE u.id= {idEleve} ")
                .append("RETURN r.id AS id, r.address AS address, r.city AS city, r.zipCode AS zipCode, r.country AS country, ")
                .append("r.lastName AS lastName, r.firstName AS firstName, r.homePhone AS homePhone, r.workPhone AS workPhone, r.mobilePhone AS mobilePhone");
        params.put("idEleve", idEleve);

        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(handler));
    }
    @Override
    public void getEnseignants(String idEleve, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        query.append(" MATCH (u:User {id:{idEleve}}), ")
                .append("(s:Structure)<-[:SUBJECT]-(f:Subject)<-[r:TEACHES]-(ens:User)")
                .append("  WHERE f.code in u.fieldOfStudy and s.externalId in u.structures ")
                .append(" WITH u, r, ens, f, s ")
                .append(" UNWIND ens.classes as j")
                .append(" WITH u,r, ens, f, j, s ")
                .append(" WHERE j in u.classes and f.code in u.fieldOfStudy and s.externalId in u.structures ")
                .append("  return ens.id as id, ens.firstName as firstName, ens.surname as name, f.id as id_matiere")
                .append(" , f.label as name_matiere");
        params.put("idEleve", idEleve);

        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(handler));
    }
    @Override
    public void getInfoEleve(String[] idEleves, Handler<Either<String, JsonArray>> handler) {

        // Récupération d'élèves en présuppression
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        query.append("MATCH (u:User),(s:Structure),(c:Class)  ")
                .append(" WHERE ")
                .append(" u.profiles= [\"Student\"]  ")
                .append(" AND u.id IN {idEleves}")
                .append("  AND c.externalId IN u.classes ")
                .append("  AND s.externalId IN u.structures")
                .append("     with u, c, s")
                .append(" OPTIONAL MATCH (f:FunctionalGroup)<-[i:IN]-(u) with  u, c, s, f")
                .append(" OPTIONAL MATCH (g:ManualGroup)<-[i:IN]-(u)")

                // Format de Retour des données
                .append("RETURN u.id as idEleve, u.firstName as firstName, u.lastName as lastName, ")
                .append(" u.deleteDate,c.id as idClasse, c.name as classeName, s.id as idEtablissement, ")
                .append(" COLLECT(f.id) as idGroupes, ")
                .append(" COLLECT(g.id) as idManualGroupes")
                .append(" ORDER BY lastName ");

        params.put("idEleves", new fr.wseduc.webutils.collections.JsonArray(Arrays.asList(idEleves)));


        // Rajout des élèves supprimés au résultat
        String [] sortedField = new  String[1];
        sortedField[0]= "lastName";
        neo4j.execute(query.toString(), params, new DefaultUtilsService().addStoredDeletedStudent(null,
                null,idEleves, sortedField, null, handler));
    }

    @Override
    public void getUsers(JsonArray idUsers, Handler<Either<String, JsonArray>> result) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        query.append("MATCH (u:`User`) WHERE u.id IN {idUsers} ")
                .append("RETURN u.id as id, u.firstName as firstName, u.surname as name, u.displayName as displayName, ")
                .append("u as data ");
        params.put("idUsers", idUsers);
        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(result));
    }


    @Override
    public void getAnnotations(String idEleve, Long idPeriode, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();

        query.append("SELECT id_devoir, annotations.*, id_eleve, owner, id_matiere, name, is_evaluated, id_periode,")
                .append("id_type, diviseur, date_publication, date, apprec_visible, coefficient,devoirs.libelle as lib")
                .append(", type.nom as _type_libelle ")
                .append(" FROM notes.rel_annotations_devoirs inner JOIN notes.devoirs on devoirs.id = id_devoir ")
                .append(" inner JOIN notes.annotations on annotations.id = id_annotation ")
                .append(" inner join notes.type on devoirs.id_type = type.id  ")
                .append(" WHERE date_publication <= NOW() AND id_eleve = ? ");
        values.add(idEleve);
        if(idPeriode != null){
            query.append(" AND id_periode = ? ");
            values.add(idPeriode);
        }
        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }
    @Override
    public void getCompetences(String idEleve, Long idPeriode, JsonArray idGroups, Long idCycle,
                               Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();

        // competences_notes.id IN " + Sql.listPrepared(idNotes.toArray())
        query.append( "SELECT res.id_devoir,id_competence , max(evaluation) as evaluation, owner, id_eleve, ")
                .append(" created, modified , id_matiere, name, is_evaluated, id_periode, ")
                .append("  id_type, diviseur, date_publication, date, apprec_visible, coefficient, libelle ")
                .append("  , _type_libelle, owner_name  FROM ( ")
                .append(" select cd.id_devoir,cd.id_competence , evaluation, cn.owner, id_eleve, ")
                .append(" devoirs.created, devoirs.modified, devoirs.id_matiere, name, devoirs.is_evaluated, ")
                .append(" id_periode, devoirs.id_type, diviseur, date_publication, date, apprec_visible, coefficient, libelle")
                .append(" , type.nom as _type_libelle, users.username as owner_name ")
                .append(" from notes.competences_devoirs as cd ")
                .append(" inner join notes.competences on cd.id_competence = competences.id ")
                .append(" inner join notes.competences_notes as cn on cd.id_devoir = cn.id_devoir ")
                .append(" and  cd.id_competence = cn.id_competence ")
                .append(" inner JOIN notes.devoirs on devoirs.id = cd.id_devoir ")
                .append(" inner join notes.type on devoirs.id_type = type.id ")
                .append(" INNER JOIN "+ Viescolaire.EVAL_SCHEMA +".users ON (users.id = devoirs.owner) ")
                .append(" WHERE date_publication <= Now() AND id_eleve = ? ");
        values.add(idEleve);
        if(idPeriode != null){
            query.append(" AND id_periode = ? ");
            values.add(idPeriode);
        }
        query.append(" UNION ")
                .append(" select competences_devoirs.id_devoir, competences_devoirs.id_competence , ")
                .append(" -1 as evaluation, owner, ? as id_eleve, ")
                .append(" created, modified, id_matiere, name, is_evaluated, id_periode, " )
                .append(" id_type, diviseur, date_publication, date, apprec_visible, coefficient, libelle")
                .append(" , type.nom as _type_libelle , users.username as owner_name ")
                .append(" from notes.competences_devoirs inner JOIN notes.devoirs on ")
                .append(" devoirs.id = competences_devoirs.id_devoir " )
                .append(" inner join notes.type on devoirs.id_type = type.id ")
                .append(" inner JOIN notes.rel_devoirs_groupes on ")
                .append(" competences_devoirs.id_devoir = rel_devoirs_groupes.id_devoir ")
                .append(" inner join "+ Viescolaire.EVAL_SCHEMA +".users ON (users.id = devoirs.owner) ")
                .append(" AND rel_devoirs_groupes.id_groupe IN " + Sql.listPrepared(idGroups.getList()))
                .append(" WHERE date_publication <= Now()") ;
        values.add(idEleve);
        for (int i = 0; i < idGroups.size(); i++) {
            values.add(idGroups.getString(i));
        }
        if(idPeriode != null){
            query.append(" AND id_periode = ? ");
            values.add(idPeriode);
        }
        if (idCycle != null) {
            query.append("AND competences.id_cycle = ? ");
            values.add(idCycle);
        } else {
            query.append("AND devoirs.eval_lib_historise = false ");
        }
        query.append(" ) AS res ")
                .append(" GROUP BY res.id_devoir,id_competence, owner, id_eleve, created, modified, " )
                .append("id_matiere, name, is_evaluated, id_periode, id_type, diviseur, date_publication, ")
                .append(" date, apprec_visible, coefficient, libelle , _type_libelle, owner_name ");

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }
    @Override
    public void getCycle(String idClasse,Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();
        query.append("SELECT id_cycle ")
                .append(" FROM notes.rel_groupe_cycle ")
                .append(" WHERE id_groupe = ? ");
        values.add(idClasse);
        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }
    @Override
    public void getAppreciationDevoir(Long idDevoir, String idEleve, Handler<Either<String, JsonArray>> handler){
        StringBuilder query = new StringBuilder();
        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();
        query.append("SELECT valeur as appreciation  ")
                .append(" FROM notes.appreciations INNER JOIN notes.devoirs  ON appreciations.id_devoir = devoirs.id ")
                .append(" WHERE id_eleve = ?  AND id_devoir = ? AND devoirs.apprec_visible = true");
        values.add(idEleve);
        values.add(idDevoir);
        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }
    @Override
    public void getGroups(String idEleve, Handler<Either<String, JsonArray>> result) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        query.append("MATCH (n:FunctionalGroup)-[:IN]-(u:User{id:{userId}}) ")
                .append(" WITH  n, u ")
                .append(" MATCH (c:Class) WHERE c.externalId IN u.classes RETURN n.id as id_groupe ")
                .append(" UNION MATCH (n:ManualGroup)-[:IN]-(u:User{id:{userId}}) ")
                .append(" WITH  n, u ")
                .append(" MATCH (c:Class) WHERE c.externalId IN u.classes RETURN n.id as id_groupe ");
        params.put("userId", idEleve);

        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(result));
    }

    @Override
    public void getStoredDeletedStudent(JsonArray idClasse,String idStructure, String[] idEleves,
                                        Handler<Either<String, JsonArray>> handler){

        StringBuilder query = new StringBuilder();
        JsonArray values  = new fr.wseduc.webutils.collections.JsonArray();

        // Sélection des critères de récupérations
         if (idClasse != null){
             for(int i=0; i< idClasse.size(); i++) {
                 values.add(idClasse.getValue(i));
             }
        }
        if (idStructure != null) {
            values.add(idStructure);
        }
        if (idEleves != null) {
            for(int i=0; i < idEleves.length; i++ ) {
                values.add(idEleves[i]);
            }
        }

        // Requête finale
        query.append(" SELECT DISTINCT  personnes_supp.id_user as id, birth_date as \"birthDate\"," )
                .append(" personnes_supp.id_user as \"idEleve\", display_name as \"displayName\", ")
                .append(" delete_date as \"deleteDate\", first_name as \"firstName\", last_name as \"lastName\", ")
                .append(" string_agg(distinct rel_groupes_personne_supp.id_groupe , ',') AS \"idGroupes\",  ")
                .append(" string_agg(distinct rel_groupes_personne_supp.id_groupe , ',') AS \"idClasse\"")
                .append(" FROM " + Viescolaire.VSCO_SCHEMA + ".personnes_supp")

                // Jointure table de relation structure
                .append(" INNER JOIN "+ Viescolaire.VSCO_SCHEMA + ".rel_structures_personne_supp ")
                .append(" ON personnes_supp.id_user = rel_structures_personne_supp.id_user ")
                .append(" AND user_type = 'Student' ")
                .append((idStructure != null)?" AND id_structure = ? " : "")
                .append((idEleves != null)? " AND personnes_supp.id_user IN " + Sql.listPrepared(idEleves): "")

                // Jointure table de relation structure
                .append(" INNER JOIN "+ Viescolaire.VSCO_SCHEMA + ".rel_groupes_personne_supp ")
                .append(" ON personnes_supp.id_user = rel_groupes_personne_supp.id_user ")
                .append((idClasse != null)? "WHERE id_groupe IN" + Sql.listPrepared(idClasse.getList().toArray()): "")
                .append(" GROUP BY  personnes_supp.id_user ");

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));

    }


}
