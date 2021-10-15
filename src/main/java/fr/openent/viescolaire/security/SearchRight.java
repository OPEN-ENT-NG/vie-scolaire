package fr.openent.viescolaire.security;

import fr.wseduc.webutils.http.Binding;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.user.UserInfos;

import java.util.*;

public class SearchRight implements ResourcesProvider {
    @Override
    public void authorize(HttpServerRequest httpServerRequest, Binding binding, UserInfos user, Handler<Boolean> handler) {
        String structureId = httpServerRequest.params().get("structureId");
        List<String> structures = user.getStructures();
        handler.handle(structures.contains(structureId)
                && WorkflowActionUtils.hasRight(user, WorkflowActionUtils.VIESCO_SEARCH));
    }
}