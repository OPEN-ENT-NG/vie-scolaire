import {Collection, Model, model, moment} from "entcore";
// Import des classes
import { Evenement } from "./parent/Evenement";
import { Structure } from "./parent/Structure";

import {getActiveStructures} from "../../utils/functions/activeStructures";

declare let _: any;

class Presences extends Model {

    structures: Collection<Structure>;
    structure: Structure;

    constructor() {
        super();
        this.collection(Structure, {
            sync: async (): Promise<any> => {
                // Synchronise les structures actives. Le resolve de la promesse est appelé
                // grâce au return et le reject grâce au Throw dans le catch
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

    async sync(): Promise<any> {
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

export {Evenement};

model.build = function () {
    (this as any).presences = presences;
    presences.sync();
};


