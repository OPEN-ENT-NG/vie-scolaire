import { routes, ng } from 'entcore/entcore';
import { absencesController } from '../controllers/absc_enseignant_ctrl';
import { mobilePanel } from '../../utils/directives/mobile-panel';

ng.controllers.push(absencesController);

ng.directives.push(mobilePanel);

routes.define(function($routeProvider){
    $routeProvider
        .when('/appel', { action: 'appel' })
        .when('/disabled', { action : 'disabled' })
        .otherwise({
            redirectTo : '/appel'
        });
});