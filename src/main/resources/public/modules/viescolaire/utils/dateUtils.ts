import {moment} from 'entcore';
import {FORMAT} from '../core/constants/dateFormat';

export class DateUtils {

    /**
     * Format date based on given format using moment
     * @param date date to format
     * @param format format
     */
    static format(date: any, format: string) {
        return moment(date).format(format);
    }

    static getDisplayDate(date: string): string {
        return moment(date).format(FORMAT.displayDate);
    }

    static getDisplayTime(date: string): string {
        return moment(date).format(FORMAT.displayTime);
    }

}