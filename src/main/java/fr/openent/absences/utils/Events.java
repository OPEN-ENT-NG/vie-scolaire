package fr.openent.absences.utils;

public enum Events {
    CREATE_APPEL ("CREATE_APPEL"),
    UPDATE_APPEL ("UPDATE_APPEL"),
    CREATE_EVENEMENT ("CREATE_EVENEMENT"),
    UPDATE_EVENEMENT ("UPDATE_EVENEMENT"),
    DELETE_EVENEMENT ("DELETE_EVENEMENT"),
    CREATE_MOTIF ("CREATE_MOTIF"),
    UPDATE_MOTIF ("UPDATE_MOTIF"),
    DELETE_MOTIF ("DELETE_MOTIF");

    private final String eventName;

    Events (String text) {
        this.eventName = text;
    }

    @Override
    public String toString () {
        return this.eventName;
    }
}