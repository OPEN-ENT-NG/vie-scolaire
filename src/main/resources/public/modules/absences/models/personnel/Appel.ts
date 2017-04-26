import { DefaultAppel } from '../common/DefaultAppel';

export class Appel extends DefaultAppel {
    constructor (o?: any) {
        super();
        if (o && typeof o === 'object') { this.updateData(o); }
    }
}
