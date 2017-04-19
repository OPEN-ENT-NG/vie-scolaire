import { Model, http } from 'entcore/entcore';

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