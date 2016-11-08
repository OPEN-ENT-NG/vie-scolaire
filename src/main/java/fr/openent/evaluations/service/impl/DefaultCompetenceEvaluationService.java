
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
import fr.openent.evaluations.service.CompetenceEvaluationService;
import fr.wseduc.webutils.Either;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.List;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public class DefaultCompetenceEvaluationService extends SqlCrudService implements CompetenceEvaluationService {
    public DefaultCompetenceEvaluationService(String schema, String table) {
        super(schema, table);
    }

    @Override
    public void createCompetenceNote(JsonObject competenceNote, UserInfos user, Handler<Either<String, JsonObject>> handler) {
        super.create(competenceNote, user, handler);
    }

    @Override
    public void updateCompetenceNote(String id, JsonObject competenceNote, UserInfos user, Handler<Either<String, JsonObject>> handler) {
        super.update(id, competenceNote, user, handler);
    }

    @Override
    public void deleteCompetenceNote(String id, Handler<Either<String, JsonObject>> handler) {
        super.delete(id, handler);
    }

    @Override
    public void getCompetencesNotes(Integer idDevoir, String idEleve, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT competences_notes.*,competences.nom as nom, competences.id_type as id_type, competences.id_parent as id_parent ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".competence_evaluations, "+ Viescolaire.EVAL_SCHEMA +".competences ")
                .append("WHERE competence_evaluations.id_competence = competences.id ")
                .append("AND competence_evaluations.id_devoir = ? AND competence_evaluations.id_eleve = ? ")
                .append("ORDER BY competence_evaluations.id ASC;");

        JsonArray params = new JsonArray();
        params.addNumber(idDevoir);
        params.addString(idEleve);

        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getCompetencesNotesDevoir(Integer idDevoir, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT competences.nom, competence_evaluations.id, competence_evaluations.id_devoir, competence_evaluations.id_eleve, competence_evaluations.id_competence, competence_evaluations.evaluation " +
                "FROM "+ Viescolaire.EVAL_SCHEMA +".competence_evaluations, "+ Viescolaire.EVAL_SCHEMA +".competences " +
                "WHERE competence_evaluations.id_devoir = ? " +
                "AND competences.id = competence_evaluations.id_competence");

        Sql.getInstance().prepared(query.toString(), new JsonArray().addNumber(idDevoir), SqlResult.validResultHandler(handler));
    }

    @Override
    public void updateCompetencesNotesDevoir(JsonArray _datas, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();
        for (int i = 0; i < _datas.size(); i++) {
            JsonObject o = _datas.get(i);
            query.append("UPDATE "+ Viescolaire.EVAL_SCHEMA +".competence_evaluations SET evaluation = ?, modified = now() WHERE id = ?;");
            values.addNumber(o.getNumber("evaluation")).addNumber(o.getNumber("id"));
        }
        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void createCompetencesNotesDevoir(JsonArray _datas, UserInfos user, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();
        for (int i = 0; i < _datas.size(); i++) {
            JsonObject o = _datas.get(i);
            query.append("INSERT INTO "+ Viescolaire.EVAL_SCHEMA +".competence_evaluations (id_devoir, id_competence, evaluation, owner, id_eleve, created) VALUES (?, ?, ?, ?, ?, now());");
            values.add(o.getInteger("id_devoir")).add(o.getInteger("id_competence")).add(o.getInteger("evaluation"))
                    .add(user.getUserId()).add(o.getString("id_eleve"));
        }
        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void dropCompetencesNotesDevoir(List<String> ids, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();
        for (int i = 0; i < ids.size(); i++) {
            values.addNumber(Integer.parseInt(ids.get(i)));
        }
        query.append("DELETE FROM "+ Viescolaire.EVAL_SCHEMA +".competence_evaluations WHERE id IN " + Sql.listPrepared(ids.toArray()) + ";");
        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getCompetencesNotesEleve(String idEleve, String idPeriode, Handler<Either<String, JsonArray>> handler) {
        JsonArray values = new JsonArray().addString(idEleve);
        StringBuilder query = new StringBuilder()
                .append("SELECT competences.id as id_competence, competences.id_parent, competences.id_type, competences.id_enseignement, rel_competences_cycle.id_cycle, " +
                        "competence_evaluations.id as id_competence_evaluations, competence_evaluations.evaluation, devoirs.owner " +
                        "FROM "+ Viescolaire.EVAL_SCHEMA +".competences INNER JOIN "+ Viescolaire.EVAL_SCHEMA +".rel_competences_cycle ON (competences.id = rel_competences_cycle.id_competence) " +
                        "INNER JOIN "+ Viescolaire.EVAL_SCHEMA +".competence_evaluations ON (competence_evaluations.id_competence = competences.id) " +
                        "INNER JOIN "+ Viescolaire.EVAL_SCHEMA +".devoirs ON (competence_evaluations.id_devoir = devoirs.id) " +
                        "WHERE competence_evaluations.id_eleve = ? ");
        if (idPeriode != null) {
            query.append("AND devoirs.id_periode = ? ");
            values.addNumber(Integer.parseInt(idPeriode));
        }
        query.append("ORDER BY competences.id ");



        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }
}
