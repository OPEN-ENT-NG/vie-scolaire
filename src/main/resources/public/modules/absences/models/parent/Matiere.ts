import { DefaultMatiere } from '../common/DefaultMatiere';

export class Matiere extends DefaultMatiere {
    constructor (o?: any) {
        super();
        if (o && typeof o === 'object') {
            this.updateData(o);
        }
    }

    toString = (): string => {
        return this.name;
    }
}