import { Model, Collection } from 'entcore/entcore';
import { Motif } from './Motif';

export class Categorie extends Model {
    id: number;
    libelle: string;
    id_etablissement: string;
    slided: boolean;
    is_appel_oublie:boolean;

    motifs: Collection< Motif >;

    constructor (o?: any) {
        super();
        if (o && typeof o === 'object') { this.updateData(o); }
    }

    get api () {
        return {
            POST: '/presences/categorie/absences',
            UPDATE: '/presences/categorie/absences'
        };
    }

    toJson () {
        return {
            id: this.id,
            libelle: this.libelle,
            id_etablissement: this.id_etablissement };
    }

    create(): Promise<any> {
        return new Promise((resolve, reject) => {
            http().postJson(this.api.POST, this.toJson())
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

    update(): Promise<any> {
        return new Promise((resolve, reject) => {
            http().putJson(this.api.UPDATE, this.toJson())
                .done((data) => {
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
            if (this.id === undefined) {
                this.create()
                    .then(resolve)
                    .catch(reject);
            } else {
                this.update()
                    .then(resolve)
                    .catch(reject);
            }
        });
    }
}
