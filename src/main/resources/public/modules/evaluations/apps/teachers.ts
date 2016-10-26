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
import {cFilAriane} from '../../utils/directives/cFilAriane';
import {navigable} from '../../utils/directives/navigable';
import {navigableCompetences} from '../directives/cNavigableCompetences';
import {navigatable} from '../../utils/directives/navigatable';
import {tabs} from '../../utils/directives/tabs';
import {pane} from '../../utils/directives/pane';
import {cSkillNoteDevoir} from '../directives/cSkillNoteDevoir';
import {cSkillsColorColumn} from '../directives/cSkillsColorColumn';
import {cSkillsColorPage} from '../directives/cSkillsColorPage';
import {cSkillsList} from '../directives/cSkillsList';

ng.directives.push(cFilAriane);
ng.directives.push(navigable);
ng.directives.push(navigatable);
ng.directives.push(navigableCompetences);
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