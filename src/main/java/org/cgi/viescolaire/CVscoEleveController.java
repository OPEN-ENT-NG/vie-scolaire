package org.cgi.viescolaire;

import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import org.cgi.viescolaire.service.IVscoEleveService;
import org.cgi.viescolaire.service.impl.CVscoEleveService;
import org.entcore.common.controller.ControllerHelper;
import org.vertx.java.core.http.HttpServerRequest;

/**
 * Created by ledunoiss on 10/02/2016.
 */
public class CVscoEleveController extends ControllerHelper {

    private final IVscoEleveService iVscoEleveService;
    public CVscoEleveController(){
        iVscoEleveService = new CVscoEleveService();
    }

    @Get("")
    @ApiDoc("Get all student in idClasse class")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getEleveClasse(final HttpServerRequest request){

    }
}
