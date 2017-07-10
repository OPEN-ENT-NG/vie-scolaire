import { routes, ng } from 'entcore/entcore';
import { sticky } from '../../utils/directives/sticky';

import { absencesController } from '../controllers/absc_personnel_ctrl';
import { abscAbssmPersonnelController } from '../controllers/absc_abssm_personnel_ctrl';
import { vscoAppoPersonnelController } from '../controllers/absc_appo_personnel_ctrl';
import { abscFiltresPersonnelController } from '../controllers/absc_filt_personnel_ctrl';
import { abscAppelController } from "../controllers/absc_appel_ctrl";
import {abscSaisieElevePersonnel} from '../controllers/absc_saisie_eleve_personnel_ctrl';

import { mobilePanel } from '../../utils/directives/mobile-panel';
import { structureLoader } from '../../utils/directives/structureLoading';

ng.controllers.push(absencesController);
ng.controllers.push(abscAbssmPersonnelController);
ng.controllers.push(vscoAppoPersonnelController);
ng.controllers.push(abscFiltresPersonnelController);
ng.controllers.push(abscAppelController);
ng.controllers.push(abscSaisieElevePersonnel);

ng.directives.push(sticky);
ng.directives.push(mobilePanel);
ng.directives.push(structureLoader);

routes.define(function($routeProvider) {
    $routeProvider
        .when('/sansmotifs', {action: 'AbsencesSansMotifs' })
        .when('/appels/noneffectues', {action: 'AppelsOublies' })
        .when('/appel', { action : 'Appel' })
        .when('/disabled', { action : 'disabled' })
        .when('/redirect', {action: 'Redirect' })
        .when('/saisie/abs/eleve', {action: 'SaisieAbsEleve'})
        .otherwise({
            redirectTo : '/redirect'
        });
});