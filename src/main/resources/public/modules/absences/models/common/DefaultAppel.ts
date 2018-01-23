import { Model, http } from 'entcore';

export class DefaultAppel extends Model {
    id?: number;
    id_personnel: string;
    id_cours: number;
    id_etat: number;
    owner: string;
    saisie_cpe: boolean;

    get api () {
        return {
            CREATE: 'presences/appel',
            UPDATE: 'presences/appel',
            DELETE: 'presences/appel/' + this.id
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
            http().putJson(this.api.UPDATE, this)
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
            http().delete(this.api.DELETE)
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