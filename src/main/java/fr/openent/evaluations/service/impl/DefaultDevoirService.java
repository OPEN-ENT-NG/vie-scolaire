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
import fr.wseduc.webutils.http.Renders;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.sql.SqlStatementsBuilder;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.List;

import static org.entcore.common.sql.SqlResult.validResultHandler;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public class DefaultDevoirService extends SqlCrudService implements fr.openent.evaluations.service.DevoirService {

    public DefaultDevoirService(String schema, String table) {
        super(schema, table);
    }

    public StringBuilder formatDate (String date) {
        StringBuilder dateFormated = new StringBuilder();
        dateFormated.append(date.split("/")[2]).append('-');
        dateFormated.append(date.split("/")[1]).append('-');
        dateFormated.append(date.split("/")[0]);
        return dateFormated;
    }

    @Override

    public void createDevoir(final JsonObject devoir, final UserInfos user, final Handler<Either<String, JsonObject>> handler) {
        // Requête de recupération de l'id du devoir à créer
        final String queryNewDevoirId =
                "SELECT nextval('" + Viescolaire.EVAL_SCHEMA + ".devoirs_id_seq') as id";

        sql.raw(queryNewDevoirId, SqlResult.validUniqueResultHandler(new Handler<Either<String, JsonObject>>() {
            @Override
            public void handle(Either<String, JsonObject> event) {

                if (event.isRight()) {
                    //Récupération de l'id du devoir à créer
                    final Long devoirId = event.right().getValue().getLong("id");
                    JsonArray statements = new JsonArray();
                    JsonArray competences = devoir.getArray("competences");
                    final JsonObject oCompetenceNote = devoir.getObject("competenceEvaluee");

                    //Merge_user dans la transaction

                    StringBuilder queryParamsForMerge = new StringBuilder();
                    JsonArray paramsForMerge = new JsonArray();
                    paramsForMerge.add(user.getUserId()).add(user.getUsername());

                    StringBuilder queryForMerge = new StringBuilder()
                            .append("SELECT " + schema + "merge_users(?,?)" );
                    statements.add(new JsonObject()
                            .putString("statement", queryForMerge.toString())
                            .putArray("values", paramsForMerge)
                            .putString("action", "prepared"));


                    //Ajout de la creation du devoir dans la pile de transaction
                    StringBuilder queryParams = new StringBuilder();
                    JsonArray params = new JsonArray();
                    StringBuilder valueParams = new StringBuilder();
                    queryParams.append("( id ");
                    valueParams.append("( ?");
                    params.addNumber(devoirId);
                    for (String attr : devoir.getFieldNames()) {
                        if(attr.contains("date")){
                            queryParams.append(" , ").append(attr);
                            valueParams.append(" , to_date(?,'YYYY-MM-DD') ");
                            params.add(formatDate(devoir.getString(attr)).toString());
                        }
                        else{
                            if(!(attr.equals("competencesAdd")
                                    ||  attr.equals("competencesRem")
                                    ||  attr.equals("competenceEvaluee")
                                    ||  attr.equals("competences"))) {
                                queryParams.append(" , ").append(attr);
                                valueParams.append(" , ? ");
                                params.add(devoir.getValue(attr));
                            }
                        }
                    }
                    queryParams.append(" )");
                    valueParams.append(" ) ");
                    queryParams.append(" VALUES ").append(valueParams.toString());
                    StringBuilder query = new StringBuilder()
                            .append("INSERT INTO " + resourceTable + queryParams.toString());
                    statements.add(new JsonObject()
                            .putString("statement", query.toString())
                            .putArray("values", params)
                            .putString("action", "prepared"));


                    //Ajout de chaque compétence dans la pile de transaction
                    if (devoir.containsField("competences") &&
                            devoir.getArray("competences").size() > 0) {

                        JsonArray paramsComp = new JsonArray();
                        StringBuilder queryComp = new StringBuilder()
                                .append("INSERT INTO "+ Viescolaire.EVAL_SCHEMA
                                        +".competences_devoirs (id_devoir, id_competence) VALUES ");
                        for(int i = 0; i < competences.size(); i++){
                            queryComp.append("(?, ?)");
                            paramsComp.addNumber(devoirId);
                            paramsComp.addNumber((Number) competences.get(i));
                            if(i != competences.size()-1){
                                queryComp.append(",");
                            }else{
                                queryComp.append(";");
                            }
                        }
                        statements.add(new JsonObject()
                                .putString("statement", queryComp.toString())
                                .putArray("values", paramsComp)
                                .putString("action", "prepared"));
                    }

                    // ajoute de l'évaluation de la compéténce (cas évaluation libre)
                    if(oCompetenceNote != null) {
                        JsonArray paramsCompLibre = new JsonArray();
                        StringBuilder valueParamsLibre = new StringBuilder();
                        oCompetenceNote.putString("owner", user.getUserId());
                        StringBuilder queryCompLibre = new StringBuilder()
                                .append("INSERT INTO "+ Viescolaire.EVAL_SCHEMA +".competences_notes ");
                        queryCompLibre.append("( id_devoir ");
                        valueParamsLibre.append("( ?");
                        paramsCompLibre.addNumber(devoirId);
                        for (String attr : oCompetenceNote.getFieldNames()) {
                            if(attr.contains("date")){
                                queryCompLibre.append(" , ").append(attr);
                                valueParamsLibre.append(" , to_timestamp(?,'YYYY-MM-DD') ");
                                paramsCompLibre.add(formatDate(oCompetenceNote.getString(attr)).toString());
                            }
                            else{
                                queryCompLibre.append(" , ").append(attr);
                                valueParamsLibre.append(" , ? ");
                                paramsCompLibre.add(oCompetenceNote.getValue(attr));
                            }
                        }
                        queryCompLibre.append(" )");
                        valueParamsLibre.append(" ) ");
                        queryCompLibre.append(" VALUES ").append(valueParamsLibre.toString());
                        statements.add(new JsonObject()
                                .putString("statement", queryCompLibre.toString())
                                .putArray("values", paramsCompLibre)
                                .putString("action", "prepared"));

                    }
                    //Exécution de la transaction avec roleBack

                    Sql.getInstance().transaction(statements, new Handler<Message<JsonObject>>() {
                        @Override
                        public void handle(Message<JsonObject> event) {
                            JsonObject result = event.body();
                            if (result.containsField("status") && result.getString("status").equals("ok")) {
                                handler.handle(new Either.Right<String, JsonObject>(new JsonObject().putNumber("id", devoirId)));
                            }
                            else {
                                handler.handle(new Either.Left<String, JsonObject>(result.getString("status")));
                            }
                        }
                    });
                } else {
                    handler.handle(new Either.Left<String, JsonObject>(event.left().getValue()));
                }
            }
        }));



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
            StringBuilder queryDelNote = new StringBuilder()
                    .append("DELETE FROM "+ Viescolaire.EVAL_SCHEMA +".competences_notes WHERE ");
            for(int i = 0; i < competenceRem.size(); i++){
                query.append("(id_devoir = ? AND  id_competence = ?)");
                queryDelNote.append("(id_devoir = ? AND  id_competence = ?)");
                params.addNumber(Integer.parseInt(id));
                params.addNumber((Number) competenceRem.get(i));
                if(i != competenceRem.size()-1){
                    query.append(" OR ");
                    queryDelNote.append(" OR ");
                }else{
                    query.append(";");
                    queryDelNote.append(";");
                }
            }
            statements.add(new JsonObject()
                    .putString("statement", query.toString())
                    .putArray("values", params)
                    .putString("action", "prepared"));
            statements.add(new JsonObject()
                    .putString("statement", queryDelNote.toString())
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
                params.add(formatDate(devoir.getString(attr)).toString());

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
    /**
     * Liste des devoirs de l'utilisateur
     * @param user utilisateur l'utilisateur connecté
     * @param handler handler portant le résultat de la requête
     */
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
                .append("WHERE devoirs.id_classe is not null ")

                 .append("AND (devoirs.owner = ? OR ") // devoirs dont on est le propriétaire
                        .append("devoirs.owner IN (SELECT DISTINCT id_titulaire ") // ou dont l'un de mes tiulaires le sont (on regarde sur tous mes établissments)
                                            .append("FROM " + Viescolaire.EVAL_SCHEMA + ".rel_professeurs_remplacants ")
                                            .append("INNER JOIN " + Viescolaire.EVAL_SCHEMA + ".devoirs ON devoirs.id_etablissement = rel_professeurs_remplacants.id_etablissement  ")
                                            .append("WHERE id_remplacant = ? ")
                                            .append("AND rel_professeurs_remplacants.id_etablissement IN " + Sql.listPrepared(user.getStructures().toArray()) + " ")
                                            .append(") OR ")
                        .append("? IN (SELECT member_id ") // ou devoirs que l'on m'a partagés (lorsqu'un remplaçant a créé un devoir pour un titulaire par exemple)
                                .append("FROM " + Viescolaire.EVAL_SCHEMA + ".devoirs_shares ")
                                .append("WHERE resource_id = devoirs.id ")
                                .append("AND action = '" + Viescolaire.DEVOIR_ACTION_UPDATE+"')")
                    .append(") ")

                .append("GROUP BY devoirs.id, devoirs.name, devoirs.created, devoirs.libelle, devoirs.id_classe, devoirs.is_evaluated, ")
                .append("devoirs.id_sousmatiere,devoirs.id_periode, devoirs.id_type, devoirs.id_etablissement, devoirs.diviseur, ")
                .append("devoirs.id_etat, devoirs.date_publication, devoirs.date, devoirs.id_matiere, devoirs.coefficient, devoirs.ramener_sur, type_sousmatiere.libelle, periode.libelle, type.nom ")
                .append("ORDER BY devoirs.date ASC;");

        // Ajout des params pour les devoirs dont on est le propriétaire
        values.add(user.getUserId());

        // Ajout des params pour la récupération des devoirs de mes tiulaires
        values.add(user.getUserId());
        for (int i = 0; i < user.getStructures().size(); i++) {
            values.add(user.getStructures().get(i));
        }

        // Ajout des params pour les devoirs que l'on m'a partagés (lorsqu'un remplaçant a créé un devoir pour un titulaire par exemple)
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
    @Deprecated // FIXME GERER LES DROITS ET PERMISSIONS COMME FAIT POUR LES ENSEIGNANTS
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
    public void getNbNotesDevoirs(UserInfos user, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT count(notes.id) as nb_notes, devoirs.id, devoirs.id_classe ")
                .append("FROM "+ Viescolaire.EVAL_SCHEMA +".notes, "+ Viescolaire.EVAL_SCHEMA +".devoirs ")
                .append("WHERE notes.id_devoir = devoirs.id ")

                .append("AND (devoirs.owner = ? OR ") // devoirs dont on est le propriétaire
                        .append("devoirs.owner IN (SELECT DISTINCT id_titulaire ") // ou dont l'un de mes tiulaires le sont (on regarde sur tous mes établissments)
                                            .append("FROM " + Viescolaire.EVAL_SCHEMA + ".rel_professeurs_remplacants ")
                                            .append("INNER JOIN " + Viescolaire.EVAL_SCHEMA + ".devoirs ON devoirs.id_etablissement = rel_professeurs_remplacants.id_etablissement  ")
                                            .append("WHERE id_remplacant = ? ")
                                            .append("AND rel_professeurs_remplacants.id_etablissement IN " + Sql.listPrepared(user.getStructures().toArray()) + " ")
                                            .append(") OR ")
                        .append("? IN (SELECT member_id ") // ou devoirs que l'on m'a partagés (lorsqu'un remplaçant a créé un devoir pour un titulaire par exemple)
                                .append("FROM " + Viescolaire.EVAL_SCHEMA + ".devoirs_shares ")
                                .append("WHERE resource_id = devoirs.id ")
                                .append("AND action = '" + Viescolaire.DEVOIR_ACTION_UPDATE+"')")
                        .append(") ")
                .append("GROUP by devoirs.id, devoirs.id_classe");

        JsonArray values =  new JsonArray();

        // Ajout des params pour les devoirs dont on est le propriétaire
        values.add(user.getUserId());

        // Ajout des params pour la récupération des devoirs de mes tiulaires
        values.add(user.getUserId());
        for (int i = 0; i < user.getStructures().size(); i++) {
            values.add(user.getStructures().get(i));
        }

        // Ajout des params pour les devoirs que l'on m'a partagés (lorsqu'un remplaçant a créé un devoir pour un titulaire par exemple)
        values.add(user.getUserId());

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }
}
