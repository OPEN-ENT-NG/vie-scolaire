import { template, ng } from 'entcore';
import { presences } from '../models/absc_personnel_mdl';
import * as utils from '../utils/personnel';

let moment = require('moment');
declare let _: any;

export let absencesController = ng.controller('AbsencesController', [
    '$scope', 'route', 'model', '$rootScope', '$location', '$route',
    function ($scope, route, model, $rootScope, $location, $route) {
        const routesActions = {
            AbsencesSansMotifs: function (params) {
                $scope.display.menu = false;
                $scope.display.dates = true;
                template.open('menu', '../templates/absences/absc_personnel_menu');
                template.open('header', '../templates/absences/absc_personnel_header');
                template.open('AbscHeadDate', '../templates/absences/absc_personnel_head_date');
                template.open('main', '../templates/absences/absc_personnel_abssm');
                utils.safeApply($scope);
            },
            AppelsOublies : function(params) {
                $scope.display.menu = false;
                $scope.display.dates = true;
                template.open('menu', '../templates/absences/absc_personnel_menu');
                template.open('header', '../templates/absences/absc_personnel_header');
                template.open('AbscHeadDate', '../templates/absences/absc_personnel_head_date');
                template.open('main', '../templates/absences/absc_personnel_appo');
                utils.safeApply($scope);
            },
            Appel : function(params) {
                template.close('menu');
                template.close('header');
                template.open('main', '../templates/absences/absc_appel');
            },
            Redirect : function(params) {
                template.close('menu');
                template.close('header');
                template.open('main', '../templates/absences/absc_personnel_acu');

            },
            Accueil: function (params) {
                template.close('menu');
                template.close('header');
                template.open('main', '../templates/absences/absc_personnel_acu');
            },
            disabled : (params) => {
                template.close('menu');
                template.close('header');
                template.open('main', '../templates/absences/absc_disabled_structure');
                utils.safeApply($scope);
            },
            SaisieAbsEleve: function (params) {
                $scope.display.menu = false;
                $scope.display.dates = false;
                template.open('header', '../templates/absences/absc_personnel_header');
                template.open('menu', '../templates/absences/absc_personnel_menu');
                template.open('main', '../templates/absences/absc_personnel_saisie_abs_eleve');
                utils.safeApply($scope);
            }
        };

        route(routesActions);
        $scope.display = {
            responsables : false,
            menu : false
        };
        let getCurrentAction = function (): string {
            return $route.current.$$route.action;
        };

        let executeAction = function (): void {
            routesActions[getCurrentAction()]($route.current.params);
        };

        /**
         * Critères de tris
         */
        $scope.pOSortParameters = {
            sortType : '',
            sortReverse : false
        };

        $scope.openAppel = (appel) => {
            $scope.selectedAppel = {
                timestamp: appel.timestamp_dt,
                id_cours: appel.id_cours,
                id_personnel: appel.id_personnel
            };
            $location.path("/appel");
            $location.replace();
            utils.safeApply($scope);
        };

        $scope.formatDate = function(pODateDebut, pODateFin) {
            return (moment(pODateDebut).format('DD/MM/YYYY') + " " + moment(pODateDebut).format('HH:mm') + "-" + moment(pODateFin).format('HH:mm'));
        };

        /**
         * Critères de recherches personnels
         */
        $scope.pORecherche = {};

        /**
         * Définition des périodes
         */
        $scope.periode = {
            debut : new Date(),
            fin : new Date()
        };

        /**
         * Initialisation des structures
         */
        $scope.structures = presences.structures;

        $scope.displayStructureLoader = false;

        $rootScope.$on('$routeChangeSuccess', ($currentRoute, $previousRoute, $location) => {
            $scope.structure = presences.structure;
            utils.safeApply($scope);
        });

        $scope.loadData = function () {

            if (($scope.periode.fin.getTime() - $scope.periode.debut.getTime()) > 0) {
                if ($location.path() === "/sansmotifs") {
                    $scope.structure.isWidget = false;
                    $scope.structure.evenements.sync($scope.periode.debut, $scope.periode.fin);
                    utils.safeApply($scope);
                } else if ($location.path() === "/appels/noneffectues") {
                    $scope.structure.isWidget = false;
                    $scope.structure.appels.sync($scope.periode.debut, $scope.periode.fin);
                    utils.safeApply($scope);
                } else {
                    $scope.structure.isWidget = true;
                    $scope.structure.appels.sync();
                    $scope.structure.evenements.sync();
                    utils.safeApply($scope);
                }
            }
        };

        $scope.changeStructure = function (structure) {
            template.close('main');
            $scope.$parent.displayStructureLoader = true;
            $scope.displayStructureLoader = true;
            $scope.structure = structure;
            presences.structure = structure;
            utils.safeApply($scope);
            presences.structures.sync(presences.structures.all.indexOf(structure)).then(() => {
                $scope.structures = presences.structures;
                $scope.initialiseStructure(structure);
                if ($location.path() === '/sansmotifs') {
                    // demande le chargement des absences après un chargement de structure
                    $rootScope.$broadcast('reloadAbsences');
                    $scope.$on('AbsencesLoaded', function () {
                        $scope.$parent.displayStructureLoader = false;
                        $scope.displayStructureLoader = false;
                    });
                }
                else if ($location.path() == '/appel') {
                    $rootScope.$broadcast('syncAppel');
                    $scope.$on('AppelLoaded', function () {
                        $scope.$parent.displayStructureLoader = false;
                        $scope.displayStructureLoader = false;
                    });
                } else if ($location.path() !== '/appels/noneffectues') {
                    $scope.$parent.displayStructureLoader = false;
                    $scope.displayStructureLoader = false;
                }
                utils.safeApply($scope);
            });
        };

        /** Fonction D'initialisation de structure **/
        $scope.initialiseStructure = function (structure) {
            $scope.structure = structure;

            $scope.structure.isWidget = true;
            $scope.structure.sync().then(() => {
                $scope.$parent.displayStructureLoader = false;
                $scope.displayStructureLoader = false;
                $scope.loadData();
            });


            if ($location.path() === '/disabled') {
                $location.path('/');
                $location.replace();
            } else {
                executeAction();
            }
        };

        presences.structures.sync().then(() => {
            if (!presences.structures.empty()) {
                /** On initiliase tout d'abord la structure **/
                if (presences.structure === undefined) {
                    $scope.structure = presences.structures.first();
                }
                else {
                    $scope.structure = presences.structure;
                }
                $scope.initialiseStructure($scope.structure);
            } else {
                $location.path() === '/disabled' ?
                    executeAction() :
                    $location.path('/disabled');
                $location.replace();
            }
        });

        $scope.goTo = function(path, id ) {
            $location.path(path);
            if (id !== undefined) {
                $location.search(id);
            }
            $location.replace();
            utils.safeApply($scope);
        };

        $scope.goToState = function(path) {
            $location.path(path);
            $location.replace();
        };

        $scope.personnelFilter = function(event) {
            return $scope.classeFilter(event) && $scope.enseignantFilter(event);
        };
        $scope.classeFilter = function(event) {
            return ($scope.structure.classes.findWhere({id : event.id_classe, selected: true}) !== undefined);
        };

        $scope.enseignantFilter = function(event) {
            return ($scope.structure.enseignants.findWhere({id : event.id_personnel, selected: true}) !== undefined);
        };

        // $scope.alert = function(message) {
        //     alert(message);
        // };

        $scope.resetFilter = () => {
            $rootScope.$broadcast("resetFilter");
        };
    }
]);