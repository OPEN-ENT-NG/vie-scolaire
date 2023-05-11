package fr.openent.viescolaire.worker;

import fr.openent.viescolaire.core.constants.*;
import fr.openent.viescolaire.helper.*;
import fr.openent.viescolaire.model.*;
import fr.openent.viescolaire.model.InitForm.*;
import fr.openent.viescolaire.model.Person.*;
import fr.openent.viescolaire.model.SlotProfile.*;
import fr.openent.viescolaire.service.*;
import fr.openent.viescolaire.service.impl.*;
import fr.wseduc.mongodb.*;
import io.vertx.core.*;
import io.vertx.core.eventbus.*;
import io.vertx.core.json.*;
import io.vertx.core.logging.*;
import org.entcore.common.neo4j.*;
import org.entcore.common.sql.*;

import java.util.*;

public abstract class InitWorker extends AbstractVerticle {
    protected final Logger log = LoggerFactory.getLogger(InitWorker.class);

    protected Context initContext;
    protected String structureId;
    protected String structureName;
    protected User owner;
    protected InitFormModel form;

    // I18N PARAMS
    protected String locale;
    protected String acceptLanguage;

    protected SubjectModel mainSubject;
    protected List<Timeslot> timeslots;
    protected InitService initService;
    protected PeriodeAnneeService periodeAnneeService;

    @Override
    public void start() {
        initContext = vertx.getOrCreateContext();
        String launchLog = String.format("[Viescolaire@%s::start] Launching worker %s, deploy verticle %s",
                this.getClass().getSimpleName(), this.getClass().getSimpleName(), initContext.deploymentID());
        log.info(launchLog);

        ServiceFactory serviceFactory = new ServiceFactory(vertx.eventBus(), Sql.getInstance(), Neo4j.getInstance(),
                MongoDb.getInstance(), config());

        initService = new DefaultInitService(serviceFactory);
        periodeAnneeService = new DefaultPeriodeAnneeService();

        vertx.eventBus().consumer(this.getClass().getName(), this::run);
    }

    protected void run(Message<JsonObject> event) {
        form = new InitFormModel(event.body().getJsonObject(Field.PARAMS));
        structureId = event.body().getString(Field.STRUCTUREID);
        structureName = event.body().getString(Field.STRUCTURENAME);
        owner = new User(event.body().getJsonObject(Field.OWNER));
        locale = event.body().getJsonObject(Field.I18N_PARAMS).getString(Field.LANGUAGE);
        acceptLanguage = event.body().getJsonObject(Field.I18N_PARAMS).getString(Field.ACCEPT_LANGUAGE);

        log.info(String.format("[Viescolaire@%s::run] Starting worker %s process, initializing structure %s",
                this.getClass().getSimpleName(), this.getClass().getSimpleName(), structureId));

        FutureHelper.all(Arrays.asList(initTimeSlots(), initSubjects()))
                .compose(r -> initServices())
                .compose(r -> initSchoolYear())
                .compose(r -> initExclusionPeriods())
                .compose(r -> initCourses())
                .compose(r -> initPresences())
                .compose(r -> setInitStatus());
    }

    protected abstract Future<Void> initTimeSlots();

    protected abstract Future<Void> initSubjects();

    protected abstract Future<Void> initServices();

    protected abstract Future<Void> initSchoolYear();
    protected abstract Future<Void> initExclusionPeriods();

    protected abstract Future<Void> initCourses();

    protected abstract Future<Void> initPresences();

    protected abstract Future<Void> setInitStatus();



}
