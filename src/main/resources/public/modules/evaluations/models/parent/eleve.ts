import {Model, Collection} from "entcore/entcore";
import {Periode} from "../../../viescolaire/models/common/Periode";
import {translate} from "../../../utils/functions/translate";

export class Eleve extends Model {
    id: string;
    idStructure: string;
    idClasse: string;
    idClasses: Array<string>;
    idStructures: Array<string>;
    displayName: string;
    periodes: Collection<Periode>;

    get api () {
        return {
            get : '/viescolaire/periodes?idGroupe='
        };
    }

    constructor () {
        super();
        this.collection(Periode, {
            sync : () => {
                http().getJson(this.api.get + this.idClasse ).done((periodes) => {
                    periodes.push({libelle: translate('viescolaire.utils.annee'), id: null});
                    this.periodes.load(periodes);
                });
            }
        });
        this.periodes.on('sync', function () {
            this.trigger('syncPeriodes');
        });
    }
}
