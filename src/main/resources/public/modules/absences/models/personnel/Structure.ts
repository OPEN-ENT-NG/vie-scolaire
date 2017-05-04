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

import { Collection, http, idiom as lang } from 'entcore/entcore';

import { DefaultStructure } from '../common/DefaultStructure';
import { Enseignant } from "./Enseignant";
import { Appel } from "./Appel";
import { Classe } from "./Classe";
import { Justificatif } from "./Justificatif";
import { Motif } from "./Motif";
import { Evenement } from "./Evenement";
import {Matiere} from "./Matiere";


export class Structure extends DefaultStructure {

    enseignants: Collection<Enseignant>;
    matieres: Collection<Matiere>;
    appels: Collection<Appel>;
    classes: Collection<Classe>;
    justificatifs: Collection<Justificatif>;
    motifs: Collection<Motif>;
    evenements: Collection<Evenement>;
    synchronized: any;
    isSynchronized: boolean;

    get api () {
        return  {
            CLASSE : {
                synchronization : '/viescolaire/classes?idEtablissement=' + this.id,
            },
            ENSEIGNANT : {
                synchronization : '/viescolaire/evaluations/user/list?profile=Teacher&structureId=' + this.id,
            },
            MATIERE : {
                synchronization : '/viescolaire/matieres?idEtablissement=',
            },
            JUSTIFICATIF : {
                synchronization : '/viescolaire/presences/justificatifs?idEtablissement='+ this.id,
            }
        };
    }
    constructor (o?: any) {
        super();
        if (o && typeof o === 'object') {
            this.updateData(o);
        }

        // Constantes utiles pour la synchro des appels
        this.isSynchronized = false;
        this.synchronized = {
            matieres : false,
            classes : false,
            enseignants: false
        };

        // Constantes utiles pour la récupération de classes et groupes d'enseignements
        const libelle = {
            CLASSE: 'Classe',
            GROUPE: "Groupe d'enseignement"
        };
        const castClasses = (classes) => {
            return _.map(classes, (classe) => {
                let libelleClasse;
                if (classe.type_groupe_libelle = classe.type_groupe === 0) {
                    libelleClasse = libelle.CLASSE;
                } else {
                    libelleClasse = libelle.GROUPE;
                }
                classe.type_groupe_libelle = libelleClasse;
                if (!classe.hasOwnProperty("remplacement")) classe.remplacement = false;
                classe = new Classe(classe);
                return classe;
            });
        };

        let that: Structure = this;
        this.collection(Motif, {
            sync : function () {
                http().getJson('/viescolaire/presences/motifs').done(function (motifs) {
                    this.load(motifs);
                    this.map(function (motif) {
                        motif.justifiant_libelle = motif.justifiant ? lang.translate("viescolaire.utils.justifiant") : lang.translate("viescolaire.utils.nonjustifiant");
                        return motif;
                    });
                }.bind(this));
            }
        });
        this.collection(Enseignant, {
            sync : function () {
                return new Promise((resolve, reject) => {
                    http().getJson(that.api.ENSEIGNANT.synchronization).done(function(res) {
                        this.load(res);
                        that.synchronized.enseignants = true;
                        resolve();
                    }.bind(this));
                });
            }
        });
        this.collection(Matiere, {
            sync : function () {
                return new Promise((resolve, reject) => {
                    http().getJson(that.api.MATIERE.synchronization).done(function(res) {
                        this.load(res);
                        that.synchronized.matieres = true;
                        resolve();
                    }.bind(this));
                });
            }
        });
        this.collection(Appel, {
            sync : function (pODateDebut, pODateFin) {
                return new Promise((resolve, reject) => {
                    if (pODateDebut !== undefined && pODateFin !== undefined) {
                        http().getJson('/viescolaire/presences/appels/' + moment(pODateDebut).format('YYYY-MM-DD') + '/' + moment(pODateFin).format('YYYY-MM-DD')).done(function(data) {
                            this.load(data);
                            resolve();
                        }.bind(this));
                    }
                });
            }
        });
        this.collection(Evenement, {
            sync : function (psDateDebut, psDateFin) {
                if (psDateDebut !== undefined && psDateDebut !== undefined) {
                    http().getJson('/viescolaire/presences/eleves/evenements/' + moment(psDateDebut).format('YYYY-MM-DD') + '/' + moment(psDateFin).format('YYYY-MM-DD')).done(function(data) {
                        let aLoadedData = [];
                        _.map(data, function(e) {
                            e.date = moment(e.timestamp_dt).format('YYYY-MM-DD');
                            return e;
                        });
                        let aDates = _.groupBy(data, 'cours_date');
                        for (let k in aDates) {
                            if (!aDates.hasOwnProperty(k)) { continue; }
                            let aEleves = _.groupBy(aDates[k], 'fk_eleve_id');
                            for (let e in aEleves) {
                                if (!aEleves.hasOwnProperty(e)) { continue; }
                                let t = aEleves[e];
                                let tempEleve = {
                                    id : t[0].fk_eleve_id,
                                    nom : t[0].nom,
                                    prenom : t[0].prenom,
                                    date : t[0].date,
                                    displayed : false,
                                    evenements : t
                                };
                                aLoadedData.push(tempEleve);
                            }
                        }
                        this.load(aLoadedData);
                    }.bind(this));
                }
            }
        });
        this.collection(Classe, {
            sync: function () {
                return new Promise((resolve, reject) => {
                    http().getJson(that.api.CLASSE.synchronization).done((res) => {
                        this.load(castClasses(res));
                        that.synchronized.classes = true;
                        resolve();
                    }).bind(this);
                });
            },
        });
        this.collection(Justificatif, {
            sync : function () {
                return new Promise((resolve, reject) => {
                    http().getJson(that.api.JUSTIFICATIF.synchronization).done(function(res) {
                        this.load(res);
                        resolve();
                    }.bind(this));
                });
            }
        });
    }

    async sync (): Promise<any> {
        return new Promise((resolve, reject) => {
            let isSynced = () => {
                let b =
                    this.synchronized.matieres &&
                    this.synchronized.classes &&
                    this.synchronized.enseignants;
                if (b) {
                    // On souhaite récupérer les appels à la toute fin car les libellés (Matières, Classes et Enseignants)
                    // sont utilisés par la suite dans les appels
                    this.appels.sync();
                    this.isSynchronized = true;
                    resolve();
                }
            };
            this.enseignants.sync().then(isSynced);
            this.classes.sync().then(isSynced);
            this.matieres.sync().then(isSynced);
            this.justificatifs.sync();
            this.motifs.sync();
            this.evenements.sync();
        });
    }
}