import { Model, Collection } from 'entcore/entcore';
import { Eleve } from './Eleve';

export class Classe extends Model {
    eleves: Collection<Eleve>;
    selected: boolean;

    constructor () {
        super();
        this.collection(Eleve);
    }
}