import { idiom as lang, template, ng} from "entcore/entcore";
import {vieScolaire} from "../models/vsco_personnel_mdl";
import {Motif} from "../../absences/models/personnel/Motif";
import {Categorie} from "../../absences/models/personnel/Categorie";
import {MotifAppel} from "../../absences/models/personnel/MotifAppel";
import {CategorieAppel} from "../../absences/models/personnel/CategorieAppel";
import * as utils from '../../utils/functions/safeApply';
import {getFormatedDate} from "../../utils/functions/formatDate";
import {Periode} from "../models/personnel/Periode";
import moment = require("moment");

declare let _: any;
declare let $: any;

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
        $scope.search = {
            name:'',
            type:0
        };

        $scope.resetLightboxPeriode = () => {
            return {
                error : {
                    errorEval: false,
                    errorFn: false,
                    errorFnS: false,
                    errorOver: false,
                    errorContig: false,
                    error: false
                }
            };
        };

        $scope.lightboxPeriode = $scope.resetLightboxPeriode();

        $scope.anyError = () => {
            return _.some($scope.lightboxPeriode.error)
        };

        $scope.checkError = () => {

            let periodes = $scope.lightboxPeriode.periodes;
            let errorObject = $scope.lightboxPeriode.error = $scope.resetLightboxPeriode().error;

            errorObject.errorFn = _.some(periodes, (periode) => {
                return !moment(periode.timestamp_fn).isAfter(moment(periode.timestamp_dt), 'days');
            });

            errorObject.errorFnS = _.some(periodes, (periode) => {
                return !moment(periode.date_fin_saisie).isAfter(moment(periode.timestamp_dt), 'days');
            });

            errorObject.errorOver =
                _.some(periodes, (periodeToCompare: Periode) => {
                    let otherPeriodes = _.reject(periodes, periodeToCompare);
                    return _.some(otherPeriodes, (periodeToCompareWith: Periode) => {
                        return moment(periodeToCompare.timestamp_dt)
                                .isBetween(moment(periodeToCompareWith.timestamp_dt),
                                    moment(periodeToCompareWith.timestamp_fn), 'days', '[]')
                            || moment(periodeToCompare.timestamp_fn)
                                .isBetween(moment(periodeToCompareWith.timestamp_dt),
                                    moment(periodeToCompareWith.timestamp_fn), 'days', '[]');
                    });
                });

            errorObject.errorContig =
                _.some(periodes, (periode, index) => {
                    let result = false;
                    if (periodes[index - 1]) {
                        result = result || moment(periode.timestamp_dt).diff(moment(periodes[index - 1].timestamp_fn), 'days') > 1;
                    }
                    if (periodes[index + 1]) {
                        result = result || moment(periodes[index + 1].timestamp_dt).diff(moment(periode.timestamp_fn), 'days') > 1;
                    }
                    return result;
                });
        };

        $scope.checkOrder = () => {
            $scope.lightboxPeriode.periodes = _.sortBy($scope.lightboxPeriode.periodes, (periode) => {
                return moment(periode.timestamp_dt);
            });
        };

        // recherche le module dont on veut aficher les paramètres.
        $scope.findParam = function (key) {
            if (key && Object.prototype.hasOwnProperty.call($location.search(), key)) {
                return ($location.search())[key];
            } else {
                return null;
            }
        };

        $scope.getTypePeriode = () => {
            return _.uniq(_.pluck($scope.structure.typePeriodes.all, 'type'));
        };

        $scope.getOrdreTypePeriode = (periode: Periode) => {
            if (periode !== undefined) {
                return _.findWhere($scope.structure.typePeriodes.all, {id: periode.id_type});
            }
        };

        $scope.getSelectedClasse = () => {
            return _.where($scope.structure.classes.all, {selected: true});
        };

        $scope.resetSelectedClasse = () => {
            _.each($scope.structure.classes.all, (classe) => {
                delete classe.selected;
            });
        };

        $scope.setPeriodes = () => {
            if ($scope.getSelectedClasse().length == 1) {
                let classe = _.first($scope.getSelectedClasse());
                $scope.lightboxPeriode.periodes = _.map(classe.periodes.all, (periode) => {
                    return _.pick(periode, 'timestamp_dt', 'timestamp_fn', 'date_fin_saisie');
                });
                $scope.lightboxPeriode.typePeriode = classe.periodes.all.length;
            } else {
                $scope.lightboxPeriode.periodes = [];
                $scope.lightboxPeriode.typePeriode = null;
            }
        };

        $scope.buildPeriodes = (typePeriode) => {
            let periodes = [];
            for (let i = 0; i < typePeriode; i++) {
                let periode = new Periode();
                periode.timestamp_dt = new Date();
                periode.timestamp_fn = new Date();
                periode.date_fin_saisie = new Date();
                periode.id_etablissement = $scope.structure.id;

                periodes.push(periode);
            }
            return periodes;
        };

        $scope.savePeriode = async (periodes) => {

            let idPeriodes = [];
            _.each($scope.getSelectedClasse(), (classe) => {
                _.each(classe.periodes.all, (periode) => {
                    if(periode.id && _.contains(_.pluck(periodes.id_type), periode.id_type))
                        idPeriodes.push(periode.id);
                });
            });

            if(!$scope.lightboxPeriode.error.errorEval && !$scope.lightboxPeriode.error.errorFn && !$scope.lightboxPeriode.error.errorFnS) {
                try {
                    await $scope.structure.savePeriodes(_.pluck($scope.getSelectedClasse(), 'id'), periodes);
                } catch (err) {
                    $scope.lightboxPeriode.error.error = true;
                }
            }

            await $scope.structure.getPeriodes();
            $scope.lightboxPeriode = $scope.resetLightboxPeriode();
            $scope.showLightboxPeriode = false;

            utils.safeApply($scope);
        };

        $scope.getCurrentPeriode = (classe): Periode => {
            let now = moment();
            let res;
            _.each(classe.periodes.all, (periode) => {
                if (moment(periode.timestamp_dt).isBefore(now) && moment(periode.timestamp_fn).isAfter(now)) {
                    res = periode;
                }
            });
            if (res === undefined ) {
                res = _.first(classe.periodes.all);
            }
            return res;
        };

        $scope.currentCycle = function (cycle) {
            return cycle.selected;
        };

        // scroller vers un élément donné
        $scope.scrollToElement = function(idElement) {
            let top = document.getElementById(idElement).offsetTop; // Getting Y of target element
            window.scrollTo(0, top);
        };

        $scope.openCreateMotif = function () {
            $scope.displayCreateCategorie = false;
            $scope.displayText = lang.translate("viescolaire.create.motif");
            $scope.opened.lightboxCreateMotif = true;
            utils.safeApply($scope);
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
            utils.safeApply($scope);
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
            utils.safeApply($scope);
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
        };

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
                            utils.safeApply($scope);
                        });
                    } else {
                        $scope.structure.categories.sync().then( () => {
                            utils.safeApply($scope);
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
                            utils.safeApply($scope);
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
                        utils.safeApply($scope);
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
                        utils.safeApply($scope);
                    });
                });
            });
        };

        $scope.getI18n = (libelle: string) => {
            return lang.translate(libelle);
        };

        $scope.updateMotif = function () {
            $scope.displayText = lang.translate("viescolaire.update.motif");
            $scope.newMotif = $scope.selected.motifs[0];
            $scope.displayCreateCategorie = false;
            $scope.opened.lightboxCreateMotif = true;
            utils.safeApply($scope);
        };

        $scope.updateCategorie = function () {
            $scope.displayText = lang.translate("viescolaire.update.categorie");
            $scope.displayCreateCategorie = true;
            $scope.newCategorie = $scope.selected.categories[0];
            $scope.opened.lightboxCreateMotif = true;
            $scope.isModif = true;
            utils.safeApply($scope);
        };

        $scope.changeEtablissementAccueil = function (structure) {
            $scope.structure = structure;
            $scope.structure.sync().then(() => {
                $scope.structure.niveauCompetences = _.groupBy($scope.structure.niveauCompetences,"id_cycle");
                if ($scope.currParam === undefined) {
                    $scope.currParam = 0;
                }
                utils.safeApply($scope);
            });
        };

        $scope.formatDate = (pODate) => {
            return getFormatedDate(pODate, 'DD MMMM YYYY');
        };

        route({
            accueil: function (params) {
                moment.locale('fr');
                if ( $scope.structure === undefined ) {
                    vieScolaire.structures.sync().then(() => {
                        $scope.structures = vieScolaire.structures;
                        vieScolaire.structure.sync().then(() => {
                            $scope.structure = vieScolaire.structure;
                            if ($scope.structure.cycles.length > 0) {
                                $scope.lastSelectedCycle = $scope.structure.cycles[0];
                                $scope.lastSelectedCycle.selected = true;
                            }

                            if ($scope.currParam === undefined) {
                                $scope.currParam = 0;
                            }
                            template.open('main', '../templates/viescolaire/vsco_acu_personnel');
                            template.open('lightboxContainerCreateMotif', '../templates/viescolaire/display_creation_motif');
                            template.open('lightboxPeriode', '../templates/viescolaire/lightbox_param_periode');
                            utils.safeApply($scope);
                        });
                    });
                }
                else {
                    $scope.structures = vieScolaire.structures;
                    template.open('main', '../templates/viescolaire/vsco_acu_personnel');
                    template.open('lightboxContainerCreateMotif', '../templates/viescolaire/display_creation_motif');
                    template.open('lightboxPeriode', '../templates/viescolaire/lightbox_param_periode');
                    utils.safeApply($scope);
                }
            }
        });
    }
]);