/**
 * Created by ledunoiss on 12/09/2016.
 */
import { model, routes, notify, http, IModel, Model, Collection, BaseModel, idiom as lang, ng, template } from 'entcore/entcore';
import {Classe, Devoir, Devoirs, DevoirsCollection, Eleve, Enseignement, Evaluation, Evaluations, Competence, CompetenceNote, evaluations, Matiere, Periode, ReleveNote, Structure, Type, SousMatiere} from '../models/eval_teacher_mdl';

//CONTROLLERS
import {evaluationsController} from '../controllers/eval_teacher_ctl';

ng.controllers.push(evaluationsController);

//FILTERS
import {uniqueFilter} from '../../utils/filters/unique';
import {customSearchFilter} from '../filters/customSearch';
import {getMatiereClasseFilter} from '../../utils/filters/getMatiereClasse';

ng.filters.push(uniqueFilter);
ng.filters.push(customSearchFilter);
ng.filters.push(getMatiereClasseFilter);

//DIRECTIVES
import {cFilAriane} from '../../utils/directives/globals/cFilAriane';
import {navigable} from '../../utils/directives/globals/navigable';
import {navigatable} from '../../utils/directives/globals/navigatable';
import {tabs} from '../../utils/directives/globals/tabs';
import {pane} from '../../utils/directives/globals/pane';
import {cSkillNoteDevoir} from '../../utils/directives/evaluations/cSkillNoteDevoir';
import {cSkillsColorColumn} from '../../utils/directives/evaluations/cSkillsColorColumn';
import {cSkillsColorPage} from '../../utils/directives/evaluations/cSkillsColorPage';
import {cSkillsList} from '../../utils/directives/evaluations/cSkillsList';

ng.directives.push(cFilAriane);
ng.directives.push(navigable);
ng.directives.push(navigatable);
ng.directives.push(tabs);
ng.directives.push(pane);
ng.directives.push(cSkillNoteDevoir);
ng.directives.push(cSkillsColorColumn);
ng.directives.push(cSkillsColorPage);
ng.directives.push(cSkillsList);

routes.define(function($routeProvider){
    $routeProvider
        .when('/devoirs/list',{action:'listDevoirs'})
        .when('/devoir/:devoirId', {action:'viewNotesDevoir'})
        .when('/releve', {action:'displayReleveNotes'})
        .otherwise({
            redirectTo : '/releve'
        });
});