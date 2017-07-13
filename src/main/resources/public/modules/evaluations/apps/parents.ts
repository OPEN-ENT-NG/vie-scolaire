/**
 * Created by ledunoiss on 12/09/2016.
 */
import { notify, idiom as lang, template, ui, http, routes, ng } from 'entcore/entcore';
import {evaluations, Matiere, Eleve, Devoir, Periode} from '../models/eval_parent_mdl';
import {evaluationsController} from '../controllers/eval_parent_ctl';

ng.controllers.push(evaluationsController);

routes.define(function($routeProvider) {
    $routeProvider
        .when('/releve', {action:'displayReleveNotes'})
        .otherwise({
            redirectTo : '/releve'
        });
});