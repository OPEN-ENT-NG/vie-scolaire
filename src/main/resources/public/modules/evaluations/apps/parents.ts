/**
 * Created by ledunoiss on 12/09/2016.
 */
import { routes, ng } from 'entcore/entcore';
import {evaluationsController} from '../controllers/eval_parent_ctl';
import {cRoundAvatar} from "../../utils/directives/cRoundAvatar";
import {releveController} from "../controllers/eval_parent_releve_ctrl";
import {cFilAriane} from "../../utils/directives/cFilAriane";

ng.controllers.push(evaluationsController);
ng.controllers.push(releveController);
ng.directives.push(cRoundAvatar);
ng.directives.push(cFilAriane);

routes.define(function($routeProvider) {
    $routeProvider
        .when('/',{action:'accueil'})
        .when('/devoirs/list', {action:'listDevoirs'})
        .when('/releve', {action:'displayReleveNotes'})
        .when('/competences/eleve', {action:'displayBilanDeCompetence'})
        .otherwise({
            redirectTo : '/'
        });
});