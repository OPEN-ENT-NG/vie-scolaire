package org.cgi.absences.controller;

import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import org.cgi.Viescolaire;
import org.cgi.absences.service.IAbscEleveService;
import org.cgi.absences.service.impl.CAbscEleveService;
import org.cgi.viescolaire.service.IVscoEleveService;
import org.cgi.viescolaire.service.impl.CVscoEleveService;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.impl.Json;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;

public class CAbscEleveController extends ControllerHelper {

    /**
     * Service relatif a des opérations concernant les élèves
     */
    private final IAbscEleveService miAbscEleveService;


    public CAbscEleveController(){
        pathPrefix = Viescolaire.ABSC_PATHPREFIX;
        miAbscEleveService = new CAbscEleveService();
    }

    @Get("/eleve/:idEleve/evenements/:dateDebut/:dateFin")
    @ApiDoc("Recupere tous le evenements d'un eleve sur une periode")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getEvenements(final HttpServerRequest request){
        String sIdEleve = request.params().get("idEleve");
        String sDateDebut = request.params().get("dateDebut");
        String sDateFin = request.params().get("dateFin");

        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

        miAbscEleveService.getEvenements(sIdEleve, sDateDebut, sDateFin, handler);
    }

    @Get("/eleve/:idEleve/absencesprev")
    @ApiDoc("Recupere tous le absences previsonnelles d'un eleve")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getAbsencesPrev(final HttpServerRequest request){
        String sIdEleve = request.params().get("idEleve");

        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

        miAbscEleveService.getAbsencesPrev(sIdEleve, handler);
    }

    @Get("/eleve/:idEleve/absencesprev/:dateDebut/:dateFin")
    @ApiDoc("Recupere tous le absences previsonnelles d'un eleve")
    @SecuredAction(value = "", type= ActionType.AUTHENTICATED)
    public void getAbsencesPrevInPeriod (final HttpServerRequest request){
        String sIdEleve = request.params().get("idEleve");
        String sDateDebut = request.params().get("dateDebut");
        String sDateFin = request.params().get("dateFin");

        Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

        miAbscEleveService.getAbsencesPrev(sIdEleve, sDateDebut, sDateFin, handler);
    }

    @Get("/eleves/evenements/:dateDebut/:dateFin")
    @ApiDoc("Recupere toutes les absences sans motifs dans une période donnée")
    @SecuredAction(value = "", type=ActionType.AUTHENTICATED)
    public void getAbsencesSansMotifs(final HttpServerRequest request){
        UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
            @Override
            public void handle(UserInfos user) {
                String sDateDebut = request.params().get("dateDebut")+" 00:00:00";
                String sDateFin = request.params().get("dateFin")+" 23:59:59";

                Handler<Either<String, JsonArray>> handler = arrayResponseHandler(request);

                miAbscEleveService.getAbsencesSansMotifs(user.getStructures().get(0), sDateDebut, sDateFin, handler);
            }
        });
    }
}
