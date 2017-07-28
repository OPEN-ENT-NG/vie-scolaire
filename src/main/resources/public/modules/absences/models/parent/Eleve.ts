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

import { IModel, Collection, http } from 'entcore/entcore';

import { Evenement } from './Evenement';
import { DefaultEleve } from "../common/DefaultEleve";
import {Matiere} from "./Matiere";
import {Enseignant} from "./Enseignant";
import {Cours} from "./Cours";


export class Eleve extends DefaultEleve {

    evenements: Collection<Evenement>;
    matieres: Collection<Matiere>;
    enseignants: Collection<Enseignant>;
    courss: Collection<Cours>;

    get api () {
        return {
            PRESENCES : {
                evenements: '/viescolaire/presences/eleve/' + this.id + '/evenements/',
            },
            VIESCOLAIRE : {
                matieres: '/viescolaire/matieres/infos?',
                enseignants: '/viescolaire/personnels?',
                cours: '/viescolaire/cours?',
            }
        };
    }

    constructor(o?: any) {
        super(o);
        if (o && typeof o === 'object') {
            this.updateData(o);
        }

        this.collection(Evenement, {
            sync : (psDateDebut, psDateFin) => {
                return new Promise((resolve, reject) => {
                    http().getJson(this.api.PRESENCES.evenements + psDateDebut + '/' + psDateFin).done((data) => {
                        this.evenements.load(data);
                        resolve();
                    });
                });
            },
            addEvt : (poEvt) => {
                if(!_.findWhere(this.evenements.all, {id : poEvt.id})) {
                    this.evenements.push(poEvt);
                    return true;
                } else {
                    return false;
                }
            },
            remEvt : (poEvt) => {
                let poExistingEvt = _.findWhere(this.evenements.all, {id: poEvt.id});
                if (poExistingEvt) {
                    this.evenements.remove(poExistingEvt);
                }
            }
        });

        this.collection(Matiere, {
            sync: (idMatieres) => {
                return new Promise((resolve, reject) => {
                    let url = this.api.VIESCOLAIRE.matieres;
                    for (let idMatiere of idMatieres) {
                        url += "idMatiere=" + idMatiere + "&";
                    }
                    http().get(url.slice(0, -1))
                        .done((matieres) => {
                            this.matieres.load(matieres);
                            resolve();
                        });
                });
            }
        });

        this.collection(Enseignant, {
            sync: (idPersonnels) => {
                return new Promise((resolve, reject) => {
                    let url = this.api.VIESCOLAIRE.enseignants;
                    for (let idPersonnel of idPersonnels) {
                        url += "idPersonnel=" + idPersonnel + "&";
                    }
                    http().get(url.slice(0, -1))
                        .done((peronnels) => {
                            this.enseignants.load(peronnels);
                            resolve();
                        });
                });
            }
        });

        this.collection(Cours, {
            sync: (idCours) => {
                return new Promise((resolve, reject) => {
                    let url = this.api.VIESCOLAIRE.cours;
                    for (let _idCours of idCours) {
                        url += "idCours=" + _idCours + "&";
                    }
                    http().get(url.slice(0, -1))
                        .done((cours) => {
                            this.courss.load(cours);
                            resolve();
                        });
                });
            }
        });
    }

    syncEvents = () => {
        return new Promise(async (resolve, reject) => {
            let dateDebut = moment().day(moment.localeData().firstDayOfWeek());
            let dateFin = dateDebut.clone().day(7);
            await this.evenements.sync(dateDebut.format('YYYY-MM-DD'), dateFin.format('YYYY-MM-DD'));
            if(_.isEmpty(this.evenements.all)) {
                resolve();
                return;
            }
            let idCours = _.uniq(_.pluck(this.evenements.all, "id_cours"));
            this.courss.sync(idCours).then(async () => {
                let idEnseignants = _.uniq(_.pluck(this.courss.all, "id_personnel"));
                let idMatieres = _.uniq(_.pluck(this.courss.all, "id_matiere"));
                Promise.all([this.enseignants.sync(idEnseignants), this.matieres.sync(idMatieres)])
                    .then(() => {
                        _.each(this.evenements.all, (_evt) => {
                            _evt.cours = _.findWhere(this.courss.all, {id: _evt.id_cours});
                            _evt.matiere = _.findWhere(this.matieres.all, {id: _evt.cours.id_matiere});
                            _evt.enseignant = _.findWhere(this.enseignants.all, {id: _evt.cours.id_personnel});
                        });
                        resolve();
                    });
            });
        });
    }
}