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
import fr.openent.evaluations.service.DomainesService;
import fr.wseduc.webutils.Either;
import org.entcore.common.service.impl.SqlCrudService;
import org.entcore.common.sql.Sql;
import org.entcore.common.sql.SqlResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonArray;

public class DefaultDomaineService extends SqlCrudService implements DomainesService {
    public DefaultDomaineService(String schema, String table) {
        super(schema, table);
    }

    @Override
    public void getArbreDomaines(Long poIdCycle, Handler<Either<String, JsonArray>> handler) {
        StringBuilder query = new StringBuilder();
        JsonArray params = new JsonArray();


        query.append("WITH RECURSIVE search_graph(niveau, id, id_parent, libelle, codification, evaluated, pathinfo) AS ");
            query.append("( ");
                query.append("SELECT 1 as niveau, id, id_parent, libelle, codification, evaluated, array[id] as pathinfo ");
                query.append("FROM "+ Viescolaire.EVAL_SCHEMA +".domaines ");
                query.append("WHERE id_parent = 0 ");
                query.append("AND id_cycle = ? ");
            query.append("UNION ");
                query.append("SELECT sg.niveau + 1  as niveau , dom.id, dom.id_parent, dom.libelle, dom.codification, dom.evaluated, sg.pathinfo||dom.id ");
                query.append("FROM "+ Viescolaire.EVAL_SCHEMA +".domaines dom , search_graph sg ");
                query.append("WHERE dom.id_parent = sg.id ");
            query.append(") ");
        query.append("SELECT niveau, id, id_parent, libelle, codification, evaluated FROM search_graph ORDER BY pathinfo");

        params.addNumber(poIdCycle);
        Sql.getInstance().prepared(query.toString(), params , SqlResult.validResultHandler(handler));
    }

}
