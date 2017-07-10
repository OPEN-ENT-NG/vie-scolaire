import { Appel as SharedAppel } from '../shared/Appel';
import {FORMAT} from "../../constants/formats";

export class Appel extends SharedAppel {
    personnel_nom: string;
    personnel_prenom: string;
    classe_libelle: string;
    cours_matiere: string;

    constructor (o?: any) {
        super();
        if (o && typeof o === 'object') { this.updateData(o); }
    }
}
