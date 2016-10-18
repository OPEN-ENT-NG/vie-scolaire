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

package fr.openent.absences.service.impl;

import fr.openent.Viescolaire;
import fr.wseduc.webutils.Either;
import fr.openent.absences.service.AppelService;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static org.entcore.common.sql.SqlResult.validUniqueResultHandler;

/**
 * Created by ledunoiss on 22/02/2016.
 */
public class DefaultAppelService extends SqlCrudService implements fr.openent.absences.service.AppelService {
    public DefaultAppelService() {
        super(Viescolaire.ABSC_SCHEMA, Viescolaire.ABSC_APPEL_TABLE);
    }

    @Override
    public void getAppelPeriode(String psIdEtablissement, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray value = new JsonArray();

        query.append("SELECT DISTINCT cours.id, cours.timestamp_dt, cours.timestamp_fn, cours.matiere, cours.salle, appel.id, personnel.prenom, personnel.nom, appel.fk_etat_appel_id, classe.libelle, classe.id, personnel.id " +
                "FROM viesco.personnel, viesco.classe, viesco.rel_personnel_cours, viesco.cours " +
                "LEFT OUTER JOIN abs.appel on (cours.id = appel.fk_cours_id) " +
                "WHERE cours.id_etablissement = ?::uuid " +
                "AND cours.timestamp_dt > to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') " +
                "AND cours.timestamp_fn <= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') " +
                "AND rel_personnel_cours.fk_cours_id = cours.id " +
                "AND rel_personnel_cours.fk_personnel_id = personnel.id " +
                "AND cours.fk_classe_id = classe.id "+
                "ORDER BY cours.timestamp_dt DESC");

        value.addString(psIdEtablissement).addString(psDateDebut).addString(psDateFin);

        Sql.getInstance().prepared(query.toString(), value, SqlResult.validResultHandler(handler));
    }

    @Override
    public void getAppelsNonEffectues(String psIdEtablissement, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT DISTINCT cours.id, cours.timestamp_dt, cours.timestamp_fn, cours.matiere, cours.salle, appel.id, personnel.prenom, personnel.nom, appel.fk_etat_appel_id, classe.libelle, classe.id, personnel.id " +
                "FROM viesco.personnel, viesco.classe, viesco.rel_personnel_cours, viesco.cours " +
                "LEFT OUTER JOIN abs.appel on (cours.id = appel.fk_cours_id) " +
                "WHERE cours.id_etablissement = ?::uuid " +
                "AND cours.timestamp_dt > to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') " +
                "AND cours.timestamp_fn <= to_timestamp(?, 'YYYY-MM-DD HH24:MI:SS') " +
                "AND rel_personnel_cours.fk_cours_id = cours.id " +
                "AND rel_personnel_cours.fk_personnel_id = personnel.id " +
                "AND cours.fk_classe_id = classe.id " +
                "AND (appel.fk_etat_appel_id != 3 OR appel.fk_etat_appel_id IS NULL) " +
                "ORDER BY cours.timestamp_dt DESC");

        values.addString(psIdEtablissement).addString(psDateDebut).addString(psDateFin);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }

    @Override
    public void createAppel(JsonObject data, UserInfos user, Handler<Either<String, JsonObject>> handler) {
        super.create(data, user, handler);
    }

    @Override
    public void updateAppel(JsonObject data, Handler<Either<String, JsonObject>> handler) {
        super.update(data.getInteger("id").toString(), data, handler);
    }

    @Override
    public void getAppelCours(Integer poCoursId, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT appel.id, appel.fk_personnel_id, appel.fk_cours_id, appel.fk_etat_appel_id, appel.fk_justificatif_appel_id " +
                "FROM abs.appel ")
                .append("WHERE appel.fk_cours_id = ?");

        values.addNumber(poCoursId);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }


}
