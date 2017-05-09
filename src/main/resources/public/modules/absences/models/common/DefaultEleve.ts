/**
 * Created by anabah on 09/05/2017.
 */
import { Model, IModel } from 'entcore/entcore';


export class DefaultEleve extends Model implements IModel {
    composer: any;
    id: String;
    absc_precedent_cours: boolean;

    get api () {
        return {GET_EVENEMENT : '/viescolaire/presences/eleve/' + this.id + '/evenements/',
            GET_ABSENCES_PREV : '/viescolaire/absences/eleve/' + this.composer.id + '/absencesprev/'
        };
    }
}