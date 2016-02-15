package org.cgi.viescolaire;

import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import org.cgi.viescolaire.service.IVscoEleveService;
import org.cgi.viescolaire.service.impl.CVscoEleveService;
import org.entcore.common.controller.ControllerHelper;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;

import static org.entcore.common.http.response.DefaultResponseHandler.*;


/**
 * Created by ledunoiss on 10/02/2016.
 */
public class CVscoEleveController extends ControllerHelper {

    private final IVscoEleveService iVscoEleveService;
    public CVscoEleveController(){
        iVscoEleveService = new CVscoEleveService();
    }

    @Get("/:idClasse/eleves")
    @ApiDoc("Get all student in idClasse class")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getEleveClasse(final HttpServerRequest request){
        String idClasse = request.params().get("idClasse");
        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

        iVscoEleveService.getEleveClasse(idClasse, handler);
    }
}
