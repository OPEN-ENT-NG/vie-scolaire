import { Model } from 'entcore/entcore';
import { Competence } from "../parent_eleve/Competence";
import { Collection } from 'entcore/entcore';

export class DefaultEnseignement extends Model {
    id;
    competences : Collection<Competence>;


}