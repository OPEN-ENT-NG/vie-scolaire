import { DefaultCreneau } from '../common/DefaultCreneau';
import { Cours } from "./Cours";

export class Creneau extends DefaultCreneau {
    cours: Cours;
    duree: any;
    style: any;
}