import { model, Model, Collection} from 'entcore';
import { getActiveStructures } from "../../utils/functions/activeStructures";
import {Structure} from './personnel/Structure';


declare let _: any;

/**
 * MODELE DE DONNEES PERSONNEL :
 *  viescolaire
 */

export class VieScolaire extends Model {
    structures: Collection<Structure>;
    structure: Structure;

    constructor () {
        super();
        this.collection(Structure, {
            sync: async (): Promise<any> => {
                try {
                    let structuresPresences;
                    let structuresEvaluations;
                    let _structureTmp = [];

                    // récupération des structures actives par module
                    structuresPresences = await getActiveStructures('presences');
                    structuresEvaluations = await getActiveStructures('notes');
                    for (let i = 0; i < model.me.structures.length; i++) {
                        let _structure = new Structure({
                            id: model.me.structures[i],
                            name: model.me.structureNames[i]
                        });

                        if (_.findWhere(structuresEvaluations, {id : _structure.id})) {
                            _structure.isActived.evaluation = true;
                        }
                        if (_.findWhere(structuresPresences, {id : _structure.id})) {
                            _structure.isActived.presence = true;
                        }
                        _structureTmp.push(_structure);
                    }
                    this.structures.load(_structureTmp);
                    this.structure = this.structures.all[0];
                    return;
                } catch (e) {
                    throw e;
                }
            }
        });
    }

    async sync (): Promise<any> {
        try {
            await this.structures.sync();
        } catch (e) {
            throw  e;
        }
    }
}

export let vieScolaire = new VieScolaire();

model.build = function () {
    (this as any).vieScolaire = vieScolaire;
    vieScolaire.sync();
};