import { Model, Collection, http } from 'entcore/entcore';

import { Appel } from './Appel';
// import { AppelElevesCollection } from './AppelElevesCollection';
import { Eleve } from './Eleve';

// interface Eleves extends Collection<Eleve>, AppelElevesCollection {}

export class Cours extends Model {
    appel: Appel;
    eleves: Collection<Eleve>;

    id: number;
    timestamp_dt: string;
    timestamp_fn: string;
    id_personnel: string; // Champs supplémentaire
    id_matiere: string;
    id_etablissement: string;
    salle: string;
    edt_classe: string;
    edt_date: string;
    edt_salle: string;
    edt_matiere: string;
    edt_id_cours: string;
    id_classe: string;

    composer: any; // Get from AppelElevesCollection

    get api () {
        return {
            getAppel : '/viescolaire/presences/appel/cours/'
        };
    }

    constructor () {
        super();
        this.appel = new Appel();
        this.appel.sync = () => {
            let that = this;
            http().getJson(this.api.getAppel + this.id).done(function (data) {
                if (data.length > 0) {
                    this.updateData(data[0]);
                }
                if (this.id === undefined) {
                    this.id_personnel = that.id_personnel;
                    this.id_cours = that.id;
                    that.appel.id_etat = 1;
                    this.create().then((data) => {
                        this.id = data.id;
                    });
                }
            }.bind(this.appel));
        };
        this.collection(Eleve, {
            sync: (): Promise<any> => {
                return new Promise((resolve, reject) => {
                    let that = this;
                    http().getJson('/directory/class/' + that.id_classe + '/users?type=Student').done((data) => {
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
        let url = '/viescolaire/presences/evenement/classe/';
        let that = this;
        url += this.id_classe + '/periode/';
        url += moment(this.timestamp_dt).format('YYYY-MM-DD') + '/';
        url += moment(this.timestamp_dt).format('YYYY-MM-DD');
        http().getJson(url)
            .done((data) => {
                for (let i = 0; i < that.eleves.all.length; i++) {
                    that.eleves.all[i].evenementsJours.load(_.where(data, {id_eleve : that.eleves.all[i].id}));
                    that.eleves.all[i].evenements.load(
                        that.eleves.all[i].evenementsJours.where({id_cours : that.id})
                    );
                }
                that.loadAbscPrev();
            });
    }

    loadAbscPrev () {
        let url = '/viescolaire/presences/absencesprev/eleves';
        url += '?dateDebut=' + moment(this.timestamp_dt).format('YYYY-MM-DD');
        url += '&dateFin=' + moment(this.timestamp_dt).format('YYYY-MM-DD');
        let that = this;
        for (let i = 0; i < this.eleves.all.length; i++) {
            url += '&id_eleve=' + this.eleves.all[i].id;
        }
        http().getJson(url)
            .done((data) => {
                for (let i = 0; i < this.eleves.all.length; i++) {
                    that.eleves.all[i].absencePrevs.load(_.where(data, {id_eleve: that.eleves.all[i].id}));
                }
            });
        this.loadAbscLastCours();
    }

    loadAbscLastCours () {
        let url = '/viescolaire/presences/precedentes/classe/';
        url += this.id_classe + '/cours/';
        url += this.id;
        let that = this;
        http().getJson(url)
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
        let url = '/viescolaire/' + this.id_classe + '/cours/';
        url += moment(this.timestamp_dt).format('YYYY-MM-DD') + '/';
        url += moment(this.timestamp_fn).format('YYYY-MM-DD');
        let that = this;
        http().getJson(url)
            .done((data) => {
                _.each(that.eleves.all, function(eleve) {
                    eleve.courss.load(data);
                    eleve.plages.sync();
                });
                this.trigger("appelSynchronized");
            });
    }

}
