package fr.openent.evaluations.security.utils;

import fr.openent.Viescolaire;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by anabah on 02/03/2017.
 */
public class FilterAppreciationUtils {
        public void validateAppreciationOwner (Long idAppreciation, String owner, final Handler<Boolean> handler) {
            StringBuilder query = new StringBuilder()
                    .append("SELECT count(devoirs.*) " +
                            "FROM notes.devoirs INNER JOIN notes.appreciations ON (appreciations.id_devoir = devoirs.id) " +
                            "WHERE appreciations.id = ? " +
                            "AND devoirs.owner = ?;");

            JsonArray params = new JsonArray().addNumber(idAppreciation).addString(owner);

            Sql.getInstance().prepared(query.toString(), params, new Handler<Message<JsonObject>>() {
                @Override
                public void handle(Message<JsonObject> sqlResult) {
                    Long count = SqlResult.countResult(sqlResult);
                    handler.handle(count != null && count > 0);
                }
            });
        }



        public void validateAccessAppreciation (Long idNote, UserInfos user, final Handler<Boolean> handler) {
            JsonArray params = new JsonArray();

            StringBuilder query = new StringBuilder()
                    .append("SELECT count(*) FROM " + Viescolaire.EVAL_SCHEMA + ".devoirs ")
                    .append("INNER JOIN " + Viescolaire.EVAL_SCHEMA + ".appreciations ON " +
                            "(appreciations.id_devoir = devoirs.id) ")
                    .append("WHERE appreciations.id = ? ")
                    .append("AND (devoirs.owner = ? OR ")
                    .append("devoirs.owner IN (SELECT DISTINCT id_titulaire ")
                    .append("FROM " + Viescolaire.EVAL_SCHEMA + ".rel_professeurs_remplacants ")
                    .append("INNER JOIN " + Viescolaire.EVAL_SCHEMA + ".devoirs ON devoirs.id_etablissement = rel_professeurs_remplacants.id_etablissement  ")
                    .append("INNER JOIN " + Viescolaire.EVAL_SCHEMA + ".appreciations ON (appreciations.id_devoir = devoirs.id) ")
                    .append("WHERE appreciations.id = ? ")
                    .append("AND id_remplacant = ? ")
                    .append(") OR ")

                    .append("? IN (SELECT member_id ")
                    .append("FROM " + Viescolaire.EVAL_SCHEMA + ".devoirs_shares ")
                    .append("WHERE resource_id = devoirs.id ")
                    .append("AND action = '" + Viescolaire.DEVOIR_ACTION_UPDATE+"')")

                    .append(")");

            // Ajout des params pour la partie de la requête où on vérifie si on est le propriétaire
            params.addNumber(idNote);
            params.addString(user.getUserId());

            // Ajout des params pour la partie de la requête où on vérifie si on a des titulaires propriétaire
            params.addNumber(idNote);
            params.addString(user.getUserId());

            // Ajout des params pour la partie de la requête où on vérifie si on a des droits de partage provenant d'un remplaçant
            params.addString(user.getUserId());


            Sql.getInstance().prepared(query.toString(), params, new Handler<Message<JsonObject>>() {
                @Override
                public void handle(Message<JsonObject> sqlResult) {
                    Long count = SqlResult.countResult(sqlResult);
                    handler.handle(count != null && count > 0);
                }
            });
        }

}
