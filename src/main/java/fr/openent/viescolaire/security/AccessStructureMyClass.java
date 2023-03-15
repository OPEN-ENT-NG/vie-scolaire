package fr.openent.viescolaire.security;

import fr.openent.viescolaire.core.constants.Field;
import fr.openent.viescolaire.service.ClasseService;
import fr.openent.viescolaire.service.impl.DefaultClasseService;
import fr.wseduc.webutils.http.Binding;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.user.UserInfos;

public class AccessStructureMyClass implements ResourcesProvider {
    public static final Logger log = LoggerFactory.getLogger(AccessStructureMyClass.class);
    private  final ClasseService service;
    public AccessStructureMyClass(){
        this.service = new DefaultClasseService();
    }

    @Override
    public void authorize(HttpServerRequest request, Binding binding, UserInfos user, Handler<Boolean> handler) {
        String classeId = request.getParam(Field.IDCLASSE);
        this.service.getEtabClasses(classeId)
                .onSuccess(etabInfos -> {
                    if (etabInfos.isEmpty() || etabInfos.getJsonObject(0).isEmpty()) {
                        handler.handle(false);
                        return;
                    }
                    String structureId = etabInfos.getJsonObject(0).getString(Field.IDSTRUCTURE);
                    handler.handle(user.getStructures().contains(structureId));
                })
                .onFailure(err -> {
                    log.error(String.format("[Viescolaire@%s] Failed to retrieve structure from classe %s", this.getClass().getSimpleName(), err.getMessage()));
                    handler.handle(false);
                });
    }
}