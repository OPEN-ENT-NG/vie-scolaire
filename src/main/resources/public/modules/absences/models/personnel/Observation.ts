import { DefaultEvenement } from '../common/DefaultEvenement';

export class Observation extends DefaultEvenement {
    constructor (o?: any) {
        super();
        if (o && typeof o === 'object') {
            this.updateData(o);
        }
    }
}