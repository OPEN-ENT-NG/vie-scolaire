import {DefaultClasse} from "../common/DefaultClasse";
import {Periode} from "./Periode";
import {Collection} from 'entcore/entcore';
/**
 * Created by rahnir on 10/08/2017.
 */
export class Classe extends DefaultClasse {
    periodes: Collection<Periode>;

    constructor(o?: any) {
        super();
        if (o && typeof o === 'object') {
            this.updateData(o);
        }
        this.collection(Periode);
    }
}