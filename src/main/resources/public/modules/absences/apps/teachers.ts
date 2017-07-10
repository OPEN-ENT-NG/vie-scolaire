import { routes, ng } from 'entcore/entcore';

import { absencesController } from '../controllers/absc_enseignant_ctrl';
import { mobilePanel } from '../../utils/directives/mobile-panel';
import { offline } from '../../utils/directives/offline';
import { abscAppelController } from "../controllers/absc_appel_ctrl";
import { structureLoader } from '../../utils/directives/structureLoading';

ng.controllers.push(absencesController);
ng.controllers.push(abscAppelController);

ng.directives.push(mobilePanel);
ng.directives.push(offline);
ng.directives.push(structureLoader);

routes.define(function($routeProvider){
    $routeProvider
        .when('/appel', { action: 'appel' })
        .when('/disabled', { action : 'disabled' })
        .otherwise({
            redirectTo : '/appel'
        });
});