import {DefaultAbsencePrev} from "../common/DefaultAbsencePrev";
import {Model} from "../../../entcore/modelDefinitions";

/**
 * Created by rahnir on 15/06/2017.
 */
export class AbsencePrev extends DefaultAbsencePrev  {


    constructor (o?: any) {
        super();
        if (o && typeof o === 'object') { this.updateData(o); }
    }

    get api () {
        return {
            CREAT: '/viescolaire/presences/absence/previsionnelle'
        };
    }
    toJson () {
        return {
         id: this.id,
        timestamp_dt: this.timestamp_dt,
        timestamp_fn: this.timestamp_fn,
        id_eleve: this.id_eleve,
        id_motif: this.id_motif,
        };
    }
    create(): Promise<any> {
        return new Promise((resolve, reject) => {
            http().postJson(this.api.CREAT, this.toJson())
                .done((data) => {
                    if (!data.hasOwnProperty('id')) {
                        reject();
                    }
                    this.id = data.id;
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


    save(): Promise<any> {
        return new Promise((resolve, reject) => {

                this.create()
                    .then(resolve)
                    .catch(reject);

        });
    }
}