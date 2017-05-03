package fr.openent.absences.utils;

public enum Events {
    CREATE_APPEL ("CREATE_APPEL"),
    CREATE_ABSENCE ("CREATE_ABSENCE"),
    DELETE_ABSENCE ("DELETE_ABSENCE"),
    CREATE_DEPART ("CREATE_DEPART"),
    UPDATE_DEPART ("UPDATE_DEPART"),
    DELETE_DEPART ("DELETE_DEPART"),
    CREATE_RETARD ("CREATE_RETARD"),
    UPDATE_RETARD ("UPDATE_RETARD"),
    DELETE_RETARD ("DELETE_RETARD"),
    CREATE_INCIDENT ("CREATE_INCIDENT"),
    UPDATE_INCIDENT ("UPDATE_INCIDENT"),
    DELETE_INCIDENT ("DELETE_INCIDENT"),
    CREATE_SANCTION ("CREATE_SANCTION"),
    UPDATE_SANCTION ("UPDATE_SANCTION"),
    DELETE_SANCTION ("DELETE_SANCTION");

    private final String eventName;

    Events (String text) {
        this.eventName = text;
    }

    public String toString () {
        return this.eventName;
    }
}