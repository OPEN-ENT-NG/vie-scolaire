package fr.openent.viescolaire.security;

import fr.wseduc.webutils.http.Binding;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.user.UserInfos;

public class StructureAdminPersonnalTeacher implements ResourcesProvider {
    @Override
    public void authorize(final HttpServerRequest request, Binding binding, final UserInfos user, final Handler<Boolean> handler) {
        String structureId = WorkflowActionUtils.getParamStructure(request);
        handler.handle(user.getStructures().contains(structureId) && WorkflowActionUtils.hasRight(user, WorkflowActionUtils.ADMIN_RIGHT) ||
                "Personnel".equals(user.getType()) || "Teacher".equals(user.getType()));
    }
}
