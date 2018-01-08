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
    synchronized :any[];
    coursMongo: any;
    coursPostgres: any;

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
        this.coursMongo =[];
        this.coursPostgres =[];
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
            console.log("syncCoursByStudid");
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


    static momentWithoutTime(momentWithTime){
        return moment(momentWithTime.format("YYYY-MM-DD"));
    }


    iterationAndCoursIsSame(iterationMongo, coursPostgres){

        var isSame = true;
        // Le cours est liée à l'iteration si sa date est égale à la date de l'itation (+ ou - la tolerance (15min))
        let toleranceMinute = 15;
        if(!(coursPostgres.startMoment.isBetween(
                iterationMongo.startMoment.clone().subtract(toleranceMinute, 'minute'),
                iterationMongo.startMoment.clone().add(toleranceMinute, 'minute'))
            &&
            coursPostgres.endMoment.isBetween(
                iterationMongo.endMoment.clone().subtract(toleranceMinute, 'minute'),
                iterationMongo.endMoment.clone().add(toleranceMinute, 'minute'))))
        {
            console.log("bad date");
            isSame= false
        }

        // Le prof correspond
        if(!iterationMongo.teacherIds.includes(coursPostgres.id_personnel)) {
            console.log("bad prof");
            isSame= false;
        }

        // La classe correspond
        if(!iterationMongo.classes.includes(coursPostgres.className))
        {
            console.log("bad classname");
            isSame= false;
        }

        // Check si c'est la meme matiere
        if(coursPostgres.id_matiere != iterationMongo.subjectId)
        {
            console.log("bad matiere");
            isSame = false;
        }
        console.log("### FINISH");
        return isSame;
    }

    async checkRapprochementCours(startMomentPeriod, endMomentPeriod, structure){
        this.coursPostgres = [];
        this.coursMongo = [];
        console.log("checkRapprochementCours");

        await this.syncCoursPostgres(startMomentPeriod, endMomentPeriod, structure.id);

        for(let i=0 ; i < this.className.length; i++){
            await this.syncCoursMongo(structure.id,startMomentPeriod.format("YYYY-MM-DD"),endMomentPeriod.format("YYYY-MM-DD"),
                this.className[i]);
        }

        console.log("Cours mongo : ");
        console.log(this.coursMongo);

        var iterationsCoursMongo = [];
        this.coursMongo.forEach(coursMongo => {
            coursMongo.startMoment = moment(coursMongo.startDate);
            coursMongo.endMoment = moment(coursMongo.endDate);

            let debutMomentIteration = moment(coursMongo.startMoment);

            let endMomentIteration = moment(coursMongo.startMoment.format("YYYY-MM-DD") + " " + coursMongo.endMoment.format("HH:mm"));
            while(endMomentIteration < coursMongo.endMoment){
                // Si l'iteration est en dans la période on l'ajoute.
                if ((startMomentPeriod < debutMomentIteration && endMomentIteration < endMomentPeriod) || // Est à l'intérieur de la période
                    (debutMomentIteration < startMomentPeriod && endMomentPeriod < endMomentIteration) || // Englobe la période
                    !(endMomentPeriod < debutMomentIteration || startMomentPeriod > endMomentIteration)) { // Touche la période

                    iterationsCoursMongo.push({
                        aStartMomentDate: debutMomentIteration.format("YYYY-MM-DD HH:mm"),
                        aEndMomentDate: endMomentIteration.format("YYYY-MM-DD HH:mm"),
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
        });

        var arrayCours = [];
        var numPostgresFound = 0;

        this.coursPostgres.forEach(coursPostgres => {
            coursPostgres.startMoment =  moment(coursPostgres.timestamp_dt);
            coursPostgres.endMoment =  moment(coursPostgres.timestamp_fn);
            var classe = structure.classes.all.find(classe => classe.id == coursPostgres.id_classe);
            if(classe != undefined){
                coursPostgres.className =  classe.name;
            }
        });

        console.log("iterationsCoursMongo : ");
        console.log(iterationsCoursMongo);

        console.log("Cours postgres : ");
        console.log(this.coursPostgres);

        var arrayCours = [];
        iterationsCoursMongo.forEach(iteration => {
            let coursPostgresFound = false;
            for(let i = 0; i < this.coursPostgres.length; i++){
                let coursPostgres = this.coursPostgres[i];
                if(this.iterationAndCoursIsSame(iteration, coursPostgres)){
                    coursPostgresFound = true;
                    coursPostgres.isAlreadyFound = true;
                    coursPostgres.isFromMongo = false;
                    coursPostgres.teacherIds = [coursPostgres.id_personnel];
                    coursPostgres.subjectId = coursPostgres.id_matiere;
                    coursPostgres.evenements = this.evenements.filter(ev => ev.id_cours == coursPostgres.id && ev.id_type != 1);
                    arrayCours.push(coursPostgres);
                    break;
                }
            }
            if(!coursPostgresFound)
                arrayCours.push(iteration);
        });

        // Mise en forme des parties de l'absence prev pour le calendar
        arrayCours.forEach(item => {

            item.isFutur = item.endMoment > moment();

            // On récupère le nom des enseignants
            item.teacherNames = [];
            item.teacherIds.forEach((teacher) => {
                item.teacherNames.push(_.findWhere(structure.enseignants.all, {id : teacher}))
            });
            // On récupère le nom de la matière
            item.subjectLabel = _.findWhere(structure.matieres.all, {id : item.subjectId});

            if(!item.isFromMongo && this.absences){
                item.absence = this.absences.find(absc => absc.id_cours == item.id);
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
        console.log("arrayCours : ");
        console.log(arrayCours);
        console.log("END ###################  ###    ###    ####");
        /*Algorythme de rapprochement
        - Subject
        - Prof
        - Classe
        - Heure (à 15min)
        */
    }

    async syncCoursMongo(structureId,firstDate, endDate, classeName) : Promise<any> {
        return new Promise((resolve) => {
            console.log("syncCoursMongo");
            let Url = '/directory/timetable/courses/'+structureId+'/'+firstDate+'/'+endDate+'?group=';
            http().getJson(Url+classeName).done((data) => {
                data.forEach(cours => {
                    this.coursMongo.push(cours);
                });
                resolve();
            })
        });
    }

    async syncCoursPostgres(startMoment, endMoment, idEtab){
        return new Promise((resolve) => {
            let dateDebut = startMoment.format("YYYY-MM-DD");
            let dateFin = endMoment.format("YYYY-MM-DD");
            let timeDb = startMoment.format("HH:mm");
            let timeFn = endMoment.format("HH:mm");

            // Pattern : /viescolaire/cours/:etabId/:eleveId/:dateDebut/:dateFin/time/:timeDb/:timeFn;
            let url = "/viescolaire/cours/" + idEtab + "/" + this.id + "/" + dateDebut + "/" + dateFin + "/time/" + timeDb + "/" + timeFn;

            http().getJson(url).done((res: any[]) => {
                this.coursPostgres = res;
                resolve();
            });
        });
    }

    formatCourses ( matieres: Matiere[] , enseignants : Enseignant[],periodeDateDebut,periodeDateFin) {
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

                    // On ajoute le cours seulement si il touche/est inside/englobe
                    if ((periodeDateDebut < c.startMoment && c.endMoment < periodeDateFin) || // Est à l'intérieur de la période
                        (c.startMoment < periodeDateDebut && periodeDateFin < c.endMoment) || // Englobe la période
                        !(periodeDateFin < c.startMoment || periodeDateDebut > c.endMoment)) { // Touche la période
                        arr.push(c);
                    }

                    startMoment = startMoment.add('days', 7);
                    endMoment = endMoment.add('days', 7);
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

                // On ajoute le cours seulement si il touche/est inside/englobe
                if ((periodeDateDebut < c.startMoment && c.endMoment < periodeDateFin) || // Est à l'intérieur de la période
                    (c.startMoment < periodeDateDebut && periodeDateFin < c.endMoment) || // Englobe la période
                    !(periodeDateFin < c.startMoment || periodeDateDebut > c.endMoment)) { // Touche la période
                    arr.push(c);
                }
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