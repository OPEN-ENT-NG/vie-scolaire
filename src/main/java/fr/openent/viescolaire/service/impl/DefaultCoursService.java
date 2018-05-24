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
import fr.openent.viescolaire.service.CoursService;
import fr.wseduc.webutils.Either;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.entcore.common.sql.SqlResult.validResultHandler;

/**
 * Sercice pour la gestion et récupération des cours
 * Schéma : viesco
 * Table : cours
 * Created by ledunoiss on 10/02/2016.
 */
public class DefaultCoursService extends SqlCrudService implements CoursService {

    protected static final Logger log = LoggerFactory.getLogger(DefaultCoursService.class);

    public DefaultCoursService() {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.VSCO_COURS_TABLE);
    }

    @Override
    public void getClasseCours(String pSDateDebut, String pSDateFin, List<String> listIdClasse, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();

        query.append("SELECT" );
                query.append("   cours.*, ");
        query.append("   string_agg(distinct rel_cours_groupes.id_groupe , ',') AS classes, ");
        query.append("   string_agg(distinct rel_cours_users.id_user , ',') AS personnels ");
        query.append("FROM ");
        query.append(Viescolaire.VSCO_SCHEMA +".cours ");
        query.append("LEFT JOIN ");
        query.append(Viescolaire.VSCO_SCHEMA +".rel_cours_users ");
        query.append("ON (cours.id = rel_cours_users.id_cours) ");
        query.append("LEFT JOIN ");
        query.append(Viescolaire.VSCO_SCHEMA +".rel_cours_groupes ");
        query.append("ON (cours.id = rel_cours_groupes.id_cours) ");
        query.append("WHERE ");
        query.append("cours.id IN (");
        query.append("SELECT ");
        query.append("cours.id ");
        query.append("FROM "+ Viescolaire.VSCO_SCHEMA +".cours ");
        query.append("LEFT JOIN "+ Viescolaire.VSCO_SCHEMA +".rel_cours_groupes ON (cours.id = rel_cours_groupes.id_cours) ");
        query.append("WHERE ");
        query.append("		rel_cours_groupes.id_groupe IN " + Sql.listPrepared(listIdClasse.toArray()));
        query.append("		AND to_timestamp( ? , 'YYYY-MM-DD HH24:MI:SS') <= cours.timestamp_fn ");
        query.append("		AND cours.timestamp_dt <= to_timestamp( ? , 'YYYY-MM-DD HH24:MI:SS') ");
        query.append("   ) ");
        query.append("GROUP BY ");
        query.append("cours.id ");
        query.append("ORDER BY ");
        query.append("cours.timestamp_fn ASC;");

        for (Integer i=0; i< listIdClasse.size(); i++){
            values.add(listIdClasse.get(i));
        }
        values.add(pSDateDebut).add(pSDateFin);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }
    @Override
    public void getClasseCoursBytime(String pSDateDebut, String pSDateFin, String pLIdClasse, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();

        query.append("SELECT cours.* , appel.id id_appel, rel_cours_groupes.id_groupe as id_classe ")
                .append("FROM "+ Viescolaire.VSCO_SCHEMA +".cours ")
                .append("LEFT JOIN "+Viescolaire.ABSC_SCHEMA+".appel ON appel.id_cours = cours.id ")
                .append("LEFT JOIN " + Viescolaire.VSCO_SCHEMA + ".rel_cours_groupes ON (cours.id = rel_cours_groupes.id_cours) ")
                .append("WHERE rel_cours_groupes.id_groupe = ? ")
                .append("AND ( cours.timestamp_dt < to_timestamp(?,'YYYY-MM-DD HH24:MI:SS') ")
                .append("AND  cours.timestamp_dt > to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') )  ")
                .append("OR (cours.timestamp_fn > to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS')  ")
                .append("AND cours.timestamp_fn < to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') )  ")
                .append("ORDER BY cours.timestamp_fn ASC");


        values.add(pLIdClasse).add(pSDateFin).add(pSDateDebut).add(pSDateDebut).add(pSDateFin);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }
    @Override
    public void getCoursByStudentId(String pSDateDebut, String pSDateFin, String[] pLIdClasse, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();

        query.append("SELECT cours.*, appel.id id_appel, string_agg(distinct rel_cours_groupes.id_groupe , ',') AS classes, ")
                .append("string_agg(distinct rel_cours_users.id_user , ',') AS personnels ")
                .append("FROM "+ Viescolaire.VSCO_SCHEMA +".cours ")
                .append("LEFT JOIN "+Viescolaire.ABSC_SCHEMA+".appel ON appel.id_cours = cours.id ")
                .append("LEFT JOIN " + Viescolaire.VSCO_SCHEMA + ".rel_cours_groupes ON (cours.id = rel_cours_groupes.id_cours) ")
                .append("LEFT JOIN " + Viescolaire.VSCO_SCHEMA + ".rel_cours_users ON (cours.id = rel_cours_users.id_cours) ")
                .append("WHERE rel_cours_groupes.id_groupe in  (");
        for(int i = 0; i < pLIdClasse.length; i++) {
            if(i == pLIdClasse.length-1){
                query.append("?) ");
                values.add(pLIdClasse[i]);
            }else{
                query.append("?,");
                values.add(pLIdClasse[i]);
            }
        }
        query.append("AND ( ( cours.timestamp_dt <= to_timestamp(? ,'YYYY-MM-DD HH24:MI:SS') ")
                .append("AND  cours.timestamp_dt >= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') )  ")
                .append("OR (cours.timestamp_fn >= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS')  ")
                .append("AND cours.timestamp_fn <= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') ) ) ")
                .append("GROUP BY cours.id, id_appel ")
                .append("ORDER BY cours.timestamp_fn ASC");


        values.add(pSDateFin).add(pSDateDebut).add(pSDateDebut).add(pSDateFin);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void getCoursByUserId(String pSDateDebut, String pSDateFin, String psUserId, String structureId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();

        query.append("SELECT" );
        query.append("   cours.*, ");
        query.append("   string_agg(distinct rel_cours_groupes.id_groupe , ',') AS classes, ");
        query.append("   string_agg(distinct rel_cours_users.id_user , ',') AS personnels ");
        query.append("FROM ");
        query.append(Viescolaire.VSCO_SCHEMA +".cours ");
        query.append("LEFT JOIN ");
        query.append(Viescolaire.VSCO_SCHEMA +".rel_cours_users ");
        query.append("ON (cours.id = rel_cours_users.id_cours) ");
        query.append("LEFT JOIN ");
        query.append(Viescolaire.VSCO_SCHEMA +".rel_cours_groupes ");
        query.append("ON (cours.id = rel_cours_groupes.id_cours) ");
        query.append("WHERE ");
        query.append("cours.id IN (");
        query.append("SELECT ");
        query.append("cours.id ");
        query.append("FROM "+ Viescolaire.VSCO_SCHEMA +".cours ");
        query.append("LEFT JOIN "+ Viescolaire.VSCO_SCHEMA +".rel_cours_users ON (cours.id = rel_cours_users.id_cours) ");
        query.append("WHERE ");
        query.append("		rel_cours_users.id_user =  ? ");
        query.append("      AND cours.id_etablissement = ? ");
        query.append("		AND cours.timestamp_dt > to_timestamp( ? , 'YYYY-MM-DD HH24:MI:SS') ");
        query.append("		AND cours.timestamp_fn < to_timestamp( ? , 'YYYY-MM-DD HH24:MI:SS') ");
        query.append("   ) ");
        query.append("GROUP BY ");
        query.append("cours.id ");
        query.append("ORDER BY ");
        query.append("cours.timestamp_fn ASC;");

        values.add(psUserId);
        values.add(structureId);
        values.add(pSDateDebut);
        values.add(pSDateFin);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getCoursById(List<Long> idCours, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();

        query.append("SELECT cours.*")
                .append("FROM " + Viescolaire.VSCO_SCHEMA + ".cours ")
                .append("WHERE id IN " + Sql.listPrepared(idCours.toArray()));

        for(Long l : idCours) {
            values.add(l);
        }

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void createCours(final String userId, final String idEtablissement, final String idMatiere, final String dateDebut, final String dateFin
            , final List<String> listIdClasse, final List<String> listIdPersonnel, final Handler<Either<String, JsonObject>> handler) {
        log.debug("DEBUG : DEBUT : createCours");

        StringBuilder query = new StringBuilder();
        JsonArray values = new fr.wseduc.webutils.collections.JsonArray();

        query.append("SELECT cours.*, string_agg(distinct rel_cours_groupes.id_groupe , ',') AS classes, ");
        query.append("string_agg(distinct rel_cours_users.id_user , ',') AS personnels ");
        query.append("FROM "+ Viescolaire.VSCO_SCHEMA +".cours ");
        query.append("LEFT JOIN " + Viescolaire.VSCO_SCHEMA + ".rel_cours_groupes ON (cours.id = rel_cours_groupes.id_cours) ");
        query.append("LEFT JOIN " + Viescolaire.VSCO_SCHEMA + ".rel_cours_users ON (cours.id = rel_cours_users.id_cours) ");
        query.append("WHERE id_etablissement = ? ");
        query.append("AND timestamp_dt = to_timestamp(?, 'YYYY-MM-DD HH24:MI') ");
        query.append("AND timestamp_fn = to_timestamp(?, 'YYYY-MM-DD HH24:MI') ");
        query.append("AND id_matiere = ? ");
        query.append("GROUP BY cours.id ");
        query.append("ORDER BY cours.timestamp_fn ASC");

        values.add(idEtablissement);
        values.add(dateDebut);
        values.add(dateFin);
        values.add(idMatiere);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validUniqueResultHandler(new Handler<Either<String, JsonObject>>() {
            @Override
            public void handle(Either<String, JsonObject> result) {
                if(result.isRight()){
                    boolean coursFoundButNotEquals = false;
                    if (result.right().getValue().size() != 0){
                        // On récupère les classes et on les prépare pour la comparaison
                        String[] arrayClasses = listIdClasse.toArray(new String[0]);
                        String[] arrayClassesFound  = result.right().getValue().getString("classes").split(",");
                        Arrays.sort(arrayClasses);
                        Arrays.sort(arrayClassesFound);

                        // On récupère les personnels et on les prépare pour la comparaison
                        String[] arrayPersonnels = listIdPersonnel.toArray(new String[0]);
                        String[] arrayPersonnelsFound  = result.right().getValue().getString("personnels").split(",");
                        Arrays.sort(arrayPersonnels);
                        Arrays.sort(arrayPersonnelsFound);

                        // Si les personnels ou les classes trouvées ne correpondent pas
                        if(!Arrays.equals(arrayClasses, arrayClassesFound) || !Arrays.equals(arrayPersonnels, arrayPersonnelsFound)){
                            coursFoundButNotEquals = true;
                        }
                    }

                    if(coursFoundButNotEquals || result.right().getValue().size() == 0){

                        final String queryNewCours =
                                "SELECT nextval('" + Viescolaire.VSCO_SCHEMA + ".cours_id_seq') as id";

                        sql.raw(queryNewCours, SqlResult.validUniqueResultHandler(new Handler<Either<String, JsonObject>>() {
                            @Override
                            public void handle(Either<String, JsonObject> event) {
                                if (event.isRight()) {
                                    Long idCours = event.right().getValue().getLong("id");

                                    StringBuilder query = new StringBuilder();
                                    JsonArray values = new fr.wseduc.webutils.collections.JsonArray();
                                    JsonArray statements = new fr.wseduc.webutils.collections.JsonArray();

                                    //#4 - Insert cours
                                    values = new fr.wseduc.webutils.collections.JsonArray();
                                    query = new StringBuilder();
                                    // Query & value
                                    query.append("INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".cours ");
                                    query.append("(id, id_etablissement, timestamp_dt, timestamp_fn, id_matiere)");
                                    query.append(" VALUES ");
                                    query.append("(?, ?, to_timestamp(?, 'YYYY-MM-DD HH24:MI'), to_timestamp(?, 'YYYY-MM-DD HH24:MI'), ?) ");
                                    values.add(idCours);
                                    values.add(idEtablissement);
                                    values.add(dateDebut);
                                    values.add(dateFin);
                                    values.add(idMatiere);

                                    // Ajout du statement
                                    statements.add(new JsonObject().put("statement", query.toString())
                                            .put("values", values).put("action", "prepared"));

                                    //#4.1 Insert dans rel_cours_groupes
                                    if (listIdClasse != null && !listIdClasse.isEmpty()) {
                                        values = new fr.wseduc.webutils.collections.JsonArray();
                                        query = new StringBuilder();
                                        // Query & value
                                        query.append("INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".rel_cours_groupes ");
                                        query.append("(id_cours, id_groupe)");
                                        query.append(" VALUES ");

                                        for (int i = 0; i < listIdClasse.size(); i++) {
                                            query.append("(?, ?)");
                                            values.add(idCours);
                                            values.add(listIdClasse.get(i));

                                            if (i != listIdClasse.size() - 1) {
                                                query.append(",");
                                            }
                                        }

                                        // Ajout du statement
                                        statements.add(new JsonObject().put("statement", query.toString())
                                                .put("values", values).put("action", "prepared"));
                                    }

                                    //#4.2 Insert dans rel_cours_users
                                    if (listIdPersonnel != null && !listIdPersonnel.isEmpty()) {
                                        values = new fr.wseduc.webutils.collections.JsonArray();
                                        query = new StringBuilder();
                                        // Query & value
                                        query.append("INSERT INTO " + Viescolaire.VSCO_SCHEMA + ".rel_cours_users ");
                                        query.append("(id_cours, id_user)");
                                        query.append(" VALUES ");

                                        for (int i = 0; i < listIdPersonnel.size(); i++) {
                                            query.append("(?, ?)");
                                            values.add(idCours);
                                            values.add(listIdPersonnel.get(i));

                                            if (i != listIdPersonnel.size() - 1) {
                                                query.append(",");
                                            }
                                        }
                                    }

                                    // Ajout du statement
                                    statements.add(new JsonObject().put("statement", query.toString())
                                            .put("values", values).put("action", "prepared"));

                                    Sql.getInstance().transaction(statements, SqlResult.validRowsResultHandler(handler));
                                    log.debug("DEBUG : FIN : createCours");
                                } else if (event.isLeft()) {
                                    log.error("ERROR : createCours : INSERT INTO " + event.left().getValue());
                                }
                            }
                        }));
                    } else {
                        log.error("ERROR : createCours : CANCELED Cours Already Exist");
                    }
                }else if (result.isLeft()) {
                    log.error("ERROR : createCours : SELECT Existing cours " + result.left().getValue());
                }
            }
        }));
    }
}
