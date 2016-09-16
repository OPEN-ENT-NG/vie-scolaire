/**
 * Created by ledunoiss on 12/09/2016.
 */
/**
 * Created by ledunoiss on 12/09/2016.
 */
import { notify, idiom as lang, template, ui, http, routes, ng } from 'entcore/entcore';
import {vieScolaire, Appel, Classe, Eleve, Enseignant, Evenement, Justificatif, Matiere, Motif, Responsable, VieScolaire} from '../models/absc_personnel_mdl';

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
        .when('/sansmotifs',{action:'AbsencesSansMotifs'})
        .when('/appels/noneffectues', {action:'AppelsOublies'})
        .when('/redirect', {action:'Redirect'})
        .otherwise({
            redirectTo : '/redirect'
        });
});