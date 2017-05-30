import {Collection} from "entcore/entcore";
import {Eleve} from "./Eleve";
import {DefaultClasse} from "../common/DefaultClasse";

export class Classe extends DefaultClasse {
    eleves: Collection<Eleve>; // les eleves de la classe (utilise pour les demis groupes

    constructor (o?: any) {
        super();
        if (o !== undefined) this.updateData(o);
    }
}