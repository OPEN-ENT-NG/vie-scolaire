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

import { model, Model, Collection, idiom as lang } from 'entcore';
import { getActiveStructures } from "../../utils/functions/activeStructures";
import {Structure} from './personnel/Structure';


declare let _: any;
declare let window: any;

/**
 * MODELE DE DONNEES PERSONNEL :
 *  viescolaire
 */

export class VieScolaire extends Model {
    structures: Collection<Structure>;
    structure: Structure;

    constructor () {
        super();
        this.collection(Structure, {
            sync: async (): Promise<any> => {
                try {
                    let structuresPresences;
                    let structuresEvaluations;
                    let _structureTmp = [];

                    // récupération des structures actives par module
                    if (window.services.presences) {
                        structuresPresences = await getActiveStructures('presences');
                    }
                    if (window.services.competences) {
                        structuresEvaluations = await getActiveStructures('notes');
                    }
                    for (let i = 0; i < model.me.structures.length; i++) {
                        let _structure = new Structure({
                            id: model.me.structures[i],
                            name: model.me.structureNames[i]
                        });

                        if (_.findWhere(structuresEvaluations, {id : _structure.id})) {
                            _structure.isActived.evaluation = true;
                        }
                        if (_.findWhere(structuresPresences, {id : _structure.id})) {
                            _structure.isActived.presence = true;
                        }
                        _structureTmp.push(_structure);
                    }
                    this.structures.load(_structureTmp);
                    this.structure = this.structures.all[0];
                    return;
                } catch (e) {
                    throw e;
                }
            }
        });
    }

    async sync (): Promise<any> {
        try {
            await this.structures.sync();
        } catch (e) {
            throw  e;
        }
    }
}

export let vieScolaire = new VieScolaire();

model.build = async function () {
    (this as any).vieScolaire = vieScolaire;
    await vieScolaire.sync();
};