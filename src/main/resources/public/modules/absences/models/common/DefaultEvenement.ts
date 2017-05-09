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
    id_motif: number;

    get api () {
        return {
          put: '/viescolaire/presences/evenement/' + this.id + '/updatemotif'
        };
    }

    update (): Promise<any> {
        return new Promise((resolve, reject) => {
            let _evenement = this.toJSON();
            http().putJson(http().parseUrl(this.api.put), _evenement).done((data) => {
               if (resolve && (typeof resolve === 'function')) {
                   resolve(data);
               }
            });
        });
    }

    toJSON() {
        return {
            id: this.id,
            commentaire: this.commentaire,
            saisie_cpe: this.saisie_cpe,
            id_eleve: this.id_eleve,
            id_appel: this.id_appel,
            id_type: this.id_type,
            id_motif: this.id_motif
        };
    }
}