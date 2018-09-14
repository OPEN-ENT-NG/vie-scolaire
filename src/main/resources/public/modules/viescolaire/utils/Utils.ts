import {model, Behaviours} from 'entcore';

export class Utils {
    static canAccessCompetences () {
        return Utils.userCanAccessModule(Behaviours.applicationsBehaviours.competences);
    }

    static canAccessPresences () {
        return Utils.userCanAccessModule(Behaviours.applicationsBehaviours.presences);
    }

    static modulePresenceIsInstalled() {
        return Utils.isModuleInstalled(Behaviours.applicationsBehaviours.presences);
    }

    static moduleCompetenceIsInstalled() {
        return Utils.isModuleInstalled(Behaviours.applicationsBehaviours.competences);
    }

    /**
     * Return if the module is installed
     * To determine if the module is installed we check if the 'rights' key exists
     * @param module
     * @returns {boolean}
     */
    static isModuleInstalled(module) {
        try {
            return !!module.rights;
        } catch {
            return false;
        }
    }

    static userCanAccessModule(module) {
        try {
            return module.rights.workflow.access !== undefined
                && model.me.hasWorkflow(module.rights.workflow.access);
        } catch {
            return false;
        }
    }
}