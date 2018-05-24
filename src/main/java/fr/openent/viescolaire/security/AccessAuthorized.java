package fr.openent.viescolaire.security;

import fr.wseduc.webutils.http.Binding;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.user.UserInfos;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;

public class AccessAuthorized implements ResourcesProvider {

    @Override
    public void authorize(final HttpServerRequest resourceRequest, Binding binding, final UserInfos user, final Handler<Boolean> handler) {
        switch (user.getType()) {
            case "Personnel" : {
                handler.handle(true);
                //FIXME ???
                /* FilterUserUtils userUtils = new FilterUserUtils(user);
                new Handler<Boolean>() {
                    @Override
                    public void handle(Boolean isValid) {
                        resourceRequest.resume();
                        handler.handle(isValid);
                    }
                };*/
            }
            break;
            default : {
                handler.handle(false);
            }
        }
    }
}