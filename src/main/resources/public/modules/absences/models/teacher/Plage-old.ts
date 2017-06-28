import { Model, Collection } from 'entcore/entcore';
import { Evenement } from './Evenement';

export class Plage extends Model {
    evenements: Collection<Evenement>;
    heure: number;
    duree: number;
    style: any;
    id_cours: number;

    constructor (o?: any) {
        super();
        if (o && typeof o === 'object') {
            this.updateData(o);
        }
    }
}