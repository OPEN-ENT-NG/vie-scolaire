import { Model, Collection } from 'entcore/entcore';
import { Evenement } from './Evenement';

export class Plage extends Model {
    evenements: Collection<Evenement>;
    heure: number;
    duree: number;
    style: any;
}