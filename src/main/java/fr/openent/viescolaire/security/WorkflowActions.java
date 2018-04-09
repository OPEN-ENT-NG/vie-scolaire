package fr.openent.viescolaire.security;

public enum WorkflowActions {
    ADMIN_RIGHT ("Viescolaire.view");

    private final String actionName;

    WorkflowActions(String actionName) {
        this.actionName = actionName;
    }

    @Override
    public String toString () {
        return this.actionName;
    }
}
