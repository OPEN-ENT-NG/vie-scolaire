package fr.openent.viescolaire.service;

import fr.openent.viescolaire.service.impl.DefaultClasseService;
import fr.openent.viescolaire.service.impl.DefaultTimeSlotService;

public class ServiceFactory {
    public TimeSlotService timeSlotService() {
        return new DefaultTimeSlotService(this);
    }

    public ClasseService classeService() {
        return new DefaultClasseService(this);
    }

}
