import {model, Behaviours} from 'entcore';

export class Utils {

    static async loadModule(moduleName: string) {
        try {
            await Behaviours.load(moduleName);
        } catch {
            console.log(`Failed loading ${moduleName}`);
        }
    }

    static canAccessCompetences () {
        return Utils.userCanAccessModule(Behaviours.applicationsBehaviours.competences);
    }

    static canAccessPresences () {
        return Utils.userCanAccessModule(Behaviours.applicationsBehaviours.presences);
    }

    static canAccessEdt () {
        return Utils.userCanAccessModule(Behaviours.applicationsBehaviours.edt);
    }

    static moduleCompetenceIsInstalled() {
        return Utils.isModuleInstalled(Behaviours.applicationsBehaviours.competences);
    }

    static modulePresenceIsInstalled() {
        return Utils.isModuleInstalled(Behaviours.applicationsBehaviours.presences);
    }

    static moduleEdtIsInstalled() {
        return Utils.isModuleInstalled(Behaviours.applicationsBehaviours.edt);
    }

    /**
     * Return true if the module is installed
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

    /**
     * Return true if the current has the 'access' right on the module
     * @param module
     * @returns {any}
     */
    static userCanAccessModule(module) {
        try {
            return module.rights.workflow.access !== undefined
                && model.me.hasWorkflow(module.rights.workflow.access);
        } catch {
            return false;
        }
    }
}