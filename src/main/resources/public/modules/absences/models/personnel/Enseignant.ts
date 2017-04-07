import { Model } from 'entcore/entcore';

export class Enseignant extends Model {
    selected: boolean;
    nom: string;
    prenom: string;
    id: string;
}