package org.cgi.evaluations.controller;

import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import org.cgi.evaluations.service.IEvalEnseignementService;
import org.cgi.evaluations.service.impl.CEvalEnseignementServiceImpl;
import org.entcore.common.controller.ControllerHelper;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServerRequest;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;

/**
 * Created by ledunoiss on 05/08/2016.
 */
public class CEvalEnseignementController extends ControllerHelper {

    /**
     * Création des constantes liés au framework SQL
     */
    private final String ENSEIGNEMENTS_TABLE = "enseignements";

    /**
     * Déclaration des services
     */
    private final IEvalEnseignementService enseignementService;

    public CEvalEnseignementController() {
        enseignementService = new CEvalEnseignementServiceImpl(ENSEIGNEMENTS_TABLE);
    }

    /**
     * Récupère la liste des enseignements
     * @param request
     */
    @Get("/enseignements")
    @ApiDoc("Recupère la liste des enseignements")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getEnseignements(final HttpServerRequest request){
        enseignementService.getEnseignements(arrayResponseHandler(request));
    }
}
