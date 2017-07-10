import { Model, Collection } from 'entcore/entcore';
import {DefaultEleve} from "./DefaultEleve";

export abstract class DefaultClasse extends Model {
    // Fields
    selected: boolean;
    id: number;
    name: string;
    type_groupe: number;
    type_groupe_libelle: string;
    mapEleves: any;
    remplacement: boolean;
    eleves: Collection<DefaultEleve>; // les eleves de la classe (utilise pour les demis groupes
}