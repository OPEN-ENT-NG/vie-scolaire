import {notify, idiom as lang, template, routes, model, ng } from 'entcore/entcore';
import {vieScolaire} from '../models/vsco_personnel_mdl';
import {Motif} from "../../absences/models/personnel/Motif";
import {Categorie} from "../../absences/models/personnel/Categorie";
import {MotifAppel} from "../../absences/models/personnel/MotifAppel";
import {CategorieAppel} from "../../absences/models/personnel/CategorieAppel";

let moment = require('moment');
declare let _: any;

export let viescolaireController = ng.controller('ViescolaireController', [
    '$scope', 'route', 'model', '$location', '$anchorScroll',
    function ($scope, route, model, $location, $anchorScroll) {
        $scope.template = template;
        $scope.lang = lang;
        $scope.opened = {
            lightboxCreateMotif: false
        };
        $scope.selected = {
            categories: [],
            motifs: [],
            all: []
        };
        $scope.safeApply = function(fn) {
            let phase = this.$root.$$phase;
            if (phase === '$apply' || phase === '$digest') {
                if (fn && (typeof(fn) === 'function')) {
                    fn();
                }
            } else {
                this.$apply(fn);
            }
        };

        // check si on doit afficher le menu viescolaire.
        $scope.hasParam = function (param) {
            return Object.prototype.hasOwnProperty.call($location.search(), param);
        };

        // recherche le module dont on veut aficher les paramètres.
        $scope.findParam = function (key) {
            if ($scope.hasParam(key)) {
                return ($location.search())[key];
            } else {
                $scope.hasFilterParam = true;
                return false;
            }
        };

        // scroller vers un élément donné
        $scope.scrollToElement = function(idElement) {
            let top = document.getElementById(idElement).offsetTop; //Getting Y of target element
            window.scrollTo(0, top);
        };


        $scope.openCreateMotif = function () {
            $scope.displayCreateCategorie = false;
            $scope.displayText = lang.translate("viescolaire.create.motif");
            $scope.opened.lightboxCreateMotif = true;
            $scope.safeApply($scope);
        };
        $scope.slideAll = function (isAppel) {
            if (isAppel) {
                $scope.structure.categorieAppels.slidedAll = !$scope.structure.categorieAppels.slidedAll;
                $scope.structure.categorieAppels.map(function (categorieAppel) {
                    categorieAppel.slided = $scope.structure.categorieAppels.slidedAll;
                });
            }
            else {
                $scope.structure.categories.slidedAll = !$scope.structure.categories.slidedAll;
                $scope.structure.categories.map(function (categorie) {
                  categorie.slided = $scope.structure.categories.slidedAll;
                });
            }
            $scope.safeApply($scope);
        };
        $scope.createMotif = function(isAppel) {
            $scope.isAppel = isAppel;
            $scope.newMotif = {
                commentaire: undefined,
                defaut: false,
                justifiant: false,
                libelle: undefined,
                id_etablissement: $scope.structure.id
            };
            $scope.openCreateMotif();
        };

        $scope.createCategorie = function (isAppel) {
            $scope.isAppel = isAppel;
            if (isAppel !== undefined) {
                $scope.isAppel = isAppel;
            }
            else if ($scope.selected !== undefined
                && $scope.selected.motifs !== undefined
                && $scope.selected.motifs[0] !== undefined
                && $scope.selected.motifs[0].is_appel_oublie !== undefined) {
                $scope.isAppel = $scope.selected.motifs[0].is_appel_oublie;
            }
            else {
                $scope.isAppel = false;
            }
            $scope.displayCreateCategorie = true;
            $scope.displayText = lang.translate("viescolaire.create.categorie");
            $scope.newCategorie = {
                libelle: undefined,
                id_etablissement: $scope.structure.id
            };
            $scope.opened.lightboxCreateMotif = true;
            $scope.isModif = false;
            $scope.safeApply($scope);
        };


        // sélection d'un motif
        $scope.selectMotif = function (motif) {
            let index = _.indexOf($scope.selected.motifs, motif);
            if (index === -1) {
                $scope.selected.motifs.push(motif);
            }
            else {
                $scope.selected.motifs = _.without($scope.selected.motifs, motif);
            }
        };

        $scope.isStringUndefinedOrEmpty = function (libelle) {
            if(libelle === undefined || null === libelle){
                return true;
            }else if(libelle.length === 0 || libelle.trim().length === 0){
                return true;
            }else{
                return false;
            }
        }

        // sélection d'une Catégorie
        $scope.selectCategorie = function (categorie) {
            let index = _.indexOf($scope.selected.categories, categorie);
            if (index === -1) {
                $scope.selected.categories.push(categorie);
            }
            else {
                $scope.selected.categories = _.without($scope.selected.categories, categorie);
            }
        };
        // enregistrement d'un motif d'absence
        $scope.saveMotif = function () {
            let _newMotif = new Motif($scope.newMotif);
            if (_newMotif.id) {
                $scope.selectMotif($scope.newMotif);
            }
            _newMotif.save().then((res) => {
                $scope.opened.lightboxCreateMotif = false;
                $scope.structure.motifs.sync().then(() => {
                    if(_newMotif.is_appel_oublie){
                        $scope.structure.categorieAppels.sync().then( () => {
                            $scope.safeApply($scope);
                        });
                    } else {
                        $scope.structure.categories.sync().then( () => {
                            $scope.safeApply($scope);
                        });
                    }
                });
            });
        };

        // enregistrement d'une categorie de motif
        $scope.saveCategorie = function (isAppel, categorie) {

            let isAppelOublie = (isAppel !== undefined && isAppel) || (categorie !== undefined && categorie.is_appel_oublie !== undefined && categorie.is_appel_oublie);
            if (isAppelOublie) {
                $scope.saveCategorieAppel();
            }
            else {
                let _newCategorie = new Categorie ($scope.newCategorie);
                // on désélectionne la catégorie si on a fait une modification
                if ($scope.isModif) {
                    $scope.selectCategorie($scope.newCategorie);
                }
                _newCategorie.save().then((res) => {
                    $scope.displayCreateCategorie = false;
                    $scope.structure.motifs.sync().then(() => {
                        $scope.structure.categories.sync().then( () => {
                            $scope.displayCreateCategorie = false;
                            if ($scope.isModif) {
                                $scope.opened.lightboxCreateMotif = false;
                                $scope.newCategorie.selected = false;
                            }
                            $scope.safeApply($scope);
                        });
                    });
                });
            }
        };

        // enregistrement d'un motif d'appel
        $scope.saveMotifAppel = function () {
            let _newMotif = new MotifAppel($scope.newMotif);
            if (_newMotif.id) {
                $scope.selectMotif($scope.newMotif);
            }
            _newMotif.save().then((res) => {
                $scope.opened.lightboxCreateMotif = false;
                $scope.structure.motifAppels.sync().then(() => {
                    $scope.structure.categorieAppels.sync().then( () => {
                        $scope.safeApply($scope);
                    });
                });
            });
        };

        // enregistrement d'une categorie de motif d'appel
        $scope.saveCategorieAppel = function () {
            let _newCategorie = new CategorieAppel ($scope.newCategorie);
            // on désélectionne la catégorie si on a fait une modification
            if ($scope.isModif) {
                $scope.selectCategorie($scope.newCategorie);
            }
            _newCategorie.save().then((res) => {
                $scope.displayCreateCategorie = false;
                $scope.structure.motifAppels.sync().then(() => {
                    $scope.structure.categorieAppels.sync().then( () => {
                        $scope.displayCreateCategorie = false;
                        if ($scope.isModif) {
                            $scope.opened.lightboxCreateMotif = false;
                            $scope.newCategorie.selected = false;
                        }
                        $scope.safeApply($scope);
                    });
                });
            });
        };

        $scope.updateMotif = function () {
            $scope.displayText = lang.translate("viescolaire.update.motif");
            $scope.newMotif = $scope.selected.motifs[0];
            $scope.displayCreateCategorie = false;
            $scope.opened.lightboxCreateMotif = true;
            $scope.safeApply($scope);
        };
        $scope.updateCategorie = function () {
            $scope.displayText = lang.translate("viescolaire.update.categorie");
            $scope.displayCreateCategorie = true;
            $scope.newCategorie = $scope.selected.categories[0];
            $scope.opened.lightboxCreateMotif = true;
            $scope.isModif = true;
            $scope.safeApply($scope);
        };
        $scope.changeEtablissementAccueil = function (structure) {
            $scope.structure = structure;
            $scope.structure.sync().then(() => {
                if ($scope.currParam === undefined) {
                    $scope.currParam = 0;
                }
                $scope.safeApply($scope);
            });
        };

        route({
            accueil: function (params) {
                $scope.hasFilterParam = params.filter === undefined;
                if ( $scope.structure === undefined ) {
                    vieScolaire.structures.sync().then(() => {
                        $scope.structures = vieScolaire.structures;
                        vieScolaire.structure.sync().then(() => {
                            $scope.structure = vieScolaire.structure;
                            if ($scope.currParam === undefined) {
                                $scope.currParam = 0;
                            }
                            $scope.safeApply($scope);
                        });
                    });
                }
                else {
                    $scope.structures = vieScolaire.structures;
                    $scope.structure.sync().then(() => {
                        if ($scope.currParam === undefined) {
                            $scope.currParam = 0;
                        }
                        $scope.safeApply($scope);
                    });
                }
                template.open('main', '../templates/viescolaire/vsco_acu_personnel');
                template.open('lightboxContainerCreateMotif', '../templates/viescolaire/display_creation_motif');
                $scope.safeApply($scope);
            }
        });
    }
]);