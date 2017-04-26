/*
 * Copyright (c) Région Hauts-de-France, Département 77, CGI, 2017.
 *
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
 *
 */

import { Collection, http } from 'entcore/entcore';

import { Cours } from './Cours';
import { DefaultStructure } from '../common/DefaultStructure';

export class Structure extends DefaultStructure {
    courss: Collection<Cours>;

    constructor (o?: any) {
        super();
        if (o && typeof o === 'object') {
            this.updateData(o);
        }
        this.collection(Cours, {
            sync : () => {
                return new Promise((resolve, reject) => {
                    http().getJson('/directory/timetable/courses/' + this.id).done((res: any) => {
                       console.log(res);
                    });
                    // TODO Récupérer les cours de la structure sur l'api timetable
                });
            }
        });
    }

    async sync (): Promise<any> {
        await this.courss.sync();
        return;
    }
}