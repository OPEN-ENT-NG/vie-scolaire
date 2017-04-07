import { Model, IModel, http } from 'entcore/entcore';

export class Evenement extends Model implements IModel {
    id: number;

    get api () {
        return {
            post : '/viescolaire/absences/evenement',
            put : '/viescolaire/absences/evenement',
            delete : '/viescolaire/absences/evenement?evenementId='
        };
    }

    create (): Promise<{ id: number, bool: boolean }> {
        return new Promise((resolve, reject) => {
            http().postJson(this.api.post, this).done((data) => {
                resolve({id : data.id, bool : true});
            });
        });
    }

    update (): Promise<{ id: number, bool: boolean }> {
        return new Promise((resolve, reject) => {
            http().putJson(this.api.put, this).done((data) => {
                resolve({id : data.id, bool : false});
            });
        });
    }

    save (): Promise<any> {
        return new Promise((resolve, reject) => {
            if (this.id) {
                this.update().then((data) => {
                    resolve(data);
                });
            } else {
                this.create().then((data) => {
                    resolve(data);
                });
            }
        });
    }

    delete (): Promise<any> {
        return new Promise((resolve, reject) => {
            http().delete(this.api.delete + this.id).done(() => {
                resolve();
            });
        });
    }
}