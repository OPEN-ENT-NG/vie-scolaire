package fr.openent.viescolaire.worker;

import fr.wseduc.webutils.*;
import io.vertx.core.*;

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
                .onSuccess(res -> promise.complete())
                .onFailure(fail -> {
                    String message = String.format("[Viescolaire@%s::initSubjects] Failed to init subjects", this.getClass().getSimpleName());
                    promise.fail(message);
                });
        return promise.future();
    }

    @Override
    protected Future<Void> initServices() {
        Promise<Void> promise = Promise.promise();
        //TODO : MA-1001
        promise.complete();
        return promise.future();
    }

    @Override
    protected Future<Void> initExclusionPeriods() {
        Promise<Void> promise = Promise.promise();
        //TODO: MA-1066
        promise.complete();
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
