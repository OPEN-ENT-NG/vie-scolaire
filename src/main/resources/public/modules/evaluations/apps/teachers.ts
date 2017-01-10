/**
 * Created by ledunoiss on 12/09/2016.
 */
import {routes, ng} from 'entcore/entcore';

//CONTROLLERS
import {evaluationsController} from '../controllers/eval_teacher_ctl';
import {evalAcuTeacherController} from '../controllers/eval_acu_teacher_ctl';
import {evalSuiviCompetenceEleveCtl} from '../controllers/eval_suivi_competences_eleve_ctl';
import {evalSuiviCompetenceClasseCtl} from '../controllers/eval_suivi_competences_classe_ctl';

ng.controllers.push(evaluationsController);
ng.controllers.push(evalAcuTeacherController);
ng.controllers.push(evalSuiviCompetenceEleveCtl);
ng.controllers.push(evalSuiviCompetenceClasseCtl);

//FILTERS
import {uniqueFilter} from '../../utils/filters/unique';
import {customSearchFilter} from '../filters/customSearch';
import {customSearchCompetencesFilter} from '../filters/customSearchCompetences';
import {getMatiereClasseFilter} from '../../utils/filters/getMatiereClasse';

ng.filters.push(uniqueFilter);
ng.filters.push(customSearchFilter);
ng.filters.push(customSearchCompetencesFilter);
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
import {autofocus} from '../../utils/directives/autofocus';
import {sticky} from '../../utils/directives/sticky';
import {proportionSuiviCompetence} from '../directives/ProportionSuiviCompetence';
import {rzslider} from '../../utils/directives/slider';

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
ng.directives.push(autofocus);
ng.directives.push(sticky);
ng.directives.push(proportionSuiviCompetence);
ng.directives.push(rzslider);


routes.define(function($routeProvider){
    $routeProvider
        .when('/devoirs/list',{action:'listDevoirs'})
        .when('/devoir/create',{action:'createDevoir'})
        .when('/devoir/:idDevoir/edit', {action : 'editDevoir'})
        .when('/devoir/:devoirId', {action:'viewNotesDevoir'})
        .when('/releve', {action:'displayReleveNotes'})
        .when('/competences/eleve', {action : 'displaySuiviCompetencesEleve'})
        .when('/competences/classe', {action : 'displaySuiviCompetencesClasse'})
        .when('/',{action:'accueil'})
        .otherwise({
            redirectTo : '/'
        });
});