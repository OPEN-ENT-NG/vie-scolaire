/**
 * Created by ledunoiss on 12/09/2016.
 */
import { notify, idiom as lang, template, ui, http, routes, ng } from '../../entcore/entcore';
import {AbsencePrev, Appel, Cours, Creneau, Eleve, Evenement, Plage, VieScolaire, vieScolaire} from '../models/absc_enseignant_mdl';
import {absencesController} from '../controllers/absc_enseignant_ctrl';

ng.controllers.push(absencesController);

routes.define(function($routeProvider){
    $routeProvider
        .when('/viescolaire/absences/appel',{action:'appel'})
        .otherwise({
            redirectTo : '/viescolaire/absences/appel'
        });
});