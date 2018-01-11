import { Collection, Model } from 'entcore';
import { Responsable } from './Responsable';
import { Eleve as SharedEleve} from '../shared/Eleve';
import { Cours } from './Cours';
import { AbsencePrev } from './AbsencePrev';
import {syncedCollection} from '../../../utils/interfaces/syncedCollection';
import {Structure} from './Structure';
import {Matiere} from './Matiere';
import {Enseignant} from './Enseignant';

export class Eleve extends SharedEleve {
    responsables: Collection<Responsable>;
    abscprev: Collection<AbsencePrev>;
    cours: any ;  // Collection<Cours>; Courseleve
    className: string[];
    groupName: string[];
    classesId: string[];
    groupesId: string[];
    synchronized: any;
    coursMongo: any;
    absences: any;
    coursPostgres: any;

    get api() {
        return _.extend(this.apiList, {
            GET_RESPONSABLES: '/viescolaire/eleves/' + this.id + '/responsables',
            GET_ALL_ABSENCES: '/viescolaire/presences/eleve/' + this.id + '/absences/',
            GET_ALL_ABSENCES_PREV: '/viescolaire/presences/eleve/' + this.id + '/absencesprev',
            GET_EVENT_ELEVE: '/viescolaire/presences/eleve/',
            GET_Eleve_COURS: '/viescolaire/cours',
            GET_CLASSE_COURS: '/viescolaire',
            GET_ABSC_PREV: '/viescolaire/presences/eleve/' + this.id + '/absencesprev/'
        });
    }

