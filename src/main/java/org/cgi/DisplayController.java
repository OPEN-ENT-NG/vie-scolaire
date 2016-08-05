package org.cgi;

import fr.wseduc.rs.ApiDoc;
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
    @ApiDoc("Get Vie Scolaire HTML view")
    @SecuredAction(value="Viescolaire.view")
    public void view(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {

            @Override
            public void handle(UserInfos user) {
                if(user.getType().equals("Teacher")) {
                    renderView(request, null, "viescolaire/vsco_teacher.html", null);
                }else if(user.getType().equals("Personnel")){
                    renderView(request, null, "viescolaire/vsco_personnel.html", null);
                }
            }
        });
    }

    @Get("/absences")
    @ApiDoc("Get Absences HTML view")
    @SecuredAction(value="Viescolaire.absences.view")
    public void viewAbsences(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                if(user.getType().equals("Teacher")) {
                    renderView(request, null, "absences/absc_teacher.html", null);
                }else if(user.getType().equals("Personnel")){
                    renderView(request, null, "absences/absc_personnel.html", null);
                }
            }
        });
    }

    @Get("/evaluations")
    @ApiDoc("Get Evaluation HTML view")
    @SecuredAction(value = "Viescolaire.evaluations.view")
    public void viewEvaluations(final HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {

            @Override
            public void handle(UserInfos user) {
                if(user.getType().equals("Teacher")) {
                    renderView(request);
                }else if(user.getType().equals("Student") || user.getType().equals("Relative")){
                    renderView(request, null, "notes-parents.html", null);
                }
            }
        });
    }
}