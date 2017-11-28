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
import { model, http, Model, Collection } from 'entcore/entcore';
import { Classe } from "./parent_eleve/Classe";
import { Devoir } from "./parent_eleve/Devoir";
import { Matiere } from "./parent_eleve/Matiere";
import { Eleve } from "./parent_eleve/Eleve";
import { Enseignant } from "./parent_eleve/Enseignant";

let moment = require('moment');
declare let _: any;
declare let location: any;

export class Evaluations extends Model {
    eleves: Collection<Eleve>;
    eleve: Eleve;
    matieres: Collection<Matiere>;
    classes: Collection<Classe>;
    enseignants: Collection<Enseignant>;
    devoirs: Collection<Devoir>;


    get api() {
        return {
            EVAL_ENFANTS: '/viescolaire/evaluations/enfants?userId=' + model.me.userId,
            GET_EVALUATIONS : '/viescolaire/evaluations/devoirs?idEtablissement=',
            GET_MATIERES : '/viescolaire/matieres/infos?',
            GET_ENSEIGNANTS: '/viescolaire/enseignants?'
        };
    }

    constructor (o?: any) {
        super(o);
    }

    sync  () : Promise<any> {
        return new Promise(async (resolve, reject) => {
            // await this.classes.sync(model.me.structures[0]);
            this.collection(Eleve, {
                sync: async () => {
                    return new Promise((resolve, reject) => {
                        http().get(this.api.EVAL_ENFANTS).done((enfants) => {
                            this.eleves.load(enfants);
                            resolve();
                        });
                    });
                }
            });
            this.collection(Enseignant, {
                sync: async (mapEnseignant) => {
                    return new Promise((resolve) => {
                        let uri = this.api.GET_ENSEIGNANTS;
                        for (let enseignant in mapEnseignant) {
                            uri += '&idUser=' + enseignant;
                        }
                        http().get(uri).done((enseignants) => {
                            this.enseignants.load(enseignants);
                            resolve();
                        }).bind(this);
                    });
                }
            });
            this.collection(Matiere, {
                sync: async (mapMatiere) => {
                    return new Promise((resolve) => {
                        let uri = this.api.GET_MATIERES;
                        for (let matiere in mapMatiere) {
                            uri = uri + '&idMatiere=' + matiere;
                        }
                        http().get(uri).done((matieres) => {
                            this.matieres.load(matieres);
                            resolve();
                        }).bind(this);
                    });
                }
            });
            this.collection(Devoir, {
                sync: async(structureId, userId, classeId) => {
                    return new Promise( (resolve) => {
                        let uri = this.api.GET_EVALUATIONS
                            + structureId + '&idEleve=' + userId;
                        if (classeId !== undefined) {
                            uri = uri + '&idClasse=' + classeId;
                        }
                        http().getJson(uri).done((devoirs) => {
                            this.devoirs.load(devoirs);
                            let matieresDevoirs = _.groupBy(devoirs, 'id_matiere');
                            let enseignants = _.groupBy(devoirs, 'owner');
                            this.enseignants.sync(enseignants).then(() => {
                                this.matieres.sync(matieresDevoirs).then(()=>{
                                    for (let o in matieresDevoirs) {
                                        matieresDevoirs[o].forEach(function (element) {
                                            let devoir = element;
                                            let _matiere = this.matieres.findWhere({id: devoir.id_matiere});
                                            let enseignant = this.enseignants.findWhere({id: devoir.owner});
                                            if (_.filter(_matiere.ens, {id: enseignant.id}).length === 0) {
                                                _matiere.ens.push(enseignant);
                                            }
                                        });
                                    }
                                    resolve();
                                });
                            });

                        }).bind(this);
                    });
                }
            });

            // Synchronisation de la collection d'élèves pour les parents
            if (model.me.type === 'PERSRELELEVE') {
                await this.eleves.sync();
                resolve ();
            }
            // Synchronisation des matières, enseignants, devoirs et de l'élève.
            // TODO REGARDER SI BESOIN DES GROUPES
            else {
                // this.makeModels([Classe]);
                this.eleve = new Eleve({
                    id: model.me.userId,
                    idClasse: model.me.classes[0],
                    displayName: model.me.username,
                    firstName: model.me.firstName,
                    lastName: model.me.lastName,
                    idStructure: model.me.structures[0],
                    classe: new Classe({id: model.me.classes[0], name: model.me.className[0]})
                });

                await this.eleve.classe.sync();
                await this.devoirs.sync(this.eleve.idStructure, this.eleve.id, this.eleve.classe.id);
                resolve();
            }
        });
    }

    getReleve (idPeriode, idUser) {
        let uri = '/viescolaire/evaluations/releve/pdf?idEtablissement=' +
            model.me.structures[0] + '&idUser=' + idUser;
        if (idPeriode !== undefined) {
            uri +=  '&idPeriode=' + idPeriode;
        }
        location.replace(uri);
    }
}




export let evaluations = new Evaluations();

model.build = function () {
    (this as any).evaluations = evaluations;
    evaluations.sync();
};

