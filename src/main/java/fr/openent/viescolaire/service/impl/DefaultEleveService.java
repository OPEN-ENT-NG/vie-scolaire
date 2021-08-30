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
import fr.openent.viescolaire.helper.RelativeHelper;
import fr.openent.viescolaire.model.Person.Relative;
import fr.openent.viescolaire.service.EleveService;
import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.VertxException;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.neo4j.Neo4j;
import org.entcore.common.neo4j.Neo4jResult;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.PatternSyntaxException;

/**
 * Created by ledunoiss on 10/02/2016.
 */
public class DefaultEleveService extends SqlCrudService implements EleveService {

    public DefaultEleveService() {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.VSCO_ELEVE_TABLE);
    }

    private final Neo4j neo4j = Neo4j.getInstance();
    protected static final Logger log = LoggerFactory.getLogger(DefaultEleveService.class);

    public static final Integer PAGE_SIZE = 20;


    @Override
    public void getEleveClasse(String pSIdClasse,Handler<Either<String, JsonArray>> handler) {
        StringBuilder returning = new StringBuilder()
                .append( "RETURN u.id as id, u.firstName as firstName, u.lastName as lastName,  u.level as level, ")
                .append( " u.classes as classes, c.name as className ORDER BY lastName");
        StringBuilder query = new StringBuilder();

        query.append(" MATCH (c:Class{id: {idClasse} })-[b:BELONGS]->(s)  with c, s ")
                .append(" MATCH (u:User {profiles:[\"Student\"]})-[:IN]->(:ProfileGroup)-[:DEPENDS]->(s) ")
                .append(" where c.externalId IN u.classes  ")
                .append(returning)

                .append(" UNION ")

                .append(" MATCH (u:User{profiles:[\"Student\"]})-[IN]->(d:DeleteGroup), ")
                .append(" (c:Class{id: {idClasse})-[b:BELONGS]->(s) ")
                .append(returning);


        neo4j.execute(query.toString(), new JsonObject().put("idClasse", pSIdClasse),
                Neo4jResult.validResultHandler(handler));

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
                .append("  return ens.id as id, ens.firstName as firstName, ens.lastName as name, f.id as id_matiere")
                .append(" , f.label as name_matiere");
        params.put("idEleve", idEleve);

        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void getInfoEleve(String[] idEleves, String idEtablissement, Handler<Either<String, JsonArray>> handler) {
        // Format de Retour des données
        StringBuilder returning = new StringBuilder()
                .append(" RETURN u.id as idEleve, u.firstName as firstName, u.lastName as lastName, u.ine as ine, ")
                .append(" u.deleteDate,c.id as idClasse, c.name as classeName, s.id as idEtablissement, ")
                .append(" u.birthDate as birthDate, u.level as level, c.externalId as externalId, ");

        // Condition de récupération des noeuds
        StringBuilder condition = new StringBuilder()
                .append(" WHERE ")
                .append(" u.id IN {idEleves}")
                .append(" AND c.externalId IN u.classes ")
                .append(" AND s.externalId IN u.structures");

        StringBuilder order = new StringBuilder()
                .append(" ORDER BY lastName, firstName ");

        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        // Récupération d'élèves non supprimés
        query.append("MATCH (u:User {profiles:[\"Student\"]})-[:IN]->(:ProfileGroup)-[:DEPENDS]->")
                .append("(s:Structure {id:{idStructure}}), (c:Class)-[b:BELONGS]->(s)  ")
                .append(condition)
                .append(" with u, c, s")
                .append(" OPTIONAL MATCH (f:FunctionalGroup)<-[i:IN]-(u) with  u, c, s, f")
                .append(" OPTIONAL MATCH (g:ManualGroup)<-[i:IN]-(u) ")
                .append(returning)
                .append(" COLLECT(f.id) as idGroupes, ")
                .append(" COLLECT(g.id) as idManualGroupes ")
                .append(order)

                .append(" UNION ")

                // Récupération des élèves supprimés présents dans l'annuaire
                .append(" MATCH (u:User{profiles:[\"Student\"]})-[IN]->(d:DeleteGroup), (c:Class)-[b:BELONGS]->(s) ")
                .append(condition)
                .append(returning)
                .append(" [] as idGroupes, ")
                .append(" [] as idManualGroupes ")
                .append(order);

        params.put("idStructure", idEtablissement)
                .put("idEleves", new fr.wseduc.webutils.collections.JsonArray(Arrays.asList(idEleves)));

        // Rajout des élèves supprimés au résultat
        try {
            neo4j.execute(query.toString(), params, new DefaultUtilsService().getEleveWithClasseName(null,
                    idEleves, null, handler));
        } catch (VertxException e){
            String error = e.getMessage();
            log.error("getInfoEleve " + e.getMessage());
            if(error.contains("Connection was closed")) {
                getInfoEleve(idEleves, idEtablissement, handler);
            }
        }
    }

    @Override
    public void getUsers(JsonArray idUsers, Handler<Either<String, JsonArray>> result) {
        StringBuilder query = new StringBuilder();
        JsonObject params = new JsonObject();

        query.append("MATCH (u:`User`) WHERE u.id IN {idUsers} ")
                .append("RETURN u.id as id, u.firstName as firstName, u.lastName as name, u.displayName as displayName, ")
                .append("u.birthDate AS birthDate, u as data ");
        params.put("idUsers", idUsers);
        neo4j.execute(query.toString(), params, Neo4jResult.validResultHandler(result));
    }

    @Override
    public void getAnnotations(String idEleve, Long idPeriode, JsonArray idGroups, String idMatiere,
                               Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();

        query.append("SELECT rel_annotations_devoirs.id_devoir, annotations.*, id_eleve, owner, id_matiere, id_sousmatiere, name, is_evaluated, id_periode, ")
                .append("id_type, diviseur, date_publication, date, apprec_visible, coefficient, devoirs.libelle as lib, ")
                .append("type.nom as _type_libelle, sum_notes, nbr_eleves, id_groupe ")
                .append("FROM notes.rel_annotations_devoirs ")
                .append("INNER JOIN notes.devoirs on devoirs.id = id_devoir ")
                .append("INNER JOIN notes.annotations on annotations.id = id_annotation ")
                .append("INNER JOIN notes.type on devoirs.id_type = type.id ")
                .append("INNER JOIN notes.rel_devoirs_groupes ON devoirs.id = rel_devoirs_groupes.id_devoir ")
                .append("LEFT JOIN (SELECT devoirs.id, SUM(notes.valeur) as sum_notes, COUNT(notes.valeur) as nbr_eleves ")
                .append("FROM notes.devoirs INNER JOIN notes.notes on devoirs.id = notes.id_devoir ")
                .append("WHERE date_publication <= Now() ")
                .append("GROUP BY devoirs.id) sum ON sum.id = devoirs.id ");

        if(idGroups != null) {
            query.append("AND rel_devoirs_groupes.id_groupe IN ").append(Sql.listPrepared(idGroups.getList()));
            for (int i = 0; i < idGroups.size(); i++) {
                values.add(idGroups.getString(i));
            }
        }

        query.append(" WHERE date_publication <= NOW() AND id_eleve = ? ");
        values.add(idEleve);

        if(idPeriode != null){
            query.append(" AND id_periode = ? ");
            values.add(idPeriode);
        }

        if(idMatiere != null){
            query.append(" AND id_matiere = ? ");
            values.add(idMatiere);
        }

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getCompetences(String idEleve, Long idPeriode, JsonArray idGroups, Long idCycle, String idMatiere,
                               Handler<Either<String, JsonArray>> handler) {
        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();
        String query = "SELECT res.id_devoir, id_competence, id_domaine, max(evaluation) as evaluation, owner, id_eleve, " +
                "created, modified, id_matiere, id_sousmatiere, name, is_evaluated, id_periode, " +
                "id_type, diviseur, date_publication, date, apprec_visible, coefficient, libelle, " +
                "_type_libelle, owner_name, id_groupe FROM ( " +
                "SELECT cd.id_devoir, cd.id_competence, id_domaine, evaluation, cn.owner, id_eleve, " +
                "devoirs.created, devoirs.modified, devoirs.id_matiere, devoirs.id_sousmatiere, name, devoirs.is_evaluated, " +
                "id_periode, devoirs.id_type, diviseur, date_publication, date, apprec_visible, coefficient, libelle, " +
                "type.nom as _type_libelle, users.username as owner_name, id_groupe " +
                "FROM notes.competences_devoirs as cd " +
                "INNER JOIN notes.competences on cd.id_competence = competences.id " +
                "INNER JOIN notes.competences_notes as cn on cd.id_devoir = cn.id_devoir " +
                "AND cd.id_competence = cn.id_competence " +
                "INNER JOIN notes.devoirs on devoirs.id = cd.id_devoir " +
                "INNER JOIN notes.type on devoirs.id_type = type.id " +
                "INNER JOIN notes.rel_competences_domaines ON rel_competences_domaines.id_competence = cd.id_competence " +
                "INNER JOIN " + Viescolaire.EVAL_SCHEMA + ".users ON (users.id = devoirs.owner) " +
                "LEFT JOIN notes.rel_devoirs_groupes on  cd.id_devoir = rel_devoirs_groupes.id_devoir " +
                "WHERE date_publication <= Now() AND id_eleve = ? ";

        values.add(idEleve);

        for (int i = 0; i < idGroups.size(); i++) {
            values.add(idGroups.getString(i));
        }

        if(idCycle == null) {
            // en vue trimestre / année, on ne recupere que les devoirs de la classe (+groupes) actuelle de l'élève
            query += "AND rel_devoirs_groupes.id_groupe IN " + Sql.listPrepared(idGroups.getList()) +
                    "AND devoirs.eval_lib_historise = false ";
        } else {
            // en vue cycle recuperation des devoirs de la classe (+groupes) actuelle de l'élève
            // + récupération des evaluations historisées
            query += "AND ( (rel_devoirs_groupes.id_groupe IN " + Sql.listPrepared(idGroups.getList()) +
                    " AND devoirs.eval_lib_historise = false) OR  devoirs.eval_lib_historise = true) " +
                    "AND competences.id_cycle = ? ";
            values.add(idCycle);
        }

        if(idPeriode != null){
            query += " AND id_periode = ? ";
            values.add(idPeriode);
        }

        if(idMatiere != null){
            query += " AND id_matiere = ? ";
            values.add(idMatiere);
        }

        query += " UNION " +
                "SELECT competences_devoirs.id_devoir, competences_devoirs.id_competence, id_domaine, "+
                "-1 as evaluation, devoirs.owner, id_eleve, " +
                "devoirs.created, devoirs.modified, id_matiere, id_sousmatiere, name, is_evaluated, id_periode, " +
                "devoirs.id_type, diviseur, date_publication, date, apprec_visible, coefficient, libelle, " +
                "type.nom as _type_libelle , users.username as owner_name, id_groupe " +
                "FROM notes.competences_devoirs " +
                "INNER JOIN notes.competences_notes ON competences_notes.id_devoir = competences_devoirs.id_devoir " +
                "INNER JOIN notes.devoirs on devoirs.id = competences_devoirs.id_devoir " +
                "INNER JOIN notes.type on devoirs.id_type = type.id " +
                "INNER JOIN notes.rel_competences_domaines ON rel_competences_domaines.id_competence = competences_devoirs.id_competence " +
                "INNER JOIN " + Viescolaire.EVAL_SCHEMA + ".users ON (users.id = devoirs.owner) " +
                "INNER JOIN notes.rel_devoirs_groupes on  competences_devoirs.id_devoir = rel_devoirs_groupes.id_devoir ";

        for (int i = 0; i < idGroups.size(); i++) {
            values.add(idGroups.getString(i));
        }

        if(idCycle == null) {
            // en vue trimestre / année, on ne recupere que les devoirs de la classe (+groupes) actuelle de l'élève
            query += "WHERE date_publication <= Now() AND id_eleve = ? " +
                    "AND rel_devoirs_groupes.id_groupe IN " + Sql.listPrepared(idGroups.getList()) +
                    " AND devoirs.eval_lib_historise = false ";
            values.add(idEleve);
        } else {
            // en vue cycle recuperation des devoirs de la classe (+groupes) actuelle de l'élève
            // + récupération des evaluations historisées
            query += "INNER JOIN notes.competences ON competences.id = competences_devoirs.id_competence " +
                    "WHERE date_publication <= Now() AND id_eleve = ? " +
                    "AND ( (rel_devoirs_groupes.id_groupe IN " + Sql.listPrepared(idGroups.getList()) +
                    " AND devoirs.eval_lib_historise = false) OR devoirs.eval_lib_historise = true) " +
                    "AND competences.id_cycle = ? ";
            values.add(idEleve);
            values.add(idCycle);
        }

        if(idPeriode != null){
            query += " AND id_periode = ? ";
            values.add(idPeriode);
        }

        if(idMatiere != null){
            query +=" AND id_matiere = ? ";
            values.add(idMatiere);
        }

        query += " ) AS res " +
                "GROUP BY res.id_devoir, id_competence, id_domaine, owner, id_eleve, created, modified, " +
                "id_matiere, id_sousmatiere, name, is_evaluated, id_periode, id_type, diviseur, date_publication, " +
                "date, apprec_visible, coefficient, libelle, _type_libelle, owner_name, id_groupe ";

        Sql.getInstance().prepared(query, values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getCycle(String idClasse,Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();
        query.append("SELECT id_cycle, libelle ")
                .append(" FROM " + Viescolaire.EVAL_SCHEMA + ".rel_groupe_cycle ")
                .append(" INNER JOIN " + Viescolaire.EVAL_SCHEMA + ".cycle ON cycle.id = id_cycle ")
                .append(" WHERE id_groupe = ? ");
        values.add(idClasse);
        Sql.getInstance().prepared(query.toString(), values,
                new DeliveryOptions().setSendTimeout(Viescolaire.TIME_OUT_HANDLER),
                SqlResult.validResultHandler(handler));
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
    public void getStoredDeletedStudent(JsonArray idClasse, String idStructure, String[] idEleves,
                                        Handler<Either<String, JsonArray>> handler){
        StringBuilder query = new StringBuilder();
        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();

        // Si les élèves sont dans le résultat Neo, on ne les récupère pas dans postgres
        /*JsonArray idsNeo = new JsonArray();
        for (int i=0; i<rNeo.size(); i++) {
            String idEleve = rNeo.getJsonObject(i).getString("id");
            if (idEleve == null) {
                idEleve = rNeo.getJsonObject(i).getString("idEleve");
            }
            idsNeo.add(idEleve);
            //values.add(idEleve);
        }*/
        if (idClasse != null){
            for(int i=0; i < idClasse.size(); i++) {
                values.add(idClasse.getValue(i));
            }
        }
        if (idEleves != null) {
            for(int i=0; i < idEleves.length; i++ ) {
                values.add(idEleves[i]);
            }
        }
        if (idStructure != null) {
            values.add(idStructure);
        }

        // Requête finale. IMPORTANT : Les guillemets permettent de respecter la case des alias
        query.append("SELECT DISTINCT \"idEleve\" AS id,\"idEleve\",\"idGroupes\", ")
                .append("display_name AS \"displayName\", ")
                .append("delete_date AS \"deleteDate\", ")
                .append("first_name AS \"firstName\", ")
                .append("last_name AS \"lastName\", ")
                .append("id_structure AS \"idEtablissement\", ")
                .append("id_groupe AS \"idClasse\", ")
                .append("birth_date AS \"birthDate\" ")
                .append("FROM ")
                .append("(SELECT * FROM ")
                .append("(SELECT personnes_supp.id_user AS \"idEleve\", MAX(delete_date) AS \"deleteDate\", ")
                .append("string_agg(DISTINCT rel_groupes_personne_supp.id_groupe, ',') AS \"idGroupes\" ")
                .append("FROM ").append(Viescolaire.VSCO_SCHEMA).append(".personnes_supp, viesco.rel_groupes_personne_supp ")
                .append("WHERE personnes_supp.id = rel_groupes_personne_supp.id ")
                // .append((idsNeo.size() >0)? " AND id_user NOT IN " + Sql.listPrepared(idsNeo.getList()) : "")
                .append((idClasse != null && idClasse.size() > 0) ? " AND id_groupe IN " + Sql.listPrepared(idClasse.getList().toArray()): "")
                .append((idEleves != null && idEleves.length > 0) ? " AND id_user IN " + Sql.listPrepared(idEleves): "")
                .append("AND user_type = 'Student' ")
                .append("GROUP BY personnes_supp.id_user) AS res ")
                .append("INNER JOIN ").append(Viescolaire.VSCO_SCHEMA).append(".personnes_supp ")
                .append("ON \"deleteDate\" = personnes_supp.delete_date ")
                .append("AND \"idEleve\" = personnes_supp.id_user) AS res1 ")
                .append("LEFT JOIN viesco.rel_groupes_personne_supp ON res1.id = rel_groupes_personne_supp.id ")
                .append("AND type_groupe = 0 ")
                .append("INNER JOIN viesco.rel_structures_personne_supp ON res1.id = rel_structures_personne_supp.id ")
                .append((idStructure != null) ? " AND id_structure = ? " : "");

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    public void isEvaluableOnPeriode(String idEleve, Long idPeriode, String idEtablissement,
                                     Handler<Either<String, JsonArray>> handler){

        String[] idEleves = new String[1];
        idEleves[0] = idEleve;
        getInfoEleve(idEleves, idEtablissement, new Handler<Either<String, JsonArray>>(){
            public void handle(Either<String, JsonArray> result) {
                if (result.isRight()) {
                    JsonObject infosEleve = result.right().getValue().getJsonObject(0);
                    String idClasse = infosEleve.getString("idClasse");
                    String idEtablissement = infosEleve.getString("idEtablissement");
                    String[] idClasses = new String[1];
                    idClasses[0] = idClasse;
                    new DefaultPeriodeService().getPeriodesClasses(idEtablissement, idClasses,
                            new Handler<Either<String, JsonArray>>(){

                                public void handle(Either<String, JsonArray> event) {
                                    if (event.isRight()) {

                                        JsonArray periodes = event.right().getValue();
                                        String[] sortedField = new String[1];
                                        sortedField[0] = "idEleve";

                                        for(Object p : periodes){

                                            JsonObject periode = (JsonObject)p;
                                            if(periode.getLong("id_type").equals(idPeriode)){
                                                String debutPeriode = periode.getString("timestamp_dt");
                                                String finPeriode = periode.getString("timestamp_fn");
                                                DateFormat formatter = new SimpleDateFormat("yy-MM-dd");

                                                try {
                                                    final Date dateDebutPeriode = formatter.parse(debutPeriode);
                                                    final Date dateFinPeriode = formatter.parse(finPeriode);

                                                    new DefaultUtilsService().getAvailableStudent(
                                                            result.right().getValue(), idPeriode,
                                                            dateDebutPeriode,
                                                            dateFinPeriode,
                                                            sortedField, handler);

                                                } catch (ParseException e) {
                                                    handler.handle(new Either.Left<>(
                                                            "Error :can not calcul students of groupe : " + idClasses[0]));
                                                }
                                            }
                                        }
                                    }else{
                                        handler.handle(new Either.Left<>("Periode not found"));
                                    }
                                }
                            });
                } else {
                    handler.handle(new Either.Left<>("Infos eleve not found"));
                }
            }
        });
    }
    @Override
    public void getResponsable(String idEleve, Handler<Either<String,JsonArray>> handler){
        StringBuilder query = new StringBuilder();
        query.append("MATCH (u:User {id: {idEleve}})-[:RELATED]-(r:User{profiles:['Relative']}) ")
                .append(" RETURN u.id as idNeo4j, u.externalId as externalId,u.attachmentId as attachmentId,")
                .append(" u.lastName as lastName,u.level as level,u.firstName as firstName,u.relative as relative,")
                .append(" r.externalId as externalIdRelative, r.title as civilite, r.lastName as lastNameRelative, ")
                .append(" r.firstName as firstNameRelative, r.address as address, r.zipCode as zipCode, r.city as city");

        JsonObject param = new JsonObject();
        param.put("idEleve", idEleve);
        // TODO PUT ExternalId of deleted students and store deleted parents
        /*
        Neo4j.getInstance().execute(query.toString(), param, new DefaultUtilsService()
                .getEleveWithClasseName((String[])idsClass.toArray(),null,null,handler));
        */
        Neo4j.getInstance().execute(query.toString(), param, Neo4jResult.validResultHandler(handler));
    }

    @Override
    public void getDeletedStudentByPeriodeByClass(String idClass, String beginningPeriode, Handler<Either<String, JsonArray>> handler) {

        String query = "SELECT id_user, display_name,first_name,last_name," +
                " json_agg(json_build_object(\'deleteDate\', personnes_supp.delete_date, \'oldIdClass\',rel_groupes_personne_supp.id_groupe))" +
                " AS delete_date_id_class FROM " + Viescolaire.VSCO_SCHEMA + ".personnes_supp"+
                " INNER JOIN " + Viescolaire.VSCO_SCHEMA + ".rel_groupes_personne_supp"+
                " ON personnes_supp.id = rel_groupes_personne_supp.id"+
                " WHERE rel_groupes_personne_supp.id_groupe = ? AND personnes_supp.user_type ='Student'"+
                " AND personnes_supp.delete_date > to_timestamp(?,'YYYY-MM-DD') " +
                " GROUP BY id_user,display_name,first_name,last_name";

        JsonArray values = new fr.wseduc.webutils.collections.JsonArray().add(idClass).add(beginningPeriode);

        Sql.getInstance().prepared(query, values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getStudentsFromStructure(String structureId, Integer page, List<String> studentId, List<String> groupNames,
                                         Boolean crossFilter, Handler<Either<String, JsonArray>> handler) {
        String query = "MATCH (s:Structure {id: {structureId}})" +
                "<-[:BELONGS]-(c:Class)<-[:DEPENDS]-(:ProfileGroup)" +
                "<-[:IN]-(u:User {profiles:['Student']}) ";

        if (groupNames != null && groupNames.size() > 0) {
            query += "WHERE c.name IN {audienceNames} ";
        }

        if (studentId != null && studentId.size() > 0) {
            if (groupNames == null || groupNames.size() == 0) {
                query += "WHERE u.id IN {studentId} ";
            } else {
                query += (crossFilter != null && crossFilter) ? "AND" : "OR";
                query += " u.id IN {studentId} ";
            }
        }

        query += "RETURN u.id as id, u.displayName as displayName, u.firstName as firstName, u.lastName as lastName, " +
                "c.id as audienceId, c.name as audienceName " +
                "ORDER BY audienceName ASC, displayName ASC ";


        if (page != null) {
            query += "SKIP " + (page * PAGE_SIZE) + " LIMIT " + PAGE_SIZE;
        }

        JsonObject params = new JsonObject()
                .put("structureId", structureId);

        if (groupNames != null && groupNames.size() > 0) {
            params.put("audienceNames", groupNames);
        }

        if (studentId != null && studentId.size() > 0) {
            params.put("studentId", studentId);
        }

        Neo4j.getInstance().execute(query, params, Neo4jResult.validResultHandler(handler));
    }


    @Override
    public void getPrimaryRelatives(JsonArray studentIds, Handler<Either<String, JsonArray>> handler) {
        String query = "MATCH(u:User)-[:RELATED]->(v:User) WHERE u.id IN {studentIds} " +
                "RETURN u.id AS studentId, u.relative AS relativesCodes, u.primaryRelatives AS primaryRelatives, " +
                "collect({id: v.id, externalId: v.externalId}) AS relatives";

        JsonObject params = new JsonObject().put("studentIds", studentIds);

        Neo4j.getInstance().execute(query, params, res -> {
            Either<String, JsonArray> resHandler = Neo4jResult.validResult(res);
            if (resHandler.isLeft()) {
                handler.handle(new Either.Left<>("[VieScolaire@DefaultEleveService::getPrimaryRelatives] Error fetching primary relatives"));
                return;
            }
            JsonArray response = new JsonArray();

            JsonArray students = resHandler.right().getValue();
            if (students != null) {
                for (int i = 0; i < students.size(); i++) {
                    JsonObject student = students.getJsonObject(i);
                    JsonArray primaryRelatives = student.getJsonArray("primaryRelatives", new JsonArray());


                    // If primary relatives previously set from memento, fetch info
                    if (primaryRelatives.size() > 0) {
                        response.add(new JsonObject()
                                .put("id", student.getValue("studentId"))
                                .put("primaryRelatives", primaryRelatives));
                    } else {
                        // Parse AAF codes
                        List<Relative> relatives = RelativeHelper.toRelativeList(student.getJsonArray("relatives", new JsonArray()));
                        JsonArray relativesCodes = student.getJsonArray("relativesCodes", new JsonArray());

                        response.add(new JsonObject()
                                .put("id", student.getValue("studentId"))
                                .put("primaryRelatives", parseAAFRelativeCodes(relatives, relativesCodes)));
                    }
                }

                handler.handle(new Either.Right<>(response));
            }
        });
    }

    /**
     * Parse AAF codes and return list of primary relative identifiers.
     *
     * @param relatives         relative list
     * @param relativesCodes    AAF codes for relatives
     *                          ex : [6518799$20$1$1$1$1, 9527953$10$0$1$0$0]
     *                                               ^                   ^
     *                          The first element of each codes is the relative externalId.
     *                          priority codes. The fifth is the relative priority code (1 if primary contact, 0 if not)
     *
     * @return list of primary relative ids
     */
    private JsonArray parseAAFRelativeCodes(List<Relative> relatives, JsonArray relativesCodes) {
        JsonArray listRelatives = new JsonArray();

        for (Relative relative: relatives) {
            for (int j = 0; j < relativesCodes.size(); j++) {
                String[] codes;
                try {
                    codes = relativesCodes.getString(j).split("\\$");
                } catch (PatternSyntaxException e) {
                    codes = new String[0];
                }
                String externalId = (codes.length > 4) ? codes[0] : relative.getExternalId();
                String isPrimary = (codes.length > 4) ? codes[4] : "0";
                if (externalId.equals(relative.getExternalId()) && (isPrimary.equals("1"))) {
                    listRelatives.add(relative.getId());
                }
            }
        }
        return listRelatives;
    }
}
