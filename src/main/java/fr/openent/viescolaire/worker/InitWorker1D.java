package fr.openent.viescolaire.worker;

import fr.openent.viescolaire.model.*;
import fr.wseduc.webutils.*;
import io.vertx.core.*;

public class InitWorker1D extends InitWorker {
    @Override
    protected Future<Void> initTimeSlots() {
        Promise<Void> promise = Promise.promise();

        this.initService.initTimeSlots(this.structureId, this.structureName, this.owner, this.form.getTimetable(), this.locale, this.acceptLanguage)
                .onSuccess(slotProfile -> {
                    this.timeslots = slotProfile.getSlots();
                    promise.complete();
                })
                .onFailure(promise::fail);
        return promise.future();
    }

    @Override
    protected Future<Void> initSubjects() {
        Promise<Void> promise = Promise.promise();

        String defaultSubjectLabel = config().getString("initDefaultSubject",
                I18n.getInstance().translate("viescolaire.default.subject.name", this.locale, this.acceptLanguage));

        this.initService.initSubject(this.structureId, new SubjectModel().setLabel(defaultSubjectLabel).setCode("999999"))
                .onSuccess(res -> {
                    this.mainSubject = res;
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
        this.initService.initExclusionPeriod(this.structureId, this.form.getHolidays(), this.form.getSchoolYear())
                        .onFailure(promise::fail)
                        .onSuccess(res -> promise.complete());
        return promise.future();
    }

    @Override
    protected Future<Void> initCourses() {
        Promise<Void> promise = Promise.promise();

        this.initService.initCourses(this.structureId, this.mainSubject.getId(),
                this.form.getSchoolYear().getStartDate(), this.form.getSchoolYear().getEndDate(),
                        this.form.getTimetable(), this.timeslots, this.owner.getId())
                        .onFailure(promise::fail)
                        .onSuccess(res -> promise.complete());

        return promise.future();
    }

    @Override
    protected Future<Void> initPresences() {
        Promise<Void> promise = Promise.promise();
        this.initService.initPresences(this.structureId, this.owner.getId())
                .compose(done -> this.initService.setInitPresencesSettings(this.structureId))
                .onFailure(fail -> {
                    String message = String.format("[Viescolaire@%s::initPresences] Failed to init presences: %s", this.getClass().getSimpleName(),
                            fail.getMessage());
                    promise.fail(message);
                })
                .onSuccess(res -> promise.complete());
        return promise.future();
    }

    @Override
    protected Future<Void> setInitStatus() {
        Promise<Void> promise = Promise.promise();
        this.initService.setInitializationStatus(this.structureId, true)
                .onFailure(promise::fail)
                .onSuccess(res -> promise.complete());
        return promise.future();
    }

}
