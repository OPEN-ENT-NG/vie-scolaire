import { DefaultEvenement } from '../common/DefaultEvenement';

export class Evenement extends DefaultEvenement {
    constructor (o?: any) {
        super();
        if (o && typeof o === 'object') {
            this.updateData(o);
        }
    }
}