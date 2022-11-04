package fr.openent.viescolaire.security.Trombinoscope;

import fr.openent.viescolaire.core.constants.Field;
import fr.wseduc.webutils.http.Binding;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import org.entcore.common.http.filter.ResourcesProvider;
import org.entcore.common.user.UserInfos;

public class AccessTrombinoscope implements ResourcesProvider {
    @Override
    public void authorize(HttpServerRequest request, Binding binding, UserInfos userInfo, Handler<Boolean> handler) {
        String structureId = request.getParam(Field.STRUCTUREID);
        String studentId = request.getParam(Field.STUDENTID);
        boolean result = true;

        if (userInfo.getType().equals("Student")) {
            result = userInfo.getUserId().equals(studentId);
        }
        if (userInfo.getType().equals("Relative")) {
            result = userInfo.getChildrenIds().contains(studentId);
        }
        handler.handle(userInfo.getStructures().contains(structureId) && result);
    }
}