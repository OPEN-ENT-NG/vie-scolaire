import { routes, ng } from 'entcore';

import { abscParentController } from '../controllers/absc_parent_ctrl';

// import { mobilePanel } from '../../utils/directives/mobile-panel';
import { structureLoader } from '../../utils/directives/structureLoading';
import { cRoundAvatar } from '../../utils/directives/cRoundAvatar';

ng.controllers.push(abscParentController);

// ng.directives.push(mobilePanel);
ng.directives.push(structureLoader);
ng.directives.push(cRoundAvatar);

routes.define(function($routeProvider) {
    $routeProvider
        .when('/accueil', {action: 'Accueil' })
        .when('/absences', {action: 'Absences' })
        .otherwise({
            redirectTo : '/accueil'
        });
});
