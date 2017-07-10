import { template, ng } from 'entcore/entcore';
import { presences } from '../models/absc_personnel_mdl';

let moment = require('moment');
declare let _: any;

export let vscoAppoPersonnelController = ng.controller('VscoAppoPersonnelController', [
    '$scope', 'route', '$rootScope', '$location',
    function ($scope, route, $rootScope, $location) {
        template.open('AbscFiltres', '../templates/absences/absc_personnel_filtres');
        $scope.pOFilterAppel = { // Objet permettant le filtre des appels oubliés / non oubliés
            noneffectues : true
        };
        $scope.psDisplayResponsables = false;
        $scope.selectedAppels = [];
        $scope.periode.fin = new Date();
        $scope.structure.isWidget = false;
        $scope.structure.appels.sync($scope.periode.debut, $scope.periode.fin);

        $scope.formatDate = function(pODateDebut, pODateFin) {
            return (moment(pODateDebut).format('DD/MM/YYYY') + " " + moment(pODateDebut).format('HH:mm') + "-" + moment(pODateFin).format('HH:mm'));
        };

        $scope.appelFilterFunction = function(appel) {
            if ($scope.pOFilterAppel.noneffectues) {
                return (appel.id_etat !== 3);
            }
            return true;
        };

        $scope.applyAppelFilter = function() {
            $scope.appelFilter = $scope.pOFilterAppel.noneffectues ? $scope.appelFilterFunction : null;
        };

        $scope.checkAppel = function(appel) {
            let index = _.indexOf($scope.selectedAppels, appel);
            if (index === -1) {
                $scope.selectedAppels.push(appel);
            } else {
                $scope.selectedAppels.splice(index, 1);
            }
        };

        // On set le filter sur les appels non effectues.
        $scope.appelFilter = $scope.appelFilterFunction;
    }
]);