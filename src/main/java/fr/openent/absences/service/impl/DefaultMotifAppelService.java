package fr.openent.absences.service.impl;

import fr.openent.Viescolaire;
import fr.wseduc.webutils.Either;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.entcore.common.sql.SqlResult.validRowsResultHandler;
import static org.entcore.common.sql.SqlResult.validUniqueResultHandler;

/**
 * Created by anabah on 06/06/2017.
 */
public class DefaultMotifAppelService extends SqlCrudService implements fr.openent.absences.service.MotifAppelService {

    public DefaultMotifAppelService() {
        super(Viescolaire.ABSC_SCHEMA, Viescolaire.ABSC_MOTIF_APPEL_TABLE);
    }

    @Override
    public void getAbscMotifsAppelEtbablissement(String psIdEtablissement, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT motif_appel.id, motif_appel.id_etablissement, motif_appel.libelle, justifiant, commentaire, defaut ")
                .append(",categorie_motif_appel.libelle as categorie, categorie_motif_appel.id as id_categorie " )
                .append("FROM "+ Viescolaire.ABSC_SCHEMA +"." + Viescolaire.ABSC_MOTIF_APPEL_TABLE + " ")
                .append("INNER JOIN "+ Viescolaire.ABSC_SCHEMA + "." + Viescolaire.ABSC_CATEGORIE_MOTIF_APPEL + " ")
                .append("ON (id_categorie = " + Viescolaire.ABSC_CATEGORIE_MOTIF_APPEL + ".id) ")
                .append("WHERE "+ Viescolaire.ABSC_CATEGORIE_MOTIF_APPEL+ ".id_etablissement = ?");

        values.addString(psIdEtablissement);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getCategorieAbscMotifsAppelEtbablissement(String psIdEtablissement, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT id, libelle, id_etablissement ")
                .append("FROM "+ Viescolaire.ABSC_SCHEMA +"." + Viescolaire.ABSC_CATEGORIE_MOTIF_APPEL + " ")
                .append("WHERE "+ Viescolaire.ABSC_SCHEMA +"."+ Viescolaire.ABSC_CATEGORIE_MOTIF_APPEL + ".id_etablissement = ?");

        values.addString(psIdEtablissement);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void createMotifAppel(JsonObject motif, final Handler<Either<String, JsonObject>> handler) {
        sql.insert(Viescolaire.ABSC_SCHEMA+ "." + Viescolaire.ABSC_MOTIF_APPEL_TABLE, motif,
                "id", validUniqueResultHandler(handler));
    }

    @Override
    public void updateMotifAppel (JsonObject motif, Handler<Either<String, JsonObject>> handler) {
        StringBuilder sb = new StringBuilder();
        JsonArray values = new JsonArray();

        for (String attr : motif.getFieldNames()) {
            if (! attr.contains("id")) {
                sb.append(attr).append(" = ?, ");
                values.add(motif.getValue(attr));
            }
        }
        sb.append("id_categorie").append(" = ? ");
        values.add(motif.getValue("id_categorie"));

        String query ="UPDATE " + Viescolaire.ABSC_SCHEMA+ "."+ Viescolaire.ABSC_MOTIF_APPEL_TABLE +
                " SET " + sb.toString() +
                " WHERE id = ? ";
        sql.prepared(query, values.add(motif.getValue("id")), validRowsResultHandler(handler));
    }

    @Override
    public void createCategorieMotifAppel(JsonObject categorie,  final Handler<Either<String, JsonObject>> handler) {
        sql.insert(Viescolaire.ABSC_SCHEMA+ "." +Viescolaire.ABSC_CATEGORIE_MOTIF_APPEL, categorie,
                "id", validUniqueResultHandler(handler));
    }

    @Override
    public void updateCategorieMotifAppel (JsonObject categorie, Handler<Either<String, JsonObject>> handler) {
        StringBuilder sb = new StringBuilder();
        JsonArray values = new JsonArray();

        sb.append("libelle").append(" = ?, ");
        values.add(categorie.getValue("libelle"));

        sb.append("id_etablissement").append(" = ? ");
        values.add(categorie.getValue("id_etablissement"));


        String query ="UPDATE " + Viescolaire.ABSC_SCHEMA+ "." + Viescolaire.ABSC_CATEGORIE_MOTIF_APPEL +
                " SET " + sb.toString() +
                " WHERE id = ? ";
        sql.prepared(query, values.add(categorie.getValue("id")), validRowsResultHandler(handler));
    }
}

