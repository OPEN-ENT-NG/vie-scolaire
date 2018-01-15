import { Collection, Model } from "entcore/entcore";
import { Responsable } from "./Responsable";
import { Eleve as SharedEleve} from "../shared/Eleve";
import { Cours } from "./Cours";
import { AbsencePrev } from "./AbsencePrev";
import {checkRapprochementCoursCommon} from "../../utils/common";

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
    structureId = String;

    get api() {
        return _.extend(this.apiList, {
            GET_RESPONSABLES: "/viescolaire/eleves/" + this.id + "/responsables",
            GET_ALL_ABSENCES: "/viescolaire/presences/eleve/" + this.id + "/absences/",
            GET_ALL_ABSENCES_PREV: "/viescolaire/presences/eleve/" + this.id + "/absencesprev",
            GET_EVENT_ELEVE: "/viescolaire/presences/eleve/",
            GET_Eleve_COURS: "/viescolaire/cours",
            GET_CLASSE_COURS: "/viescolaire",
            GET_ABSC_PREV: "/viescolaire/presences/eleve/" + this.id + "/absencesprev/",
            GET_COURS_FROM_MONGO: "/directory/timetable/courses/" + this.structureId,
            GET_COURS_FROM_SQL: "/viescolaire/cours/" + this.structureId + "/" + this.id
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
        if (variable === "classe") {
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
        return moment(momentWithTime.format("YYYY-MM-DD"));
    }

    async checkRapprochementCours(startMomentPeriod, endMomentPeriod, structure) {
        this.coursPostgres = [];
        this.coursMongo = [];

        await this.syncCoursPostgres(startMomentPeriod, endMomentPeriod);

        await this.syncCoursMongo(startMomentPeriod.format("YYYY-MM-DD"), endMomentPeriod.format("YYYY-MM-DD"), this.className);


        // On récupère les cours après rapprochement (Mongo/PostgreSQL)
        let arrayCours = checkRapprochementCoursCommon(startMomentPeriod, endMomentPeriod, structure, this.evenements, this.coursPostgres, this.coursMongo);

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

            item.color = moment() > item.startMoment ? "red" : "grey" ;

            item.startCalendarHour = item.startMoment.seconds(0).millisecond(0).toDate();
            item.startMomentDate = item.startMoment.format("DD/MM/YYYY");
            item.startMomentTime = item.startMoment.format("HH:mm");

            item.endCalendarHour = item.endMoment.seconds(0).millisecond(0).toDate();
            item.endMomentDate = item.endMoment.format("DD/MM/YYYY");
            item.endMomentTime = item.endMoment.format("HH:mm");
        });

        this.cours = arrayCours;
    }

    async syncCoursMongo(firstDate, endDate, classesName): Promise<any> {
        return new Promise((resolve) => {
            let groupParam = "";
            for (let i = 0 ; i < classesName.length; i++) {
                if ( i !== 0 ) {
                    groupParam += "&";
                }
                groupParam += "group=" + classesName[i];
            }
            let Url = this.api.GET_COURS_FROM_MONGO + "/" + firstDate + "/" + endDate + "?" + groupParam;
            http().getJson(Url).done((data) => {
                data.forEach(cours => {
                    this.coursMongo.push(cours);
                });
                resolve();
            });
        });
    }

    async syncCoursPostgres(startMoment, endMoment) {
        return new Promise((resolve) => {
            let dateDebut = startMoment.format("YYYY-MM-DD");
            let dateFin = endMoment.format("YYYY-MM-DD");
            let timeDb = startMoment.format("HH:mm");
            let timeFn = endMoment.format("HH:mm");

            // Pattern : /viescolaire/cours/:etabId/:eleveId/:dateDebut/:dateFin/time/:timeDb/:timeFn;
            let url = this.api.GET_COURS_FROM_SQL + "/" + dateDebut + "/" + dateFin + "/time/" + timeDb + "/" + timeFn;

            http().getJson(url).done((res: any[]) => {
                res.forEach(cours => {
                    cours.classeIds = cours.classes.split(",");
                    cours.teacherIds = cours.personnels.split(",");
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
                if (resolve && typeof resolve === "function") {
                    resolve();
                }
            })
                .error(function () {
                    if (reject && typeof reject === "function") {
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
                if (resolve && typeof resolve === "function") {
                    resolve();
                }
            })
                .error(function () {
                    if (reject && typeof reject === "function") {
                        reject();
                    }
                });
        });
    }
    // Synchronise les évènements de l'élève entre la
    syncEvenement(dateDebut, dateFin): Promise<any> {
        return new Promise((resolve, reject) => {
            http().getJson(this.api.GET_EVENT_ELEVE + this.id + "/evenements/" + moment(dateDebut).format("YYYY-MM-DD") + "/" + moment(dateFin).format("YYYY-MM-DD")).done((data) => {
                this.evenements = data;
                if (resolve && typeof resolve === "function") {
                    resolve();
                }
            })
                .error(function () {
                    if (reject && typeof reject === "function") {
                        reject();
                    }
                });
        });
    }

    toString () {
        return this.hasOwnProperty("displayName") ? this.displayName : this.firstName + " " + this.lastName;
    }
}