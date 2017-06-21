import {routes, ng } from 'entcore/entcore';
import { sticky } from '../../utils/directives/sticky';

import {absencesController} from '../controllers/absc_personnel_ctrl';
import {abscAbssmPersonnelController} from '../controllers/absc_abssm_personnel_ctrl';
import {vscoAppoPersonnelController} from '../controllers/absc_appo_personnel_ctrl';
import {abscFiltresPersonnelController} from '../controllers/absc_filt_personnel_ctrl';
import {abscSaisieElevePersonnel} from '../controllers/absc_saisie_eleve_personnel_ctrl';


ng.controllers.push(absencesController);
ng.controllers.push(abscAbssmPersonnelController);
ng.controllers.push(vscoAppoPersonnelController);
ng.controllers.push(abscFiltresPersonnelController);
ng.controllers.push(abscSaisieElevePersonnel);

ng.directives.push(sticky);

routes.define(function($routeProvider) {
    $routeProvider
        .when('/sansmotifs', {action: 'AbsencesSansMotifs' })
        .when('/appels/noneffectues', {action: 'AppelsOublies' })
        .when('/disabled', { action : 'disabled' })
        .when('/redirect', {action: 'Redirect' })
        .when('/saisie/abs/eleve', {action: 'SaisieAbsEleve'})
        .otherwise({
            redirectTo : '/redirect'
        });
});