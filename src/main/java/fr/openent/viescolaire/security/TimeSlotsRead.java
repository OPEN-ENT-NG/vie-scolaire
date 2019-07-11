package fr.openent.viescolaire.security;

import fr.wseduc.webutils.http.Binding;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.user.UserInfos;

public class TimeSlotsRead implements ResourcesProvider {
    @Override
    public void authorize(HttpServerRequest resourceRequest, Binding binding, UserInfos user,
                          Handler<Boolean> handler) {
        handler.handle(WorkflowActionUtils.hasRight(user, WorkflowActionUtils.TIME_SLOTS_READ));
    }
}