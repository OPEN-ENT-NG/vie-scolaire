import { ng, template } from "entcore";
import * as utils from "../utils/parent";
import { presences } from '../models/absc_parent_mdl';

export let abscParentController = ng.controller('AbscParentController', [
    '$scope', 'route', 'model', '$rootScope', '$location', '$route',
    async ($scope, route, model, $rootScope, $location, $route) => {

        route({
            Accueil: (params) => {
                template.open('header', '../templates/absences/absc_parent_selectEnfants');
                template.open('menu', '../templates/absences/absc_parent_menu');
                template.open('main', '../templates/absences/absc_parent_acu');
                utils.safeApply($scope);
            },
        });

        $scope.evenements = [];

        $scope.pOSortParameters = {
            sortType: null,
            sortReverse: false
        };

        $scope.piFilterAbsences = 20;

        // Permet le choix d'un élève et la synchronisation des évènements et déclarations de l'élève
        $scope.chooseChild = async (eleve, number?) => {
            if ($scope.selectedEleve == null || $scope.selectedEleve.id != eleve.id) {
                $scope.selectedEleve = eleve;
                await $scope.selectedEleve.syncEvents();
                $scope.evenements = $scope.selectedEleve.evenements.all;
                utils.safeApply($scope);
            }
        };

        $rootScope.$on('$routeChangeSuccess', async () => {
            if($scope.selectedEleve != null) {
                await $scope.selectedEleve.syncEvents();
                $scope.evenements = $scope.selectedEleve.evenements.all;
            }
        });

        $scope.formatDate = (dateDt, dateFn?, format?) => {
            let _return = "";
            let _dateDt = moment(dateDt);
            let _format = format ? format : "DD/MM/YYYY HH:mm";
            if(dateFn) {
                let _dateFn = moment(dateFn);

                if(_dateDt.diff(_dateFn, 'days') < 1) {
                    _return = _dateDt.format(_format) + " - " + _dateFn.format('HH:mm');
                } else {
                    _return = _dateDt.format(_format) + " " + _dateFn.format(_format);
                }
            } else {
                _return = _dateDt.format(_format);
            }
            return _return;
        };

        await presences.sync();
        $scope.structure = presences.structure;
        await $scope.structure.eleves.sync();
        $scope.eleves = $scope.structure.eleves.all;
        $scope.selectedEleve = _.first($scope.eleves);
        await $scope.selectedEleve.syncEvents();
        $scope.evenements = $scope.selectedEleve.evenements.all;
        utils.safeApply($scope);
    }]);