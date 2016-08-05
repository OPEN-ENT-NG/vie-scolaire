package org.cgi.evaluations.service.impl;

import fr.wseduc.webutils.Either;
import org.cgi.evaluations.service.IEvalDevoirService;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;

import static org.entcore.common.sql.SqlResult.validResultHandler;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public class CEvalDevoirServiceImpl extends SqlCrudService implements IEvalDevoirService {

    public CEvalDevoirServiceImpl(String table) {
        super(table);
    }

    @Override
    public void listDevoirs(UserInfos user, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT devoirs.id, devoirs.name, devoirs.created, devoirs.libelle, devoirs.idclasse, ")
                .append("devoirs.idsousmatiere,devoirs.idperiode, devoirs.idtype, devoirs.idetablissement, devoirs.diviseur, ")
                .append("devoirs.idetat, devoirs.datepublication, devoirs.idmatiere, devoirs.coefficient, devoirs.ramenersur, ")
                .append("typesousmatiere.libelle as _sousmatiere_libelle, devoirs.date, ")
                .append("type.nom as _type_libelle, periode.libelle as _periode_libelle, COUNT(competences_devoirs.id) as nbcompetences ")
                .append("FROM notes.devoirs ")
                .append("inner join notes.type on devoirs.idtype = type.id ")
                .append("inner join notes.periode on devoirs.idperiode = periode.id ")
                .append("left join notes.competences_devoirs on devoirs.id = competences_devoirs.iddevoir ")
                .append("left join notes.sousmatiere  on devoirs.idsousmatiere = sousmatiere.id ")
                .append("left join notes.typesousmatiere on sousmatiere.id_typesousmatiere = typesousmatiere.id ")
                .append("WHERE devoirs.owner = ? ")
                .append("GROUP BY devoirs.id, devoirs.name, devoirs.created, devoirs.libelle, devoirs.idclasse, ")
                .append("devoirs.idsousmatiere,devoirs.idperiode, devoirs.idtype, devoirs.idetablissement, devoirs.diviseur, ")
                .append("devoirs.idetat, devoirs.datepublication, devoirs.idmatiere, devoirs.coefficient, devoirs.ramenersur, typesousmatiere.libelle, periode.libelle, type.nom ")
                .append("ORDER BY devoirs.date ASC;");
        values.add(user.getUserId());

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }



    @Override
    public void listDevoirs(String idEtablissement, String idClasse, String idMatiere, Integer idPeriode, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT devoirs.*, ")
                .append("type.nom as _type_libelle, periode.libelle as _periode_libelle ")
                .append("FROM ")
                .append("notes.devoirs ")
                .append("inner join notes.periode on devoirs.idperiode = periode.id ")
                .append("inner join notes.type on devoirs.idtype = type.id ")
                .append("WHERE ")
                .append("devoirs.idetablissement = ? ")
                .append("AND ")
                .append("devoirs.idclasse = ? ")
                .append("AND ")
                .append("devoirs.idmatiere = ? ")
                .append("AND ")
                .append("devoirs.idperiode = ? ")
                .append("ORDER BY devoirs.date ASC, devoirs.id ASC");

        values.addString(idEtablissement);
        values.addString(idClasse);
        values.addString(idMatiere);
        values.addNumber(idPeriode);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void listDevoirs(String idEtablissement, Integer idPeriode, String idUser, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT devoirs.*,notes.valeur as note, notes.appreciation, ")
                .append("typesousmatiere.libelle as _sousmatiere_libelle,")
                .append("sousmatiere.id as _sousmatiere_id ")
                .append("FROM ")
                .append("notes.devoirs ")
                .append("INNER JOIN notes.notes ON devoirs.id = notes.iddevoir ")
                .append("LEFT JOIN notes.sousmatiere ON devoirs.idsousmatiere = sousmatiere.id ")
                .append("LEFT JOIN notes.typesousmatiere ON sousmatiere.id_typesousmatiere = typesousmatiere.id ")
                .append("WHERE devoirs.idetablissement = ? ")
                .append("AND devoirs.idperiode = ? ")
                .append("AND notes.ideleve = ? ")
                .append("AND devoirs.datepublication <= current_date ")
                .append("ORDER BY devoirs.date ASC");

        values.addString(idEtablissement);
        values.addNumber(idPeriode);
        values.addString(idUser);

        // date du jour
        // DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        //String datePublication = df.format(new Date());

        // values.addString(datePublication);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }
}
