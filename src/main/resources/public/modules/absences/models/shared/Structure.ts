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

import { Collection, http, moment } from 'entcore';

import { Cours } from './Cours';
import { DefaultStructure } from '../../../viescolaire/models/common/DefaultStructure';
import { Plage } from "./Plage-old";
import { Creneau } from "./Creneau";
import { Classe } from "./Classe";

import { FORMAT } from '../../constants/formats';
import { PLAGES } from '../../constants/plages';

import { getPlage } from '../../../utils/functions/getPlages';
import {checkRapprochementCoursCommon} from '../../utils/common';

export class Structure extends DefaultStructure {
    isSynchronized: boolean;
    synchronization: any;

    courss: Collection<Cours>;
    plages: Collection<Plage>;
    creneaus: Collection<Creneau>;
    classes: Collection<Classe>;

    coursMongo: any;
    coursPostgres: any;

    protected apiList = {
        COURS: {
            syncParEnseignant: '/viescolaire/enseignant/',
            syncParClasse: '/viescolaire/',
            GET_COURS_FROM_MONGO: '/directory/timetable/courses/' + this.id,
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
        this.coursPostgres = [];
        this.coursMongo = [];
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
            sync: async (dateDebut: any, dateFin: any, idClassOrIdTeach: string, isClass: boolean, arrayClasseName: any) => {
                // Le resolve de la promesse est appelé grâce au return et le reject grâce au Throw dans le catch
                try {
                    await this.syncCoursPostgres(dateDebut, dateFin, idClassOrIdTeach, isClass);
                    await this.syncCoursMongo(dateDebut, dateFin, arrayClasseName);

                    if (!isClass) {
                        this.coursMongo = this.coursMongo.filter(c => c.teacherIds.includes(idClassOrIdTeach));
                    }

                    let arrayCours = checkRapprochementCoursCommon(moment(dateDebut), moment(dateFin), this, undefined, this.coursPostgres, this.coursMongo);

                    arrayCours.forEach(cours => {
                       cours.isFutur = cours.startMoment > moment();
                    });

                    this.courss.all = arrayCours;
                    return;
                } catch (e) {
                    throw e;
                }
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

                            let otimestamp_dtCours = moment(moment(oCurrentCours.startMoment).format(FORMAT.heureMinutes), FORMAT.heureMinutes);
                            let otimestamp_fnCours = moment(moment(oCurrentCours.endMoment).format(FORMAT.heureMinutes), FORMAT.heureMinutes);

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

    private async syncCoursPostgres(dateDebut: any, dateFin: any, idClassOrIdTeach?: string, isClass?: boolean) {
        return new Promise((resolve, reject) => {
            if (idClassOrIdTeach == null) {    idClassOrIdTeach = model.me.userId;   }
            let url = '';
            if (isClass) {
                url = this.api.COURS.syncParClasse + idClassOrIdTeach
                    + '/cours/' + dateDebut + '/' + dateFin;
            } else {
                url = this.api.COURS.syncParEnseignant + idClassOrIdTeach + '/' + this.id
                    + '/cours/' + dateDebut + '/' + dateFin;
            }
            http().getJson(url).done((res: any[]) => {
                this.coursPostgres = res;
                _.each(this.coursPostgres, (cours) => {
                    cours.classe = _.findWhere(this.classes.all, {id : cours.id_classe});
                    if (cours.classes !== null) {
                        cours.classeIds = cours.classes.split(',');
                    } else {
                        cours.classeIds = [];
                    }

                    cours.teacherIds = cours.personnels.split(',');
                });
                this.synchronization.cours = true;
                if (resolve && typeof (resolve) === 'function') {
                    resolve();
                }
            });
        });
    }

    private async syncCoursMongo(startDate, endDate, classesName): Promise<any> {
        return new Promise((resolve) => {
            if (classesName === undefined || classesName.length === 0) {
                return;
            }
            let groupParam = '';
            for (let i = 0 ; i < classesName.length; i++) {
                if ( i !== 0 ) {
                    groupParam += '&';
                }
                groupParam += 'group=' + classesName[i];
            }
            let Url = this.api.COURS.GET_COURS_FROM_MONGO + '/' + startDate + '/' + endDate + '?' + groupParam;
            http().getJson(Url).done((data) => {
                this.coursMongo = [];
                data.forEach(cours => {
                    this.coursMongo.push(cours);
                });
                resolve();
            });
        });
    }
}