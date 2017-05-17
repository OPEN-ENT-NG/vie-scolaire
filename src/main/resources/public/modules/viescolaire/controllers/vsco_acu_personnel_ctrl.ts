/**
 * Created by ledunoiss on 13/09/2016.
 */
import {notify, idiom as lang, template, routes, model, ng } from 'entcore/entcore';
import { vieScolaire} from '../models/vsco_personnel_mdl';

let moment = require('moment');
declare let _: any;

export let adminVieScolaireController = ng.controller('VscoAdminController', [
    '$scope', 'route', 'model',
    function ($scope, route, model) {
        $scope.structures = vieScolaire.structures;

        $scope.formatDate = function(pODateDebut, pODateFin) {
            return (moment(pODateDebut).format('DD/MM/YYYY') + " " + moment(pODateDebut).format('HH:mm') + "-" + moment(pODateFin).format('HH:mm'));
        };
    }
]);