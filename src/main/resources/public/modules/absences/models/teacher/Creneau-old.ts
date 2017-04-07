import { Model } from 'entcore/entcore';
import { Cours } from './Cours';

export class Creneau extends Model {
    heureDebut: any;
    heureFin: any;
    cours: Cours;
    duree: any;
    style: any;
}