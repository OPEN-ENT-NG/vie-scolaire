package fr.openent.viescolaire.service;

import fr.openent.viescolaire.service.impl.DefaultClasseService;
import fr.openent.viescolaire.service.impl.DefaultGroupeService;
import fr.openent.viescolaire.service.impl.DefaultGroupingService;
import fr.openent.viescolaire.service.impl.DefaultTimeSlotService;

public class ServiceFactory {
    public TimeSlotService timeSlotService() {
        return new DefaultTimeSlotService(this);
    }

    public ClasseService classeService() {
        return new DefaultClasseService(this);
    }

    public GroupingService groupingService() {
        return new DefaultGroupingService(this);
    }

    public GroupeService groupeService() {
        return new DefaultGroupeService();
    }
}
