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
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

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
    public void getClasseCours(String pSDateDebut, String pSDateFin, String pLIdClasse, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT cours.*, rel_cours_groupes.id_groupe as id_classe  ")
                .append("FROM "+ Viescolaire.VSCO_SCHEMA +".cours ")
                .append("LEFT JOIN " + Viescolaire.VSCO_SCHEMA + ".rel_cours_groupes ON (cours.id = rel_cours_groupes.id_cours) ")
                .append("WHERE rel_cours_groupes.id_groupe = ? ")
                .append("AND cours.timestamp_dt > to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') ")
                .append("AND cours.timestamp_fn < to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') ")
                .append("ORDER BY cours.timestamp_fn ASC");


        values.addString(pLIdClasse).addString(pSDateDebut).addString(pSDateFin);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }
    @Override
    public void getClasseCoursBytime(String pSDateDebut, String pSDateFin, String pLIdClasse, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

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


        values.addString(pLIdClasse).addString(pSDateFin).addString(pSDateDebut).addString(pSDateDebut).addString(pSDateFin);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }
    @Override
    public void getCoursByStudentId(String pSDateDebut, String pSDateFin, String[] pLIdClasse, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

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
                values.addString(pLIdClasse[i]);
            }else{
                query.append("?,");
                values.addString(pLIdClasse[i]);
            }
        }
        query.append("AND ( cours.timestamp_dt < to_timestamp(? ,'YYYY-MM-DD HH24:MI:SS') ")
                .append("AND  cours.timestamp_dt > to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') )  ")
                .append("OR (cours.timestamp_fn > to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS')  ")
                .append("AND cours.timestamp_fn < to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') )  ")
                .append("GROUP BY cours.id, id_appel ")
                .append("ORDER BY cours.timestamp_fn ASC");


        values.addString(pSDateFin).addString(pSDateDebut).addString(pSDateDebut).addString(pSDateFin);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void getCoursByUserId(String pSDateDebut, String pSDateFin, String psUserId, String structureId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT cours.*, to_char(cours.timestamp_dt, 'HH24:MI') as heure_debut, rel_cours_users.id_user as id_personnel ")
                .append("FROM "+ Viescolaire.VSCO_SCHEMA +".cours ")
                .append("LEFT JOIN " + Viescolaire.VSCO_SCHEMA + ".rel_cours_users ON (cours.id = rel_cours_users.id_cours) ")
                .append("WHERE rel_cours_users.id_user = ? ")
                .append("AND id_etablissement = ?")
                .append("AND to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') < cours.timestamp_dt ")
                .append("AND cours.timestamp_fn < to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') ")
                .append("ORDER BY cours.timestamp_dt ASC");

        values.addString(psUserId);
        values.addString(structureId);
        values.addString(pSDateDebut);
        values.addString(pSDateFin);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getCoursById(List<Long> idCours, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT cours.*")
                .append("FROM " + Viescolaire.VSCO_SCHEMA + ".cours ")
                .append("WHERE id IN " + Sql.listPrepared(idCours.toArray()));

        for(Long l : idCours) {
            values.addNumber(l);
        }

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }
}
