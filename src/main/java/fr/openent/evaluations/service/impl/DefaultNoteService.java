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
    public void listNotesParDevoir(Long devoirId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();
        //tables
        String table_appreciation = Viescolaire.EVAL_SCHEMA + "." +Viescolaire.EVAL_APPRECIATIONS_TABLE;
        String table_note         = Viescolaire.EVAL_SCHEMA + "." +Viescolaire.EVAL_NOTES_TABLE;

        //colonne note
        String note_id        = table_note + ".id";
        String note_id_eleve  = table_note + ".id_eleve";
        String note_id_devoir = table_note + ".id_devoir";
        String note_valeur    = table_note + ".valeur";

        //colonne appreciation
        String appreciation_id        = table_appreciation + ".id";
        String appreciation_valeur    = table_appreciation +".valeur";
        String appreciation_id_eleve  = table_appreciation + ".id_eleve";
        String appreciation_id_devoir = table_appreciation + ".id_devoir";

        query.append("SELECT res.*,devoirs.date, devoirs.coefficient, devoirs.ramener_sur  ")
                .append(" FROM ( SELECT "+ appreciation_id_devoir +" as id_devoir, " + appreciation_id_eleve+", " + note_id + " as id, ")
                .append(note_valeur + " as valeur, " + appreciation_id +" as id_appreciation, " + appreciation_valeur)
                .append(" as appreciation FROM " + table_appreciation +
                        "\n LEFT JOIN " + table_note)
                .append( "\n ON ( " + appreciation_id_devoir + " = " + note_id_devoir + " AND " )
                .append(appreciation_id_eleve + " = " + note_id_eleve + " ) UNION ")

                .append("\n SELECT " +  note_id_devoir +" as id_devoir, " +note_id_eleve + ", " + note_id + " as id, ")
                .append(note_valeur + " as valeur, null, null FROM " + table_note + " WHERE NOT EXISTS ( ")
                .append("\n SELECT 1 FROM " + table_appreciation + " WHERE "+ note_id_devoir +" = " + appreciation_id_devoir)
                .append(" AND " + note_id_eleve + " = " + appreciation_id_eleve + " ) " +
                        "ORDER BY 1, 2")
                .append(") AS res, notes.devoirs WHERE res.id_devoir = devoirs.id AND devoirs.id = ? ");
        values.add(devoirId);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void getNoteParDevoirEtParEleve(Long idDevoir, String idEleve, Handler<Either<String, JsonArray>> handler) {
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
    public void deleteNote(Long idNote, UserInfos user, Handler<Either<String, JsonObject>> handler) {
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
    public void getNoteElevePeriode(String userId, String etablissementId, String classeId, String matiereId, Long periodeId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT devoirs.id as id_devoir, devoirs.date, devoirs.coefficient, devoirs.ramener_sur, ")
                .append(" notes.valeur, notes.id ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".devoirs ")
                .append("left join "+ Viescolaire.EVAL_SCHEMA +".notes on devoirs.id = notes.id_devoir and notes.id_eleve = ? ")
                .append("INNER join "+ Viescolaire.EVAL_SCHEMA +".rel_devoirs_groupes ON rel_devoirs_groupes.id_devoir = devoirs.id AND rel_devoirs_groupes.id_groupe = ? ")
                .append("WHERE devoirs.id_etablissement = ? ")
                .append("AND devoirs.id_matiere = ? ")
                .append("AND devoirs.id_periode = ? ")
                .append("ORDER BY devoirs.date ASC, devoirs.id ASC;");

        values.addString(userId).addString(classeId).addString(etablissementId).addString(matiereId).addNumber(periodeId);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void getNotesReleve(String etablissementId, String classeId, String matiereId, Long periodeId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT devoirs.id as id_devoir, devoirs.date, devoirs.coefficient, devoirs.ramener_sur,notes.valeur, notes.id, notes.id_eleve, devoirs.is_evaluated " +
                "FROM "+ Viescolaire.EVAL_SCHEMA +".devoirs " +
                "left join "+ Viescolaire.EVAL_SCHEMA +".notes on devoirs.id = notes.id_devoir " +
                "INNER JOIN "+ Viescolaire.EVAL_SCHEMA +".rel_devoirs_groupes ON (rel_devoirs_groupes.id_devoir = devoirs.id AND rel_devoirs_groupes.id_groupe = ? ) " +
                "WHERE devoirs.id_etablissement = ? " +
                "AND devoirs.id_matiere = ? " +
                "AND devoirs.id_periode = ? " +
                "ORDER BY devoirs.date ASC ;");
        values.addString(classeId).addString(etablissementId).addString(matiereId).addNumber(periodeId);
        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }
}
