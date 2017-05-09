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
import { Plage } from "./Plage-old";
import { Creneau } from "./Creneau";

import { HEURE } from '../constants/heures';
import { PLAGES } from '../constants/plages';

export class Structure extends DefaultStructure {
    isSynchronized: boolean;
    synchronization: any;

    courss: Collection<Cours>;
    plages: Collection<Plage>;
    creneaus: Collection<Creneau>;

    get api () {
        return {
            GET_COURS :  '/viescolaire/enseignant/' + model.me.userId + '/cours/',
        };
    }

    constructor (o?: any) {
        super();
        if (o && typeof o === 'object') {
            this.updateData(o);
        }
        this.synchronization = {
            cours: false,
            plages: false,
            creneaux: false
        };
        this.collection(Cours, {
            sync : (dateDebut: any, dateFin: any) => {
                return new Promise((resolve, reject) => {
                    http().getJson(this.api.GET_COURS + dateDebut + '/' + dateFin).done((res: any[]) => {
                        this.courss.load(res);
                        this.synchronization.cours = true;
                        if (resolve && typeof (resolve) === 'function') {
                            resolve();
                        }
                    });
                    // TODO Récupérer les cours de la structure sur l'api timetable
                });
            }
        });


        this.collection(Plage, {
            sync : (): Promise<any> => {
                return new Promise((resolve, reject) => {
                    let oListePlages = [];
                    for (let heure = PLAGES.heureDebut; heure <= PLAGES.heureFin; heure++) {
                        let oPlage = new Plage();
                        oPlage.heure = heure;
                        if (heure === PLAGES.heureFin) {
                            oPlage.duree = 0; // derniere heure
                        } else {
                            oPlage.duree = 60; // 60 minutes à rendre configurable ?
                        }
                        oPlage.style = {
                            "width": (1 / (PLAGES.heureFin - PLAGES.heureDebut + 1) ) * 100 + "%"
                        };
                        oListePlages.push(oPlage);
                    }
                    this.plages.load(oListePlages);
                    this.synchronization.plages = true;
                    if (resolve && typeof (resolve) === 'function') {
                        resolve();
                    }
                });
            }
        });

        this.collection(Creneau, {
            sync : () : Promise<any> => {
                return new Promise((resolve, reject) => {
                    let oListeCreneauxJson = [];
                    let oHeureEnCours;

                    // creation d'un objet moment pour la plage du debut de la journée
                    let gotimestamp_dtPlage = moment();
                    gotimestamp_dtPlage.hour(PLAGES.heureDebut);
                    gotimestamp_dtPlage.minute(0);
                    gotimestamp_dtPlage = moment(moment(gotimestamp_dtPlage).format(HEURE.format), HEURE.format);

                    // creation d'un objet moment pour la plage de fin de journée
                    let gotimestamp_fnPlage = moment();
                    gotimestamp_fnPlage.hour(PLAGES.heureFin);
                    gotimestamp_fnPlage.minute(0);
                    gotimestamp_fnPlage = moment(moment(gotimestamp_fnPlage).format(HEURE.format), HEURE.format);

                    if (this.courss !== undefined && this.courss.all.length > 0) {

                        // initialsiation heure en cours (1ère heure à placer sur les crenaux)
                        oHeureEnCours = gotimestamp_dtPlage;

                        for (let i = 0; i < this.courss.all.length; i++) {

                            let oCurrentCours = this.courss.all[i];

                            let otimestamp_dtCours = moment(moment(oCurrentCours.timestamp_dt).format(HEURE.format), HEURE.format);
                            let otimestamp_fnCours = moment(moment(oCurrentCours.timestamp_fn).format(HEURE.format), HEURE.format);

                            // si le cours est après le dernier creneau ajouté
                            if (otimestamp_dtCours.diff(oHeureEnCours) > 0) {

                                // on ajoute un crenau "vide" jusqu'au cours
                                let creneau = new Creneau();
                                creneau.timestamp_dt = oHeureEnCours.format(HEURE.format);
                                creneau.timestamp_fn = otimestamp_dtCours.format(HEURE.format);
                                creneau.cours = undefined;
                                creneau.duree = otimestamp_dtCours.diff(oHeureEnCours, "minute");
                                creneau.style = {
                                    "height": creneau.duree + "px"
                                };
                                oListeCreneauxJson.push(creneau);
                                oHeureEnCours = otimestamp_dtCours;
                            }

                            // TODO tester si timestamp_fn = 18h
                            let creneau = new Creneau();
                            this.initialiazeCreneaux(otimestamp_dtCours.format(HEURE.format),
                                otimestamp_fnCours.format(HEURE.format), oCurrentCours ,
                                otimestamp_fnCours.diff(otimestamp_dtCours, "minute") );

                            oListeCreneauxJson.push(creneau);
                            oHeureEnCours = otimestamp_fnCours;

                            // Lors du dernier cours parcouru, on complète par un dernier créneau vide
                            // si le cours ne se termine pas à la fin de la journée
                            if (i === (this.courss.all.length - 1)) {

                                // si le cours ne termine pas la journée
                                // on ajoute un crenau "vide" jusqu'à la fin de la journée
                                if (gotimestamp_fnPlage.diff(otimestamp_fnCours) > 0) {

                                    let creneau = this.initialiazeCreneaux(otimestamp_fnCours.format(HEURE.format),
                                        gotimestamp_fnPlage.format(HEURE.format), undefined,
                                        gotimestamp_fnPlage.diff(otimestamp_fnCours, "minute"));
                                    oListeCreneauxJson.push(creneau);
                                }
                            }
                        }
                    }

                    this.creneaus.load(oListeCreneauxJson);
                    this.synchronization.creneaux = true;
                    if (resolve && typeof (resolve) === 'function') {
                        resolve();
                    }
                });
            }
        });
    }

    sync (): Promise<any> {
        return new Promise((resolve, reject) => {
            let isSynchronized = () => {
                let b: boolean = this.synchronization.cours
                    && this.synchronization.creneaux
                    && this.synchronization.plages;
                if (b) {
                    this.isSynchronized = true;
                    resolve();
                }
            };
            let today = new Date();
            let sDateDebut = moment(today).format(HEURE.format);
            let sDateFin =  moment(today).add(1, 'days').format(HEURE.format);

            this.plages.sync().then(isSynchronized);
            this.courss.sync(sDateDebut, sDateFin).then(() => {
                this.creneaus.sync().then(isSynchronized);
            });
        });
    }

    initialiazeCreneaux (timestamp_dt, timestamp_fn, cours, duree ): Creneau  {
        let creneau = new Creneau();

        creneau.timestamp_dt = timestamp_dt;
        creneau.timestamp_fn = timestamp_fn;
        creneau.cours = cours;
        creneau.duree = duree;
        creneau.style = {
        "height": creneau.duree + "px"};

        return creneau;
    }
}