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

package fr.openent.viescolaire.service.impl;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.service.EleveService;
import fr.wseduc.webutils.Either;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;

import static org.entcore.common.sql.SqlResult.validResultHandler;

/**
 * Created by ledunoiss on 10/02/2016.
 */
public class DefaultEleveService extends SqlCrudService implements EleveService {
    public DefaultEleveService() {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.VSCO_ELEVE_TABLE);
    }

    @Override
    public void getEleveClasse(String pSIdClasse, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT eleve.* ")
        .append("FROM viesco.eleve, viesco.rel_eleve_classe, viesco.classe ")
        .append("WHERE classe.id = ? ")
        .append("AND classe.id = rel_eleve_classe.fk_classe_id ")
        .append("AND eleve.id = rel_eleve_classe.fk_eleve_id ")
        .append("ORDER BY nom ASC, prenom ASC");

        values.addNumber(new Integer(pSIdClasse));

        Sql.getInstance().prepared(query.toString(), values, validResultHandler(handler));
    }

    @Override
    public void getEvenements(String psIdEleve, String psDateDebut, String psDateFin, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray values = new JsonArray();

        query.append("SELECT abs.evenement.* ")
                .append("FROM abs.evenement, viesco.eleve, viesco.cours, abs.appel ")
                .append("WHERE eleve.id = ? ")
                .append("AND eleve.id = evenement.fk_eleve_id ")
                .append("AND evenement.fk_appel_id = appel.id ")
                .append("AND appel.fk_cours_id = cours.id ")
                .append("AND to_date(?, 'DD-MM-YYYY') < cours.timestamp_dt ")
                .append("AND cours.timestamp_fn < to_date(?, 'DD-MM-YYYY')");

        values.addNumber(new Integer(psIdEleve));
        values.addString(psDateDebut);
        values.addString(psDateFin);

        Sql.getInstance().prepared(query.toString(), values, SqlResult.validResultHandler(handler));
    }
}
