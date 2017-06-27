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
import { presences } from '../absc_enseignant_mdl';
import {DefaultEleve} from "../common/DefaultEleve";


export class Eleve extends DefaultEleve implements IModel {
    evenements: Collection<Evenement>;
    courss: Collection<Cours>;
    evenementsJours: Collection<Evenement>;
    absencePrevs: Collection<AbsencePrev>;
    plages: Collection<Plage>;

    get api () {
        return {GET_EVENEMENT : '/viescolaire/presences/eleve/' + this.id + '/evenements/',
            GET_ABSENCES_PREV : '/viescolaire/absences/eleve/' + this.composer.id + '/absencesprev/'
        };
    }

    constructor () {
        super();
        this.collection(Evenement, {
            sync : (psDateDebut, psDateFin) => {
                http().getJson(this.api.GET_EVENEMENT + psDateDebut + '/' + psDateFin).done((data) => {
                    this.evenements.load(data);
                });
            }
        });
        this.collection(Cours);
        this.evenementsJours =  new Collection<Evenement>(Evenement);
        this.evenementsJours.model = this;
        this.collection(Plage, {
            sync : () => {
                // Evenements du jours
                let otEvt = this.evenementsJours;
                // Liste des cours
                let otCours = this.courss;
                let that = this.plages;
                // On copie les plages dans un tableau
                that.load(JSON.parse(JSON.stringify(presences.structure.plages)));
                for (let i = 0; i < that.all.length; i++) {
                    that.all[i].evenements = new Collection<Evenement>(Evenement);
                    that.all[i].evenements.model = that.all[i];
                    that.all[i].evenements.composer = that.all[i].evenements.model = this;
                }
                /**
                 * Pour chaque plage, on récupere le cours correspondant, puis pour la plage, on ajoute au tableau evenements
                 * la liste des evenements relatifs à la plage horaire.
                 */
                _.each(otEvt.all, (evenement) => {
                    let otCurrentCours = otCours.findWhere({id : evenement.id_cours});
                    for (let i = parseInt(moment(otCurrentCours.timestamp_dt).format('HH'));
                         i < parseInt(moment(otCurrentCours.timestamp_fn).format('HH')); i++) {

                        let otCurrentPlage = that.filter((plage) => {
                            return plage.heure === i;
                        })[0];
                        otCurrentPlage.evenements.push(evenement, false);
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
                            dt : undefined,
                            fn : undefined
                        };
                        oIndex.dt = that.indexOf(that.findWhere({heure : dt}));
                        oIndex.fn = that.indexOf(that.findWhere({heure : fn}));
                        if (oIndex.dt !== -1 && oIndex.fn !== -1) {
                            for (let i = oIndex.dt; i < oIndex.fn; i++) {
                                that.all[i].evenements.push(abs);
                            }
                        }
                    });
                }
            }
        });
        this.collection(AbsencePrev, {
            sync : (psDateDebut, psDateFin) => {
                http().getJson(this.api.GET_ABSENCES_PREV + psDateDebut + '/' + psDateFin).done((data) => {
                    this.absencePrevs.load(data);
                });
            }
        });

    }
}
