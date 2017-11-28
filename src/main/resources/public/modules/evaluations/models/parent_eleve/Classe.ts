import { Periode } from "./Periode";
import { DefaultClasse } from "../common/DefaultClasse";
import { Collection } from 'entcore/entcore';
import { translate } from "../../../utils/functions/translate";
import {TypePeriode} from "../common/TypePeriode";

export class Classe extends DefaultClasse {
    periodes: Collection<Periode>;
    typePeriodes: Collection<TypePeriode>;


    get api() {
        return {
            syncClasse: '/viescolaire/classes/' + this.id + '/users?type=Student',
            syncGroupe: '/viescolaire/groupe/enseignement/users/' + this.id + '?type=Student',
            syncClasseChefEtab: '/viescolaire/classes/' + this.id + '/users',
            syncPeriode: '/viescolaire/periodes?idGroupe=' + this.id,

            TYPEPERIODES: {
                synchronisation: '/viescolaire/periodes/types'
            }
        }
    }

    constructor(o?: any) {
        super();
        if (o !== undefined) this.updateData(o);
    }

    async sync(): Promise<any> {
        return new Promise(async (resolve, reject) => {
            this.collection(Periode, {
                sync: async (): Promise<any> => {
                    return new Promise((resolve, reject) => {
                        http().getJson(this.api.syncPeriode).done((res) => {
                            res.push({libelle: translate('viescolaire.utils.annee'), id: null});
                            this.periodes.load(res);
                            resolve();
                        }).error(function () {
                            if (reject && typeof reject === 'function') {
                                reject();
                            }
                        });
                    });
                }
            });
            this.collection(TypePeriode, {
                sync: async (): Promise<any> => {
                    return await http().getJson(this.api.TYPEPERIODES.synchronisation).done((res) => {
                        this.typePeriodes.load(res);
                    })
                    .error(function () {
                        if (reject && typeof reject === 'function') {
                            reject();
                        }
                    });
                }
            });
            await this.periodes.sync();
            await this.typePeriodes.sync();
            resolve();
        });
    }
}