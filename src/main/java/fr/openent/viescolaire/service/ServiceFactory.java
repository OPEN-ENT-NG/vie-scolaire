package fr.openent.viescolaire.service;

import fr.openent.viescolaire.service.impl.DefaultClasseService;
import fr.openent.viescolaire.service.impl.DefaultGroupeService;import fr.openent.viescolaire.service.impl.DefaultGroupingService;
import fr.openent.viescolaire.service.impl.DefaultTimeSlotService;
import io.vertx.core.eventbus.EventBus;

public class ServiceFactory {
    private final EventBus eb;
    private final TimeSlotService timeSlotService;
    private final ClasseService classeService;
    private final GroupeService groupeService;
    private final GroupingService groupingService;

    public ServiceFactory(EventBus eb) {
        this.eb = eb;
        this.classeService = new DefaultClasseService(this);
        this.groupeService = new DefaultGroupeService();
        this.timeSlotService = new DefaultTimeSlotService(this);
        this.groupingService = new DefaultGroupingService(this);
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

    public EventBus getEventbus() {
        return eb;
    }
}
