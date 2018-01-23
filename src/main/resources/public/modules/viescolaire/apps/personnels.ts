/**
 * Created by ledunoiss on 12/09/2016.
 */

import { routes, ng } from 'entcore';

import { sticky } from '../../utils/directives/sticky';
import { cSkillsBubble } from '../../utils/directives/cSkillsBubble'
import {viescolaireController} from '../controllers/vsco_personnel_ctrl';
import {adminVieScolaireController} from '../controllers/vsco_acu_personnel_ctrl';
import {periodeSearch} from "../filtres/periodeSearch";

ng.controllers.push(viescolaireController);
ng.controllers.push(adminVieScolaireController);

ng.filters.push(periodeSearch);

ng.directives.push(sticky);
ng.directives.push(cSkillsBubble);

routes.define(function($routeProvider) {
    $routeProvider
        .when('/viescolaire/accueil', {
            action: 'accueil'
        }).otherwise({
            redirectTo : '/viescolaire/accueil'
        });
});