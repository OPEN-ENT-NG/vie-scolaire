import { DefaultAppel } from '../common/impl/DefaultAppel';

export class Appel extends DefaultAppel {
    constructor (o?: any) {
        super();
        if (o && typeof o === 'object') {
            this.updateData(o);
        }
    }
}