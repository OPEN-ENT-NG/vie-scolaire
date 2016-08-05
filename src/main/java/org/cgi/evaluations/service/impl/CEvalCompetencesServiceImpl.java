package org.cgi.evaluations.service.impl;

import fr.wseduc.webutils.Either;
import org.cgi.evaluations.service.IEvalCompetencesService;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public class CEvalCompetencesServiceImpl extends SqlCrudService implements IEvalCompetencesService {
    public CEvalCompetencesServiceImpl(String table) {
        super(table);
    }

    @Override
    public void getCompetences(Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT competences.id, competences.nom, competences.description, competences.idtype, competences.idparent, typecompetences.nom as type ")
                .append("FROM notes.competences, notes.typecompetences ")
                .append("WHERE competences.idtype = typecompetences.id ")
                .append("ORDER BY competences.id ASC");
        Sql.getInstance().prepared(query.toString(), new JsonArray(), SqlResult.validResultHandler(handler));
    }

    @Override
    public void setDevoirCompetences(Integer devoirId, JsonArray values, Handler<Either<String, JsonObject>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray data = new JsonArray();
        query.append("INSERT INTO notes.competences_devoirs (iddevoir, idcompetence) VALUES ");
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

        query.append("SELECT notes.competences_devoirs.*, notes.competences.nom as nom, notes.competences.idtype as idtype, notes.competences.idparent as idparent ")
                .append("FROM notes.competences_devoirs, notes.competences ")
                .append("WHERE notes.competences_devoirs.idcompetence = notes.competences.id ")
                .append("AND competences_devoirs.iddevoir = ? ")
                .append("ORDER BY notes.competences_devoirs.id ASC;");

        Sql.getInstance().prepared(query.toString(), new JsonArray().addNumber(devoirId), SqlResult.validResultHandler(handler));
    }

    @Override
    public void getLastCompetencesDevoir(String userId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT * FROM notes.competences_devoirs  ")
                .append("WHERE iddevoir IN ( ")
                .append("SELECT id FROM notes.devoirs WHERE notes.devoirs.owner = ? ORDER BY notes.devoirs.created DESC LIMIT 1 ")
                .append(");");

        Sql.getInstance().prepared(query.toString(), new JsonArray().addString(userId), SqlResult.validResultHandler(handler));
    }

    @Override
    public void getSousCompetences(Integer skillId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT * ")
                .append("FROM notes.competences ")
                .append("WHERE competences.idparent = ?;");

        Sql.getInstance().prepared(query.toString(), new JsonArray().addNumber(skillId), SqlResult.validResultHandler(handler));
    }

    @Override
    public void getCompetencesEnseignement(Integer teachingId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT * ")
                .append("FROM notes.competences ")
                .append("WHERE competences.idenseignement = ? ")
                .append("AND competences.idparent = 0 ;");

        Sql.getInstance().prepared(query.toString(), new JsonArray().addNumber(teachingId), SqlResult.validResultHandler(handler));
    }
}
