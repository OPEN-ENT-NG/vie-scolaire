import { Collection, Model } from 'entcore/entcore';
import { Evenement } from './Evenement';
import { Responsable } from './Responsable';

export class Eleve extends Model {
    responsables: Collection<Responsable>;
    evenements: Collection<Evenement>;

    constructor () {
        super();
        this.collection(Responsable);
        this.collection(Evenement);
    }
}