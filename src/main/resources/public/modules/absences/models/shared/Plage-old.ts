import { Model, Collection } from 'entcore';
import { Evenement } from './Evenement';

export class Plage extends Model {
    evenements: Collection<Evenement>;
    heure: number;
    duree: number;
    style: any;

    constructor(o?: any) {
        super(o);
        if (o && typeof o === 'object') {
            this.updateData(o);
        }
        this.collection(Evenement);
    }
}