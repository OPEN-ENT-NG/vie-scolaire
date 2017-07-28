import { DefaultCours } from "../common/DefaultCours";
import {Enseignant} from "./Enseignant";
import {Matiere} from "./Matiere";

export class Cours extends DefaultCours {

    enseignant: Enseignant;
    matiere: Matiere;

    constructor(o?: any) {
        super(o);
        if (o && typeof o === 'object') {
            this.updateData(o);
        }

    }
}
