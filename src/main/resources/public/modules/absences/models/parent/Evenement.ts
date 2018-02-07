import { DefaultEvenement } from '../common/DefaultEvenement';
import {IModel} from 'entcore';

export class Evenement extends DefaultEvenement {

    constructor(o?: any) {
        super();
        if (o && typeof o === 'object') {
            this.updateData(o);
        }
    }
}