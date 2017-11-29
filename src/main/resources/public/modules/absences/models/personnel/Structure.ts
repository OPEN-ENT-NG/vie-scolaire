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

import { Structure as SharedStructure } from '../shared/Structure';
import { Enseignant } from "./Enseignant";
import { Eleve } from "./Eleve";
import { Appel } from "./Appel";
import { Justificatif } from "./Justificatif";
import { Motif } from "./Motif";
import { Evenement } from "./Evenement";
import {Matiere} from "./Matiere";
import {Observation} from "./Observation";
import {MotifAppel} from "./MotifAppel";
import {FORMAT} from "../../constants/formats";
import {Declaration} from "./Declaration";

let moment = require('moment');

export class Structure extends SharedStructure {

    enseignants: Collection<Enseignant>;
    eleves: Collection<Eleve>;
    matieres: Collection<Matiere>;
    appels: Collection<Appel>;
    justificatifs: Collection<Justificatif>;
    motifs: Collection<Motif>;
    motifAppels: Collection<MotifAppel>;
    evenements: Collection<Evenement>;
    observations: Collection<Observation>;
    declarations: Collection<Declaration>;
    synchronized: any;
    isWidget: boolean;

    pODateDebut: any;
    pODateFin: any;

    get api () {
        return _.extend(this.apiList, {
            ENSEIGNANT : {
                synchronization : '/competences/user/list?profile=Teacher&structureId=' + this.id
            },
            ELEVE : {
                synchronization : '/viescolaire/etab/eleves/' + this.id
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
                categorie : '/viescolaire/presences/motifs/categorie?idEtablissement=' + this.id,
            },
            MOTIF_APPEL : {
                synchronization : '/viescolaire/presences/motifs/appel?idEtablissement=' + this.id,
                categorie : '/viescolaire/presences/motifs/appel/categorie?idEtablissement=' + this.id,
            },
            OBSERVATION : {
                synchronization: '/viescolaire/presences/observations/'+ moment(new Date()).format('YYYY-MM-DD') + '/' + moment(new Date()).format('YYYY-MM-DD') + '?idEtablissement=' + this.id
            },
            DECLARATION : {
                synchronization: '/viescolaire/presences/declarations?',
                infosParent : '/viescolaire/users?',
                infosEleve : '/viescolaire/eleves?'
            }
        });
    }
    constructor (o?: any) {
        super(o);
        if (o && typeof o === 'object') {
            this.updateData(o);
        }

        // Constantes utiles pour la synchro des appels
        this.synchronized = {
            matieres : false,
            enseignants: false,
            eleves: false
        };

        this.collection(Motif, {
            sync : () => {
                return new Promise((resolve, reject) => {
                    http().getJson(this.api.MOTIF_ABS.synchronization).done((motifs) => {
                        this.motifs.load(motifs);
                        this.motifs.map(function (motif) {
                            motif.justifiant_libelle = motif.justifiant ? lang.translate("viescolaire.utils.justifiant") : lang.translate("viescolaire.utils.nonjustifiant");
                            return motif;
                        });
                        resolve();
                    });
                });
            }
        });

        this.collection(MotifAppel, {
            sync : () => {
                return new Promise((resolve, reject) => {
                    http().getJson(this.api.MOTIF_APPEL.synchronization).done((motifs) => {
                        this.motifAppels.load(motifs);
                        this.motifAppels.map(function (motif) {
                            motif.justifiant_libelle = motif.justifiant ? lang.translate("viescolaire.utils.justifiant") : lang.translate("viescolaire.utils.nonjustifiant");
                            return motif;
                        });
                        resolve();
                    });
                });
            }
        });

        this.collection(Enseignant, {
            sync : () => {
                return new Promise((resolve, reject) => {
                    http().getJson(this.api.ENSEIGNANT.synchronization).done((res) => {
                        this.enseignants.load(res);
                        _.each(this.enseignants.all, (enseignant) => {
                            enseignant.selected = true;
                        });
                        this.synchronized.enseignants = true;
                        resolve();
                    });
                });
            }
        });

        this.collection(Eleve, {
            sync : () => {
                return new Promise((resolve, reject) => {
                    // chargement des élèves
                    http().getJson(this.api.ELEVE.synchronization).done(async (res) => {
                        this.eleves.load(res);
                        this.synchronized.eleves = true;
                        await this.evenements.sync(this.pODateDebut, this.pODateFin);
                        resolve();
                    });
                });
            }
        });

        this.collection(Matiere, {
            sync : () => {
                return new Promise((resolve, reject) => {
                    http().getJson(this.api.MATIERE.synchronization).done((res) => {
                        this.matieres.load(res);
                        this.synchronized.matieres = true;
                        resolve();
                    });
                });
            }
        });

        this.collection(Observation, {
            sync : () => {
                return new Promise((resolve, reject) => {
                    http().getJson(this.api.OBSERVATION.synchronization).done((res) => {
                        this.observations.load(res);
                        resolve();
                    });
                });
            }
        });
        this.collection(Declaration, {
            sync : (nb?) => {
                let addInfoEleve = (_tabEleves) : Promise<any> => {
                    return new Promise((resolve, reject) => {
                        let url = this.api.DECLARATION.infosEleve;
                        _.each(_tabEleves, (idEleve) => {
                            url += "idUser=" + idEleve + "&";
                        });
                        url = url.slice(0, url.length - 1);
                        http().getJson(url).done((res) => {
                            _.each(this.declarations.all, (declaration) => {
                                let _eleve = _.findWhere(res, {idEleve: declaration.id_eleve});
                                declaration.nom_eleve = _eleve.firstName + " " + _eleve.lastName;
                                declaration.classe = _eleve.classeName;
                            });
                            resolve();
                        });
                    });
                };
                let addInfoOwner = (_tabParents) : Promise<any> => {
                    return new Promise((resolve,reject) => {
                        let url = this.api.DECLARATION.infosParent;
                        _.each(_tabParents, (idParent) => {
                            url += "idUser=" + idParent + "&";
                        });
                        url = url.slice(0, url.length - 1);
                        http().getJson(url).done((res) => {
                            _.each(this.declarations.all, (declaration) => {
                                declaration.nom_parent = _.findWhere(res, {id: declaration.owner}).displayName;
                            });
                            resolve();
                        });
                    });
                };
                let addInfoDeclaration = (resolve) => {
                    let _tabEleves = [];
                    let _tabParents = [];
                    _.each(this.declarations.all, (declaration) => {
                        _tabEleves.push(declaration.id_eleve);
                        _tabParents.push(declaration.owner);
                    });
                    _tabEleves = _.uniq(_tabEleves);
                    _tabParents = _.uniq(_tabParents);
                    Promise.all([addInfoEleve(_tabEleves), addInfoOwner(_tabParents)]).then(resolve);
                };
                return new Promise((resolve, reject) => {
                    let url = this.api.DECLARATION.synchronization + '&idEtablissement=' + this.id + '&etat=false';
                    if (nb) {
                        url += '&number=10';
                    }
                    http().getJson(url).done((res) => {
                        this.declarations.load(res);
                        addInfoDeclaration(resolve());
                    });
                });
            }
        });

        this.collection(Appel, {
            sync : (pODateDebut, pODateFin) => {

                let addInfoAppel = () => {
                    _.each(this.appels.all, (appel) => {
                        let enseignant = this.enseignants.findWhere({id: appel.id_personnel});
                        appel.personnel = enseignant != null ? enseignant.firstName + " " + enseignant.lastName : "";
                        let classe = this.classes.findWhere({id: appel.id_classe});
                        appel.classe_libelle = classe != null ? classe.name : "";
                        let matiere = this.matieres.findWhere({id: appel.id_matiere});
                        appel.cours_matiere = matiere != null ? matiere.name : "";
                    });
                };

                return new Promise((resolve, reject) => {
                    if (this.isWidget) {
                        http().getJson(this.api.APPEL.appelsNonEffectues  + moment().format('YYYY-MM-DD') + '/' + moment().format('YYYY-MM-DD') + '?idEtablissement=' + this.id).done((data) => {
                            this.appels.load(data);
                            addInfoAppel();
                            resolve();
                        });
                    }else {
                        if (pODateDebut !== undefined && pODateFin !== undefined) {
                            http().getJson(this.api.APPEL.synchronization  + moment(pODateDebut).format('YYYY-MM-DD') + '/' + moment(pODateFin).format('YYYY-MM-DD') + '?idEtablissement=' + this.id).done((data) => {
                                this.appels.load(data);
                                addInfoAppel();
                                resolve();
                            });
                        }
                        else {
                            resolve();
                        }
                    }
                });
            }
        });

        this.collection(Evenement, {
            sync : (psDateDebut?, psDateFin?) : Promise<any> => {
                return new Promise((resolve, reject) => {
                    if (this.isWidget) {
                        http().getJson(this.api.EVENEMENT.absencesSansMotifs + moment().format('YYYY-MM-DD') + '/' + moment().format('YYYY-MM-DD') + '?idEtablissement=' + this.id).done((data) => {
                            if (data !== undefined
                                && this.eleves.all !== undefined
                                && this.eleves.all.length > 0) {
                                _.each(data, (evenement) => {
                                    let tempEleve = _.findWhere(this.eleves.all, {id: evenement.id_eleve});
                                    evenement.eleve_prenom = tempEleve.firstName;
                                    evenement.eleve_nom = tempEleve.lastName;
                                });
                            }
                            this.evenements.load(data);
                            this.trigger('sync');
                            resolve();
                        });
                    } else {
                        if (psDateDebut !== undefined && psDateFin !== undefined && moment(psDateDebut)._isValid && moment(psDateFin)._isValid) {
                            http().getJson(this.api.EVENEMENT.synchronization + moment(psDateDebut).format('YYYY-MM-DD') + '/' + moment(psDateFin).format('YYYY-MM-DD') + '?idEtablissement=' + this.id).done((data) => {
                                this.evenements.load(data);
                                this.trigger('sync');
                                resolve();
                            });
                        } else {
                            resolve();
                        }
                    }
                });
            }
        });

        this.collection(Justificatif, {
            sync : () => {
                return new Promise((resolve, reject) => {
                    http().getJson(this.api.JUSTIFICATIF.synchronization).done((res) => {
                        this.justificatifs.load(res);
                        resolve();
                    });
                });
            }
        });
    }

    sync (): Promise<any> {
        return new Promise(async (resolve, reject) => {
            await Promise.all([this.enseignants.sync(), this.classes.sync(), this.eleves.sync(), this.matieres.sync(),
                this.justificatifs.sync(), this.motifs.sync(), this.motifAppels.sync(), this.observations.sync(), this.declarations.sync(10)]);
            await this.evenements.sync(this.pODateDebut, this.pODateFin);
            await this.appels.sync();
            this.trigger('synchronized');
            resolve();
        });
    };
}