package fr.openent.evaluations.service.impl;

import fr.wseduc.webutils.Either;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by vogelmt on 29/03/2017.
 */
public class DefaultBFCService  extends SqlCrudService implements fr.openent.evaluations.service.BFCService {
    public DefaultBFCService(String schema, String table) {
        super(schema, table);
    }

    /**
     * Créer un BFC pour un élève
     * @param bfc objet contenant les informations relative au BFC
     * @param user utilisateur
     * @param handler handler portant le résultat de la requête
     */
    public void createBFC(final JsonObject bfc, final UserInfos user, final Handler<Either<String, JsonObject>> handler){
        super.create(bfc, user, handler);
    }

    /**
     * Mise à jour d'un BFC pour un élève
     * @param data appreciation à mettre à jour
     * @param user utilisateur
     * @param handler handler portant le resultat de la requête
     */
    public void updateBFC(JsonObject data, UserInfos user, Handler<Either<String, JsonObject>> handler){
        super.update(data.getValue("id").toString(), data, user, handler);
    }

    /**
     * Suppression d'un BFC pour un élève
     * @param idBFC identifiant de la note
     * @param user user
     * @param handler handler portant le résultat de la requête
     */
    public void deleteBFC(Long idBFC, UserInfos user, Handler<Either<String, JsonObject>> handler){
        super.delete(idBFC.toString(), user, handler);
    }

    /**
     * Récupère les BFCs d'un élève pour chaque domaine
     * @param idEleve
     * @param idEtablissement
     * @param handler
     */
    @Override
    public void getBFCsByEleve(String idEleve, String idEtablissement, Handler<Either<String,JsonArray>> handler) {
        JsonArray values = new JsonArray();
        StringBuilder query = new StringBuilder()
                .append("SELECT * ")
                .append("FROM notes.bilan_fin_cycle ")
                .append("WHERE bilan_fin_cycle.id_eleve = ? ")
                    .append("AND bilan_fin_cycle.id_etablissement = ?");
        values.addString(idEleve);
        values.addString(idEtablissement);
        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }
}
