import { Model } from 'entcore/entcore';
import { Collection } from 'entcore/entcore';
import { Competence } from "../teacher/eval_teacher_mdl";

export class DefaultCompetence extends Model {
    competences: Collection<Competence>;
    selected: boolean;
    id: number;
    id_competence: number;
    nom: string;
    code_domaine: string;
    ids_domaine: string;
    composer: any;
}