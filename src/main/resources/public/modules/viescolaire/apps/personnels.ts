/**
 * Created by ledunoiss on 12/09/2016.
 */

import { idiom as lang, routes, ng } from 'entcore/entcore';

import { sticky } from '../../utils/directives/sticky';
import  {cSkillsBubble } from "../../evaluations/directives/cSkillsBubble"
import {viescolaireController} from '../controllers/vsco_personnel_ctrl';

ng.controllers.push(viescolaireController);



import {periodeSearch} from "../filtres/periodeSearch";
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