    constructor () {
        super();
        this.cours = [] ; // à enlever Courseleve
        this.coursMongo = [];
        this.coursPostgres = [];
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
    syncClasseGroupName(classesGroup, variable) {
        if (variable === 'classe') {
            this.className = [];
            _.each(this.classesId, (externalClasseId) => {
                let classe = _.findWhere(classesGroup, {externalId: externalClasseId});
                if (classe !== undefined) {
                this.className.push(classe.name);
                }
            });
            this.synchronized.className = true;
        } else {
            this.groupName = [];
            _.each(this.groupesId, (groupId) => {
                let group = _.findWhere(classesGroup, {id: groupId});
                if (group !== undefined) {
                    this.groupName.push(group.name);
                }
            });
            this.synchronized.groupName = true;
        }
    }

    static momentWithoutTime(momentWithTime) {
        return moment(momentWithTime.format('YYYY-MM-DD'));
    }

    iterationAndCoursIsSame(iterationMongo, coursPostgres) {
        // Le cours est liée à l'iteration si sa date est égale à la date de l'itation (+ ou - la tolerance (15min))
        let toleranceMinute = 15;
        if (!(coursPostgres.startMoment.isBetween(
                iterationMongo.startMoment.clone().subtract(toleranceMinute, 'minute'),
                iterationMongo.startMoment.clone().add(toleranceMinute, 'minute'))
            &&
            coursPostgres.endMoment.isBetween(
                iterationMongo.endMoment.clone().subtract(toleranceMinute, 'minute'),
                iterationMongo.endMoment.clone().add(toleranceMinute, 'minute')))) {
            return false;
        }

        // Le prof correspond
        if (!_.isEqual(iterationMongo.teacherIds.sort(), coursPostgres.teacherIds.sort())) {
           return false;
        }

        // La classe correspond
        if (!_.isEqual(iterationMongo.classes.sort(), coursPostgres.classeNames.sort())) {
            return false;
        }

        // Check si c'est la meme matiere
        if (coursPostgres.id_matiere !== iterationMongo.subjectId) {
            return false;
        }

        return true;
    }

    async checkRapprochementCours(startMomentPeriod, endMomentPeriod, structure) {
        this.coursPostgres = [];
        this.coursMongo = [];

        await this.syncCoursPostgres(startMomentPeriod, endMomentPeriod, structure.id);

        for (let i = 0 ; i < this.className.length; i++) {
            await this.syncCoursMongo(structure.id, startMomentPeriod.format('YYYY-MM-DD'), endMomentPeriod.format('YYYY-MM-DD'),
                this.className[i]);
        }

        let iterationsCoursMongo = [];
        this.coursMongo.forEach(coursMongo => {
            coursMongo.startMoment = moment(coursMongo.startDate);
            coursMongo.endMoment = moment(coursMongo.endDate);

            let debutMomentIteration = moment(coursMongo.startMoment);
            let endMomentIteration = moment(coursMongo.startMoment.format('YYYY-MM-DD') + ' ' + coursMongo.endMoment.format('HH:mm'));

            if (coursMongo.dayOfWeek !== undefined) {
                if (debutMomentIteration.isoWeekday() <= coursMongo.dayOfWeek) {
                    debutMomentIteration = debutMomentIteration.isoWeekday(coursMongo.dayOfWeek);
                    endMomentIteration = moment(debutMomentIteration.format('YYYY-MM-DD') + ' ' + coursMongo.endMoment.format('HH:mm'));
                } else {
                    debutMomentIteration = debutMomentIteration.add(1, 'weeks').isoWeekday(coursMongo.dayOfWeek);
                    endMomentIteration = moment(debutMomentIteration.format('YYYY-MM-DD') + ' ' + coursMongo.endMoment.format('HH:mm'));
                }
            } else {
                // Si dayOfWeek n'existe pas, alors on se base sur la startDate
                // et on considère que le cours est ponctuel, donc sa date de fin est la même que la startDate
                // en gardant sa date de fin
                endMomentIteration = moment(debutMomentIteration.format('YYYY-MM-DD') + ' ' + endMomentIteration.format('HH:mm'));
                coursMongo.endMoment = endMomentIteration.clone();
            }

            if (debutMomentIteration < endMomentIteration) {
                while (endMomentIteration <= coursMongo.endMoment) {
                    // Si l'iteration est en dans la période on l'ajoute.
                    if ((startMomentPeriod < debutMomentIteration && endMomentIteration < endMomentPeriod) || // Est à l'intérieur de la période
                        (debutMomentIteration < startMomentPeriod && endMomentPeriod < endMomentIteration) || // Englobe la période
                        !(endMomentPeriod < debutMomentIteration || startMomentPeriod > endMomentIteration)) { // Touche la période

                        iterationsCoursMongo.push({
                            idMongo: coursMongo._id,
                            startMoment: debutMomentIteration.clone(),
                            endMoment: endMomentIteration.clone(),
                            teacherIds: coursMongo.teacherIds,
                            roomLabels: coursMongo.roomLabels,
                            classes: coursMongo.classes,
                            subjectId: coursMongo.subjectId,
                            isFromMongo: true,
                        });
                    }
                    debutMomentIteration.add(7, 'days');
                    endMomentIteration.add(7, 'days');
                }
            }
        });

        this.coursPostgres.forEach(coursPostgres => {
            coursPostgres.startMoment =  moment(coursPostgres.timestamp_dt);
            coursPostgres.endMoment =  moment(coursPostgres.timestamp_fn);
            coursPostgres.isFromMongo = false;
            coursPostgres.subjectId = coursPostgres.id_matiere;
            coursPostgres.evenements = this.evenements.filter(ev => ev.id_cours === coursPostgres.id && ev.id_type !== 1);
            coursPostgres.classeNames = structure.classes.all.filter(classe => coursPostgres.classeIds.includes(classe.id)).map(a => a.name);
        });

        let arrayCours = [];
        iterationsCoursMongo.forEach(iteration => {
            let coursPostgresFound = false;
            for (let i = 0; i < this.coursPostgres.length; i++) {
                let coursPostgres = this.coursPostgres[i];
                if (this.iterationAndCoursIsSame(iteration, coursPostgres)) {
                    coursPostgresFound = true;
                    coursPostgres.isAlreadyFound = true;
                    arrayCours.push(coursPostgres);
                    break;
                }
            }
            if (!coursPostgresFound) {
                arrayCours.push(iteration);
            }
        });

        this.coursPostgres.forEach(coursPostgres => {
            if (!coursPostgres.isAlreadyFound) {
                arrayCours.push(coursPostgres);
            }
        });
        // Mise en forme des parties de l'absence prev pour le calendar
        arrayCours.forEach(item => {

            item.isFutur = item.endMoment > moment();

            // On récupère le nom des enseignants
            item.teacherNames = [];
            item.teacherIds.forEach((teacher) => {
                item.teacherNames.push(_.findWhere(structure.enseignants.all, {id : teacher}));
            });
            // On récupère le nom de la matière
            item.subjectLabel = _.findWhere(structure.matieres.all, {id : item.subjectId});

            if (!item.isFromMongo && this.absences) {
                item.absence = this.absences.find(absc => absc.id_cours === item.id);
            }

            item.locked = true;
            item.is_periodic = false;

            item.color = moment() > item.startMoment ? 'red' : 'grey' ;

            item.startCalendarHour = item.startMoment.seconds(0).millisecond(0).toDate();
            item.startMomentDate = item.startMoment.format('DD/MM/YYYY');
            item.startMomentTime = item.startMoment.format('HH:mm');

            item.endCalendarHour = item.endMoment.seconds(0).millisecond(0).toDate();
            item.endMomentDate = item.endMoment.format('DD/MM/YYYY');
            item.endMomentTime = item.endMoment.format('HH:mm');
        });

        this.cours = arrayCours;
    }

    async syncCoursMongo(structureId, firstDate, endDate, classeName): Promise<any> {
        return new Promise((resolve) => {
            let Url = '/directory/timetable/courses/' + structureId + '/' + firstDate + '/' + endDate + '?group=';
            http().getJson(Url + classeName).done((data) => {
                data.forEach(cours => {
                    this.coursMongo.push(cours);
                });
                resolve();
            });
        });
    }

    async syncCoursPostgres(startMoment, endMoment, idEtab) {
        return new Promise((resolve) => {
            let dateDebut = startMoment.format('YYYY-MM-DD');
            let dateFin = endMoment.format('YYYY-MM-DD');
            let timeDb = startMoment.format('HH:mm');
            let timeFn = endMoment.format('HH:mm');

            // Pattern : /viescolaire/cours/:etabId/:eleveId/:dateDebut/:dateFin/time/:timeDb/:timeFn;
            let url = '/viescolaire/cours/' + idEtab + '/' + this.id + '/' + dateDebut + '/' + dateFin + '/time/' + timeDb + '/' + timeFn;

            http().getJson(url).done((res: any[]) => {
                res.forEach(cours => {
                    cours.classeIds = cours.classes.split(',');
                    cours.teacherIds = cours.personnels.split(',');
                });
                this.coursPostgres = res;
                resolve();
            });
        });
    }

    // Récupère toutes les absences de l'élève
    syncAllAbsence(isAscending): Promise<any> {
        return new Promise((resolve, reject) => {
            http().getJson(this.api.GET_ALL_ABSENCES + isAscending).done((data) => {
                this.absences = data;
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
        return new Promise((resolve, reject) => {
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
        return new Promise((resolve, reject) => {
            http().getJson(this.api.GET_EVENT_ELEVE + this.id + '/evenements/' + moment(dateDebut).format('YYYY-MM-DD') + '/' + moment(dateFin).format('YYYY-MM-DD')).done((data) => {
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

    toString () {
        return this.hasOwnProperty('displayName') ? this.displayName : this.firstName + ' ' + this.lastName;
    }
}