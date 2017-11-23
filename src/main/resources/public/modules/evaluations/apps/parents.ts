/**
 * Created by ledunoiss on 12/09/2016.
 */
import { routes, ng } from 'entcore/entcore';
import {evaluationsController} from '../controllers/eval_parent_ctl';

ng.controllers.push(evaluationsController);

routes.define(function($routeProvider) {
    $routeProvider
        .when('/releve', {action:'displayReleveNotes'})
        .otherwise({
            redirectTo : '/releve'
        });
});