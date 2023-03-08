package fr.openent.viescolaire.security;

import fr.wseduc.webutils.http.Binding;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.user.UserInfos;

public class AccessStructureAdminRightParamService implements ResourcesProvider {
    @Override
    public void authorize(final HttpServerRequest request, Binding binding, UserInfos user, final Handler<Boolean> handler) {
        boolean hasAdminRight = WorkflowActionUtils.hasRight(user, WorkflowActionUtils.ADMIN_RIGHT);
        boolean hasParamServiceRight = WorkflowActionUtils.hasRight(user, WorkflowActionUtils.PARAM_SERVICES_RIGHT);
        String structureId = WorkflowActionUtils.getParamStructure(request);

        handler.handle(hasAdminRight && hasParamServiceRight && user.getStructures().contains(structureId));
    }
}
