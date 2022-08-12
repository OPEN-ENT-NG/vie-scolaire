package fr.openent.viescolaire.security;

import fr.openent.viescolaire.core.constants.Field;
import fr.wseduc.webutils.http.Binding;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.user.UserInfos;

public class StructureManage implements ResourcesProvider {
    @Override
    public void authorize(final HttpServerRequest resourceRequest, Binding binding, final UserInfos user, final Handler<Boolean> handler) {
        handler.handle(WorkflowActionUtils.hasRight(user, WorkflowActionUtils.ADMIN_RIGHT)
                && user.getStructures().contains(resourceRequest.getParam(Field.ID)));
    }
}
