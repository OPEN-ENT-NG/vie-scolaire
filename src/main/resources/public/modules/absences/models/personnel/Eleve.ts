import { Collection, Model } from 'entcore/entcore';
import { Evenement } from './Evenement';
import { Responsable } from './Responsable';
import {DefaultEleve} from "../common/DefaultEleve";

export class Eleve extends DefaultEleve {

    responsables: Collection<Responsable>;
    evenements: Collection<Evenement>;

    constructor () {
        super();
        this.collection(Responsable);
        this.collection(Evenement);
    }
}