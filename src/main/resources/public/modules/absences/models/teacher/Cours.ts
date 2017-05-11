import { Collection, http } from 'entcore/entcore';
import { Appel } from './Appel';
import { Eleve } from './Eleve';
import {DefaultCours} from "../common/DefaultCours";
import {IModel} from "../../../entcore/modelDefinitions";
import {workflow} from "../../../entcore/directives/workflow";
import {Evenement} from "./Evenement";



export class Cours extends DefaultCours implements IModel {
    appel: Appel;
    eleves: Collection<Eleve>;

    public get api () {
        // Construction de l' API de récupération des évènements d'un élève
        let url_evenement_eleve = '/viescolaire/presences/evenement/classe/';
        url_evenement_eleve += this.id_classe + '/periode/';
        url_evenement_eleve += moment(this.timestamp_dt).format('YYYY-MM-DD') + '/';
        url_evenement_eleve += moment(this.timestamp_dt).format('YYYY-MM-DD');

        // Construction de l'API de récupération des absences au derniers Cours
        let url_absence_last_cours = '/viescolaire/presences/precedentes/classe/';
        url_absence_last_cours += this.id_classe + '/cours/';
        url_absence_last_cours += this.id;

        // Construction de l'API de récupération des cours d'une classe
        let url_cours_classe = '/viescolaire/' + this.id_classe + '/cours/';
        url_cours_classe += moment(this.timestamp_dt).format('YYYY-MM-DD') + '/';
        url_cours_classe += moment(this.timestamp_fn).format('YYYY-MM-DD');


        // Construction de l'API de récupération des absences prévisionnelles
        let url_absence_prev = '/viescolaire/presences/absencesprev/eleves';
        url_absence_prev += '?dateDebut=' + moment(this.timestamp_dt).format('YYYY-MM-DD');
        url_absence_prev += '&dateFin=' + moment(this.timestamp_dt).format('YYYY-MM-DD');
        for (let i = 0; i < this.eleves.all.length; i++) {
            url_absence_prev += '&id_eleve=' + this.eleves.all[i].id;
        }

        return {
            GET_APPEL : '/viescolaire/presences/appel/cours/',
            GET_ELEVE : '/directory/class/' + this.id_classe + '/users?type=Student',
            GET_EVENEMENT_ELEVE : url_evenement_eleve,
            GET_ABSENCE_LAST_COURS : url_absence_last_cours,
            GET_COURS_CLASSE : url_cours_classe,
            GET_ABSENCE_PREV : url_absence_prev,
        };
    }

    constructor () {
        super();
        this.appel = new Appel();
        this.appel.sync = () => {
            let that = this;
            http().getJson(this.api.GET_APPEL + this.id).done(function (data) {
                if (data.length > 0) {
                    this.updateData(data[0]);
                }
                if (this.id === undefined) {
                    this.id_personnel = that.id_personnel;
                    this.id_cours = that.id;
                    that.appel.id_etat = 1;
                    this.create();
                }
            }.bind(this.appel));
        };
        this.collection(Eleve, {
            sync: (): Promise<any> => {
                return new Promise((resolve, reject) => {
                    let that = this;
                    http().getJson(this.api.GET_ELEVE).done((data) => {
                        _.map(data, function (eleve) {
                            eleve.cours = that;
                        });
                        that.eleves.load(data);
                        that.loadEvenements();
                        if (resolve && typeof (resolve) === 'function') {
                            resolve();
                        }
                    });
                });
            }
        });
    }

    loadEvenements () {
        /**
         * On recupere tous les évènements de l'élève de la journée, quelque soit le cours puis on la disperse en
         * 2 listes :
         * - evenementsJours qui nous permettera d'afficher l'historique.
         * - evenements qui va nous permettre de gérer les évènements de l'appel en cours.
         */
        let that = this;
        http().getJson(this.api.GET_EVENEMENT_ELEVE)
            .done((data) => {
                for (let i = 0; i < that.eleves.all.length; i++) {
                    that.eleves.all[i].evenementsJours.load(_.where(data, {id_eleve : that.eleves.all[i].id}));
                    that.eleves.all[i].evenements.load(_.where( that.eleves.all[i].evenementsJours.all, {id_appel: that.appel.id}));
                }
                that.loadAbscPrev();
            });
    }

    loadAbscPrev () {
        let that = this;
        http().getJson(this.api.GET_ABSENCE_PREV)
            .done((data) => {
                for (let i = 0; i < this.eleves.all.length; i++) {
                    that.eleves.all[i].absencePrevs.load(_.where(data, {id_eleve: that.eleves.all[i].id}));
                    let evtsAbsPrev = _.where(data, {id_eleve: that.eleves.all[i].id});
                    for (let j = 0; j < evtsAbsPrev; j++) {
                        that.eleves.all[i].evenements.all.push( <Evenement> evtsAbsPrev[j]);
                    }
                }
            });
        this.loadAbscLastCours();
    }

    loadAbscLastCours () {
        let that = this;
        http().getJson(this.api.GET_ABSENCE_LAST_COURS)
            .done((data) => {
                _.each(data, function(absc) {
                    let eleve = that.eleves.findWhere({id : absc.id_eleve});
                    if (eleve !== undefined) {
                        eleve.absc_precedent_cours = true;
                    }
                });
                that.loadCoursClasse();
            });
    }

    loadCoursClasse () {
        let that = this;
        http().getJson(this.api.GET_COURS_CLASSE)
            .done((data) => {
                _.each(that.eleves.all, function(eleve) {
                    eleve.courss.load(data);
                    eleve.plages.sync();
                });
                that.trigger("appelSynchronized");
            });
    }

}
