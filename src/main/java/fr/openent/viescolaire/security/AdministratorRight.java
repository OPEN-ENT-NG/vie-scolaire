package fr.openent.viescolaire.security;

import fr.wseduc.webutils.http.Binding;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.user.UserInfos;

/**
 * Concerne uniquement les droits pour viescolaire et competences
 */
public class AdministratorRight implements ResourcesProvider {
    @Override
    public void authorize(HttpServerRequest resourceRequest, Binding binding, UserInfos user, Handler<Boolean> handler) {
        String structureId = WorkflowActionUtils.getParamStructure(resourceRequest);
        boolean allowViesco = WorkflowActionUtils.hasRight(user, WorkflowActions.ADMIN_RIGHT.toString());
        boolean allowCompetences = WorkflowActionUtils.hasRight(user, WorkflowActions.COMPETENCES_ACCESS.toString());
        handler.handle( structureId != null && user.getStructures().contains(structureId) && allowViesco && allowCompetences);
    }
}
