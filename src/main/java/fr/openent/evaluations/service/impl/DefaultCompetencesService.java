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

/**
 * Created by ledunoiss on 05/08/2016.
 */
public class DefaultCompetencesService extends SqlCrudService implements fr.openent.evaluations.service.CompetencesService {
    public DefaultCompetencesService(String schema, String table) {
        super(schema, table);
    }

    @Override
    public void getCompetences(Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT competences.id, competences.nom, competences.description, competences.id_type, competences.id_parent, type_competences.nom as type, rel_competences_cycle.id_cycle ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".competences, INNER JOIN "+ Viescolaire.EVAL_SCHEMA +".rel_competences_cycle ON (competences.id = rel_competences_cycle.id_competence) "+ Viescolaire.EVAL_SCHEMA +".type_competences ")
                .append("WHERE competences.id_type = type_competences.id ")
                .append("ORDER BY competences.id ASC");
        Sql.getInstance().prepared(query.toString(), new JsonArray(), SqlResult.validResultHandler(handler));
    }

    @Override
    public void setDevoirCompetences(Integer devoirId, JsonArray values, Handler<Either<String, JsonObject>> handler) {
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
    public void getDevoirCompetences(Integer devoirId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT competences_devoirs.*, competences.nom as nom, competences.id_type as id_type, competences.id_parent as id_parent ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".competences_devoirs, "+ Viescolaire.EVAL_SCHEMA +".competences ")
                .append("WHERE competences_devoirs.id_competence = competences.id ")
                .append("AND competences_devoirs.id_devoir = ? ")
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
    public void getSousCompetences(Integer skillId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT * ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".competences ")
                .append("WHERE competences.id_parent = ?;");

        Sql.getInstance().prepared(query.toString(), new JsonArray().addNumber(skillId), SqlResult.validResultHandler(handler));
    }

    @Override
    public void getCompetencesEnseignement(Integer teachingId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT * ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".competences ")
                .append("WHERE competences.id_enseignement = ? ")
                .append("AND competences.id_parent = 0 ;");

        Sql.getInstance().prepared(query.toString(), new JsonArray().addNumber(teachingId), SqlResult.validResultHandler(handler));
    }

    @Override
    public void getCompetencesByLevel(String filter, String idCycle, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray params = new JsonArray();

        query.append("SELECT competences.id, competences.nom, competences.id_parent, competences.id_type, competences.id_enseignement, rel_competences_cycle.id_cycle " +
                "FROM "+ Viescolaire.EVAL_SCHEMA +".competences " +
                "INNER JOIN "+ Viescolaire.EVAL_SCHEMA +".rel_competences_cycle ON (competences.id = rel_competences_cycle.id_competence) " +
                "WHERE competences."+ filter);
        if (idCycle != null) {
            query.append("AND rel_competences_cycle.id_cycle = ?");
            params.addNumber(Integer.parseInt(idCycle));
        }
        Sql.getInstance().prepared(query.toString(), params , SqlResult.validResultHandler(handler));
    }
}
