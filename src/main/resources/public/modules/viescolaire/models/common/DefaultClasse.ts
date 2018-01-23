/**
 * Created by rahnir on 10/08/2017.
 */
import { Model, Collection } from 'entcore';


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