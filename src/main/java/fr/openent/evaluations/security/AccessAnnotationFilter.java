package fr.openent.evaluations.security;

import fr.openent.evaluations.security.utils.FilterAppreciationUtils;
import fr.openent.evaluations.security.utils.FilterDevoirUtils;
import fr.wseduc.webutils.http.Binding;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.user.UserInfos;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

/**
 * Created by anabah on 02/03/2017.
 */
public class AccessAnnotationFilter implements ResourcesProvider {

    protected static final Logger log = LoggerFactory.getLogger(AccessAnnotationFilter.class);

    @Override
    public void authorize(final HttpServerRequest resourceRequest, Binding binding, UserInfos user, final Handler<Boolean> handler) {
        switch (user.getType()) {
            case "Teacher": {
                resourceRequest.pause();

                Long idDevoir;
                try {
                    idDevoir = Long.valueOf(resourceRequest.params().get("idDevoir"));
                } catch (NumberFormatException e) {
                    log.error("Error : idAppreciation must be a long object", e);
                    handler.handle(false);
                    return;
                }
                new FilterDevoirUtils().validateAccessDevoir(idDevoir, user,false, new Handler<Boolean>() {
                    @Override
                    public void handle(Boolean isValid) {
                        resourceRequest.resume();
                        handler.handle(isValid);
                    }
                });
            }
            break;
            case "Personnel" : {
                resourceRequest.pause();
                if(user.getFunctions().containsKey("DIR")){
                    resourceRequest.pause();
                    if (!resourceRequest.params().contains("idDevoir")) {
                        handler.handle(false);
                    }
                    resourceRequest.resume();
                    handler.handle(true);
                }else{
                    handler.handle(false);
                }
            }
            break;
            default: {
                handler.handle(false);
            }
        }
    }
}