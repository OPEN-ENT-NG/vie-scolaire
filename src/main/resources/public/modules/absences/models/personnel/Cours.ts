import { Model } from '../../../entcore/modelDefinitions';
import { Cours as SharedCours} from '../shared/Cours';

/**
 * Created by rahnir on 09/06/2017.
 */


export class Cours extends SharedCours implements Model {
    id_appel: number;
    structureId: string;
    startMoment: any;
    endMoment: any;
    startDate: string | Object;
    endDate: string | Object;
    dayOfWeek: number;
    roomLabels: string[];
    classes: string[];
    groups: string[];
    absence: any;
    evenements: any;
    teacherNames: string[];
    teacherIds: string[];
    classeNames: string[];
    classeIds: string[];
    subjectId: string;
    subjectLabel: string;

    isFromMongo: boolean;
    _id: string;

    color: string;
    is_periodic: boolean;
    locked: boolean;

    isAlreadyFound: boolean;
    isFutur: boolean;

    constructor (obj: Object, startDate?: string | Object, endDate?: string | Object) {
        super();
        if (obj instanceof Object) {
            for (let key in obj) {
                this[key] = obj[key];
            }
        }
        this.is_periodic = false;
        if (startDate) {
            this.startMoment = moment(startDate);
        }
        if (endDate) {
            this.endMoment = moment(endDate);
        }
    }
}
