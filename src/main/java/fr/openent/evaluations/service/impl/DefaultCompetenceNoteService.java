
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
        /*StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();
            query.append("INSERT INTO "+ Viescolaire.EVAL_SCHEMA +".competences_notes (id_devoir, id_competence, evaluation, owner, id_eleve, created) VALUES (?, ?, ?, ?, ?, now());");
            values.addNumber(competenceNote.getNumber("id_devoir"))
                    .addNumber(competenceNote.getNumber("id_competence"))
                    .addNumber(competenceNote.getNumber("evaluation"))
                    .add(user.getUserId())
                    .add(competenceNote.getString("id_eleve"));
        Sql.getInstance().prepared(query.toString(), values, SqlResult.validRowsResultHandler(handler));*/
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
                .append("SELECT DISTINCT competences.id as id_competence, competences.id_parent, competences.id_type, competences.id_cycle, ")
                .append("competences_notes.id as id_competences_notes, competences_notes.evaluation, competences_notes.owner, competences_notes.created, devoirs.name as evaluation_libelle, devoirs.date as evaluation_date,")
                .append("rel_competences_domaines.id_domaine, ")
                .append("users.username as owner_name ")
                .append("FROM notes.competences ")
                .append("INNER JOIN "+ Viescolaire.EVAL_SCHEMA +".rel_competences_domaines ON (competences.id = rel_competences_domaines.id_competence) ")
                .append("INNER JOIN "+ Viescolaire.EVAL_SCHEMA +".competences_notes ON (competences_notes.id_competence = competences.id) ")
                .append("INNER JOIN "+ Viescolaire.EVAL_SCHEMA +".devoirs ON (competences_notes.id_devoir = devoirs.id) ")
                .append("INNER JOIN "+ Viescolaire.EVAL_SCHEMA +".users ON (users.id = devoirs.owner) ")
                .append("WHERE competences_notes.id_eleve = ? ");
        if (idPeriode != null) {
            query.append("AND devoirs.id_periode = ? ");
            values.addNumber(idPeriode);
        }
        query.append("ORDER BY competences_notes.created ");



        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }


    @Override
    public void getCompetencesNotesClasse(String idClasse, Long idPeriode, Handler<Either<String, JsonArray>> handler) {
        JsonArray values = new JsonArray().addString(idClasse);
        StringBuilder query = new StringBuilder()
                .append("SELECT competences_notes.id_eleve AS id_eleve, competences.id as id_competence, max(competences_notes.evaluation) as evaluation,rel_competences_domaines.id_domaine, competences_notes.owner ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".competences ")
                .append("INNER JOIN "+ Viescolaire.EVAL_SCHEMA +".rel_competences_domaines ON (competences.id = rel_competences_domaines.id_competence) ")
                .append("INNER JOIN "+ Viescolaire.EVAL_SCHEMA +".competences_notes ON (competences_notes.id_competence = competences.id) ")
                .append("INNER JOIN "+ Viescolaire.EVAL_SCHEMA +".devoirs ON (competences_notes.id_devoir = devoirs.id) ")
                .append("WHERE devoirs.id_classe = ? ");
        if (idPeriode != null) {
            query.append("AND devoirs.id_periode = ? ");
            values.addNumber(idPeriode);
        }
        query.append("GROUP BY competences.id, competences.id_cycle,rel_competences_domaines.id_domaine, competences_notes.id_eleve, competences_notes.owner ");

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getConverssionNoteCompetence(String idEtablissement, String idClasse, Handler<Either<String,JsonArray>> handler){
        JsonArray values = new JsonArray();
        StringBuilder query = new StringBuilder()
                .append("Select valmin, valmax, libelle, ordre, couleur from notes.niveau_competences  niv ")
                .append("INNER JOIN  notes.echelle_converssion_niv_note echelle on niv.id = echelle.id_niveau ")
                .append("INNER JOIN  notes.rel_classe_cycle CC on cc.id_cycle = niv.id_cycle ")
                .append("AND cc.id_classe = ? ")
                .append("AND echelle.id_structure = ? ");
        values.addString(idClasse);
        values.addString(idEtablissement);
        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }
}
