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
                template.open('main', '../templates/viescolaire/vsco_acu_personnel');

            },
            Accueil: function (params) {
                template.close('menu');
                template.close('header');
                template.open('main', '../templates/viescolaire/vsco_acu_personnel');
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

        /** On initiliase tout d'abord la structure **/
        presences.structures.sync().then(() => {
            if (!presences.structures.empty()) {
                $scope.structure = presences.structures.first();
                presences.structure = $scope.structure;
                $scope.structure.sync().then(() => {
                    $scope.appels = presences.structure.appels;
                    $scope.classes = presences.structure.classes;
                    $scope.enseignants = presences.structure.enseignants;
                    $scope.matieres = presences.structure.matieres;
                    $scope.evenements = presences.structure.evenements;
                    $scope.motifs = presences.structure.motifs;
                    $scope.justificatifs = presences.structure.justificatifs;
                    presences.structure = $scope.structure;
                    $scope.loadData = function() {
                        if (($scope.periode.fin.getTime() - $scope.periode.debut.getTime()) > 0) {
                            if ($location.path() === "/sansmotifs") {
                                presences.structure.evenements.sync($scope.periode.debut, $scope.periode.fin);
                                utils.safeApply($scope);
                            }else if ($location.path() === "/appels/noneffectues") {
                                presences.structure.appels.sync($scope.periode.debut, $scope.periode.fin);
                                utils.safeApply($scope);
                            }
                        }
                    };
                    presences.structure.classes.on('sync', function() {
                        presences.structure.classes.map(function(classe) {
                            classe.selected = true;
                            return classe;
                        });
                    });

                    presences.structure.enseignants.on('sync', function() {
                        presences.structure.enseignants.map(function(enseignant) {
                            enseignant.selected = true;
                            return enseignant;
                        });
                    });

                    presences.structure.appels.on('sync', function() {
                        if ( $scope.enseignants !== undefined
                            && $scope.enseignants.all.length > 0 ) {
                            _.map(presences.structure.appels.all, (appel) => {
                                let enseignant = $scope.enseignants.findWhere({id : appel.id_personnel});
                                appel.personnel_prenom = enseignant.firstName;
                                appel.personnel_nom = enseignant.lastName ;
                                return appel;
                            });
                        }
                        if ( $scope.classes !== undefined
                            && $scope.classes.all.length > 0 ) {
                            _.map(presences.structure.appels.all, (appel) => {
                                let classe = $scope.classes.findWhere({id : appel.id_classe});
                                appel.classe_libelle = classe.name;
                                return appel;
                            });
                        }
                        if ( $scope.matieres !== undefined
                            && $scope.matieres.all.length > 0 ) {
                            _.map(presences.structure.appels.all, (appel) => {
                                let matiere = $scope.matieres.findWhere({id : appel.id_matiere});
                                appel.cours_matiere = matiere.name;
                                return appel;
                            });
                        }
                    });
                    // presences.structure.motifs.on('sync', function() {
                    //     presences.structure.motifs.synced = true;
                    // });
                });

                if ($location.path() === '/disabled') {
                    $location.path('/');
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

        $scope.alert = function(message) {
            alert(message);
        };
    }
]);