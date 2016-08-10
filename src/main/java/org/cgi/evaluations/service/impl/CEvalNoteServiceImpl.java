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
import org.cgi.evaluations.service.IEvalNoteService;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.entcore.common.sql.SqlResult.validResultHandler;
import static org.entcore.common.sql.SqlResult.validUniqueResultHandler;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public class CEvalNoteServiceImpl extends SqlCrudService implements IEvalNoteService{
    public CEvalNoteServiceImpl(String schema, String table) {
        super(schema, table);
    }

    @Override
    public void createNote(JsonObject note, UserInfos user, Handler<Either<String, JsonObject>> handler) {
        super.create(note, user, handler);
    }

    @Override
    public void listNotesParDevoir(Integer devoirId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT notes.* ")
                .append("FROM notes.notes ")
                .append("WHERE notes.iddevoir = ? ");
        values.add(devoirId);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void getNoteParDevoirEtParEleve(Integer idDevoir, String idEleve, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT notes.* ")
                .append("FROM notes.notes ")
                .append("WHERE notes.iddevoir = ? ")
                .append("AND notes.ideleve = ? ");

        values.addNumber(idDevoir);
        values.addString(idEleve);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void updateNote(JsonObject data, UserInfos user, Handler<Either<String, JsonObject>> handler) {
//        StringBuilder query = new StringBuilder();
//        JsonArray values = new JsonArray();
//
//        query.append("UPDATE notes.notes ")
//                .append("SET ")
//                .append("ideleve = ? ,")
//                .append("iddevoir = ? ,")
//                .append("valeur = ? ,")
//                .append("appreciation = ?")
//                .append("WHERE ")
//                .append("notes.id = ?");
//        values.add(data.getValue("ideleve"));
//        values.add((Integer) data.getValue("iddevoir"));
//        values.add((Number) data.getValue("valeur"));
//        values.addString(data.getString("appreciation"));
//        values.add((Integer) data.getValue("id"));
//        Sql.getInstance().prepared(query.toString(), values, validUniqueResultHandler(handler));
        super.update(data.getValue("id").toString(), data, user, handler);
    }

    @Override
    public void deleteNote(Integer idNote, UserInfos user, Handler<Either<String, JsonObject>> handler) {
//        StringBuilder query = new StringBuilder();
//        JsonArray values = new JsonArray();
//
//        query.append("DELETE FROM notes.notes ")
//                .append("WHERE notes.id = ?");
//        values.addNumber(idNote);
//        Sql.getInstance().prepared(query.toString(), values, validUniqueResultHandler(handler));
        super.delete(idNote.toString(), user, handler);
    }

    @Override
    public void getWidgetNotes(String userId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT notes.valeur, devoirs.id, devoirs.date, devoirs.idmatiere, devoirs.diviseur, devoirs.libelle, devoirs.name ")
                .append("FROM notes.notes, notes.devoirs ")
                .append("WHERE notes.ideleve = ? ")
                .append("AND notes.iddevoir = devoirs.id ")
                .append("AND devoirs.datepublication <= current_date ")
                .append("ORDER BY notes.id DESC ")
                .append("LIMIT 5;");
        values.addString(userId);
        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void getNoteElevePeriode(String userId, String etablissementId, String classeId, String matiereId, Integer periodeId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT devoirs.id as iddevoir, devoirs.date, devoirs.coefficient, devoirs.ramenersur, ")
                .append(" notes.valeur, notes.id ")
                .append("FROM notes.devoirs ")
                .append("left join notes.notes on devoirs.id = notes.iddevoir and notes.ideleve = ?")
                .append("WHERE devoirs.idetablissement = ? ")
                .append("AND devoirs.idclasse = ? ")
                .append("AND devoirs.idmatiere = ? ")
                .append("AND devoirs.idperiode = ? ")
                .append("ORDER BY devoirs.date ASC, devoirs.id ASC;");

        values.addString(userId).addString(etablissementId).addString(classeId).addString(matiereId).addNumber(periodeId);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }
}
