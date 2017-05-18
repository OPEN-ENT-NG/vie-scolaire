import {Collection, Model, model} from "entcore/entcore";
// Import des classes
import {Evenement} from "./personnel/Evenement";
import {Structure} from "./personnel/Structure";

import {getActiveStructures} from "../../utils/functions/activeStructures";


let moment = require('moment');
declare let _: any;
/**
 * MODELE DE DONNEES PERSONNEL :
 *  1. Responsable : Coordonnées des responsables de l'élève.
 *  2. Evenements : Liste des évènements relatifs à l'élève : Absences, retards, départs, ...
 *  3. Eleve : Objet contenant toutes les informations relatives à un élève. Contient une liste de Responables et d'Evenements.
 *  4. Classe : Objet contenant toutes les informations relatives à une Classe. Contient une liste d'élève.
 *  5. Enseignant : Objet contenant toutes les informations relatives à un enseignant.
 *  6. Matiere : Objet contenant toutes les informations relatives à une matière.
 *  7. Appel : Object contenant toutes les informations relatives à un appel fait en classe ou réalisé par le CPE/Personnel d'éducation.
 *  8. Motif : Contient les différents motifs d'absences relatif à l'établissement.
 */



class Presences extends Model {

    // constructor () {
    //     super();
    // }
    //

    structures: Collection<Structure>;
    structure: Structure;

    constructor () {
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


