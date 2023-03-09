package fr.openent.viescolaire.security;

import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.service.ClasseService;
import fr.openent.viescolaire.service.impl.DefaultClasseService;
import fr.wseduc.webutils.http.Binding;
import fr.wseduc.webutils.http.Renders;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.user.UserInfos;

public class AccessIfMyStructureFromAudience implements ResourcesProvider {
    @Override
    public void authorize(HttpServerRequest request, Binding binding, UserInfos user, Handler<Boolean> handler) {
        final Logger log = LoggerFactory.getLogger(Renders.class);
        String audienceId = request.params().get(Field.AUDIENCEID);
        ClasseService service = new DefaultClasseService();
        if (audienceId == null) {
            handler.handle(false);
            return;
        }
        service.getClasseIdFromAudience(audienceId)
                .compose(service::getEtabClasses)
                .onSuccess(etabInfos -> {
                    if (etabInfos.isEmpty() || etabInfos.getJsonObject(0).isEmpty()) {
                        handler.handle(false);
                        return;
                    }
                    String structureId = etabInfos.getJsonObject(0).getString(Field.IDSTRUCTURE);
                    handler.handle(user.getStructures().contains(structureId));
                })
                .onFailure(err -> {
                    log.error("[Viescolaire@TimeSlotController] Failed to retrieve structure from audience", err.getMessage());
                    handler.handle(false);
                });

    }
}
