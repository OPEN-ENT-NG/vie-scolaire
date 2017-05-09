/**
 * Created by anabah on 09/05/2017.
 */
import { Model, IModel, Collection } from 'entcore/entcore';

export class DefaultCours extends Model implements IModel {
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

    get api () {
        // Construction de l' API de récupération des évènements d'un élève
        let url_evenement_eleve = '/viescolaire/presences/evenement/classe/';
        url_evenement_eleve += this.id_classe + '/periode/';
        url_evenement_eleve += moment(this.timestamp_dt).format('YYYY-MM-DD') + '/';
        url_evenement_eleve += moment(this.timestamp_dt).format('YYYY-MM-DD');

        // Construction de l'API de récupération des absences au derniers Cours
        let url_absence_last_cours = '/viescolaire/presences/precedentes/classe/';
        url_absence_last_cours += this.id_classe + '/cours/';
        url_absence_last_cours += this.id;

        // Construction de l'API de récupération des cours d'une classe
        let url_cours_classe = '/viescolaire/' + this.id_classe + '/cours/';
        url_cours_classe += moment(this.timestamp_dt).format('YYYY-MM-DD') + '/';
        url_cours_classe += moment(this.timestamp_fn).format('YYYY-MM-DD');

        return {
            GET_APPEL : '/viescolaire/presences/appel/cours/',
            GET_ELEVE : '/directory/class/' + this.id_classe + '/users?type=Student',
            GET_EVENEMENT_ELEVE : url_evenement_eleve,
            GET_ABSENCE_LAST_COURS : url_absence_last_cours,
            GET_COURS_CLASSE : url_cours_classe
        };
    }
}