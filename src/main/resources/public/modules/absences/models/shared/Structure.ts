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
import { DefaultStructure } from '../../../viescolaire/models/common/DefaultStructure';
import { Plage } from "./Plage-old";
import { Creneau } from "./Creneau";
import { Classe } from "./Classe";

import { FORMAT } from '../../constants/formats';
import { PLAGES } from '../../constants/plages';

import { getPlage } from '../../../utils/functions/getPlages'

export class Structure extends DefaultStructure {
    isSynchronized: boolean;
    synchronization: any;

    courss: Collection<Cours>;
    plages: Collection<Plage>;
    creneaus: Collection<Creneau>;
    classes: Collection<Classe>;

    protected apiList = {
        COURS: {
            syncParEnseignant: '/viescolaire/enseignant/',
            syncParClasse: '/viescolaire/',
        },
        CLASSE: {
            synchronization: '/viescolaire/classes?idEtablissement=' + this.id,
        }
    };

    get api () {
        return this.apiList;
    }

    constructor (o?: any) {
        super(o);
        if (o && typeof o === 'object') {
            this.updateData(o);
        }
        this.synchronization = {
            cours: false,
            plages: false,
            creneaux: false,
            classes: false
        };

        let initializeCreneaux = (timestamp_dt, timestamp_fn, cours, duree) : Creneau => {
            let creneau = new Creneau();

            creneau.timestamp_dt = timestamp_dt;
            creneau.timestamp_fn = timestamp_fn;
            creneau.cours = cours;
            creneau.duree = duree;
            creneau.style = {
                "height": creneau.duree + "px"
            };

            return creneau;
        };

        this.collection(Classe, {
            sync: () => {
                return new Promise((resolve, reject) => {
                    http().getJson(this.api.CLASSE.synchronization).done((res) => {
                        this.classes.load(res);
                        _.each(this.classes.all, (classe) => {
                            classe.selected = true;
                        });
                        this.synchronization.classes = true;
                        resolve();
                    });
                });
            },
        });

        this.collection(Cours, {
            sync : (dateDebut: any, dateFin: any, idClassOrTeach?: string, isClass?: boolean) => {
                return new Promise((resolve, reject) => {
                    if(idClassOrTeach == null) {    idClassOrTeach = model.me.userId;   }
                    let url = "";
                    if(isClass) {
                        url = this.api.COURS.syncParClasse + idClassOrTeach
                            + '/cours/' + dateDebut + '/' + dateFin;
                    } else {
                        url = this.api.COURS.syncParEnseignant + idClassOrTeach + '/' + this.id
                            + '/cours/' + dateDebut + '/' + dateFin;
                    }
                    http().getJson(url).done((res: any[]) => {
                        this.courss.load(res);
                        _.each(this.courss.all, (cours) => {
                            cours.classe = _.findWhere(this.classes.all, {id : cours.id_classe});
                        });
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
                    this.plages.load(getPlage());
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

                    if (this.courss.all.length > 0) {

                        // initialsiation heure en cours (1ère heure à placer sur les crenaux)
                        oHeureEnCours = gotimestamp_dtPlage;

                        for (let i = 0; i < this.courss.all.length; i++) {

                            let oCurrentCours = this.courss.all[i];

                            let otimestamp_dtCours = moment(moment(oCurrentCours.timestamp_dt).format(FORMAT.heureMinutes), FORMAT.heureMinutes);
                            let otimestamp_fnCours = moment(moment(oCurrentCours.timestamp_fn).format(FORMAT.heureMinutes), FORMAT.heureMinutes);

                            // si le cours est après le dernier creneau ajouté
                            if (otimestamp_dtCours.diff(oHeureEnCours) > 0) {

                                // on ajoute un crenau "vide" jusqu'au cours
                                let creneau = initializeCreneaux( oHeureEnCours.format(FORMAT.heureMinutes),
                                    otimestamp_dtCours.format(FORMAT.heureMinutes), undefined,
                                    otimestamp_dtCours.diff(oHeureEnCours, "minute"));

                                oListeCreneauxJson.push(creneau);
                                oHeureEnCours = otimestamp_dtCours;
                            }

                            // TODO tester si timestamp_fn = 18h
                            let creneau = initializeCreneaux(otimestamp_dtCours.format(FORMAT.heureMinutes),
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

                                    let creneau = initializeCreneaux(otimestamp_fnCours.format(FORMAT.heureMinutes),
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

    syncAppel(): Promise<any> {
        return new Promise(async (resolve, reject) => {
            await this.plages.sync();
            await this.classes.sync();
            await this.syncCours();
            resolve();
        });
    }

    syncCours(sDateDebut?, sDateFin?): Promise<any> {
        return new Promise(async (resolve, reject) => {
            if(sDateDebut == null || sDateFin == null) {
                sDateDebut = moment().format(FORMAT.date);
                sDateFin = moment().add(1, 'days').format(FORMAT.date);
            }

            await this.courss.sync(sDateDebut, sDateFin);
            await this.creneaus.sync();
            resolve();
        });
    }
}