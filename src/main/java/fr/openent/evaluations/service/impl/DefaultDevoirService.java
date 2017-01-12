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
import fr.openent.evaluations.service.DevoirService;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static org.entcore.common.sql.Sql.parseId;
import static org.entcore.common.sql.SqlResult.validResultHandler;
import static org.entcore.common.sql.SqlResult.validRowsResultHandler;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public class DefaultDevoirService extends SqlCrudService implements fr.openent.evaluations.service.DevoirService {

    public DefaultDevoirService(String schema, String table) {
        super(schema, table);
    }

    @Override
    public void createDevoir(JsonObject devoir, UserInfos user, Handler<Either<String, JsonObject>> handler) {
        super.create(devoir, user, handler);
    }

    @Override
    public void updateDevoir(String id, JsonObject devoir, Handler<Either<String, JsonArray>> handler) {
        JsonArray statements = new JsonArray();
        if (devoir.containsField("competencesAdd") &&
                devoir.getArray("competencesAdd").size() > 0) {
            JsonArray competenceAdd = devoir.getArray("competencesAdd");
            JsonArray params = new JsonArray();
            StringBuilder query = new StringBuilder()
                    .append("INSERT INTO "+ Viescolaire.EVAL_SCHEMA +".competences_devoirs (id_devoir, id_competence) VALUES ");
            for(int i = 0; i < competenceAdd.size(); i++){
                query.append("(?, ?)");
                params.addNumber(Integer.parseInt(id));
                params.addNumber((Number) competenceAdd.get(i));
                if(i != competenceAdd.size()-1){
                    query.append(",");
                }else{
                    query.append(";");
                }
            }
            statements.add(new JsonObject()
                    .putString("statement", query.toString())
                    .putArray("values", params)
                    .putString("action", "prepared"));
        }
        if (devoir.containsField("competencesRem") &&
                devoir.getArray("competencesRem").size() > 0) {
            JsonArray competenceRem = devoir.getArray("competencesRem");
            JsonArray params = new JsonArray();
            StringBuilder query = new StringBuilder()
                    .append("DELETE FROM "+ Viescolaire.EVAL_SCHEMA +".competences_devoirs WHERE ");
            for(int i = 0; i < competenceRem.size(); i++){
                query.append("(id_devoir = ? AND  id_competence = ?)");
                params.addNumber(Integer.parseInt(id));
                params.addNumber((Number) competenceRem.get(i));
                if(i != competenceRem.size()-1){
                    query.append(" OR ");
                }else{
                    query.append(";");
                }
            }
            statements.add(new JsonObject()
            .putString("statement", query.toString())
            .putArray("values", params)
            .putString("action", "prepared"));
        }

        StringBuilder queryParams = new StringBuilder();
        JsonArray params = new JsonArray();
        devoir.removeField("competencesRem");
        devoir.removeField("competencesAdd");
        devoir.removeField("competences");

        for (String attr : devoir.getFieldNames()) {
            if(attr.contains("date")){
                queryParams.append(attr).append(" =to_date(?,'YYYY-MM-DD'), ");
                try {
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    String date_publication = devoir.getString(attr);
                    Date parsedDate = formatter.parse(date_publication);
                    StringBuilder dateFormated = new StringBuilder();
                    dateFormated.append(date_publication.split("/")[2]).append('-');
                    dateFormated.append(date_publication.split("/")[1]).append('-');
                    dateFormated.append(date_publication.split("/")[0]);

                    params.add(dateFormated.toString());
                }
                catch(ParseException pe){
                    System.err.println(pe);
                }
            }
            else {
                queryParams.append(attr).append(" = ?, ");
                params.add(devoir.getValue(attr));
            }
        }
        StringBuilder query = new StringBuilder()
                .append("UPDATE " + resourceTable +" SET " + queryParams.toString() + "modified = NOW() WHERE id = ? ");
        statements.add(new JsonObject()
                .putString("statement", query.toString())
                .putArray("values", params.addNumber(Integer.parseInt(id)))
                .putString("action", "prepared"));
        Sql.getInstance().transaction(statements, SqlResult.validResultHandler(handler));
    }

    @Override
    public void listDevoirs(UserInfos user, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT devoirs.id, devoirs.name, devoirs.created, devoirs.libelle, devoirs.id_classe, devoirs.is_evaluated,")
                .append("devoirs.id_sousmatiere,devoirs.id_periode, devoirs.id_type, devoirs.id_etablissement, devoirs.diviseur, ")
                .append("devoirs.id_etat, devoirs.date_publication, devoirs.id_matiere, devoirs.coefficient, devoirs.ramener_sur, ")
                .append("type_sousmatiere.libelle as _sousmatiere_libelle, devoirs.date, ")
                .append("type.nom as _type_libelle, periode.libelle as _periode_libelle, COUNT(competences_devoirs.id) as nbcompetences ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".devoirs ")
                .append("inner join "+ Viescolaire.EVAL_SCHEMA +".type on devoirs.id_type = type.id ")
                .append("inner join "+ Viescolaire.VSCO_SCHEMA +".periode on devoirs.id_periode = periode.id ")
                .append("left join "+ Viescolaire.EVAL_SCHEMA +".competences_devoirs on devoirs.id = competences_devoirs.id_devoir ")
                .append("left join "+ Viescolaire.VSCO_SCHEMA +".sousmatiere  on devoirs.id_sousmatiere = sousmatiere.id ")
                .append("left join "+ Viescolaire.VSCO_SCHEMA +".type_sousmatiere on sousmatiere.id_type_sousmatiere = type_sousmatiere.id ")
                .append("WHERE devoirs.owner = ? ")
                .append("AND devoirs.id_classe is not null ")
                .append("GROUP BY devoirs.id, devoirs.name, devoirs.created, devoirs.libelle, devoirs.id_classe, devoirs.is_evaluated, ")
                .append("devoirs.id_sousmatiere,devoirs.id_periode, devoirs.id_type, devoirs.id_etablissement, devoirs.diviseur, ")
                .append("devoirs.id_etat, devoirs.date_publication, devoirs.date, devoirs.id_matiere, devoirs.coefficient, devoirs.ramener_sur, type_sousmatiere.libelle, periode.libelle, type.nom ")
                .append("ORDER BY devoirs.date ASC;");
        values.add(user.getUserId());

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }



    @Override
    public void listDevoirs(String idEtablissement, String idClasse, String idMatiere, Long idPeriode, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT devoirs.*, ")
                .append("type.nom as _type_libelle, periode.libelle as _periode_libelle ")
                .append("FROM ")
                .append(Viescolaire.EVAL_SCHEMA +".devoirs ")
                .append("inner join "+ Viescolaire.VSCO_SCHEMA +".periode on devoirs.id_periode = periode.id ")
                .append("inner join "+ Viescolaire.EVAL_SCHEMA +".type on devoirs.id_type = type.id ")
                .append("WHERE ")
                .append("devoirs.id_etablissement = ? ")
                .append("AND ")
                .append("devoirs.id_classe = ? ")
                .append("AND ")
                .append("devoirs.id_matiere = ? ")
                .append("AND ")
                .append("devoirs.id_periode = ? ")
                .append("ORDER BY devoirs.date ASC, devoirs.id ASC");

        values.addString(idEtablissement);
        values.addString(idClasse);
        values.addString(idMatiere);
        values.addNumber(idPeriode);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void listDevoirs(String idEtablissement, Long idPeriode, String idUser, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT devoirs.*,type_sousmatiere.libelle as _sousmatiere_libelle,sousmatiere.id as _sousmatiere_id " +
                "FROM "+ Viescolaire.EVAL_SCHEMA +".devoirs " +
                "LEFT JOIN "+ Viescolaire.VSCO_SCHEMA +".sousmatiere ON devoirs.id_sousmatiere = sousmatiere.id " +
                "LEFT JOIN "+ Viescolaire.VSCO_SCHEMA +".type_sousmatiere ON sousmatiere.id_type_sousmatiere = type_sousmatiere.id " +
                "WHERE devoirs.id_etablissement = ?" +
                "AND devoirs.id_periode = ? " +
                "AND devoirs.owner = ? " +
                "AND devoirs.date_publication <= current_date " +
                "ORDER BY devoirs.date ASC;");

        values.addString(idEtablissement);
        values.addNumber(idPeriode);
        values.addString(idUser);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void getNbNotesDevoirs(String userId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT count(notes.id) as nb_notes, devoirs.id, devoirs.id_classe " +
                "FROM "+ Viescolaire.EVAL_SCHEMA +".notes, "+ Viescolaire.EVAL_SCHEMA +".devoirs " +
                "WHERE notes.id_devoir = devoirs.id " +
                "AND devoirs.owner = ? " +
                "GROUP by devoirs.id, devoirs.id_classe");

        Sql.getInstance().prepared(query.toString(), new JsonArray().addString(userId), SqlResult.validResultHandler(handler));
    }
}
