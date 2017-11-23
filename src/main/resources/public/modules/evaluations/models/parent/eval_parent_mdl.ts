/*
 * Copyright (c) Région Hauts-de-France, Département 77, CGI, 2016.
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

/**
 * Created by ledunoiss on 08/08/2016.
 */
import { model, notify, http, IModel, Model, Collection, BaseModel, idiom as lang } from 'entcore/entcore';
import {Classe, Enseignant} from "../teacher/eval_teacher_mdl";
import {Periode} from "../../../viescolaire/models/common/Periode";
import {Devoir} from "./devoir";
import {Matiere} from "./matiere";
import {Eleve} from "./eleve";

let moment = require('moment');
declare let _: any;
declare let location: any;

export class Evaluations extends Model {
    eleves: Collection<Eleve>;
    matieres: Collection<Matiere>;
    periodes: Collection<Periode>;
    classes: Collection<Classe>;
    enseignants: Collection<Enseignant>;
    devoirs: Collection<Devoir>;
    idEtablissement: string;

    get api() {
        return {
            GET_CLASSES: '/viescolaire/classes?idEtablissement=',
            EVAL_ENFANTS: '/viescolaire/evaluations/enfants?userId=' + model.me.userId
        };
    }

    constructor (o?: any) {
        super(o);
        this.collection(Classe, {
            sync: function (idEtablissement) {
                return new Promise((resolve, reject) => {
                    http().getJson(this.composer.api.GET_CLASSES + idEtablissement).done(function (classes) {
                        this.load(classes);
                        resolve();
                    }.bind(this));
                });
            }
        });
    }

    sync  () : Promise<any> {
        return new Promise(async (resolve, reject) => {
            await this.classes.sync(model.me.structures[0]);
            if (model.me.type === 'PERSRELELEVE') {
                this.collection(Eleve, {
                    sync: () => {
                        return new Promise((resolve, reject) => {
                            http().get(this.api.EVAL_ENFANTS).done((enfants) => {
                                this.eleves.load(enfants);
                                resolve();
                            });
                        });
                    }
                });
                await this.eleves.sync();
                resolve ();

            } else {
                this.makeModels([Devoir, Periode, Matiere]);
            }
        });
    }
}




export let evaluations = new Evaluations();

model.build = function () {
    (this as any).evaluations = evaluations;
    evaluations.sync();
};

