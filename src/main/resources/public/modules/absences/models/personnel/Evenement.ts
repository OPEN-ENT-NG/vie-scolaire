import { Model, IModel, http } from 'entcore/entcore';

export class Evenement extends Model implements IModel {
    id: number;

    get api () {
        return {
            update : '/viescolaire/absences/evenement/:id/updatemotif'
        };
    }

    constructor (e) {
        super();
        this.updateData(e);
    }

    update (): Promise<any> {
        return new Promise((resolve, reject) => {
            http().putJson(http().parseUrl(this.api.update)).done((data) => {
                if (resolve && (typeof (resolve) === 'function')) {
                    resolve(data);
                }
            });
        });
    }
}