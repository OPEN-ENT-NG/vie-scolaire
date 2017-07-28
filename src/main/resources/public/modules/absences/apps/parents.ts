import { routes, ng } from 'entcore/entcore';

import { abscParentController } from '../controllers/absc_parent_ctrl';

import { mobilePanel } from '../../utils/directives/mobile-panel';
import { structureLoader } from '../../utils/directives/structureLoading';

ng.controllers.push(abscParentController);

ng.directives.push(mobilePanel);
ng.directives.push(structureLoader);

routes.define(function($routeProvider) {
    $routeProvider
        .when('/accueil', {action: 'AbsencesParentAccueil' })
        .otherwise({
            redirectTo : '/accueil'
        });
});
