package fr.openent.viescolaire.security;

import fr.openent.Viescolaire;
import fr.wseduc.webutils.http.Binding;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.user.UserInfos;

public class ParamServicesRight implements ResourcesProvider {

    protected static final Logger log = LoggerFactory.getLogger(ParamServicesRight.class);

    @Override
    public void authorize(final HttpServerRequest resourceRequest, Binding binding, UserInfos user, final Handler<Boolean> handler) {
        handler.handle(WorkflowActionUtils.hasRight(user, WorkflowActionUtils.PARAM_SERVICES_RIGHT));
    }
}

