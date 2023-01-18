package fr.openent.viescolaire.security;

import fr.openent.Viescolaire;
import fr.openent.viescolaire.core.constants.Field;
import fr.wseduc.webutils.http.Binding;
import fr.wseduc.webutils.request.RequestUtils;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.user.UserInfos;

public class AccesCompetencesVieScoStructureFilter implements ResourcesProvider {
    @Override
    public void authorize(HttpServerRequest request, Binding binding, UserInfos user, Handler<Boolean> handler) {
        if ("POST".equals(request.method().toString())) {
            RequestUtils.bodyToJson(request, params -> {
                boolean isInStructure = false;

                if (params.containsKey(Field.STRUCTUREID)) {
                    isInStructure = validateStructure(user, params.getString(Field.STRUCTUREID));
                }

                handler.handle(
                        WorkflowActionUtils.hasRight(user, WorkflowActionUtils.ADMIN_RIGHT)
                                && WorkflowActionUtils.hasRight(user, WorkflowActionUtils.COMPETENCE_ACCESS)
                                && isInStructure
                );
            });
        } else {
            boolean isInStructure = false;

            if (request.params().contains(Field.STRUCTUREID)) {
                isInStructure = validateStructure(user, request.params().get(Field.STRUCTUREID));
            }

            handler.handle(
                    WorkflowActionUtils.hasRight(user, WorkflowActionUtils.ADMIN_RIGHT)
                            && WorkflowActionUtils.hasRight(user, WorkflowActionUtils.COMPETENCE_ACCESS)
                            && isInStructure
            );
        }
    }

    public boolean validateStructure(UserInfos user, String idEtablissement) {
        return user.getStructures().contains(idEtablissement);
    }
}
