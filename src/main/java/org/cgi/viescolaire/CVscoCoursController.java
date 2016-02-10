package org.cgi.viescolaire;

import com.sun.xml.internal.bind.v2.TODO;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import org.cgi.viescolaire.service.IVscoCoursService;
import org.cgi.viescolaire.service.impl.CVscoCoursService;
import org.entcore.common.controller.ControllerHelper;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;

import static org.entcore.common.http.response.DefaultResponseHandler.*;

/**
 * Created by ledunoiss on 10/02/2016.
 */
public class CVscoCoursController extends ControllerHelper{
    private final IVscoCoursService iVscoCoursService;
    public CVscoCoursController(){
        iVscoCoursService = new CVscoCoursService();
    }

    // TODO : Ajouter le filtre
    @Get("/:idClasse/cours/:dateDebut/:dateFin")
    @ApiDoc("Get all course within a periode")
    @SecuredAction(value="", type= ActionType.AUTHENTICATED)
    public void getClasseCours(final HttpServerRequest request){
        String idClasse = request.params().get("idClasse");
        String dateDebut= request.params().get("dateDebut");
        String dateFin= request.params().get("dateFin");

        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

        iVscoCoursService.getClasseCours(dateDebut, dateFin, idClasse, handler);
    }
}
