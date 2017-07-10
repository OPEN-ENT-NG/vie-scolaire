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

import { AbsencePrev } from './AbsencePrev';
import { Cours } from './Cours';
import { Evenement } from './Evenement';
import { Plage } from './Plage-old';
import { getPlage } from '../../../utils/functions/getPlages';
import { DefaultEleve } from "../common/DefaultEleve";


export class Eleve extends DefaultEleve implements IModel {
    evenements: Collection<Evenement>;
    courss: Collection<Cours>;
    absencePrevs: Collection<AbsencePrev>;
    plages: Collection<Plage>;

    apiList = {
        GET_EVENEMENT: '/viescolaire/presences/eleve/' + this.id + '/evenements/',
        GET_ABSENCES_PREV: '/viescolaire/presences/eleve/' + this.id + '/absencesprev/'
    };

    get api () {
        return this.apiList;
    }

    constructor(o?: any) {
        super(o);
        if (o && typeof o === 'object') {
            this.updateData(o);
        }
        this.collection(Evenement, {
            sync : (psDateDebut, psDateFin) => {
                return new Promise((resolve, reject) => {
                    http().getJson(this.api.GET_EVENEMENT + psDateDebut + '/' + psDateFin).done((data) => {
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
        this.collection(Cours);
        this.collection(Plage, {
            sync: () => {
                return new Promise((resolve, reject) => {
                    let otEvt = this.evenements.all;
                    let otCours = this.courss;
                    let that = this.plages;
                    // On copie les plages dans un tableau
                    that.load(getPlage());
                    /**
                     * Pour chaque plage, on récupere le cours correspondant, puis pour la plage, on ajoute au tableau evenements
                     * la liste des evenements relatifs à la plage horaire.
                     */
                    //_.filter(otEvt, (_evt) => {return _evt.id != null })
                    _.each(otEvt, (poEvt) => {
                        let otCours = this.courss.findWhere({id: poEvt.id_cours});
                        for (let i = parseInt(moment(otCours.timestamp_dt).format('HH'));
                             i < parseInt(moment(otCours.timestamp_fn).format('HH')); i++) {
                            let otPlage = this.plages.findWhere({heure: i});
                            otPlage.evenements.push(poEvt);
                        }
                    });

                    // Fusion des plages d'un même cours
                    _.each(otCours.all, (cours) => {
                        let fusion_plages_cours = [];
                        for (let i = parseInt(moment(cours.timestamp_dt).format('HH'));
                             i < parseInt(moment(cours.timestamp_fn).format('HH')); i++) {
                            let otCurrentPlage = that.filter((plage) => {
                                return plage.heure === i;
                            })[0];
                            fusion_plages_cours.push(otCurrentPlage);
                        }
                        if (fusion_plages_cours.length > 0) {
                            let width = parseFloat(fusion_plages_cours[0].style.width.split('%')[0]);
                            for (let i = 1; i < fusion_plages_cours.length; i++) {
                                fusion_plages_cours[0].duree += fusion_plages_cours[i].duree;
                                width += parseFloat(fusion_plages_cours[i].style.width.split('%')[0]);
                                fusion_plages_cours[i].style.width = '0%';
                                fusion_plages_cours[0].style.width = width.toString() + '%';
                            }
                        }
                    });

                    /**
                     * Si il y a des absences previsionnelles, on les rajoutes dans le tableau d'évènements
                     */
                    if (this.absencePrevs.all.length > 0) {
                        _.each(this.absencePrevs.all, (abs) => {
                            abs.id_type = 'abs-prev';
                            let dt = parseInt(moment(abs.timestamp_dt).format('HH'));
                            let fn = parseInt(moment(abs.timestamp_fn).format('HH'));
                            let oIndex = {
                                dt: undefined,
                                fn: undefined
                            };
                            oIndex.dt = that.indexOf(that.findWhere({heure: dt}));
                            oIndex.fn = that.indexOf(that.findWhere({heure: fn}));
                            if (oIndex.dt !== -1 && oIndex.fn !== -1) {
                                for (let i = oIndex.dt; i < oIndex.fn; i++) {
                                    that.all[i].evenements.push(abs);
                                }
                            }
                        });
                    }
                    resolve();
                });
            },
            addEvtPlage: (poEvt) => {
                let otCours = this.courss.findWhere({id: poEvt.id_cours});
                for (let i = parseInt(moment(otCours.timestamp_dt).format('HH'));
                     i < parseInt(moment(otCours.timestamp_fn).format('HH')); i++) {
                    let otPlage = this.plages.findWhere({heure: i});
                    otPlage.evenements.push(poEvt);
                }
            },
            remEvtPlage: (poEvt) => {
                let otCours = this.courss.findWhere({id: poEvt.id_cours});
                for (let i = parseInt(moment(otCours.timestamp_dt).format('HH'));
                     i < parseInt(moment(otCours.timestamp_fn).format('HH')); i++) {
                    let otPlage = this.plages.findWhere({heure: i});
                    otPlage.evenements.remove(otPlage.evenements.findWhere({
                        id: poEvt.id
                    }));
                }
            }
        });
        this.collection(AbsencePrev, {
            sync : (psDateDebut, psDateFin) => {
                return new Promise((resolve, reject) => {
                    http().getJson(this.api.GET_ABSENCES_PREV + psDateDebut + '/' + psDateFin).done((data) => {
                        this.absencePrevs.load(data);
                        resolve();
                    });
                });
            }
        });
    }

    sync(psDateDebut?, psDateFin?):Promise<any> {
        return new Promise(async (resolve, reject) => {
            if(psDateDebut && psDateFin) {
                await Promise.all([this.evenements.sync(psDateDebut, psDateFin), this.absencePrevs.sync(psDateDebut, psDateFin)]);
            }
            this.plages.sync(resolve());
        });
    }
}
