/**
 * Created by ledunoiss on 13/09/2016.
 */
import {notify, idiom as lang, template, routes, model, ng } from '../../entcore/entcore';

let moment = require('moment');
let _ = require('underscore');

export let acuVieScolaireController = ng.controller('VscoAcuPersonnelController', [
    '$scope', 'route', 'model',
    function ($scope, route, model) {
        $scope.widget = model.widget;

        $scope.formatDate = function(pODateDebut, pODateFin){
            return (moment(pODateDebut).format('DD/MM/YYYY')+" "+moment(pODateDebut).format('HH:mm')+"-"+moment(pODateFin).format('HH:mm'))
        };
    }
]);