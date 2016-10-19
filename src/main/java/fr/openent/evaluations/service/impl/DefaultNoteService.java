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
import fr.openent.evaluations.service.NoteService;
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
public class DefaultNoteService extends SqlCrudService implements fr.openent.evaluations.service.NoteService {
    public DefaultNoteService(String schema, String table) {
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

        query.append("SELECT devoirs.id as id_devoir, devoirs.date, devoirs.coefficient, devoirs.ramener_sur,notes.valeur, notes.id, notes.id_eleve ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".notes, "+ Viescolaire.EVAL_SCHEMA +".devoirs ")
                .append("WHERE notes.id_devoir = ? " +
                        "AND notes.id_devoir = devoirs.id");
        values.add(devoirId);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void getNoteParDevoirEtParEleve(Integer idDevoir, String idEleve, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT notes.* ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".notes ")
                .append("WHERE notes.id_devoir = ? ")
                .append("AND notes.id_eleve = ? ");

        values.addNumber(idDevoir);
        values.addString(idEleve);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void updateNote(JsonObject data, UserInfos user, Handler<Either<String, JsonObject>> handler) {
        super.update(data.getValue("id").toString(), data, user, handler);
    }

    @Override
    public void deleteNote(Integer idNote, UserInfos user, Handler<Either<String, JsonObject>> handler) {
        super.delete(idNote.toString(), user, handler);
    }

    @Override
    public void getWidgetNotes(String userId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT notes.valeur, devoirs.id, devoirs.date, devoirs.id_matiere, devoirs.diviseur, devoirs.libelle, devoirs.name ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".notes, "+ Viescolaire.EVAL_SCHEMA +".devoirs ")
                .append("WHERE notes.id_eleve = ? ")
                .append("AND notes.id_devoir = devoirs.id ")
                .append("AND devoirs.date_publication <= current_date ")
                .append("ORDER BY notes.id DESC ")
                .append("LIMIT 5;");
        values.addString(userId);
        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void getNoteElevePeriode(String userId, String etablissementId, String classeId, String matiereId, Integer periodeId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT devoirs.id as id_devoir, devoirs.date, devoirs.coefficient, devoirs.ramener_sur, ")
                .append(" notes.valeur, notes.id ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".devoirs ")
                .append("left join "+ Viescolaire.EVAL_SCHEMA +".notes on devoirs.id = notes.id_devoir and notes.id_eleve = ?")
                .append("WHERE devoirs.id_etablissement = ? ")
                .append("AND devoirs.id_classe = ? ")
                .append("AND devoirs.id_matiere = ? ")
                .append("AND devoirs.id_periode = ? ")
                .append("ORDER BY devoirs.date ASC, devoirs.id ASC;");

        values.addString(userId).addString(etablissementId).addString(classeId).addString(matiereId).addNumber(periodeId);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void getNotesReleve(String etablissementId, String classeId, String matiereId, Integer periodeId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT devoirs.id as id_devoir, devoirs.date, devoirs.coefficient, devoirs.ramener_sur,notes.valeur, notes.id, notes.id_eleve " +
                "FROM "+ Viescolaire.EVAL_SCHEMA +".devoirs " +
                "left join "+ Viescolaire.EVAL_SCHEMA +".notes on devoirs.id = notes.id_devoir " +
                "WHERE devoirs.id_etablissement = ? " +
                "AND devoirs.id_classe = ? " +
                "AND devoirs.id_matiere = ? " +
                "AND devoirs.id_periode = ?" +
                "ORDER BY devoirs.date ASC ;");
        values.addString(etablissementId).addString(classeId).addString(matiereId).addNumber(periodeId);
        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }
}
