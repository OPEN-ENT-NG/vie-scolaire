import { Model, IModel, http } from 'entcore/entcore';

export class Evenement extends Model implements IModel {
    id: number;
    timestamp_arrive: string;
    timestamp_depart: string;
    commentaire: string;
    saisie_cpe: boolean;
    id_eleve: string;
    id_appel:  number;
    id_type: number;
    id_pj: number;
    id_motif: number;

    get api () {
        return {
            post : '/viescolaire/presences/evenement',
            put : '/viescolaire/presences/evenement',
            delete : '/viescolaire/presences/evenement?evenementId='
        };
    }

    create (): Promise<{ id: number, bool: boolean }> {
        return new Promise((resolve, reject) => {
            http().postJson(this.api.post, this).done((data) => {
                this.id = data.id;
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
                this.id = undefined;
                resolve();
            });
        });
    }
}