
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
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import java.util.List;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public class DefaultCompetenceNoteService extends SqlCrudService implements fr.openent.evaluations.service.CompetenceNoteService {

    protected static final Logger log = LoggerFactory.getLogger(DefaultCompetenceNoteService.class);

    public DefaultCompetenceNoteService(String schema, String table) {
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
    public void getCompetencesNotes(Long idDevoir, String idEleve, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT competences_notes.*,competences.nom as nom, competences.id_type as id_type, competences.id_parent as id_parent ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +"competences_notes, "+ Viescolaire.EVAL_SCHEMA +"competences ")
                .append("WHERE competences_notes.id_competence = competences.id ")
                .append("AND competences_notes.id_devoir = ? AND competences_notes.id_eleve = ? ")
                .append("ORDER BY competences_notes.id ASC;");

        JsonArray params = new JsonArray();
        params.addNumber(idDevoir);
        params.addString(idEleve);

        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getCompetencesNotesDevoir(Long idDevoir, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT competences.nom, competences_notes.id, competences_notes.id_devoir, competences_notes.id_eleve, competences_notes.id_competence, competences_notes.evaluation " +
                "FROM "+ Viescolaire.EVAL_SCHEMA +".competences_notes , "+ Viescolaire.EVAL_SCHEMA +".competences " +
                "WHERE competences_notes.id_devoir = ? " +
                "AND competences.id = competences_notes.id_competence");

        Sql.getInstance().prepared(query.toString(), new JsonArray().addNumber(idDevoir), SqlResult.validResultHandler(handler));
    }

    @Override
    public void updateCompetencesNotesDevoir(JsonArray _datas, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();
        for (int i = 0; i < _datas.size(); i++) {
            JsonObject o = _datas.get(i);
            query.append("UPDATE "+ Viescolaire.EVAL_SCHEMA +".competences_notes SET evaluation = ?, modified = now() WHERE id = ?;");
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
            query.append("INSERT INTO "+ Viescolaire.EVAL_SCHEMA +".competences_notes (id_devoir, id_competence, evaluation, owner, id_eleve, created) VALUES (?, ?, ?, ?, ?, now());");
            values.add(o.getInteger("id_devoir")).add(o.getInteger("id_competence")).add(o.getInteger("evaluation"))
                    .add(user.getUserId()).add(o.getString("id_eleve"));
        }
        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void dropCompetencesNotesDevoir(JsonArray oIdsJsonArray, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();

        query.append("DELETE FROM "+ Viescolaire.EVAL_SCHEMA +".competences_notes WHERE id IN " + Sql.listPrepared(oIdsJsonArray.toArray()) + ";");
        Sql.getInstance().prepared(query.toString(), oIdsJsonArray, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getCompetencesNotesEleve(String idEleve, Long idPeriode, Handler<Either<String, JsonArray>> handler) {
        JsonArray values = new JsonArray().addString(idEleve);
        StringBuilder query = new StringBuilder()
                .append("SELECT competences.id as id_competence, competences.id_parent, competences.id_type, competences.id_enseignement, rel_competences_cycle.id_cycle, " +
                        "competences_notes.id as id_competences_notes, competences_notes.evaluation, competences_notes.owner, competences_notes.created, devoirs.name as evaluation_libelle " +
                        "FROM notes.competences INNER JOIN notes.rel_competences_cycle ON (competences.id = rel_competences_cycle.id_competence) " +
                        "INNER JOIN notes.competences_notes ON (competences_notes.id_competence = competences.id) " +
                        "INNER JOIN notes.devoirs ON (competences_notes.id_devoir = devoirs.id) " +
                        "WHERE competences_notes.id_eleve = ? ");
        if (idPeriode != null) {
            query.append("AND devoirs.id_periode = ? ");
            values.addNumber(idPeriode);
        }
        query.append("ORDER BY competences_notes.created ");



        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }
}
