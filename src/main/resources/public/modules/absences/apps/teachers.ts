/**
 * Created by ledunoiss on 12/09/2016.
 */
import { notify, idiom as lang, template, ui, http, routes, ng } from 'entcore/entcore';
import {AbsencePrev, Appel, Cours, Creneau, Eleve, Evenement, Plage, VieScolaire, vieScolaire} from '../models/absc_enseignant_mdl';
import {absencesController} from '../controllers/absc_enseignant_ctrl';
import {mobilePanel} from '../../utils/directives/absences/mobile-panel';

ng.controllers.push(absencesController);

ng.directives.push(mobilePanel);

routes.define(function($routeProvider){
    $routeProvider
        .when('/appel',{action:'appel'})
        .otherwise({
            redirectTo : '/appel'
        });
});