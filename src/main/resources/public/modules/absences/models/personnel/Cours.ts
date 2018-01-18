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
