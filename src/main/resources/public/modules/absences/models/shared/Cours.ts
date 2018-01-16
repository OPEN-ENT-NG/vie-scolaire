import { http, IModel } from 'entcore';
import { Appel } from './Appel';
import { DefaultCours } from "../common/DefaultCours";
import { Evenement } from "./Evenement";
import { Classe } from "./Classe";
import { FORMAT } from "../../constants/formats";

export class Cours extends DefaultCours {
    appel: Appel;
    classe: Classe;
    synchronized: any;

    public get api () {

        let debut = moment(this.timestamp_dt).format(FORMAT.date);
        let fin = moment(this.timestamp_fn).format(FORMAT.date);

        // Construction de l' API de récupération des évènements d'un élève
        let url_evenement_eleve = '/viescolaire/presences/evenement/classe/';
        url_evenement_eleve += this.id_classe + '/periode/';
        url_evenement_eleve += debut + '/';
        url_evenement_eleve += fin;

        // Construction de l'API de récupération des cours d'une classe
        let url_cours_classe = '/viescolaire/' + this.id_classe + '/cours/';
        url_cours_classe += debut + '/';
        url_cours_classe += fin;


        // Construction de l'API de récupération des absences prévisionnelles
        let url_absence_prev = '/viescolaire/presences/absencesprev/eleves';
        url_absence_prev += '?dateDebut=' + debut;
        url_absence_prev += '&dateFin=' + fin;
        if (this.classe !== undefined) {
            for (let i = 0; i < this.classe.eleves.all.length; i++) {
                url_absence_prev += '&id_eleve=' + this.classe.eleves.all[i].id;
            }
        }
        return {
            GET_APPEL : '/viescolaire/presences/appel/cours/',
            GET_EVENEMENT_ELEVE : url_evenement_eleve,
            GET_COURS_CLASSE : url_cours_classe,
            GET_ABSENCE_PREV : url_absence_prev,
        };
    }

    constructor(o?: any) {
        super(o);
        if (o && typeof o === 'object') {
            this.updateData(o);
        }
        this.synchronized = {
            appel: false,
            events: false,
            abscPrev: false,
            abscLastCours: false,
            coursClasse: false
        };
        this.appel = new Appel();
        this.appel.sync = ():Promise<any> => {
            return new Promise((resolve, reject) => {
                http().getJson(this.api.GET_APPEL + this.id).done((data) => {
                    if (data.length > 0) {
                        this.appel.updateData(data[0]);
                    }
                    if (this.appel.id === undefined) {
                        this.appel.id_personnel = this.id_personnel;
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

    sync(isTeacher?:string): Promise<any> {
        return new Promise(async (resolve, reject) => {

            await Promise.all(
                [this.appel.sync(),
                this.classe.sync()]);
            await Promise.all(
                [this.loadEvenements(),
                this.loadAbscPrev(),
                this.loadAbscLastCours(isTeacher),
                this.loadCoursClasse()]);
            resolve();
        });
    }

    loadEvenements = ():Promise<any> => {
        return new Promise((resolve, reject) => {

            /**
             * On recupere tous les évènements de l'élève de la journée, quelque soit le cours puis on la disperse en
             * 2 listes :
             * - evenementsJours qui nous permettera d'afficher l'historique.
             * - evenements qui va nous permettre de gérer les évènements de l'appel en cours.
             */
            http().getJson(this.api.GET_EVENEMENT_ELEVE).done((data) => {
                for (let i = 0; i < this.classe.eleves.all.length; i++) {
                    this.classe.eleves.all[i].evenements.load(_.where(data, {id_eleve: this.classe.eleves.all[i].id}));
                }
                this.synchronized.events = true;
                resolve();
            });
        });
    };

    loadAbscPrev = (): Promise<any> => {
        return new Promise((resolve, reject) => {
            http().getJson(this.api.GET_ABSENCE_PREV).done((data) => {
                for (let i = 0; i < this.classe.eleves.all.length; i++) {
                    this.classe.eleves.all[i].absencePrevs.load(_.where(data, {id_eleve: this.classe.eleves.all[i].id}));
                    let evtsAbsPrev = _.where(data, {id_eleve: this.classe.eleves.all[i].id});
                    for (let j = 0; j < evtsAbsPrev; j++) {
                        this.classe.eleves.all[i].evenements.all.push(<Evenement> evtsAbsPrev[j]);
                    }
                }
                this.synchronized.abscPrev = true;
                resolve();
            });
        });
    };

    loadAbscLastCours = (isTeacher?): Promise<any> => {
        return new Promise((resolve, reject) => {

            // Construction de l'API de récupération des absences au derniers Cours
            let url = '/viescolaire/presences/precedentes/cours/' + this.id;
            url += isTeacher ? "/true" : "/false";

            http().getJson(url).done((data) => {
                _.each(data, (absc) => {
                    let eleve = _.findWhere(this.classe.eleves.all, {id : absc.id_eleve});
                    if (eleve !== undefined) {
                        eleve.absc_precedent_cours = true;
                    }
                });
                this.synchronized.abscLastCours = true;
                resolve();
            });
        });
    };

    loadCoursClasse = (): Promise<any> => {
        return new Promise((resolve, reject) => {
            http().getJson(this.api.GET_COURS_CLASSE).done((data) => {
                _.each(this.classe.eleves.all, function(eleve) {
                    eleve.courss.load(data);
                    eleve.plages.sync();
                });
                this.synchronized.coursClasse = true;
                resolve();
            });
        });
    };
}
