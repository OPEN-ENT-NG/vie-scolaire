package fr.openent.viescolaire.controller;

import fr.openent.viescolaire.security.PeriodSetting;
import fr.openent.viescolaire.security.WorkflowActionUtils;
import fr.openent.viescolaire.service.PeriodeAnneeService;
import fr.openent.viescolaire.service.impl.DefaultPeriodeAnneeService;
import fr.openent.viescolaire.utils.DateHelper;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.rs.Put;
import fr.wseduc.rs.Delete;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.request.RequestUtils;
import io.vertx.core.http.HttpServerRequest;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.http.filter.ResourceFilter;


import java.util.Date;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;

public class PeriodeAnneeController extends ControllerHelper {

    private final PeriodeAnneeService periodeAnneeService;


    public PeriodeAnneeController() {
        periodeAnneeService = new DefaultPeriodeAnneeService();
    }


    @Get("/settings/periode")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    @ApiDoc("Recupère les periodes d'inclusion et d'exclusion de l'année en cours")
    public void getPeriodeAnnee (final HttpServerRequest request) {
        if (request.params().contains("structure")) {
            periodeAnneeService.getPeriodeAnnee(request.params().get("structure"),
                    defaultResponseHandler(request));
        } else if (request.params().contains("structureId")) {
            periodeAnneeService.listExclusion(request.params().get("structureId"),
                    arrayResponseHandler(request));
        }
        else {
            badRequest(request);
        }
    }


    /**
     * Créer une periode  d'inclusion avec les données passées en POST
     *
     * @param request
     */

    @Post("/settings/periode")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    @ApiDoc("Créer une période pour l'année scolaire en cours")
    public void createYear(final HttpServerRequest request) {
        RequestUtils.bodyToJson(request,
                periode -> {
                    DateHelper dateHelper = new DateHelper();
                    Date start_date =  dateHelper.getDate( periode.getString("start_date"), dateHelper.DATE_FORMATTER_SQL);
                    Date end_date =  dateHelper.getDate( periode.getString("end_date"), dateHelper.DATE_FORMATTER_SQL);
                    periode.put("code", "YEAR");
                    if(end_date.after(start_date)) {
                        periodeAnneeService.createPeriode(periode, true, arrayResponseHandler(request));
                    } else {
                        badRequest(request);
                    }
                });
    }

    /**
     * Créer une periode  d'exlusion avec les données passées en POST
     *
     * @param request
     */

    @Post("/settings/exclusion")
    @SecuredAction(value = WorkflowActionUtils.PERIOD_SETTING, type = ActionType.WORKFLOW)
    @ResourceFilter(PeriodSetting.class)
    @ApiDoc("Créer une période d'exclusion")
    public void createExclusion(final HttpServerRequest request) {
        RequestUtils.bodyToJson(request,
                periode -> {
                    DateHelper dateHelper = new DateHelper();
                    Date start_date =  dateHelper.getDate( periode.getString("start_date"), dateHelper.DATE_FORMATTER_SQL);
                    Date end_date =  dateHelper.getDate( periode.getString("end_date"), dateHelper.DATE_FORMATTER_SQL);
                    Date now = new Date();
                    if(end_date.after(start_date)&& now.before(start_date)) {
                        periodeAnneeService.createPeriode(periode,false, arrayResponseHandler(request));
                    } else {
                        badRequest(request);
                    }
                });
    }


    @Put("/settings/periode/:id")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(PeriodSetting.class)
    @ApiDoc("Update a period year based on provided id")
    public void updateYear (final HttpServerRequest request) {
        final Integer id = Integer.parseInt(request.params().get("id"));
        RequestUtils.bodyToJson(request,
                periode -> {
                    DateHelper dateHelper = new DateHelper();
                    Date start_date = dateHelper.getDate( periode.getString("start_date"),dateHelper.DATE_FORMATTER_SQL);
                    Date end_date = dateHelper.getDate( periode.getString("end_date"), dateHelper.DATE_FORMATTER_SQL);
                    if(start_date.before(end_date)) {
                        periodeAnneeService.updatePeriode(id, periode, true, arrayResponseHandler(request));
                    }else {
                        badRequest(request);
                    }
                });
    }


    @Put("/settings/exclusion/:id")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(PeriodSetting.class)
    @ApiDoc("Update a period exclusion based on provided id")
    public void updateExclusion (final HttpServerRequest request) {
        try {
            final Integer id = Integer.parseInt(request.params().get("id"));
            RequestUtils.bodyToJson(request,
                    exclusion -> {
                        DateHelper dateHelper = new DateHelper();
                        Date start_date = dateHelper.getDate( exclusion.getString("start_date"),dateHelper.DATE_FORMATTER_SQL);
                        Date end_date = dateHelper.getDate( exclusion.getString("end_date"), dateHelper.DATE_FORMATTER_SQL);
                        Date now = new Date();
                        if(start_date.before(end_date) && now.before(start_date)) {
                            periodeAnneeService.updatePeriode(id, exclusion, false, arrayResponseHandler(request));
                        }else {
                            badRequest(request);
                        }
                    });
        } catch (ClassCastException e) {
            log.error("E008 : An error occurred when casting exclusion id");
            badRequest(request);
        }
    }

    @Delete("/settings/exclusion/:id")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(PeriodSetting.class)
    @ApiDoc("Delete a period exclusion based on provided id")
    public void deleteExclusion (final HttpServerRequest request) {
        try {
            Integer id = Integer.parseInt(request.params().get("id"));
            periodeAnneeService.deleteExclusion(id, arrayResponseHandler(request));
        } catch (ClassCastException e) {
            log.error("E009 : An error occurred when casting exclusion id");
            badRequest(request);
        }
    }
}



