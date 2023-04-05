package fr.openent.viescolaire.worker;

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
        //TODO : MA-1000
        promise.complete();
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
