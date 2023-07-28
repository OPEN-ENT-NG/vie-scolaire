package fr.openent.viescolaire.controller;

import fr.openent.*;
import fr.openent.viescolaire.security.*;
import fr.wseduc.rs.*;
import fr.wseduc.security.*;
import io.vertx.core.http.*;
import org.entcore.common.controller.*;

public class FakeRight extends ControllerHelper {
    public FakeRight() {
        super();
    }

    private void notImplemented(HttpServerRequest request) {
        request.response().setStatusCode(501).end();
    }

    @Get("/rights/search/restricted")
    @SecuredAction(Viescolaire.SEARCH_RESTRICTED)
    public void searchRestricted(HttpServerRequest request) {notImplemented(request);}

    @Get("/rights/viescolaire/1d")
    @SecuredAction(Viescolaire.VIESCOLAIRE_1D)
    public void viescolaire1d(HttpServerRequest request) {notImplemented(request);}

}
