package fr.openent.absences.utils;

public enum Events {
    CREATE_APPEL ("CREATE_APPEL"),
    UPDATE_APPEL ("UPDATE_APPEL"),
    CREATE_EVENEMENT ("CREATE_EVENEMENT"),
    UPDATE_EVENEMENT ("UPDATE_EVENEMENT"),
    DELETE_EVENEMENT ("DELETE_EVENEMENT");

    private final String eventName;

    Events (String text) {
        this.eventName = text;
    }

    @Override
    public String toString () {
        return this.eventName;
    }
}