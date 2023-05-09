package fr.openent.viescolaire.service;

import fr.openent.viescolaire.service.impl.*;
import fr.wseduc.mongodb.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.*;
import org.entcore.common.neo4j.*;
import org.entcore.common.sql.*;

public class ServiceFactory {
    private final EventBus eb;

    private final Sql sql;
    private final Neo4j neo4j;

    private final MongoDb mongoDb;
    private final JsonObject config;
    private final TimeSlotService timeSlotService;
    private final ClasseService classeService;
    private final GroupeService groupeService;
    private final GroupingService groupingService;

    private final InitService initService;
    private final UserService userService;
    private final ServicesService servicesService;

    private final MatiereService matiereService;

    public ServiceFactory(EventBus eb, Sql sql, Neo4j neo4j, MongoDb mongoDb, JsonObject config) {
        this.eb = eb;
        this.sql = sql;
        this.neo4j = neo4j;
        this.mongoDb = mongoDb;
        this.config = config;
        this.classeService = new DefaultClasseService(this);
        this.groupeService = new DefaultGroupeService();
        this.timeSlotService = new DefaultTimeSlotService(this);
        this.groupingService = new DefaultGroupingService(this);
        this.matiereService = new DefaultMatiereService(eb);
        this.servicesService = new DefaultServicesService(eb);
        this.initService = new DefaultInitService(this);
        this.userService = new DefaultUserService(eb);
    }

    public TimeSlotService timeSlotService() {
        return this.timeSlotService;
    }

    public ClasseService classeService() {
        return this.classeService;
    }

    public GroupeService groupeService() {
        return this.groupeService;
    }

    public GroupingService groupingService() {
        return this.groupingService;
    }

    public InitService initService() {
        return this.initService;
    }

    public UserService userService() {
        return this.userService;
    }

    public ServicesService servicesService() {
        return this.servicesService;
    }

    public MatiereService matiereService() {
        return this.matiereService;
    }

    public EventBus getEventbus() {
        return eb;
    }

    public Sql sql() {
        return sql;
    }

    public MongoDb mongoDb() {
        return mongoDb;
    }

    public Neo4j neo4j() {
        return neo4j;
    }

    public JsonObject config() {
        return this.config;
    }
}
