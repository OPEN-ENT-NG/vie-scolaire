package org.cgi;

import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

public class DisplayController extends ControllerHelper {

    public DisplayController(){
        super();
    }

    @Get("")
    @SecuredAction(value="viescolaire.view")
    public void view(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {

            @Override
            public void handle(UserInfos user) {
                if(user.getType().equals("Teacher")) {
                    renderView(request, null, "modules/absences/absc_vis_appel.html", null);
                }

                // TODO rediriger sur le bon fichier pour les CPE
            }
        });
    }
}