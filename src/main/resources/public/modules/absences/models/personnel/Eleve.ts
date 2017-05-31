import { Collection, Model } from 'entcore/entcore';
import { Evenement } from './Evenement';
import { Responsable } from './Responsable';
import {DefaultEleve} from "../common/DefaultEleve";

export class Eleve extends DefaultEleve {

    responsables: Collection<Responsable>;
    evenements: Collection<Evenement>;

    get api() {
        return {
            GET_RESPONSABLES: '/viescolaire/eleves/' + this.id + '/responsables'
        };
    }

    constructor () {
        super();
        this.collection(Responsable, {
            sync: () => {
                return new Promise((resolve, reject) => {
                    http().getJson(this.api.GET_RESPONSABLES).done((data) => {
                        this.responsables.load(data);
                        resolve();
                    });
                });
            }
        });
        this.collection(Evenement);
    }
}