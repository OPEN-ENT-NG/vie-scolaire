import { template, ng } from 'entcore/entcore';
import { presences } from '../models/absc_personnel_mdl';
import * as utils from '../utils/personnel';

let moment = require('moment');
declare let _: any;

export let absencesController = ng.controller('AbsencesController', [
    '$scope', 'route', 'model', '$rootScope', '$location', '$route',
    function ($scope, route, model, $rootScope, $location, $route) {
       const routesActions = {
            AbsencesSansMotifs: function (params) {
                template.open('menu', '../templates/absences/absc_personnel_menu');
                template.open('header', '../templates/absences/absc_personnel_header');
                template.open('main', '../templates/absences/absc_personnel_abssm');
                $scope.display.menu = false;
            },
            AppelsOublies : function(params) {
                template.open('menu', '../templates/absences/absc_personnel_menu');
                template.open('header', '../templates/absences/absc_personnel_header');
                template.open('main', '../templates/absences/absc_personnel_appo');
                $scope.display.menu = false;
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
            }
        };

        route(routesActions);

        let getCurrentAction = function (): string {
            return $route.current.$$route.action;
        };


        let executeAction = function (): void {
            routesActions[getCurrentAction()]($route.current.params);
        };

        $scope.display = {
            responsables : false,
            menu : false
        };

        /**
         * Critères de tris
         */
        $scope.pOSortParameters = {
            sortType : '',
            sortReverse : false
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

        $rootScope.$on('$routeChangeSuccess', ($currentRoute, $previousRoute, $location) => {
            utils.safeApply($scope);
        });

        /** Fonction D'initialisation de structure **/
        let initialiseStructure = function (structure) {
            $scope.structure = structure;

            $scope.structure.classes.on('sync', function () {
                $scope.structure.classes.map(function (classe) {
                    classe.selected = true;
                    return classe;
                });
            });

            $scope.structure.enseignants.on('sync', function () {
                $scope.structure.enseignants.map(function (enseignant) {
                    enseignant.selected = true;
                    return enseignant;
                });
            });

            $scope.structure.motifs.on('sync', function() {
                $scope.structure.motifs.synced = true;
            });

            $scope.structure.appels.on('sync', function () {
                if ($scope.structure.enseignants !== undefined
                    && $scope.structure.enseignants.all.length > 0) {
                    _.map($scope.structure.appels.all, (appel) => {
                        let enseignant = $scope.structure.enseignants.findWhere({id: appel.id_personnel});
                        appel.personnel_prenom = enseignant.firstName;
                        appel.personnel_nom = enseignant.lastName;
                        return appel;
                    });
                }
                if ($scope.structure.classes !== undefined
                    && $scope.structure.classes.all.length > 0) {
                    _.map($scope.structure.appels.all, (appel) => {
                        let classe = $scope.structure.classes.findWhere({id: appel.id_classe});
                        appel.classe_libelle = classe.name;
                        return appel;
                    });
                }
                if ($scope.structure.matieres !== undefined
                    && $scope.structure.matieres.all.length > 0) {
                    _.map($scope.structure.appels.all, (appel) => {
                        let matiere = $scope.structure.matieres.findWhere({id: appel.id_matiere});
                        appel.cours_matiere = matiere != null ? matiere.name : "";
                        return appel;
                    });
                }
            });

            $scope.structure.isWidget = true;
            $scope.structure.sync().then(() => {
                $scope.structure.isWidget = true;
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
                            $scope.structure.appels.sync($scope.periode.debut, $scope.periode.fin);
                            utils.safeApply($scope);
                        }
                    }
                };
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
                initialiseStructure(presences.structures.first());
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
        }
    }
]);