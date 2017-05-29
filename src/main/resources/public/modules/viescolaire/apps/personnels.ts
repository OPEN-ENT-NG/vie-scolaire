/**
 * Created by ledunoiss on 12/09/2016.
 */

import { idiom as lang, routes, ng } from 'entcore/entcore';

import { sticky } from '../../utils/directives/sticky';

import {viescolaireController} from '../controllers/vsco_personnel_ctrl';
import {adminVieScolaireController} from '../controllers/vsco_acu_personnel_ctrl';

ng.controllers.push(viescolaireController);
ng.controllers.push(adminVieScolaireController);

ng.directives.push(sticky);

routes.define(function($routeProvider) {
    $routeProvider
        .when('/viescolaire/accueil', {
            action: 'accueil'
        }).otherwise({
            redirectTo : '/viescolaire/accueil'
        });
});