import { Collection, Model, model } from "entcore";
// Import des classes
import {Evenement} from "./personnel/Evenement";
import {Structure} from "./personnel/Structure";

import {getActiveStructures} from "../../utils/functions/activeStructures";


let moment = require('moment');
declare let _: any;

class Presences extends Model {

    structures: Collection<Structure>;
    structure: Structure;

    constructor () {
        super();
        this.collection(Structure, {
            sync: async (): Promise<any> => {
                try {
                    let structures;
                    structures = await getActiveStructures('presences');
                    this.structures.load(structures);
                    return;
                } catch (e) {
                    throw e;
                }
            }
        });
    }

    async sync (): Promise<any> {
        try {
            await  this.structures.sync();
            this.structure = this.structures.first();
            return;
        } catch (e) {
            throw e;
        }
    }
}

export let presences = new Presences();

export {  Evenement };

model.build = function () {
    (this as any).presences = presences;
    presences.sync();
};


