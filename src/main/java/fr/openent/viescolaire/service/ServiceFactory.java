package fr.openent.viescolaire.service;

import fr.openent.viescolaire.service.impl.*;
import io.vertx.core.eventbus.EventBus;
import org.entcore.common.neo4j.*;
import org.entcore.common.sql.*;

public class ServiceFactory {
    private final EventBus eb;

    private final Sql sql;
    private final Neo4j neo4j;
    private final TimeSlotService timeSlotService;
    private final ClasseService classeService;
    private final GroupeService groupeService;
    private final GroupingService groupingService;

    private final InitService initService;

    public ServiceFactory(EventBus eb, Sql sql, Neo4j neo4j) {
        this.eb = eb;
        this.sql = sql;
        this.neo4j = neo4j;
        this.classeService = new DefaultClasseService(this);
        this.groupeService = new DefaultGroupeService();
        this.timeSlotService = new DefaultTimeSlotService(this);
        this.groupingService = new DefaultGroupingService(this);
        this.initService = new DefaultInitService(this);
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

    public EventBus getEventbus() {
        return eb;
    }

    public Sql sql() {
        return sql;
    }

    public Neo4j neo4j() {
        return neo4j;
    }
}
