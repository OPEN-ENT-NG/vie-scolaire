import { Evenement as SharedEvenement } from '../shared/Evenement';

export class Evenement extends SharedEvenement {
    constructor(o?: any) {
        super();
        if (o && typeof o === 'object') {
            this.updateData(o);
        }
    }
}