/*
 * Copyright (c) Région Hauts-de-France, Département de la Seine-et-Marne, Région Nouvelle Aquitaine, Mairie de Paris, CGI, 2016.
 * This file is part of OPEN ENT NG. OPEN ENT NG is a versatile ENT Project based on the JVM and ENT Core Project.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 * For the sake of explanation, any module that communicate over native
 * Web protocols, such as HTTP, with OPEN ENT NG is outside the scope of this
 * license and could be license under its own terms. This is merely considered
 * normal use of OPEN ENT NG, and does not fall under the heading of "covered work".
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

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

    /**
     * Add the fields succeed and toastMessage to the response
     * @param response
     * @param message
     * @param errorMessage
     * @returns {any}
     */

    static setToastMessage(response, message, errorMessage){
        if(response.status === 200 || response.status === 201){
            response.succeed = true;
            response.toastMessage = message;

        } else {
            response.succeed = false;
            response.toastMessage = errorMessage;
        }
        return response;
    }

}