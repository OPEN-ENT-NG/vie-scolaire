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
import {Classe, Enseignant} from "./eval_teacher_mdl";
import {translate} from "../../utils/functions/translate";


let moment = require('moment');
declare let _: any;

export class Devoir extends Model {}
export class Eleve extends Model {
    periodes: Collection<Periode>;
    id: any;

    get api () {
        return {
            get : '/viescolaire/periodes?idEtablissement='
        };
    }

    constructor () {
        super();
        this.collection(Periode, {
            sync : (idEtablissement) => {
                let that = this;
                if (this.id !== undefined) {
                    let idEtab = idEtablissement;
                    if (idEtab === undefined) {
                        idEtab = model.me.structures[0];
                    }
                    http().getJson(this.api.get + idEtab ).done(function (periodes) {
                        periodes.push({libelle: translate('viescolaire.utils.annee'), id: undefined});
                        that.periodes.load(periodes);
                    });
                }
            }
        });
        this.periodes.on('sync', function () {
            this.trigger('syncPeriodes');
        });
    }
}

export class Periode extends Model {
    id: any;
    moyenne: number;
    timestamp_dt: any;
    timestamp_fn: any;
    devoirs: Collection<Devoir>;
    matieres: Collection<Matiere>;
    enseignants: Collection<Enseignant>;
    synchronised_matieres: boolean;
    synchronised_enseignants: boolean;

    get api () {
        return {
            GET_EVALUATIONS : '/viescolaire/evaluations/devoirs?idEtablissement=',
            GET_MATIERES : '/viescolaire/matieres/infos?',
            GET_ENSEIGNANTS: '/viescolaire/enseignants?'
        };
    }

    constructor () {
        super();
        this.synchronised_matieres = false;
        this.synchronised_enseignants = false;
        this.collection(Enseignant, {
            sync: (mapEnseignant) => {
                return new Promise((resolve, reject) => {
                    let uri = this.api.GET_ENSEIGNANTS;
                    for (let enseignant in mapEnseignant) {
                        uri += '&idUser=' + enseignant;
                    }
                    let that = this;
                    http().get(uri).done((enseignants) => {
                        that.enseignants.load(enseignants);
                        that.synchronised_enseignants = true;
                        resolve();
                    });
                });
            }
        });
        this.collection(Matiere, {
            sync: (mapMatiere) => {
                return new Promise((resolve, reject) => {
                    let uri = this.api.GET_MATIERES;
                    let that = this;
                    for (let matiere in mapMatiere) {
                        uri = uri + '&idMatiere=' + matiere;
                    }
                    http().get(uri).done((matieres) => {
                        that.matieres.load(matieres);
                        that.synchronised_matieres = true;
                        resolve();
                    });
                });
            }
        });
        this.collection(Devoir, {
            sync: (structureId, userId, classes) => {
                return new Promise((resolve, reject) => {
                let uri = this.api.GET_EVALUATIONS
                    + structureId + '&idEleve=' + userId;
                if (this.id !== undefined) {
                    uri = uri + '&idPeriode=' + this.id;
                }


                http().getJson(uri)
                    .done((devoirs) => {
                        this.devoirs.load(devoirs);
                        let matieresDevoirs = _.groupBy(devoirs, 'id_matiere');
                        let enseignants = _.groupBy(devoirs, 'owner');
                        this.synchronised_matieres = false;
                        this.synchronised_enseignants = false;
                        this.enseignants.sync(enseignants).then(() => {
                            if (this.buildMatieres(matieresDevoirs)) {
                               resolve();
                            }
                        });
                        this.matieres.sync(matieresDevoirs).then(() => {
                            if (this.buildMatieres(matieresDevoirs)) {
                                resolve();
                            }
                        });
                    });
                });
            }
        });
     }

    buildMatieres (mapMatiere) {
        if (this.synchronised_matieres && this.synchronised_enseignants) {
            for (let o in mapMatiere) {
                for (let e in mapMatiere[o]) {
                    let devoir = mapMatiere[o][e];

                    let _matiere = this.matieres.findWhere({id: devoir.id_matiere});
                    let enseignant = this.enseignants.findWhere({id: devoir.owner});
                    if (_.filter(_matiere.ens, {id: enseignant.id}).length === 0) {
                        _matiere.ens.push(enseignant);
                    }
                }
            }
            return true;
        }
        return false;
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

export class Matiere extends Model {
    id: string;
    name: string;
    externalId: string;
    ens: any = [];
    moyenne: number;

    get api () {
        return {
            calculMoyenne: '/viescolaire/evaluations/eleve/'
        }
    }

    getMoyenne (id_eleve, devoirs?) : Promise<any> {
        return new Promise((resolve, reject) => {
            if (devoirs) {
                let idDevoirsURL = "";

                _.each(_.pluck(devoirs,'id'), (id) => {
                    idDevoirsURL += "devoirs=" + id + "&";
                });
                idDevoirsURL = idDevoirsURL.slice(0, idDevoirsURL.length - 1);

                http().getJson(this.api.calculMoyenne + id_eleve + "/moyenne?" + idDevoirsURL).done(function (res) {
                    if (!res.error) {
                        this.moyenne = res.moyenne;
                    } else {
                        this.moyenne = "";
                    }
                    if(resolve && typeof(resolve) === 'function'){
                        resolve();
                    }
                }.bind(this));
            }
        });
    }
}

export class Evaluations extends Model {
    eleves: Collection<Eleve>;
    matieres: Collection<Matiere>;
    periodes: Collection<Periode>;
    classes: Collection<Classe>;
    enseignants: Collection<Enseignant>;
    devoirs: Collection<Devoir>;
    idEtablissement: string;

    constructor () {
        super();
    }

    get api () {
        return {
            GET_CLASSES: '/viescolaire/classes?idEtablissement=',
            EVAL_ENFANTS : '/viescolaire/evaluations/enfants?userId=' + model.me.userId ,
            PERIODES : '/viescolaire/periodes?idEtablissement='
        };
    }

    sync () {
        this.collection(Classe, {
            sync: function(idEtablissement) {
                return new Promise((resolve, reject) => {
                    http().getJson(this.composer.api.GET_CLASSES + idEtablissement).done(function (classes) {
                        this.load(classes);
                        resolve();
                    }.bind(this));
                });
            }
        });
        this.classes.sync( model.me.structures[0]);
        if ( model.me.type === 'PERSRELELEVE' ) {
            this.collection(Eleve, {
                sync: function() {
                    return new Promise((resolve, reject) => {
                        http().get(this.composer.api.EVAL_ENFANTS).done(
                            function (eleves) {
                                let listeEleves = [];
                                for (let i = 0; i < eleves.length; i++) {
                                    listeEleves.push({
                                        id: eleves[i]["n.id"], displayName: eleves[i]["n.displayName"],
                                        structures: [eleves[i]["s.id"]]
                                    });
                                }
                                this.load(listeEleves);
                                resolve();
                            }.bind(this));
                    });
                }
            });
            this.eleves.sync();

        }else {
            this.makeModels([Devoir, Periode, Matiere]);
            this.collection(Periode, {
                sync : function(structureId?) {
                    let idStructure = structureId;
                    if (idStructure === undefined) {
                        idStructure = model.me.structures[0];
                    }
                    http().getJson(this.composer.api.PERIODES + idStructure).done(function(periodes) {
                        periodes.push({libelle: translate('viescolaire.utils.annee'), id: undefined});
                        this.load(periodes);
                    }.bind(this));
                }
            });
        }
    }
}

export let evaluations = new Evaluations();

model.build = function () {
    (this as any).evaluations = evaluations;
    evaluations.sync();
};

