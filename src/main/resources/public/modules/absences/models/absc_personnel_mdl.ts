import { model, notify, http, IModel, Model, Collection, BaseModel } from 'entcore/entcore';

let moment = require('moment');
declare let _:any;
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

export class Responsable extends Model {}
export class Justificati extends Model {}
export class Evenement extends Model implements IModel {
    id : number;

    get api () {
        return {
            update : '/viescolaire/absences/evenement' + this.id + '/updatemotif'
        }
    }

    
}