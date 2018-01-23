import { Model } from 'entcore';

export class DefaultAbsencePrev extends Model {
    id?: number;
    timestamp_dt: string;
    timestamp_fn: string;
    id_eleve: string;
    id_motif: string;
    get api () {
        return {};
    }
}