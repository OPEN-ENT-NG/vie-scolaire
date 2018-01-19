import { template, ng } from 'entcore';
import { presences } from '../models/absc_enseignant_mdl';
import * as utils from '../utils/personnel';

let moment = require('moment');
declare let _: any;

export let absencesController = ng.controller('AbsencesController', [
    '$scope', 'route', 'model', '$rootScope', '$location', '$route',
    function ($scope, route, model, $rootScope, $location, $route) {
        const routesActions = {
            appel: (params) => {
                template.open('main', '../templates/absences/absc_appel');
                utils.safeApply($scope);
            },
            disabled : (params) => {
                template.open('main', '../templates/absences/absc_disabled_structure');
                utils.safeApply($scope);
            }
        };

        route(routesActions);

        $scope.template = template;

        /**
         * Message pour les fonctionnalié pas encore développées
         */
        $scope.alertNonImplementee = function() {
            alert("Fonctionnalité actuellement non implémentée.");
        };

        let getCurrentAction = function (): string {
            return $route.current.$$route.action;
        };

        let executeAction = function (): void {
            routesActions[getCurrentAction()]($route.current.params);
        };

        /**
         *  permet de changer l'établissement courrant
         * @param structure
         */
        $scope.changeStructure = async function (structure) {
            $scope.$parent.displayStructureLoader = true;
            $scope.displayStructureLoader = true;

            $scope.structure = structure;

            await $scope.structure.plages.sync();
            await $scope.structure.classes.sync();
            await $scope.structure.courss.sync(moment().format(FORMAT.date), moment().add(1, 'days').format(FORMAT.date)
                , model.me.userId, false, model.me.classNames);

            $scope.$parent.displayStructureLoader = false;
            $scope.displayStructureLoader = false;
            presences.structure = structure;
            utils.safeApply($scope);
        };

        presences.structures.sync().then( async () => {
            if (!presences.structures.empty()) {
                $scope.structures = presences.structures;
                $scope.structure = presences.structures.first();
                await $scope.structure.plages.sync();
                await $scope.structure.classes.sync();
                await $scope.structure.courss.sync(moment().format(FORMAT.date), moment().add(1, 'days').format(FORMAT.date)
                    , model.me.userId, false, model.me.classNames);
                if ($location.path() === '/disabled') {
                    $location.path('/appel');
                    $location.replace();
                } else {
                    executeAction();
                }
            } else {
                $location.path() === '/disabled' ?
                    executeAction() :
                    $location.path('/disabled');
                $location.replace();
            }
        });
    }
]);