/**
 * Created by ledunoiss on 12/09/2016.
 */

import { notify, idiom as lang, template, ui, http, routes, ng } from '../../entcore/entcore';
import {Evenement, Appel, Observation, vieScolaire, WAbsSansMotifs, WAppelsOublies, Widget, WObservations} from '../models/vsco_personnel_mdl';

import {viescolaireController} from '../controllers/vsco_personnel_ctrl';
import {acuVieScolaireController} from '../controllers/vsco_acu_personnel_ctrl';

ng.controllers.push(viescolaireController);
ng.controllers.push(acuVieScolaireController);

routes.define(function($routeProvider){
    $routeProvider
        .when('/viescolaire/accueil',{
            action:'accueil'
        }).otherwise({
            redirectTo : '/viescolaire/accueil'
        });
});