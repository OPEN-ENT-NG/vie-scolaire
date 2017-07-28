import { ng, template } from "entcore";
import * as utils from "../utils/parent";
import { presences } from '../models/absc_parent_mdl';

export let abscParentController = ng.controller('AbscParentController', [
    '$scope', 'route', 'model', '$rootScope', '$location', '$route',
    function ($scope, route, model, $rootScope, $location, $route) {
        route({
            AbsencesParentAccueil : async (params) =>  {
                await presences.sync();
                $scope.structure = presences.structure;
                await $scope.structure.eleves.sync();
                $scope.eleves = $scope.structure.eleves;
                template.open('menu','../templates/absences/absc_parent_menu');
                template.open('main','../templates/absences/absc_parent_absEnfants');
                utils.safeApply($scope);
            }
        });

        template.open('selectEnfants', '../templates/absences/absc_parent_selectEnfants');

        $scope.evenements = [];

        $scope.pOSortParameters = {
            sortType: null,
            sortReverse: false
        };

        // Permet le choix d'un élève et la synchronisation des évènements de l'élève
        $scope.chooseChild = async (eleve) => {
            if($scope.eleve == null || $scope.eleve.id != eleve.id) {
                $scope.eleve = eleve;
                await $scope.eleve.syncEvents();
                $scope.evenements = $scope.eleve.evenements.all;
                utils.safeApply($scope);
            }
        };

        $scope.formatDate = (dateDt, dateFn) => {
            return moment(dateDt).format('DD/MM/YYYY HH:mm') + " à " + moment(dateFn).format('DD/MM/YYYY HH:mm');
        };

        // $scope.convertDate = (date) => {
        //     if(date instanceof String) {
        //         return moment(date);
        //     } else {
        //         return date.format()
        //     }
        // }
    }]);