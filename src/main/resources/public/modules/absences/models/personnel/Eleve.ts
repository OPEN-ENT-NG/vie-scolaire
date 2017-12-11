import { Collection, Model } from 'entcore';
import { Responsable } from './Responsable';
import { Eleve as SharedEleve} from "../shared/Eleve";
import { Cours } from "./Cours";
import { AbsencePrev } from "./AbsencePrev";
import {syncedCollection} from "../../../utils/interfaces/syncedCollection";
import {Structure} from "./Structure";
import {Matiere} from "./Matiere";
import {Enseignant} from "./Enseignant";

export class Eleve extends SharedEleve {
    responsables: Collection<Responsable>;
    abscprev : Collection<AbsencePrev>;
    cours: any ;  // Collection<Cours>; Courseleve
    className:string[];
    groupName:string[];
    classesId : string[];
    groupesId: string[];
    synchronized :any ;

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
        this.cours =[] ;// à enlever Courseleve
        this.synchronized = {
            className : false,
            groupName: false,
            cours: false
        };
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
    syncClasseGroupName(classesGroup,variable){
        if(variable == 'classe'){
            this.className =[];
            _.each(this.classesId, (externalClasseId)=>{
                let classe = _.findWhere(classesGroup, {externalId: externalClasseId});
                if(classe != undefined)
                this.className.push(classe.name)
            });
            this.synchronized.className = true;
        }else{
            this.groupName =[];
            _.each(this.groupesId, (groupId)=>{
                let group = _.findWhere(classesGroup, {id: groupId});
                if(group != undefined)
                    this.groupName.push(group.name)
            });
            this.synchronized.groupName = true;
        }
    }
    syncCoursByStudid(structureId,firstDate, endDate , classeName) : Promise<any> {
        return new Promise((resolve, reject) => {

            let Url = '/directory/timetable/courses/'+structureId+'/'+firstDate+'/'+endDate+'?group=';
            http().getJson(Url+classeName).done((data) => {
                _.forEach(data,(cour) => {
                    this.cours.push(cour) ;
                });


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
    formatCourses ( matieres: Matiere[] , enseignants : Enseignant[],dateDb,datefn) {
        const arr = [];
        this.cours.forEach((course) => {
            let numberWeek = Math.floor(moment(course.endDate).diff(course.startDate, 'days') / 7);
           course.locked = true;
            if (numberWeek > 0) {
                let startMoment = moment(course.startDate);
                let endMoment = moment(course.startDate);
                endMoment.hour(moment(course.endDate).hour()).minute(moment(course.endDate).minute());
                for (let i = 0; i < numberWeek + 1  ; i++) {
                    let c = new Cours(course, startMoment.format(), endMoment.format());
                    moment().format() > endMoment.format() ? c.color = 'red' : c.color = 'grey' ;
                    c.subjectLabel = _.findWhere(matieres, {id : course.subjectId});
                    let teacherNames = [];
                    course.teacherIds.forEach((teacher) => {
                        teacherNames.push(_.findWhere(enseignants, {id : teacher}))
                    });
                    c.enseignantName =  teacherNames;
                    if(!(moment(endMoment).hour(0).minute(0).format('YYYY-MM-DD') < moment(dateDb).hour(0).minute(0).format('YYYY-MM-DD'))){arr.push(c)};
                    startMoment = startMoment.add('days', 7);
                    endMoment = endMoment.add('days', 7);
                    if(moment(startMoment).hour(0).minute(0).format('YYYY-MM-DD') > moment(datefn).hour(0).minute(0).format('YYYY-MM-DD')){break;}
                }
            } else {
                let c = new Cours(course, moment(course.startDate).format(), moment(course.endDate).format());
                moment().format() > c.endMoment.format()  ? c.color = 'red' : c.color = 'grey' ;
                let teacherNames = [];
                course.teacherIds.forEach((teacher) => {
                    teacherNames.push(_.findWhere(enseignants, {id : teacher}))
                });
                c.enseignantName =  teacherNames;
                c.subjectLabel = _.findWhere(matieres, {id : course.subjectId});
                arr.push(c);
            }
        });
        this.cours = arr;
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