import { Model, Collection } from 'entcore/entcore';
import { Eleve } from './Eleve';

export class Classe extends Model {
    eleves: Collection<Eleve>;
    selected: boolean;
    id: number;
    name: string;
    type_groupe: number;
    type_groupe_libelle: string;
    mapEleves: any;
    remplacement: boolean;

    get api () {
        return {
            syncClasse: '/directory/class/' + this.id + '/users?type=Student',
            syncGroupe : '/viescolaire/groupe/enseignement/users/' + this.id + '?type=Student'
           // syncClasseChefEtab : '/viescolaire/classes/' + this.id + '/users'
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
                    // if(isChefEtab()){
                    //     url = this.type_groupe === 1 ? this.api.syncGroupe : this.api.syncClasseChefEtab;
                    // }else {
                    url = this.type_groupe === 1 ? this.api.syncGroupe : this.api.syncClasse;
                    // }
                    http().getJson(url).done((data) => {
                        this.eleves.load(data);
                        for (let i = 0; i < this.eleves.all.length; i++) {
                            this.mapEleves[this.eleves.all[i].id] = this.eleves.all[i];
                        }
                        // this.trigger('sync');
                        resolve();
                    });
                });
            }
        });
    }
}