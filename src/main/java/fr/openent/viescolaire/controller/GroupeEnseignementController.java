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

package fr.openent.viescolaire.controller;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.core.constants.*;
import fr.openent.viescolaire.security.*;
import fr.openent.viescolaire.service.GroupeService;
import fr.openent.viescolaire.service.impl.DefaultGroupeService;
import fr.openent.viescolaire.service.ClasseService;
import fr.openent.viescolaire.service.impl.DefaultClasseService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.http.Renders;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.*;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.List;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.leftToResponse;

/**
 * Created by vogelmt on 13/02/2017.
 */
public class GroupeEnseignementController extends ControllerHelper {

    private final GroupeService groupeService;
    private final ClasseService classeService;
    public GroupeEnseignementController() {
        pathPrefix = Viescolaire.VSCO_PATHPREFIX;
        groupeService = new DefaultGroupeService();
        classeService = new DefaultClasseService();
    }


    /**
     * Liste les groupes d'enseignement d'un utilisateur
     * @param request
     */
    @Get("/groupe/enseignement/users/:groupId")
    @ApiDoc("Liste les groupes dse utilisateurs d'un groupe d'enseignement")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(StructureAdminPersonnalTeacherFromGroup.class)
    public void getGroupesEnseignementUsers(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                if (user != null) {
                    final String groupId = request.params().get("groupId");
                    if (groupId != null && !groupId.trim().isEmpty()) {
                        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);
                        final String profile = request.params().get("type");
                        groupeService.listUsersByGroupeEnseignementId(groupId, profile, null, handler);
                    }else{
                        log.error("Error getGroupesEnseignementUsers : groupId can't be null ");
                        badRequest(request);
                        return;
                    }
                } else {
                    unauthorized(request);
                }
            }
        });
    }


    /**
     * get the groups from a class
     */
    @Get("/group/from/class")
    @ApiDoc("get the groups of a class")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getGroupsFromClass(final HttpServerRequest request){

        List<String> classesId = request.params().getAll("classes");
        List<String> studentIds = request.params().getAll("student");
        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

        if (studentIds != null && !studentIds.isEmpty()) {
            classeService.getGroupFromStudents(studentIds.toArray(new String[0]), handler);
        } else {
            classeService.getGroupeFromClasse(classesId.toArray(new String[0]), handler);
        }
    }

    @Get("/group/search")
    @ApiDoc("Search group from name")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(SearchRight.class)
    public void searchGroups(HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, user -> {
            String userId = (WorkflowActionUtils.hasRight(user, WorkflowActionUtils.VIESCO_SEARCH_RESTRICTED)
            && Field.TEACHER.equals(user.getType())) ? user.getUserId() : null;
            if (request.params().contains(Field.Q)
                    && !"".equals(request.params().get(Field.Q).trim())
                    && request.params().contains(Field.FIELD)
                    && request.params().contains(Field.STRUCTUREID)) {

                String query = request.getParam(Field.Q);
                List<String> fields = request.params().getAll(Field.FIELD);
                String structureId = request.getParam(Field.STRUCTUREID);

                groupeService.search(structureId, userId, query, fields, arrayResponseHandler(request));
            } else {
                badRequest(request);
            }
        });
    }
}
