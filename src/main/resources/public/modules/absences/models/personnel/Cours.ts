import {DefaultCours} from "../common/DefaultCours";
import { Model } from 'entcore';

/**
 * Created by rahnir on 09/06/2017.
 */


export class Cours extends DefaultCours implements Model{
    etatcours: string;
    id_appel: number;
    _id: string;
    _occurenceId?: string;
    structureId: string;
    startDate: string | Object;
    endDate: string | Object;
    dayOfWeek: number;
    teacherIds: string[];
    subjectId: string;
    roomLabels: string[];
    classes: string[];
    groups: string[];
    color: string;
    is_periodic: boolean;
    startMoment: any;
    startMomentDate: string;
    startMomentTime: string;
    startCalendarHour: Date;
    endCalendarHour: Date;
    endMoment: any;
    endMomentDate: string;
    endMomentTime: string;
    subjectLabel: string;
    courseOccurrences: CourseOccurrence[];
    teachers: any;
    enseignantName : string[];
    originalStartMoment?: any;
    originalEndMoment?: any;


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
            this.startCalendarHour = this.startMoment.seconds(0).millisecond(0).toDate();
            this.startMomentDate = this.startMoment.format('DD/MM/YYYY');
            this.startMomentTime = this.startMoment.format('hh:mm');
        }
        if (endDate) {
            this.endMoment = moment(endDate);
            this.endCalendarHour = this.endMoment.seconds(0).millisecond(0).toDate();
            this.endMomentDate = this.endMoment.format('DD/MM/YYYY');
            this.endMomentTime = this.endMoment.format('hh:mm');
        }
    }


}
export class CourseOccurrence {
    dayOfWeek: any;
    startTime: Date;
    endTime: Date;
    roomLabels: string[];
}