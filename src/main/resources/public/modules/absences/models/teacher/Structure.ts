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

import { FORMAT } from '../../constants/formats';
import { PLAGES } from '../../constants/plages';
import { Classe } from "../teacher/Classe";

export class Structure extends DefaultStructure {
    isSynchronized: boolean;
    synchronization: any;

    courss: Collection<Cours>;
    plages: Collection<Plage>;
    creneaus: Collection<Creneau>;
    classes: Collection<Classe>;

    get api () {
        return {
            GET_COURS :  '/viescolaire/enseignant/' + model.me.userId + '/cours/',
            CLASSE : {
                synchronization : '/viescolaire/classes?idEtablissement=' + this.id,
            }
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
            creneaux: false,
            classes: false
        };

        let that: Structure = this;

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

        this.collection(Classe, {
            sync: function () {
                return new Promise((resolve, reject) => {
                    http().getJson(that.api.CLASSE.synchronization).done((res) => {
                        this.load(castClasses(res));
                        that.synchronization.classes = true;
                        resolve();
                    }).bind(this);
                });
            },
        });

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
                    gotimestamp_dtPlage = moment(moment(gotimestamp_dtPlage).format(FORMAT.heureMinutes), FORMAT.heureMinutes);

                    // creation d'un objet moment pour la plage de fin de journée
                    let gotimestamp_fnPlage = moment();
                    gotimestamp_fnPlage.hour(PLAGES.heureFin);
                    gotimestamp_fnPlage.minute(0);
                    gotimestamp_fnPlage = moment(moment(gotimestamp_fnPlage).format(FORMAT.heureMinutes), FORMAT.heureMinutes);

                    if (this.courss !== undefined && this.courss.all.length > 0) {

                        // initialsiation heure en cours (1ère heure à placer sur les crenaux)
                        oHeureEnCours = gotimestamp_dtPlage;

                        for (let i = 0; i < this.courss.all.length; i++) {

                            let oCurrentCours = this.courss.all[i];

                            let otimestamp_dtCours = moment(moment(oCurrentCours.timestamp_dt).format(FORMAT.heureMinutes), FORMAT.heureMinutes);
                            let otimestamp_fnCours = moment(moment(oCurrentCours.timestamp_fn).format(FORMAT.heureMinutes), FORMAT.heureMinutes);

                            // si le cours est après le dernier creneau ajouté
                            if (otimestamp_dtCours.diff(oHeureEnCours) > 0) {

                                // on ajoute un crenau "vide" jusqu'au cours
                                let creneau = this.initialiazeCreneaux( oHeureEnCours.format(FORMAT.heureMinutes),
                                    otimestamp_dtCours.format(FORMAT.heureMinutes), undefined,
                                    otimestamp_dtCours.diff(oHeureEnCours, "minute"));

                                oListeCreneauxJson.push(creneau);
                                oHeureEnCours = otimestamp_dtCours;
                            }

                            // TODO tester si timestamp_fn = 18h
                            let creneau = this.initialiazeCreneaux(otimestamp_dtCours.format(FORMAT.heureMinutes),
                                otimestamp_fnCours.format(FORMAT.heureMinutes), oCurrentCours ,
                                otimestamp_fnCours.diff(otimestamp_dtCours, "minute") );

                            oListeCreneauxJson.push(creneau);
                            oHeureEnCours = otimestamp_fnCours;

                            // Lors du dernier cours parcouru, on complète par un dernier créneau vide
                            // si le cours ne se termine pas à la fin de la journée
                            if (i === (this.courss.all.length - 1)) {

                                // si le cours ne termine pas la journée
                                // on ajoute un crenau "vide" jusqu'à la fin de la journée
                                if (gotimestamp_fnPlage.diff(otimestamp_fnCours) > 0) {

                                    let creneau = this.initialiazeCreneaux(otimestamp_fnCours.format(FORMAT.heureMinutes),
                                        gotimestamp_fnPlage.format(FORMAT.heureMinutes), undefined,
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
                    && this.synchronization.plages
                    && this.synchronization.classes;
                if (b) {
                    this.isSynchronized = true;
                    resolve();
                }
            };
            let today = new Date();
            let sDateDebut = moment(today).format(FORMAT.heureMinutes);
            let sDateFin =  moment(today).add(1, 'days').format(FORMAT.heureMinutes);

            this.classes.sync().then(isSynchronized);
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