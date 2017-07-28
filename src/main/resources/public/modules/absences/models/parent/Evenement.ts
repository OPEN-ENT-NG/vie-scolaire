import { DefaultEvenement } from '../common/DefaultEvenement';
import {IModel} from "../../../entcore/modelDefinitions";

export class Evenement extends DefaultEvenement implements IModel {
    constructor(o?: any) {
        super();
        if (o && typeof o === 'object') {
            this.updateData(o);
        }
    }
}