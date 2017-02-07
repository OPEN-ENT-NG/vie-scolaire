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
import fr.openent.evaluations.service.CompetencesService;
import fr.wseduc.webutils.Either;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public class DefaultCompetencesService extends SqlCrudService implements CompetencesService {

    protected static final Logger log = LoggerFactory.getLogger(DefaultCompetencesService.class);

    public DefaultCompetencesService(String schema, String table) {
        super(schema, table);
    }

    @Override
    public void getCompetencesItem(String idClasse, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT rel_competences_domaines.id_domaine, competences.* ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".competences ")
                .append("INNER JOIN "+ Viescolaire.EVAL_SCHEMA +".rel_competences_domaines ON (competences.id = rel_competences_domaines.id_competence) ")
                .append("WHERE competences.id_cycle = (SELECT id_cycle FROM "+ Viescolaire.EVAL_SCHEMA +
                        ".rel_classe_cycle WHERE id_classe = ?) ")
                .append("AND NOT EXISTS ( ")
                    .append("SELECT 1 ")
                    .append("FROM "+ Viescolaire.EVAL_SCHEMA +".competences AS competencesChildren ")
                    .append("WHERE competencesChildren.id_parent = competences.id ")
                    .append("AND competences.id_cycle = (SELECT id_cycle FROM "+ Viescolaire.EVAL_SCHEMA +
                            ".rel_classe_cycle WHERE id_classe = ?) ")
                .append(")");

        JsonArray params = new JsonArray();
        params.addString(idClasse);
        params.addString(idClasse);

        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));
    }


    @Override
    public void getCompetences(Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT competences.id, competences.nom, competences.description, competences.id_type, competences.id_parent, type_competences.nom as type, competences.id_cycle ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".competences ")
                .append("WHERE competences.id_type = type_competences.id ")
                .append("ORDER BY competences.id ASC");
        Sql.getInstance().prepared(query.toString(), new JsonArray(), SqlResult.validResultHandler(handler));
    }

    @Override
    public void setDevoirCompetences(Long devoirId, JsonArray values, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray data = new JsonArray();
        query.append("INSERT INTO "+ Viescolaire.EVAL_SCHEMA +".competences_devoirs (id_devoir, id_competence) VALUES ");
        for(int i = 0; i < values.size(); i++){
            query.append("(?, ?)");
            data.addNumber(devoirId);
            data.addNumber((Number) values.get(i));
            if(i != values.size()-1){
                query.append(",");
            }else{
                query.append(";");
            }
        }

        Sql.getInstance().prepared(query.toString(), data, SqlResult.validRowsResultHandler(handler));
    }
    @Override
    public void remDevoirCompetences(Long devoirId, JsonArray values, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray data = new JsonArray();
        query.append("DELETE FROM "+ Viescolaire.EVAL_SCHEMA +".competences_devoirs WHERE ");
        for(int i = 0; i < values.size(); i++){
            query.append("(id_devoir = ? AND  id_competence = ?)");
            data.addNumber(devoirId);
            data.addNumber((Number) values.get(i));
            if(i != values.size()-1){
                query.append(" OR ");
            }else{
                query.append(";");
            }
        }

        Sql.getInstance().prepared(query.toString(), data, SqlResult.validRowsResultHandler(handler));
    }

    @Override
    public void getDevoirCompetences(Long devoirId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT string_agg(domaines.codification, ', ') as code_domaine, competences_devoirs.*, competences.nom as nom, competences.id_type as id_type, competences.id_parent as id_parent ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".competences ")
                .append("INNER JOIN "+ Viescolaire.EVAL_SCHEMA +".competences_devoirs ON (competences.id = competences_devoirs.id_competence ) ")
                .append("LEFT OUTER JOIN "+ Viescolaire.EVAL_SCHEMA +".rel_competences_domaines ON (competences.id = rel_competences_domaines.id_competence) ")
                .append("LEFT OUTER JOIN "+ Viescolaire.EVAL_SCHEMA +".domaines ON (domaines.id = rel_competences_domaines.id_domaine) ")
                .append("WHERE competences_devoirs.id_devoir = ? ")
                .append("GROUP BY competences_devoirs.id, competences.nom, competences.id_type, competences.id_parent ")
                .append("ORDER BY competences_devoirs.id ASC;");

        Sql.getInstance().prepared(query.toString(), new JsonArray().addNumber(devoirId), SqlResult.validResultHandler(handler));
    }


    @Override
    public void getLastCompetencesDevoir(String userId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT competences_devoirs.*, competences.nom as nom ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".competences_devoirs, "+ Viescolaire.EVAL_SCHEMA +".competences ")
                .append("WHERE competences_devoirs.id_competence = competences.id ")
                .append("AND id_devoir IN ")
                .append("(SELECT id FROM "+ Viescolaire.EVAL_SCHEMA +".devoirs WHERE devoirs.owner = ? ORDER BY devoirs.created DESC LIMIT 1);");

        Sql.getInstance().prepared(query.toString(), new JsonArray().addString(userId), SqlResult.validResultHandler(handler));
    }

    @Override
    public void getSousCompetences(Long skillId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT * ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".competences ")
                .append("WHERE competences.id_parent = ?;");

        Sql.getInstance().prepared(query.toString(), new JsonArray().addNumber(skillId), SqlResult.validResultHandler(handler));
    }

    @Override
    public void getCompetencesEnseignement(Long teachingId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT * ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".competences ")
                .append("INNER JOIN "+ Viescolaire.EVAL_SCHEMA +".rel_competences_enseignements ON (competences.id = rel_competences_enseignements.id_competence) ")
                .append("WHERE rel_competences_enseignements.id_enseignement = ? ")
                .append("AND competences.id_parent = 0 ;");

        Sql.getInstance().prepared(query.toString(), new JsonArray().addNumber(teachingId), SqlResult.validResultHandler(handler));
    }

    @Override
    public void getCompetencesByLevel(String filter, String idClasse, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray params = new JsonArray();

        query.append("SELECT DISTINCT string_agg(domaines.codification, ', ') as code_domaine, competences.id, competences.nom, competences.id_parent, competences.id_type, rel_competences_enseignements.id_enseignement, competences.id_cycle ")

                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".competences ")
                .append("INNER JOIN "+ Viescolaire.EVAL_SCHEMA +".rel_competences_enseignements ON (competences.id = rel_competences_enseignements.id_competence) ");

                if (idClasse != null) {
                    query.append("INNER JOIN " + Viescolaire.EVAL_SCHEMA + ".rel_classe_cycle ON (rel_classe_cycle.id_cycle = competences.id_cycle) ");
                }

                query.append("LEFT OUTER JOIN "+ Viescolaire.EVAL_SCHEMA +".rel_competences_domaines ON (competences.id = rel_competences_domaines.id_competence) ")
                .append("LEFT OUTER JOIN "+ Viescolaire.EVAL_SCHEMA +".domaines ON (domaines.id = rel_competences_domaines.id_domaine) ")
                .append("WHERE competences."+ filter);

        if (idClasse != null) {
            query.append(" AND rel_classe_cycle.id_classe = ?");
            params.addString(idClasse);
        }

        query.append(" GROUP BY competences.id, competences.nom, competences.id_parent, competences.id_type, rel_competences_enseignements.id_enseignement, competences.id_cycle");

        Sql.getInstance().prepared(query.toString(), params , SqlResult.validResultHandler(handler));
    }
}
