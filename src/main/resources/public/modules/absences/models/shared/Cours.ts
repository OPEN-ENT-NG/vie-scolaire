import { http, IModel, notify } from 'entcore/entcore';
import { Appel } from './Appel';
import { DefaultCours } from '../common/DefaultCours';
import { Evenement } from './Evenement';
import { Classe } from './Classe';
import { FORMAT } from '../../constants/formats';

export class Cours extends DefaultCours implements IModel {
    appel: Appel;
    id_appel: number;
    roomLabels: string[];
    classe: Classe;
    synchronized: any;
    startMoment: any;
    endMoment: any;
    teacherNames: string[];
    teacherIds: string[];
    classeNames: string[];
    classeIds: string[];
    absence: any;

    structureId: string;
    classes: Classe[];
    groups: string[];

    subjectId: string;
    subjectLabel: string;

    evenements: any;
    isFromMongo: boolean;
    _id: string;
    dayOfWeek: number;

    isAlreadyFound: boolean;
    isFutur: boolean;

    public get api () {
        return {
            GET_APPEL : '/viescolaire/presences/appel/cours/',
            GET_EVENEMENT_ELEVE : '/viescolaire/presences/evenement/classe/',
            GET_COURS_CLASSE : '/viescolaire/',
            GET_ABSENCE_PREV : '/viescolaire/presences/absencesprev/eleves',
            GET_LAST_COURS : '/viescolaire/presences/precedentes/cours/'
        };
    }

    constructor(o?: any) {
        super(o);
        if (o && typeof o === 'object') {
            this.updateData(o);
        }
        this.classes = [];
        this.synchronized = {
            appel: false,
            events: false,
            abscPrev: false,
            abscLastCours: false,
            coursClasse: false
        };
        this.appel = new Appel();
        this.appel.sync = (): Promise<any> => {
            return new Promise((resolve, reject) => {
                http().getJson(this.api.GET_APPEL + this.id).done((data) => {
                    if (data.length > 0) {
                        this.appel.updateData(data[0]);
                    }
                    if (this.appel.id === undefined) {
                        this.appel.id_personnel = this.teacherIds[0];
                        this.appel.id_cours = this.id;
                        this.appel.id_etat = 1;
                        this.appel.create();
                    }
                    this.synchronized.appel = true;
                    resolve();
                });
            });
        };
    }


    static createCoursPostgres(coursMongo: Cours): Promise<any> {
        return new Promise((resolve, reject) => {
            let url = '/viescolaire/cours';
            let resource = {
                idEtablissement: coursMongo.structureId, // string
                idMatiere: coursMongo.subjectId, // string
                dateDebut: coursMongo.startMoment.format('YYYY-MM-DD HH:mm'), // format YYYY-MM-DD HH:mm:ss
                dateFin: coursMongo.endMoment.format('YYYY-MM-DD HH:mm'), // format YYYY-MM-DD HH:mm:ss
                classeIds: coursMongo.classeIds, // Tableau d'id string
                teacherIds: coursMongo.teacherIds // Tableau d'id string
            };

            http().postJson(url, resource)
                .done((data) => {
                    if (resolve && typeof resolve === 'function') {
                        resolve();
                    }
                })
                .error(function () {
                    if (reject && typeof reject === 'function') {
                        notify.error('Problème lors de la création du cours');
                        reject();
                    }
                });
        });
    }

    sync(isTeacher?: string): Promise<any> {
        return new Promise(async (resolve, reject) => {

            await this.appel.sync();
            for (let i = 0; i < this.classes.length; i++) {
                await this.classes[i].sync();
            }

            await this.loadEvenements();

            await this.loadAbscPrev();

            await this.loadAbscLastCours(isTeacher);

            await this.loadCoursClasse();

            resolve();
        });
    }

