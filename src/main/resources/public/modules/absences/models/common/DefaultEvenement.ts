import { Model, IModel, http } from 'entcore/entcore';

export class DefaultEvenement extends Model implements IModel {
    id?: number;
    timestamp_arrivee?: string;
    timestamp_depart?: string;
    commentaire?: string;
    saisie_cpe: boolean;
    id_eleve: string;
    id_appel: string;
    id_type: string;
    id_pj?: string;

    get api () {
        return {
          put: '/viescolaire/presences/evenement/' + this.id + '/updatemotif'
        };
    }

    update (): Promise<any> {
        return new Promise((resolve, reject) => {
            http().putJson(http().parseUrl(this.api.put)).done((data) => {
               if (resolve && (typeof resolve === 'function')) {
                   resolve(data);
               }
            });
        });
    }
}