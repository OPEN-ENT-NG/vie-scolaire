package fr.openent.viescolaire.security;

import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.service.ClasseService;
import fr.openent.viescolaire.service.impl.DefaultClasseService;
import fr.wseduc.webutils.http.Binding;
import fr.wseduc.webutils.http.Renders;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.user.UserInfos;

public class AccessIfMyStructureFromAudience implements ResourcesProvider {
    private final ClasseService service;
    public static final Logger log = LoggerFactory.getLogger(Renders.class);
    public AccessIfMyStructureFromAudience(){
        this.service = new DefaultClasseService();
    }


    @Override
    public void authorize(HttpServerRequest request, Binding binding, UserInfos user, Handler<Boolean> handler) {
        String audienceId = request.params().get(Field.AUDIENCEID);
        if (audienceId == null) {
            handler.handle(false);
            return;
        }
        this.service.getClasseIdFromAudience(audienceId)
                .compose(classeId -> {
                    if(classeId.isEmpty()){
                        handler.handle(false);
                        return Future.failedFuture("");
                    }
                   return this.service.getEtabClasses(classeId);
                })
                .onSuccess(etabInfos -> {
                    if (etabInfos.isEmpty() || etabInfos.getJsonObject(0).isEmpty()) {
                        handler.handle(false);
                        return;
                    }
                    String structureId = etabInfos.getJsonObject(0).getString(Field.IDSTRUCTURE);
                    handler.handle(user.getStructures().contains(structureId));
                })
                .onFailure(err -> {
                    log.error("[Viescolaire@AccessIfMyStructureFromAudience::authorize] Failed to retrieve structure from audience", err.getMessage());
                    handler.handle(false);
                });

    }
}
