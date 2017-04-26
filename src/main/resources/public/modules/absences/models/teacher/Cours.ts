import { Model, Collection, http } from 'entcore/entcore';

// import { Appel } from './Appel';
// import { AppelElevesCollection } from './AppelElevesCollection';
// import { Eleve } from './Eleve';

// interface Eleves extends Collection<Eleve>, AppelElevesCollection {}

export class Cours extends Model {
    cours: any;
    // appel: Appel;
    // eleves: Eleves;

    id: number;
    fk_classe_id: number;
    cours_timestamp_dt: string;
    cours_id: number;
    cours_timestamp_fn: string;
    fk_personnel_id: string;

    get api () {
        return {
            getAppel : '/viescolaire/absences/appel/cours/'
        };
    }

    constructor () {
        super();
        // this.cours = this;
        // this.appel = new Appel();
        // this.appel.sync = () => {
        //     const that = this;
        //     http().getJson(this.api.getAppel + this.cours.cours_id).done(function (data) {
        //         this.updateData(data[0]);
        //         if (this.id === undefined) {
        //             this.fk_personnel_id = that.fk_personnel_id;
        //             this.fk_cours_id = that.cours_id;
        //             this.fk_etat_appel_id = 1;
        //             this.create().then((data) => {
        //                 this.id = data.id;
        //             });
        //         }
        //     }.bind(this.appel));
        // };

        // this.collection(Eleve, new AppelElevesCollection());
    }
}
