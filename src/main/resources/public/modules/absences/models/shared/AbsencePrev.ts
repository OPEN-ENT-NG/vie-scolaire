import { DefaultAbsencePrev } from '../common/DefaultAbsencePrev';

export class AbsencePrev extends DefaultAbsencePrev {
    constructor(o?: any) {
        super();
        if (o && typeof o === 'object') {
            this.updateData(o);
        }
    }
}
