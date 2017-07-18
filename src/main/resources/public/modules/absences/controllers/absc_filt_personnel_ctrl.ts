/**
 * Created by ledunoiss on 19/02/2016.
 */
import {ng } from 'entcore/entcore';
import * as utils from '../utils/personnel';
import {presences} from "../models/absc_personnel_mdl";


let moment = require('moment');
declare let _: any;

export let abscFiltresPersonnelController = ng.controller('AbscFiltresPersonnelController', [
    '$scope', 'route', '$rootScope', '$location',
    function ($scope, route, model, $rootScope, $location) {
        $scope.pOResponsable = [];
        $scope.pOOpenedResponsable = null;
        $scope.bSelectAllEnseignants = true;
        $scope.pOFilterCtrl = {
            enseignants : true,
            classes : true,
            responsables : false,
            structures: false
        };
        //
        // $scope.switchAll = function(oListe, b) {
        //     oListe.each(function(o) {
        //         o.selected = b;
        //     });
        // };
        $scope.switchAll = function(oListe) {
                oListe.each(function(o) {
                    o.selected = $scope.bSelectAllEnseignants;
                });
            };


        $scope.$watch('$parent.pOSelectedEvent', () => {
            if ($scope.$parent.pOSelectedEvent == null) {
                $scope.pOResponsable = [];
                $scope.pOOpenedResponsable = null;
                $scope.pOFilterCtrl.responsables = false;
            } else {
                $scope.syncEleve();
            }
        });

        $scope.syncEleve = () => {
            if ($scope.structure.synchronized.eleves === false) {
                $scope.structure.eleves.sync().then(() => {
                    $scope.syncResponsable();
                });
            } else {
                $scope.syncResponsable();
            }
        };

        $scope.syncResponsable = async () => {
            let eleve = _.findWhere($scope.structure.eleves.all, {id: $scope.$parent.pOSelectedEvent.id});
            if (eleve.responsables === undefined || _.isEmpty(eleve.responsables.all))  {
                await eleve.responsables.sync();
            }

            $scope.pOResponsable = eleve.responsables.all;
            if (_.isEmpty($scope.pOResponsable)) {
                $scope.$parent.psDisplayResponsables = false;
                $scope.pOFilterCtrl.responsables = false;
            } else {
                $scope.$parent.psDisplayResponsables = true;
                $scope.pOFilterCtrl.responsables = true;
            }
            utils.safeApply($scope);
        };

        $scope.openResponsable = (id) => {
            if ($scope.pOOpenedResponsable != id) {
                $scope.pOOpenedResponsable = id;
            } else {
                $scope.pOOpenedResponsable = null;
            }
            utils.safeApply($scope);
        };

        $scope.$on("resetFilter", () => {
            _.each($scope.pOFilterCtrl, (val, key) => {
                $scope.pOFilterCtrl[key] = false;
            });
            $scope.pOOpenedResponsable = null;
            $scope.pOResponsable = [];
            utils.safeApply($scope);
        });
    }
]);