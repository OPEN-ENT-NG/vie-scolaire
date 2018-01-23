import {Eleve} from "./Eleve";
import {DefaultClasse} from "../common/DefaultClasse";
import {Collection} from "entcore";

export class Classe extends DefaultClasse {

    eleves: Collection<Eleve>; // les eleves de la classe (utilise pour les demis groupes

    get api() {
        return {
            syncClasse: '/viescolaire/classes/' + this.id + '/users',
            syncGroupe: '/viescolaire/groupe/enseignement/users/' + this.id + '?type=Student',
        };
    }

    constructor(o?: any) {
        super();
        if (o !== undefined) this.updateData(o);
        if (!this.hasOwnProperty("remplacement")) this.remplacement = false;
        this.type_groupe_libelle = this.type_groupe === 0 ? "Classe" : "Groupe d'enseignement";

        this.collection(Eleve, {
            sync: (): Promise<any> => {
                return new Promise((resolve, reject) => {
                    this.mapEleves = {};
                    let url;
                    url = this.type_groupe === 0 ? this.api.syncClasse : this.api.syncGroupe;
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

    sync(): Promise<any> {
        return new Promise((resolve, reject) => {
            this.eleves.sync().then(resolve);
        });
    }

    toString = (): string => {
        return this.name;
    }
}