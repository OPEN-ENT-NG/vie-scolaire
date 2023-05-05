package fr.openent.viescolaire.worker;

import fr.openent.viescolaire.core.constants.*;
import fr.openent.viescolaire.model.*;
import fr.wseduc.webutils.*;
import io.vertx.core.*;
import io.vertx.core.json.*;

public class InitWorker1D extends InitWorker {
    @Override
    protected Future<Void> initTimeSlots() {
        Promise<Void> promise = Promise.promise();

        this.initService.initTimeSlots(this.structureId, this.structureName, this.owner, this.form.getTimetable(), this.locale, this.acceptLanguage)
                .onSuccess(res -> promise.complete())
                .onFailure(promise::fail);
        return promise.future();
    }

    @Override
    protected Future<Void> initSubjects() {
        Promise<Void> promise = Promise.promise();

        String defaultSubjectLabel = config().getString("initDefaultSubject",
                I18n.getInstance().translate("viescolaire.default.subject.name", this.locale, this.acceptLanguage));

        this.initService.initSubjects(this.structureId, defaultSubjectLabel, "999999")
                .onSuccess(res -> {
                    JsonArray resArray = res.getJsonArray(Field.RESULTS, new JsonArray());
                    if (!resArray.isEmpty()
                            && !resArray.getJsonArray(0).isEmpty() && !resArray.getJsonArray(0).getJsonObject(0).isEmpty()) {
                        this.mainSubject = new SubjectModel(resArray.getJsonArray(0).getJsonObject(0));
                    }
                    promise.complete();
                })
                .onFailure(fail -> {
                    String message = String.format("[Viescolaire@%s::initSubjects] Failed to init subjects: %s", this.getClass().getSimpleName(),
                            fail.getMessage());
                    promise.fail(message);
                });
        return promise.future();
    }

    @Override
    protected Future<Void> initServices() {
        Promise<Void> promise = Promise.promise();
        this.initService.initServices(this.structureId, this.mainSubject)
                .onSuccess(res -> promise.complete())
                .onFailure(fail -> {
                    String message = String.format("[Viescolaire@%s::initServices] Failed to init services: %s", this.getClass().getSimpleName(),
                            fail.getMessage());
                    promise.fail(message);
                });
        return promise.future();
    }

    @Override
    protected Future<Void> initSchoolYear() {
        Promise<Void> promise = Promise.promise();
        Period schoolYear = this.form.getSchoolYear().getPeriod(this.structureId);
        this.periodeAnneeService.getPeriodeAnnee(this.structureId)
                .compose(periodYear -> periodYear.getId() == null ?
                        this.periodeAnneeService.createPeriode(schoolYear, true) :
                        this.periodeAnneeService.updatePeriode(periodYear.getId(), schoolYear, true))
                .onFailure(promise::fail)
                .onSuccess(res -> promise.complete());

        return promise.future();
    }

    @Override
    protected Future<Void> initExclusionPeriods() {
        Promise<Void> promise = Promise.promise();
        this.initService.initExclusionPeriod(this.structureId, this.form.getHolidays().getZone())
                        .onFailure(promise::fail)
                        .onSuccess(res -> promise.complete());
        return promise.future();
    }

    @Override
    protected Future<Void> initCourses() {
        Promise<Void> promise = Promise.promise();
        //TODO: MA-1002
        promise.complete();
        return promise.future();
    }

}
