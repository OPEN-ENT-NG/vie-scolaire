/**
 * Created by ledunoiss on 19/02/2016.
 */
import {ng } from 'entcore/entcore';
import * as utils from '../utils/personnel';

let moment = require('moment');
declare let _: any;

export let abscFiltresPersonnelController = ng.controller('AbscFiltresPersonnelController', [
    '$scope', 'route', '$rootScope', '$location',
    function ($scope, route, model, $rootScope, $location) {
        $scope.pOResponsable = [];
        $scope.pOOpenedResponsable = null;

        $scope.pOFilterCtrl = {
            enseignants : false,
            classes : false,
            responsables : false
        };

        $scope.switchAll = function(oListe, b) {
            oListe.each(function(o) {
                o.selected = b;
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
            if ($scope.structure.synchronized.eleves == false) {
                $scope.structure.eleves.sync().then(() => {
                    $scope.syncResponsable();
                });
            } else {
                $scope.syncResponsable();
            }
        };

        $scope.syncResponsable = () => {
            let eleve = _.findWhere($scope.structure.eleves.all, {id: $scope.$parent.pOSelectedEvent.id});
            if (_.isEmpty(eleve.responsables.all)) {
                eleve.responsables.sync().then(() => {
                    $scope.pOResponsable = eleve.responsables.all;
                    utils.safeApply($scope);
                })
            } else {
                $scope.pOResponsable = eleve.responsables.all;
                utils.safeApply($scope);
            }
        };

        $scope.openResponsable = (id) => {
            if ($scope.pOOpenedResponsable != id) {
                $scope.pOOpenedResponsable = id;
            } else {
                $scope.pOOpenedResponsable = null;
            }
            utils.safeApply($scope);
        }
    }
]);