import { Model } from '../../../entcore/modelDefinitions';
import { Cours as SharedCours} from '../shared/Cours';

/**
 * Created by rahnir on 09/06/2017.
 */


export class Cours extends SharedCours implements Model {
    color: string;
    is_periodic: boolean;
    locked: boolean;

    constructor (sharedCours?: SharedCours) {
        super();
        if (sharedCours instanceof SharedCours) {
            // Default Cours
            this.id = sharedCours.id;
            this.timestamp_dt = sharedCours.timestamp_dt;
            this.timestamp_fn = sharedCours.timestamp_fn;
            this.id_personnel = sharedCours.id_personnel;
            this.id_matiere = sharedCours.id_matiere;
            this.id_etablissement = sharedCours.id_etablissement;
            this.salle = sharedCours.salle;
            this.edt_classe = sharedCours.edt_date;
            this.edt_salle = sharedCours.edt_salle;
            this.edt_matiere = sharedCours.edt_matiere;
            this.edt_id_cours = sharedCours.edt_id_cours;
            this.id_classe = sharedCours.id_classe;
            this.composer = sharedCours.composer;

            // Shared Cours
            this.appel = sharedCours.appel;
            this.id_appel = sharedCours.id_appel;
            this.roomLabels = sharedCours.roomLabels;
            this.classe = sharedCours.classe;
            this.synchronized = sharedCours.synchronized;
            this.startMoment = sharedCours.startMoment;
            this.endMoment = sharedCours.endMoment;
            this.teacherNames = sharedCours.teacherNames;
            this.teacherIds = sharedCours.teacherIds;
            this.classeNames = sharedCours.classeNames;
            this.classeIds = sharedCours.classeIds;
            this.absence = sharedCours.absence;
            this.structureId = sharedCours.structureId;
            this.classes = sharedCours.classes;
            this.groups = sharedCours.groups;
            this.subjectId = sharedCours.subjectId;
            this.subjectLabel = sharedCours.subjectLabel;
            this.evenements = sharedCours.evenements;
            this.isFromMongo = sharedCours.isFromMongo;
            this._id = sharedCours._id;
            this.dayOfWeek = sharedCours.dayOfWeek;
            this.isAlreadyFound = sharedCours.isAlreadyFound;
            this.isFutur = sharedCours.isFutur;
        }
    }
}
