package fr.openent.viescolaire.security.memento;

import fr.openent.viescolaire.core.constants.Field;
import fr.wseduc.webutils.http.Binding;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.user.UserInfos;

public class AccessMemento implements ResourcesProvider {
    @Override
    public void authorize(HttpServerRequest request, Binding binding, UserInfos userInfos, Handler<Boolean> handler) {
        handler.handle((Field.TEACHER.equals(userInfos.getType()) || Field.PERSONNEL.equals(userInfos.getType())));
    }
}
