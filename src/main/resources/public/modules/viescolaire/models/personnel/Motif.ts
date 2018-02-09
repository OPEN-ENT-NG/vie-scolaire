/**
 * Created by anabah on 06/06/2017.
 */

import { Model } from 'entcore';

export class Motif extends Model {
    id: number;
    commentaire: string;
    defaut: boolean;
    id_etablissement: string;
    justifiant: boolean;
    justifiant_libelle: string;
    libelle: string;
    id_categorie: string;
    is_appel_oublie:boolean;

    constructor (o?: any) {
        super();
        if (o && typeof o === 'object') { this.updateData(o); }
    }

    get api () {
        return {
            POST: '/presences/motif',
            UPDATE: '/presences/motif',
            UPDATE_APPEL: '/presences/motif/appel'
        };
    }

    toJson () {
        return {
            id: this.id,
            commentaire: this.commentaire,
            defaut: this.defaut,
            id_etablissement: this.id_etablissement,
            justifiant: this.justifiant,
            libelle: this.libelle,
            id_categorie: this.id_categorie };
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
            let url = this.api.UPDATE;
            if(this.is_appel_oublie){
                url = this.api.UPDATE_APPEL;
            }
            http().putJson(url, this.toJson())
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
