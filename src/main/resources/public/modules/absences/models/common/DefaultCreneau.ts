import { Model } from 'entcore/entcore';

export class DefaultCreneau extends Model {
    id: number;
    id_etablissement: string;
    timestamp_dt: string;
    timestamp_fn: string;
}