import {Model} from 'entcore/entcore';
/**
 * Created by rahnir on 10/08/2017.
 */
export class Periode extends Model{
    id: number;
    id_etablissement : string;
    id_classe: string;
    timestamp_dt:string;
    timestamp_fn:string;
    date_fin_saisie:string;
    type: number;
    ordre: number;
}