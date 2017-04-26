import { model, http, Model, Collection } from 'entcore/entcore';

// import { AbsencePrev } from './teacher/AbsencePrev';
// import { Appel } from './teacher/Appel';
// import { Cours } from './teacher/Cours';
import { Evenement } from './teacher/Evenement';
import { Structure } from './teacher/Structure';

let moment = require('moment');
declare let _: any;

class Presences extends Model {
    structures: Collection<Structure>;

    constructor () {
        super();
        this.collection(Structure, {
            sync: () => {
                return new Promise((resolve, reject) => {
                    http().getJson('/viescolaire/presences/user/structures/actives')
                        .done((activesStructures) => {
                            let structures: Structure[] = [];
                            for (let i = 0; i < model.me.structures.length; i++) {
                                let id_structure: string = model.me.structures[i];
                                if (_.findWhere(activesStructures, {id_etablissement: id_structure})) {
                                    structures.push(new Structure({
                                        id: id_structure,
                                        name: model.me.structureNames[i]
                                    }));
                                }
                            }
                            this.structures.load(structures);
                            resolve();
                        })
                        .error(() => {
                            reject();
                        });
                });
            }
        });
    }

    async sync (): Promise<any> {
        try {
            await this.structures.sync();
            return;
        } catch (e) {
            throw e;
        }
    }
}

let presences = new Presences();

// export { AbsencePrev, Appel, Cours, Evenement, vieScolaire }
export { presences, Evenement }

model.build = function () {
    (this as any).vieScolaire = presences;
};