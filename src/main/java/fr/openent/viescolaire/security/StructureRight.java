package fr.openent.viescolaire.security;

import fr.wseduc.webutils.http.*;
import io.vertx.core.*;
import io.vertx.core.http.*;
import org.entcore.common.http.filter.*;
import org.entcore.common.user.*;

public class StructureRight implements ResourcesProvider {
    @Override
    public void authorize(HttpServerRequest request, Binding binding, UserInfos user, Handler<Boolean> handler) {
        String structureId = WorkflowActionUtils.getParamStructure(request);
        handler.handle(user.getStructures().contains(structureId));
    }
}
