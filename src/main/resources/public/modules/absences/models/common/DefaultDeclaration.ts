import { Model, IModel, http } from 'entcore/entcore';

export class DefaultDeclaration extends Model implements IModel {
    id?: number;
    titre: string;
    commentaire: string;
    timestamp_dt: string;
    timestamp_fn: string;



    get api () {
        return {
            CREATE: '/viescolaire/presences/declarations?',
            UPDATE: '/viescolaire/presences/declarations?',
            delete: '/viescolaire/presences/declarations?' + this.id
        };
    }

    create(): Promise<any> {
        return new Promise((resolve, reject) => {
            http().postJson(this.api.CREATE, this)
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
            let updateDecl = "titre=" + this.titre + "&commentaire="
            + this.commentaire + "&dateDebut=" + this.timestamp_dt + "&dateFin="
            + this.timestamp_fn + "&id=" +this.id;
            http().putJson(this.api.UPDATE + updateDecl)
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

    delete(): Promise<any> {
        return new Promise((resolve, reject) => {
            http().delete(this.api.delete)
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