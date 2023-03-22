/*
 * Copyright (c) Région Hauts-de-France, Département de la Seine-et-Marne, Région Nouvelle Aquitaine, Mairie de Paris, CGI, 2016.
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
 */

package fr.openent.viescolaire.security;

import fr.openent.*;
import fr.openent.viescolaire.core.constants.Field;
import io.vertx.core.http.HttpServerRequest;
import org.entcore.common.user.UserInfos;

import java.util.Arrays;
import java.util.List;

public final class WorkflowActionUtils {

    public static final String ADMIN_RIGHT = "Viescolaire.view";
    public static final String PERIOD_YEAR_MANAGE = "period.year.manage";
    public static final String PERIOD_YEAR_READ = "period.year.read";
    public static final String TIME_SLOTS_MANAGE = "time.slot.manage";
    public static final String TIME_SLOTS_READ = "time.slot.read";
    public static final String PARAM_SERVICES_RIGHT = "viescolaire.paramServices";
    public static final String VIESCO_SEARCH = "viescolaire.search";
    public static final String VIESCO_SEARCH_RESTRICTED = Viescolaire.SEARCH_RESTRICTED;
    public static final String COMPETENCE_ACCESS = "competences.access";


    private WorkflowActionUtils() {
        throw new IllegalAccessError("Utility class");
    }

    public static boolean hasRight (UserInfos user, String action) {
        List<UserInfos.Action> actions = user.getAuthorizedActions();
        for (UserInfos.Action userAction : actions) {
            if (action.equals(userAction.getDisplayName())) {
                return true;
            }
        }
        return false;
    }

    public static String getParamStructure(HttpServerRequest request){
        List<String> structureIdFields = Arrays.asList(Field.ID_STRUCTURE, Field.IDETABLISSEMENT, Field.ID_ETABLISSEMENT, Field.IDSTRUCTURE, Field.STRUCTUREID, Field.STRUCTURE);
        return structureIdFields.stream()
                .map(structureIdField -> request.params().get(structureIdField))
                .filter(structureIdField ->  structureIdField != null && !structureIdField.isEmpty())
                .findFirst()
                .orElse(null);
    }

}
