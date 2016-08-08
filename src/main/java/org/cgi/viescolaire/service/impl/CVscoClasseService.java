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

package org.cgi.viescolaire.service.impl;

import fr.wseduc.webutils.Either;
import org.cgi.Viescolaire;
import org.cgi.viescolaire.service.IVscoClasseService;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.vertx.java.core.Handler;
import org.entcore.common.sql.SqlResult;
import org.vertx.java.core.json.JsonArray;

/**
 * Created by ledunoiss on 19/02/2016.
 */
public class CVscoClasseService extends SqlCrudService implements IVscoClasseService {
    public CVscoClasseService() {
        super(Viescolaire.VSCO_SCHEMA, Viescolaire.VSCO_CLASSE_TABLE);
    }

    @Override
    public void getClasseEtablissement(String idEtablissement, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray params = new JsonArray();

        query.append("SELECT classe.classe_id, classe.fk4j_classe_id, classe.classe_libelle ")
                .append("FROM viesco.classe ")
                .append("WHERE classe.fk4j_etab_id = ?::uuid");

        params.addString(idEtablissement);

        Sql.getInstance().prepared(query.toString(), params, SqlResult.validResultHandler(handler));
    }
}
