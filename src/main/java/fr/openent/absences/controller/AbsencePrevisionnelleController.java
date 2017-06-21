package fr.openent.absences.controller;

import fr.openent.Viescolaire;
import fr.openent.absences.service.AbsencePrevisionnelleService;
import fr.openent.absences.service.impl.DefaultAbsencePrevisionnelleService;
import fr.openent.absences.utils.EventRegister;
import fr.openent.viescolaire.service.EventService;
import fr.openent.viescolaire.service.impl.DefaultEventService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Post;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.request.RequestUtils;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import static org.entcore.common.http.response.DefaultResponseHandler.notEmptyResponseHandler;

/**
 * Created by rahnir on 20/06/2017.
 */
public class AbsencePrevisionnelleController extends ControllerHelper {
    private final AbsencePrevisionnelleService AbscPrevService;
    private final EventService eventService;
    private final EventRegister eventRegister = new EventRegister();

    public AbsencePrevisionnelleController(){
        pathPrefix = Viescolaire.ABSC_PATHPREFIX;
        AbscPrevService = new DefaultAbsencePrevisionnelleService();
        eventService = new DefaultEventService();
    }

    @Post("/absence/previsionnelle")
    @ApiDoc("Création d'une absence previsionnelle.")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void createAbscPrev(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(final UserInfos user) {
                RequestUtils.bodyToJson(request, Viescolaire.VSCO_PATHPREFIX + Viescolaire.SCHEMA_ABSPREV_CREATE, new Handler<JsonObject>() {
                    @Override
                    public void handle(final JsonObject poAbscencePrev) {
                        AbscPrevService.createAbsencePrev(poAbscencePrev, notEmptyResponseHandler(request));
                    }
                });
            }
        });
    }
}
