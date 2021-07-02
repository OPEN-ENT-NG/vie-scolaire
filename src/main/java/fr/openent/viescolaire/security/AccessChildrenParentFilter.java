package fr.openent.viescolaire.security;

import fr.wseduc.webutils.http.Binding;
import fr.wseduc.webutils.request.RequestUtils;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.user.UserInfos;

public class AccessChildrenParentFilter implements ResourcesProvider {
    private static final String ID_ELEVE_KEY = "idEleve";

    @Override
    public void authorize(final HttpServerRequest request, Binding binding, UserInfos user, Handler<Boolean> handler) {
        boolean isAdminTeacherPersonnel = WorkflowActionUtils.hasRight(user, WorkflowActions.ADMIN_RIGHT.toString())
                || "Personnel".equals(user.getType()) || "Teacher".equals(user.getType());

        if ("GET".equals(request.method().toString())) {
            handler.handle(isAdminTeacherPersonnel
                    || (user.getUserId().equals(request.params().get(ID_ELEVE_KEY))
                    || user.getChildrenIds().contains(request.params().get(ID_ELEVE_KEY)))
            );
        } else {
            RequestUtils.bodyToJson(request, params -> {
                handler.handle(isAdminTeacherPersonnel
                        || (user.getUserId().equals(params.getString(ID_ELEVE_KEY))
                        || user.getChildrenIds().contains(params.getString(ID_ELEVE_KEY)))
                );
            });
        }
    }
}
