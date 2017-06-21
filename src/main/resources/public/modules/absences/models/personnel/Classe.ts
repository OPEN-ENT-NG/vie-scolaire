import { Model, Collection } from 'entcore/entcore';
import { Eleve } from './Eleve';
import { DefaultClasse } from '../common/DefaultClasse';

export class Classe extends DefaultClasse {
    eleves: Collection<Eleve>;

    get api () {
        return {
            syncClasse: '/viescolaire/classe/'+this.id+'/eleves',
            syncGroupe : '/viescolaire/groupe/enseignement/users/' + this.id + '?type=Student'
        };
    }

    constructor (o?: any) {
        super();
        if (o !== undefined) this.updateData(o);
        this.collection(Eleve, {
            sync : () : Promise<any> => {
                return new Promise((resolve, reject) => {
                    this.mapEleves = {};
                    let url;
                    url = this.type_groupe === 1 ? this.api.syncGroupe : this.api.syncClasse;
                    http().getJson(url).done((data) => {
                        this.eleves.load(data);
                        for (let i = 0; i < this.eleves.all.length; i++) {
                            this.mapEleves[this.eleves.all[i].id] = this.eleves.all[i];
                        }
                        resolve();
                    });
                });
            }
        });
    }
}