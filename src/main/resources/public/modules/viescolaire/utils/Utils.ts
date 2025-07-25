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
import rights from '../ts/rights';

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

    static async canAccessPresences () {
        // Fix: #COCO-4263, hide Presences tab if user is Director 1D and non ADMC
        const isDirector1DButNonAdmc: boolean = await Utils.isDirector1DButNonAdmc();
        if (isDirector1DButNonAdmc) {
            return false;
        }

        return Utils.userCanAccessModule(Behaviours.applicationsBehaviours.presences);
    }

    static async canAccessEdt () {
        // Fix: #COCO-4263, hide EDT tab if user is Director 1D and non ADMC
        const isDirector1DButNonAdmc: boolean = await Utils.isDirector1DButNonAdmc();
        if (isDirector1DButNonAdmc) {
            return false;
        }

        return Utils.userCanAccessModule(Behaviours.applicationsBehaviours.edt);
    }

    static canAccessDiary () {
        return Utils.userCanAccessModule(Behaviours.applicationsBehaviours.diary);
    }

    // Fix: #COCO-4007 & #COCO-4263, utility method to hide the following items if user is Director 1D and non ADMC:
    // - Presences Tab
    // - EDT Tab
    static async isDirector1DButNonAdmc(): Promise<boolean> {
        const isUserDirector1D = await Utils.isUserDirector1D();
        return isUserDirector1D && !Utils.isAdmc();
    }

    static async isUserDirector1D(): Promise<boolean> {
        if (model.me.workflow && !model.me.workflow.hasOwnProperty("presences")) {
            await model.me.workflow.load(["presences"]);
        }
        return model.me.hasWorkflow(rights.workflow["initSettings1D"]) 
            && model.me.hasWorkflow(rights.workflow["initPopup"]);
    }

    static isAdmc(): boolean {
        return model.me.functions["SUPER_ADMIN"];
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

    static isNull =  function (object) {
        return (object === undefined) || (object === null);
    };


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

    /**
     * Modify all key name with underscore to camelCase Style (e.g 'last_name' to 'lastName')
     * @param array
     * @returns array with all key camelCased
     */
    static toCamelCase(array: Array<any>): Array<any> {
        return JSON.parse(JSON.stringify(array).replace(/\B([-_][a-z])/ig, ($1) => {
            return $1.toUpperCase()
                .replace('-', '')
                .replace('_', '');
        }));
    }

}