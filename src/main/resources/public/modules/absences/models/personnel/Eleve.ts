import { Collection, Model } from 'entcore';
import { Responsable } from './Responsable';
import { Eleve as SharedEleve} from "../shared/Eleve";
import { Cours } from "./Cours";
import { AbsencePrev } from "./AbsencePrev";

export class Eleve extends SharedEleve {
    responsables: Collection<Responsable>;
    abscprev : Collection<AbsencePrev>;
    cours: Collection<Cours>;

    get api() {
        return _.extend(this.apiList, {
            GET_RESPONSABLES: '/viescolaire/eleves/' + this.id + '/responsables',
            GET_ALL_ABSENCES: '/viescolaire/presences/eleve/' + this.id + '/absences/',
            GET_ALL_ABSENCES_PREV:'/viescolaire/presences/eleve/' + this.id + '/absencesprev',
            GET_EVENT_ELEVE:'/viescolaire/presences/eleve/',
            GET_Eleve_COURS:'/viescolaire/cours',
            GET_CLASSE_COURS:'/viescolaire',
            GET_ABSC_PREV:'/viescolaire/presences/eleve/' + this.id + '/absencesprev/'
        });
    }

    constructor () {
        super();
        this.collection(Responsable, {
            sync: () => {
                return new Promise((resolve, reject) => {
                    http().getJson(this.api.GET_RESPONSABLES).done((data) => {
                        this.responsables.load(data);
                        resolve();
                    });
                });
            }
        });
        this.collection(Cours);
        this.collection(AbsencePrev);

    }
    syncCoursByStud(structureId, DateD, timeDb, DateF, timeFn): Promise<any> {
        return new Promise((resolve, reject) => {
            let Url = this.api.GET_Eleve_COURS+'/'+structureId+'/'+this.id+'/'+moment(DateD).format('YYYY-MM-DD')+'/'+moment(DateF).format('YYYY-MM-DD')+'/time/'+timeDb+'/'+timeFn;

            http().getJson(Url).done((data) => {
                    if (resolve && typeof resolve === 'function') {
                        resolve();
                    }

                    this.cours = data ;
                })
                .error(function () {
                    if (reject && typeof reject === 'function') {
                        reject();
                    }
                });
        });
    }
    syncCoursByClasseStud(ClasseId, DateD,timeDb, DateF, timeFn): Promise<any> {
        return new Promise((resolve, reject) => {
            let Url = this.api.GET_CLASSE_COURS+'/'+ClasseId+'/cours/'+moment(DateD).format('YYYY-MM-DD')+'/'+moment(DateF).format('YYYY-MM-DD')+'/time/'+timeDb+'/'+timeFn;
            http().getJson(Url).done((data) => {
                if (resolve && typeof resolve === 'function') {
                    resolve();
                }

                this.cours = data ;
            })
                .error(function () {
                    if (reject && typeof reject === 'function') {
                        reject();
                    }
                });
        });
    }

    // Récupère toutes les absences de l'élève
    syncAllAbsence(isAscending): Promise<any> {
        return new Promise((resolve,reject) => {
            http().getJson(this.api.GET_ALL_ABSENCES + isAscending).done((data) => {
                this.evenements = data;
                if (resolve && typeof resolve === 'function') {
                    resolve();
                }
            })
                .error(function () {
                    if (reject && typeof reject === 'function') {
                        reject();
                    }
                });
        });
    }

    // Récupère toutes les absences de l'élève
    syncAllAbsencePrev(): Promise<any> {
        return new Promise((resolve,reject) => {
            http().getJson(this.api.GET_ALL_ABSENCES_PREV).done((data) => {
                this.abscprev = data;
                if (resolve && typeof resolve === 'function') {
                    resolve();
                }
            })
                .error(function () {
                    if (reject && typeof reject === 'function') {
                        reject();
                    }
                });
        });
    }
    // Synchronise les évènements de l'élève entre la
    syncEvenement(dateDebut, dateFin): Promise<any> {
        return new Promise((resolve,reject) => {
            http().getJson(this.api.GET_EVENT_ELEVE+this.id+'/evenements/'+moment(dateDebut).format('YYYY-MM-DD')+'/'+moment(dateFin).format('YYYY-MM-DD')).done((data) => {
                this.evenements = data;
                if (resolve && typeof resolve === 'function') {
                    resolve();
                }
            })
                .error(function () {
                    if (reject && typeof reject === 'function') {
                        reject();
                    }
                });
        });
    }

    // buguée
    syncEvenment(DateD, DateF): Promise<any> {
        return new Promise((resolve,reject) => {
            http().getJson(this.api.GET_EVENT_ELEVE+this.id+'/evenements/'+moment(DateD).format('YYYY-MM-DD')+'/'+moment(DateF).format('YYYY-MM-DD')).done((data) => {
                this.evenements.load(data); // load is not a function
                if (resolve && typeof resolve === 'function') {
                    resolve();
                }
            })
                .error(function () {
                    if (reject && typeof reject === 'function') {
                        reject();
                    }
                });
        });
    }
    syncAbscPrev(DateD, DateF): Promise<any> {
        return new Promise((resolve,reject) => {
            http().getJson(this.api.GET_ABSC_PREV+moment(DateD).format('YYYY-MM-DD')+'/'+moment(DateF).format('YYYY-MM-DD')).done((data) => {
                this.abscprev = data;
                if (resolve && typeof resolve === 'function') {
                    resolve();
                }
            })
                .error(function () {
                    if (reject && typeof reject === 'function') {
                        reject();
                    }
                });
        });
    }

    toString () {
        return this.hasOwnProperty("displayName") ? this.displayName : this.firstName+" "+this.lastName;
    }
}