import { http } from 'entcore/entcore';

import { Cours } from './Cours';
import { Eleve } from './Eleve';

export class AppelElevesCollection {
    sync: any;
    load: (data: Eleve[], cb?: (item: Eleve) => void, refreshView?: boolean) => void;
    findWhere: (props: any) => Eleve;
    all: Eleve[];
    trigger: (eventName: string) => void;
    cours: Cours;
    composer: any;

    constructor () {
        this.sync = function () {
            let that = this;
            http().getJson('/viescolaire/classe/' + this.composer.fk_classe_id + '/eleves').done((data) => {
                _.map(data, function(eleve) {
                    eleve.cours = that.cours;
                });
                that.load(data);
                that.loadEvenements();
            });
        };
    }



    loadEvenements () {
        /**
         * On recupere tous les évènements de l'élève de la journée, quelque soit le cours puis on la disperse en 2 listes :
         * - evenementsJours qui nous permettera d'afficher l'historique.
         * - evenements qui va nous permettre de gérer les évènements de l'appel en cours.
         */
        http().getJson('/viescolaire/absences/evenement/classe/' + this.composer.fk_classe_id + '/periode/' + moment(this.composer.cours_timestamp_dt).format('YYYY-MM-DD') + '/' + moment(this.composer.cours_timestamp_dt).format('YYYY-MM-DD'))
            .done((data) => {
                for (let i = 0; i < this.all.length; i++) {
                    this.all[i].evenementsJours.load(_.where(data, {fk_eleve_id : this.all[i].eleve_id}));
                    this.all[i].evenements.load(this.all[i].evenementsJours.where({cours_id : this.composer.cours_id}));
                }
                this.loadAbscPrev();
            });
    }

    loadAbscPrev () {
        http().getJson('/viescolaire/absences/absencesprev/classe/' + this.composer.fk_classe_id + '/' + moment(this.composer.cours_timestamp_dt).format('YYYY-MM-DD') + '/' + moment(this.composer.cours_timestamp_dt).format('YYYY-MM-DD'))
            .done((data) => {
                for (let i = 0; i < this.all.length; i++) {
                    this.all[i].absencePrevs.load(_.where(data, {fk_eleve_id: this.all[i].eleve_id}));
                }
            });
        this.loadAbscLastCours();
    }

    loadAbscLastCours () {
        http().getJson('/viescolaire/absences/precedentes/classe/' + this.composer.fk_classe_id + '/cours/' + this.composer.cours_id)
            .done((data) => {
                _.each(data, function(absc){
                    let eleve = this.findWhere({eleve_id : absc.fk_eleve_id});
                    if (eleve !== undefined) {
                        eleve.absc_precedent_cours = true;
                    }
                });
                this.loadCoursClasse();
            });
    }

    loadCoursClasse () {
        http().getJson('/viescolaire/' + this.composer.fk_classe_id + '/cours/' + moment(this.composer.cours_timestamp_dt).format('YYYY-MM-DD') + '/' + moment(this.composer.cours_timestamp_fn).format('YYYY-MM-DD'))
            .done((data) => {
                _.each(this.all, function(eleve){
                    eleve.courss.load(data);
                });
                this.trigger("appelSynchronized");
            });
    }
}
