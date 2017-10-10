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
import fr.openent.evaluations.service.NiveauDeMaitriseService;
import fr.wseduc.webutils.Either;
import io.netty.util.Recycler;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.sql.SqlStatementsBuilder;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.entcore.common.sql.Sql.parseId;
import static org.entcore.common.sql.SqlResult.validResultHandler;
import static org.entcore.common.sql.SqlResult.validRowsResultHandler;
import static org.entcore.common.sql.SqlResult.validUniqueResultHandler;

/**
 * Created by anabah on 30/08/2017.
 */
public class DefaultNiveauDeMaitriseService extends SqlCrudService implements NiveauDeMaitriseService {


    public DefaultNiveauDeMaitriseService() {
        super(Viescolaire.EVAL_SCHEMA,Viescolaire.EVAL_PERSO_NIVEAU_COMPETENCES_TABLE);
    }

     /**
     * Recupère l'ensemble des couleurs des niveaux de maitrise pour un établissement.
     * @param idEtablissement identifiant de l'établissement
     * @param handler handler portant le resultat de la requête 
     */
    public void getNiveauDeMaitrise(String idEtablissement, Handler<Either<String, JsonArray>> handler){
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT "+Viescolaire.EVAL_NIVEAU_COMPETENCES_TABLE +".libelle, ordre, ")
                .append( Viescolaire.EVAL_NIVEAU_COMPETENCES_TABLE +".couleur AS default, id_cycle, ")
                .append( Viescolaire.EVAL_NIVEAU_COMPETENCES_TABLE +".id AS id_niveau, ")
                .append(" niv.id_etablissement, niv.couleur, niv.lettre, niv.id As id, ")
                .append(Viescolaire.EVAL_CYCLE_TABLE+".libelle  AS cycle")
                .append(" FROM "+ Viescolaire.EVAL_SCHEMA +"." + Viescolaire.EVAL_NIVEAU_COMPETENCES_TABLE)
                .append(" INNER JOIN " + Viescolaire.EVAL_SCHEMA +"." + Viescolaire.EVAL_CYCLE_TABLE )
                .append(" ON id_cycle = " + Viescolaire.EVAL_CYCLE_TABLE+ ".id ")
                .append(" LEFT JOIN ")
                .append(" (SELECT * FROM "+ Viescolaire.EVAL_SCHEMA + "." + Viescolaire.EVAL_PERSO_NIVEAU_COMPETENCES_TABLE)
                .append(" WHERE id_etablissement = ? ) AS niv")
                .append(" ON (niv.id_niveau = " + Viescolaire.EVAL_NIVEAU_COMPETENCES_TABLE +".id) " );


        values.addString(idEtablissement);

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    public void getPersoNiveauMaitrise(String idUser,Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT * FROM " + Viescolaire.EVAL_SCHEMA+"."+Viescolaire.EVAL_USE_PERSO_NIVEAU_COMPETENCES_TABLE)
                .append(" WHERE id_user = ? ");
        values.add(idUser);
        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    public void markUsePerso(final JsonObject idUser, final Handler<Either<String, JsonArray>> handler) {
        final String queryNewUserId =
                "SELECT nextval('" + Viescolaire.EVAL_SCHEMA + "."+ Viescolaire.EVAL_USE_PERSO_NIVEAU_COMPETENCES_TABLE
                        +"_id_seq') as id";

        sql.raw(queryNewUserId, SqlResult.validUniqueResultHandler(new Handler<Either<String, JsonObject>>() {
            @Override
            public void handle(Either<String, JsonObject> event) {

                if (event.isRight()) {
                    final Long userId = event.right().getValue().getLong("id");
                    final String table = Viescolaire.EVAL_SCHEMA + "."+
                            Viescolaire.EVAL_USE_PERSO_NIVEAU_COMPETENCES_TABLE;
                    doCreate(handler,userId,idUser, table);
                }
                else {
                    handler.handle(new Either.Left<String, JsonArray>(event.left().getValue()));
                }
            }
        }));
    }
    public void createMaitrise(final JsonObject maitrise, UserInfos user, final Handler<Either<String, JsonArray>> handler) {

        final String queryNewNivCompetenceId =
                "SELECT nextval('" + Viescolaire.EVAL_SCHEMA + ".perso_niveau_competences_id_seq') as id";

        sql.raw(queryNewNivCompetenceId, SqlResult.validUniqueResultHandler(new Handler<Either<String, JsonObject>>() {
            @Override
            public void handle(Either<String, JsonObject> event) {

                if (event.isRight()) {
                    final Long niveauCompetenceId = event.right().getValue().getLong("id");
                    maitrise.putNumber("id", niveauCompetenceId);
                   doCreate(handler,niveauCompetenceId,maitrise,resourceTable);
                }
                else {
                    handler.handle(new Either.Left<String, JsonArray>(event.left().getValue()));
                }
            }
        }));
    }

    public void doCreate ( final Handler<Either<String, JsonArray>> handler,
                           final Long returning, final JsonObject o, String table) {
        SqlStatementsBuilder s = new SqlStatementsBuilder();
        s.insert(table, o, "id");
        Sql.getInstance().transaction(s.build(), new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> event) {
                JsonObject result = event.body();
                if (result.containsField("status") && "ok".equals(result.getString("status"))) {
                    handler.handle(new Either.Right<String, JsonArray>(new JsonArray()
                            .add(new JsonObject().putNumber("id", returning))));
                } else {
                    handler.handle(new Either.Left<String, JsonArray>(result.getString("status")));
                }
            }
        });
    }

    @Override
    public void update(JsonObject data, UserInfos user, Handler<Either<String, JsonObject>> handler) {
        super.update(data.getValue("id").toString(), data, user, handler);
    }

    @Override
    public void delete(String idEtablissement, UserInfos user, Handler<Either<String, JsonObject>> handler) {
        String query = "DELETE FROM " + resourceTable + " WHERE id_etablissement = ?";
        sql.prepared(query, new JsonArray().add(idEtablissement), validRowsResultHandler(handler));
    }

    public void deleteUserFromPerso(String idUser,Handler<Either<String, JsonObject>> handler ) {
        final String table = Viescolaire.EVAL_SCHEMA + "."+
                Viescolaire.EVAL_USE_PERSO_NIVEAU_COMPETENCES_TABLE;

        String query = "DELETE FROM " + table + " WHERE id_user = ?";
        sql.prepared(query, new JsonArray().add(idUser), validRowsResultHandler(handler));
    }
}