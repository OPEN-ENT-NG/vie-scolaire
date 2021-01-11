package fr.openent.viescolaire.security.Trombinoscope;

import fr.openent.viescolaire.security.WorkflowActionUtils;
import fr.openent.viescolaire.security.WorkflowActions;
import fr.wseduc.webutils.http.Binding;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.user.UserInfos;

public class ManageTrombinoscope implements ResourcesProvider {
    @Override
    public void authorize(HttpServerRequest request, Binding binding, UserInfos user, Handler<Boolean> handler) {
        handler.handle(WorkflowActionUtils.hasRight(user, WorkflowActions.MANAGE_TROMBINOSCOPE.toString()));
    }
}
