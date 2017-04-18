/**
 * Created by ledunoiss on 13/09/2016.
 */
import {notify, idiom as lang, template, routes, model, ng } from 'entcore/entcore';
import {Evenement, Appel, Observation, vieScolaire, WAbsSansMotifs, WAppelsOublies, Widget, WObservations} from '../models/vsco_personnel_mdl';

let moment = require('moment');
declare let _:any;

export let acuVieScolaireController = ng.controller('VscoAcuPersonnelController', [
    '$scope', 'route', 'model',
    function ($scope, route, model) {
        $scope.widget = vieScolaire.widget;

        $scope.formatDate = function(pODateDebut, pODateFin){
            return (moment(pODateDebut).format('DD/MM/YYYY') + " " + moment(pODateDebut).format('HH:mm') + "-" + moment(pODateFin).format('HH:mm'));
        };
    }
]);