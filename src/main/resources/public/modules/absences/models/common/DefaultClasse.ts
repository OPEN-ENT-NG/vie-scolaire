import { Model, Collection } from 'entcore/entcore';

export abstract class DefaultClasse extends Model {
    // Fields
    selected: boolean;
    id: number;
    name: string;
    type_groupe: number;
    type_groupe_libelle: string;
    mapEleves: any;
    remplacement: boolean;
}