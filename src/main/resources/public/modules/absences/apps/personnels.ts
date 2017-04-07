import {routes, ng } from 'entcore/entcore';

import {absencesController} from '../controllers/absc_personnel_ctrl';
import {abscAbssmPersonnelController} from '../controllers/absc_abssm_personnel_ctrl';
import {vscoAppoPersonnelController} from '../controllers/absc_appo_personnel_ctrl';
import {abscFiltresPersonnelController} from '../controllers/absc_filt_personnel_ctrl';

ng.controllers.push(absencesController);
ng.controllers.push(abscAbssmPersonnelController);
ng.controllers.push(vscoAppoPersonnelController);
ng.controllers.push(abscFiltresPersonnelController);

routes.define(function($routeProvider){
    $routeProvider
        .when('/sansmotifs', {action: 'AbsencesSansMotifs' })
        .when('/appels/noneffectues', {action: 'AppelsOublies' })
        .when('/redirect', {action: 'Redirect' })
        .otherwise({
            redirectTo : '/redirect'
        });
});