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
        $scope.chargeStructure = (structure) =>  {
            structure.classes.sync();
        };
        $scope.changeSelection = function (elem){
            if (elem) {
                elem = ! elem;
            }
            else {
                elem = true;
            }
            $scope.safeApply($scope);
        };
        $scope.selectCycle = function (cycle) {
            $scope.lastSelectedCycle.selected = false;
            cycle.selected = true;
            $scope.lastSelectedCycle = cycle;
            $scope.safeApply($scope);

        };
        // Sauvegarder niveau de maitrise
        $scope.saveNiveau = function(level){
          level.save((res) => {
                console.dir(res);
          });
        };


        $scope.openDeletePerso = function () {
            $scope.opened.lightboxDeletePerso = true;
        };

        $scope.deletePerso = function () {
          $scope.structure.deletePerso().then( () => {
            $scope.structure.cycles = vieScolaire.structure.cycles;
              if ($scope.structure.cycles.length > 0) {
                  $scope.lastSelectedCycle = $scope.structure.cycles[0];
                  $scope.lastSelectedCycle.selected = true;
              }
              $scope.opened.lightboxDeletePerso = false;
            $scope.safeApply($scope);
          });
        };

        $scope.formatDate = function(pODateDebut, pODateFin) {
            return (moment(pODateDebut).format('DD/MM/YYYY') + " " + moment(pODateDebut).format('HH:mm') + "-" + moment(pODateFin).format('HH:mm'));
        };
    }
]);