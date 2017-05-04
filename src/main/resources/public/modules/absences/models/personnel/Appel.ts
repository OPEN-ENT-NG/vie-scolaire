import { DefaultAppel } from '../common/DefaultAppel';

export class Appel extends DefaultAppel {
    personnel_nom: string;
    personnel_prenom: string;
    classe_libelle: string;
    cours_matiere: string;
    constructor (o?: any) {
        super();
        if (o && typeof o === 'object') { this.updateData(o); }
    }
}
