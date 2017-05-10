/**
 * Created by anabah on 09/05/2017.
 */
import { Model } from 'entcore/entcore';

export class DefaultCours extends Model {
    id: number;
    timestamp_dt: string;
    timestamp_fn: string;
    id_personnel: string; // Champs supplémentaire
    id_matiere: string;
    id_etablissement: string;
    salle: string;
    edt_classe: string;
    edt_date: string;
    edt_salle: string;
    edt_matiere: string;
    edt_id_cours: string;
    id_classe: string;

    composer: any;

}