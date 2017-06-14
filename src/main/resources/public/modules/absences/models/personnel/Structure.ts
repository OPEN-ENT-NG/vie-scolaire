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

import { DefaultStructure } from '../../../viescolaire/models/common/DefaultStructure';
import { Enseignant } from "./Enseignant";
import { Eleve } from "./Eleve";
import { Appel } from "./Appel";
import { Classe } from "./Classe";
import { Justificatif } from "./Justificatif";
import { Motif } from "./Motif";
import { Evenement } from "./Evenement";
import {Matiere} from "./Matiere";
import {Observation} from "./Observation";
import {MotifAppel} from "./MotifAppel";

let moment = require('moment');

export class Structure extends DefaultStructure {

    enseignants: Collection<Enseignant>;
    eleves: Collection<Eleve>;
    matieres: Collection<Matiere>;
    appels: Collection<Appel>;
    classes: Collection<Classe>;
    justificatifs: Collection<Justificatif>;
    motifs: Collection<Motif>;
    motifAppels: Collection<MotifAppel>;
    evenements: Collection<Evenement>;
    observations: Collection<Observation>;
    synchronized: any;
    isSynchronized: boolean;
    isWidget: boolean;

    get api () {
        return  {
            CLASSE : {
                synchronization : '/viescolaire/classes?idEtablissement=' + this.id
            },
            ENSEIGNANT : {
                synchronization : '/viescolaire/evaluations/user/list?profile=Teacher&structureId=' + this.id
            },
            ELEVE : {
               synchronization : '/viescolaire/eleves?idEtablissement=' + this.id
            },
            MATIERE : {
                synchronization : '/viescolaire/matieres?idEtablissement=' + this.id
            },
            JUSTIFICATIF : {
                synchronization : '/viescolaire/presences/justificatifs?idEtablissement=' + this.id
            },
            APPEL :  {
                synchronization : '/viescolaire/presences/appels/',
                appelsNonEffectues : '/viescolaire/presences/appels/noneffectues/'
            },
            EVENEMENT : {
                synchronization : '/viescolaire/presences/eleves/evenements/',
                absencesSansMotifs : '/viescolaire/presences/sansmotifs/'
            },
            MOTIF_ABS : {
                synchronization : '/viescolaire/presences/motifs?idEtablissement=' + this.id,
                categorie : '/viescolaire/presences/motifs/categorie'
            },
            MOTIF_APPEL : {
                synchronization : '/viescolaire/presences/motifsAppel?idEtablissement=' + this.id,
                categorie : '/viescolaire/presences/motifsAppel/categorie'
            },
            OBSERVATION : {
                synchronization: '/viescolaire/presences/observations/' + moment(new Date()).format('YYYY-MM-DD') + '/' + moment(new Date()).format('YYYY-MM-DD') + '?idEtablissement=' + this.id
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
            enseignants: false,
            eleves: false
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
                http().getJson(that.api.MOTIF_ABS.synchronization  ).done(function (motifs) {
                    this.load(motifs);
                    this.map(function (motif) {
                        motif.justifiant_libelle = motif.justifiant ? lang.translate("viescolaire.utils.justifiant") : lang.translate("viescolaire.utils.nonjustifiant");
                        return motif;
                    });
                }.bind(this));
            }
        });
        this.collection(MotifAppel, {
            sync : function () {
                http().getJson(that.api.MOTIF_APPEL.synchronization + '?idEtablissement=' + this.id ).done(function (motifs) {
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

        this.collection(Eleve, {
            sync : function () {
                return new Promise((resolve, reject) => {
                    // chargement des élèves
                    http().getJson(that.api.ELEVE.synchronization).done((res) => {
                        this.load(res);
                        that.synchronized.eleves = true;
                        resolve();
                    });
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
        this.collection(Observation, {
            sync : function () {
                return new Promise((resolve, reject) => {
                    http().getJson(that.api.OBSERVATION.synchronization).done(function(res) {
                        this.load(res);
                        resolve();
                    }.bind(this));
                });
            }
        });
        this.collection(Appel, {
            sync : function (pODateDebut, pODateFin) {
                return new Promise((resolve, reject) => {
                    if (that.isWidget) {
                        http().getJson(that.api.APPEL.appelsNonEffectues  + moment(new Date()).format('YYYY-MM-DD') + '/' + moment(new Date()).format('YYYY-MM-DD') + '?idEtablissement=' + that.id).done(function(data) {
                            this.load(data);
                            resolve();
                        }.bind(this));
                    }else {
                        if (pODateDebut !== undefined && pODateFin !== undefined) {
                            http().getJson(that.api.APPEL.synchronization  + moment(pODateDebut).format('YYYY-MM-DD') + '/' + moment(pODateFin).format('YYYY-MM-DD') + '?idEtablissement=' + that.id).done(function(data) {
                                this.load(data);
                                resolve();
                            }.bind(this));
                        }
                    }

                });
            }
        });
        this.collection(Evenement, {
            sync : function (psDateDebut, psDateFin) {
                if (that.isWidget) {
                    http().getJson(that.api.EVENEMENT.absencesSansMotifs + moment(new Date()).format('YYYY-MM-DD') + '/' + moment(new Date()).format('YYYY-MM-DD') + '?idEtablissement=' + that.id).done(function (data) {
                        if (data !== undefined
                            && that.eleves.all !== undefined
                            && that.eleves.all.length > 0 ) {
                            _.map(data, (evenement) => {
                                let tempEleve = _.findWhere(that.eleves.all, {id: evenement.id_eleve});
                                evenement.eleve_nom = tempEleve.firstName;
                                evenement.eleve_prenom = tempEleve.lastName ;
                                return evenement;
                            });
                        }
                        this.load(data);
                    }.bind(this));
                } else {
                    if (psDateDebut !== undefined && psDateDebut !== undefined) {
                        http().getJson(that.api.EVENEMENT.synchronization + moment(psDateDebut).format('YYYY-MM-DD') + '/' + moment(psDateFin).format('YYYY-MM-DD') + '?idEtablissement=' + that.id).done(function (data) {
                            let aLoadedData = [];

                            // conversion date et set du nom/prénom de l'élève ainsi que le nom de la matiere
                            _.map(data, function (e) {
                                e.cours_date = moment(e.timestamp_dt).format('DD/MM/YYYY');

                                let loadedEleve = _.findWhere(that.eleves.all, {id: e.id_eleve});
                                e.firstName = loadedEleve.firstName;
                                e.lastName = loadedEleve.lastName;

                                let loadedMatiere = _.findWhere(that.matieres.all, {id: e.id_matiere});
                                if(loadedMatiere) {
                                    e.cours_matiere = loadedMatiere.name;
                                }
                                return e;
                            });

                            // regroupement par date de début d'un cours
                            let aDates = _.groupBy(data, 'cours_date');


                            // parcours des absences par date
                            for (let k in aDates) {
                                if (!aDates.hasOwnProperty(k)) {
                                    continue;
                                }

                                // regroupement des absences par élève
                                let aEleves = _.groupBy(aDates[k], 'id_eleve');

                                // parcours des élèves d'une date (évenement lié à un élève)
                                for (let e in aEleves) {
                                    if (!aEleves.hasOwnProperty(e)) {
                                        continue;
                                    }
                                    let t = aEleves[e];


                                    let tempEleve = {
                                        id: t[0].id_eleve,
                                        id_classe: t[0].id_classe,
                                        id_personnel: t[0].id_personnel,
                                        eleve_nom: t[0].lastName,
                                        eleve_prenom: t[0].firstName,
                                        cours_date: t[0].cours_date,
                                        displayed: false,
                                        evenements: t
                                    };
                                    aLoadedData.push(tempEleve);
                                }
                            }
                            this.load(aLoadedData);
                        }.bind(this));
                    }
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
                    this.synchronized.enseignants &&
                    this.synchronized.eleves;
                if (b) {
                    // On souhaite récupérer les appels à la toute fin car les libellés (Matières, Classes, Enseignants, Eleves)
                    // sont utilisés par la suite dans les appels
                    this.appels.sync();
                    this.isSynchronized = true;
                    resolve();
                }
            };
            this.enseignants.sync().then(isSynced);
            this.eleves.sync().then(() => {
                this.evenements.sync(isSynced);
            });
            this.classes.sync().then(isSynced);
            this.matieres.sync().then(isSynced);
            this.justificatifs.sync();
            this.motifs.sync();
            this.motifAppels.sync();
            this.observations.sync();
        });
    }
}