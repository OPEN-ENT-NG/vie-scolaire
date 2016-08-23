
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

package org.cgi.evaluations.service.impl;

import fr.wseduc.webutils.Either;
import org.cgi.evaluations.service.IEvalCompetenceNoteService;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.impl.Json;

import java.util.List;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public class CEvalCompetenceNoteServiceImpl extends SqlCrudService implements IEvalCompetenceNoteService {
    public CEvalCompetenceNoteServiceImpl(String schema, String table) {
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

        query.append("SELECT notes.competences_notes.*,notes.competences.nom as nom, notes.competences.idtype as idtype, notes.competences.idparent as idparent ")
                .append("FROM notes.competences_notes, notes.competences ")
                .append("WHERE notes.competences_notes.idcompetence = notes.competences.id ")
                .append("AND competences_notes.iddevoir = ? AND competences_notes.ideleve = ? ")
                .append("ORDER BY notes.competences_notes.id ASC;");

        JsonArray params = new JsonArray();
        params.addNumber(idDevoir);
        params.addString(idEleve);

        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getCompetencesNotesDevoir(Integer idDevoir, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT competences.nom, competences_notes.id, competences_notes.iddevoir, competences_notes.ideleve, competences_notes.idcompetence, competences_notes.evaluation " +
                "FROM notes.competences_notes, notes.competences " +
                "WHERE competences_notes.iddevoir = ? " +
                "AND competences.id = competences_notes.idcompetence");

        Sql.getInstance().prepared(query.toString(), new JsonArray().addNumber(idDevoir), SqlResult.validResultHandler(handler));
    }

    @Override
    public void updateCompetencesNotesDevoir(JsonArray _datas, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();
        for (int i = 0; i < _datas.size(); i++) {
            JsonObject o = _datas.get(i);
            query.append("UPDATE notes.competences_notes SET evaluation = ?, modified = now() WHERE id = ?;");
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
            query.append("INSERT INTO notes.competences_notes (iddevoir, idcompetence, evaluation, owner, ideleve, created) VALUES (?, ?, ?, ?, ?, now());");
            values.add(o.getInteger("iddevoir")).add(o.getInteger("idcompetence")).add(o.getInteger("evaluation"))
                    .add(user.getUserId()).add(o.getString("ideleve"));
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
        query.append("DELETE FROM notes.competences_notes WHERE id IN " + Sql.listPrepared(ids.toArray()) + ";");
        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }
}
