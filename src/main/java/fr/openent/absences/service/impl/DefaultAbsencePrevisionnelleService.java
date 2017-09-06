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
import fr.openent.absences.service.AbsencePrevisionnelleService;
import fr.wseduc.webutils.Either;
import org.entcore.common.service.impl.SqlCrudService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import static org.entcore.common.sql.SqlResult.validUniqueResultHandler;


/**
 * Created by rahnir on 20/06/2017.
 */
public class DefaultAbsencePrevisionnelleService extends SqlCrudService implements AbsencePrevisionnelleService {
    public DefaultAbsencePrevisionnelleService() {
        super(Viescolaire.ABSC_SCHEMA, Viescolaire.ABSC_APPEL_TABLE);
    }

    @Override
    public void createAbsencePrev(JsonObject poAbscencePrev,final Handler<Either<String, JsonObject>> handler){
        sql.insert(Viescolaire.ABSC_SCHEMA+ ".absence_prev", poAbscencePrev, "id", validUniqueResultHandler(handler));
    }
}