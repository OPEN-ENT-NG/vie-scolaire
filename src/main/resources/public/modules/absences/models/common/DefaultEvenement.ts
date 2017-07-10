import { Model, IModel, http } from 'entcore/entcore';

export class DefaultEvenement extends Model implements IModel {
    id?: number;
    timestamp_arrive?: string;
    timestamp_depart?: string;
    commentaire?: string;
    saisie_cpe: boolean;
    id_eleve: string;
    id_appel: number;
    id_type: number;
    id_pj?: number;
    id_motif: number;
    id_personnel: string;
    id_matiere: string;
    id_cours?: number;

    get api () {
        return {
            POST : '/viescolaire/presences/evenement',
            PUT : '/viescolaire/presences/evenement',
            DELETE : '/viescolaire/presences/evenement?evenementId=',
            UPDATE_MOTIF: '/viescolaire/presences/evenement/' + this.id + '/updatemotif'
        };
    }

    create (): Promise<{ id: number, bool: boolean }> {
        return new Promise((resolve, reject) => {
            http().postJson(this.api.POST, this).done((data) => {
                this.id = data.id;
                resolve({id : data.id, bool : true});
            });
        });
    }

    update (): Promise<{ id: number, bool: boolean }> {
        return new Promise((resolve, reject) => {
            http().putJson(this.api.PUT, this).done((data) => {
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
            http().delete(this.api.DELETE + this.id).done(() => {
                this.id = undefined;
                resolve();
            });
        });
    }

    toJSON() {
        let JSONformat = {
            id: this.id,
            saisie_cpe: this.saisie_cpe,
            id_eleve: this.id_eleve,
            id_appel: this.id_appel,
            id_type: this.id_type,
            id_motif: this.id_motif
        };
        if(this.commentaire != null) {
            JSONformat = _.extend(JSONformat, { commentaire: this.commentaire});
        }
        if (this.timestamp_depart != null) {
            JSONformat = _.extend(JSONformat, {timestamp_depart: this.timestamp_depart});
        }
        if (this.timestamp_arrive != null) {
            JSONformat = _.extend(JSONformat, {timestamp_arrive: this.timestamp_arrive});
        }
        return JSONformat;
    }
}