    loadEvenements = (): Promise<any> => {
        return new Promise((resolve, reject) => {

            let debut = moment(this.timestamp_dt).format(FORMAT.date);
            let fin = moment(this.timestamp_fn).format(FORMAT.date);

            this.classes.forEach(currentClass => {
                // Construction de l' API de récupération des évènements d'un élève
                let url_evenement_eleve = this.api.GET_EVENEMENT_ELEVE;
                url_evenement_eleve += currentClass.id + '/periode/';
                url_evenement_eleve += debut + '/';
                url_evenement_eleve += fin;

                /**
                 * On recupere tous les évènements de l'élève de la journée, quelque soit le cours puis on la disperse en
                 * 2 listes :
                 * - evenementsJours qui nous permettera d'afficher l'historique.
                 * - evenements qui va nous permettre de gérer les évènements de l'appel en cours.
                 */
                http().getJson(url_evenement_eleve).done((data) => {
                    for (let i = 0; i < currentClass.eleves.all.length; i++) {
                        currentClass.eleves.all[i].evenements.load(_.where(data, {id_eleve: currentClass.eleves.all[i].id}));
                    }
                    this.synchronized.events = true;
                    resolve();
                });
            });
        });
    }

    loadAbscPrev = (): Promise<any> => {
        return new Promise((resolve, reject) => {

            let debut = moment(this.timestamp_dt).format(FORMAT.date);
            let fin = moment(this.timestamp_fn).format(FORMAT.date);

            this.classes.forEach(currentClass => {
                let url_absence_prev = this.api.GET_ABSENCE_PREV;
                url_absence_prev += '?dateDebut=' + debut;
                url_absence_prev += '&dateFin=' + fin;

                if (currentClass !== undefined) {
                    currentClass.eleves.all.forEach(eleve => {
                        url_absence_prev += '&id_eleve=' + eleve.id;
                    });
                }
                http().getJson(url_absence_prev).done((data) => {
                    for (let i = 0; i < currentClass.eleves.all.length; i++) {
                        currentClass.eleves.all[i].absencePrevs.load(_.where(data, {id_eleve: currentClass.eleves.all[i].id}));
                        let evtsAbsPrev = _.where(data, {id_eleve: currentClass.eleves.all[i].id});
                        for (let j = 0; j < evtsAbsPrev; j++) {
                            currentClass.eleves.all[i].evenements.all.push(<Evenement> evtsAbsPrev[j]);
                        }
                    }
                    this.synchronized.abscPrev = true;
                    resolve();
                });

            });
        });
    }

    loadAbscLastCours = (isTeacher?): Promise<any> => {
        return new Promise((resolve, reject) => {

            this.classes.forEach(currentClass => {
                // Construction de l'API de récupération des absences au derniers Cours
                let url = this.api.GET_LAST_COURS + this.id;
                url += isTeacher ? '/true' : '/false';

                http().getJson(url).done((data) => {
                    _.each(data, (absc) => {
                        let eleve = _.findWhere(currentClass.eleves.all, {id : absc.id_eleve});
                        if (eleve !== undefined) {
                            eleve.absc_precedent_cours = true;
                        }
                    });
                    this.synchronized.abscLastCours = true;
                    resolve();
                });

            });
        });
    }

    loadCoursClasse = (): Promise<any> => {
        return new Promise((resolve, reject) => {

            let debut = moment(this.timestamp_dt).format(FORMAT.date);
            let fin = moment(this.timestamp_fn).format(FORMAT.date);

            this.classes.forEach(currentClass => {

            // Construction de l'API de récupération des cours d'une classe
            let url_cours_classe = '/viescolaire/' + currentClass.id + '/cours/';
            url_cours_classe += debut + '/';
            url_cours_classe += fin;

                http().getJson(url_cours_classe).done((data) => {
                    _.each(currentClass.eleves.all, function(eleve) {
                        eleve.courss.load(data);
                        eleve.plages.sync();
                    });
                    this.synchronized.coursClasse = true;
                    resolve();
                });
            });
        });
    }
}
