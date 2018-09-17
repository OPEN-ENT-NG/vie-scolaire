import { idiom as lang, template, ng, moment, _, $ } from 'entcore';
import {vieScolaire} from "../models/vsco_personnel_mdl";
import {Motif} from "../models/personnel/Motif";
import {Categorie} from "../models/personnel/Categorie";
import {MotifAppel} from "../models/personnel/MotifAppel";
import {CategorieAppel} from "../models/personnel/CategorieAppel";
import * as utils from '../../utils/functions/safeApply';
import {getFormatedDate} from "../../utils/functions/formatDate";
import {Periode} from "../models/common/Periode";
import {Utils} from "../utils/Utils";

export let viescolaireController = ng.controller('ViescolaireController', [
    '$scope', 'route', 'model', '$location', '$anchorScroll', '$sce',
    function ($scope, route, model, $location, $anchorScroll, $sce) {
        console.log('viescolaireController');

        /**
         * Check if the modules are installed and if the current user can access them
         * @returns {Promise<void>}
         */
        async function loadAndCheckModulesAccess() {
            try {
                await model.me.workflow.load(['competences', 'presences', 'edt']);
            } catch {
                // Continue
            }

            $scope.moduleCompetenceIsInstalled = Utils.moduleCompetenceIsInstalled();
            $scope.modulePresenceIsInstalled = Utils.modulePresenceIsInstalled();
            $scope.moduleEdtIsInstalled = Utils.moduleEdtIsInstalled();
            $scope.canAccessCompetences = Utils.canAccessCompetences();
            $scope.canAccessPresences = Utils.canAccessPresences();
            $scope.canAccessEdt = Utils.canAccessEdt();

            let modulesAccess = {
                moduleCompetenceIsInstalled: $scope.moduleCompetenceIsInstalled,
                modulePresenceIsInstalled: $scope.modulePresenceIsInstalled,
                moduleEdtIsInstalled: $scope.moduleEdtIsInstalled,
                canAccessPresences: $scope.canAccessPresences,
                canAccessCompetences: $scope.canAccessCompetences,
                canAccessEdt: $scope.canAccessEdt
            };
            console.log('ModulesAccess', modulesAccess);
        }

        async function forceSynchronous() { await loadAndCheckModulesAccess() };

        forceSynchronous();

        $scope.template = template;
        $scope.lang = lang;
        $scope.opened = {
            lightboxCreateMotif: false,
            lightboxCreateItem: false,
            lightboxDeletePerso: false
        };
        $scope.selected = {
            categories: [],
            motifs: [],
            all: [],
            allClassePeriode: false
        };
        $scope.search = {
            name:'',
            type:0
        };

        $scope.resetLightboxPeriode = () => {
            return {
                show : false,
                periodes : [],
                typePeriode: 0,
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

        $scope.switchSelectedClasse = (classes, bool) => {
            let idClasses = _.pluck(classes, 'id');
            _.each($scope.structure.classes.all, (classe) => {
                if(_.contains(idClasses, classe.id)) {
                    classe.selected = bool;
                }
            });
        };

        $scope.setPeriodes = () => {
            $scope.selectedClasseSorted = _.sortBy($scope.getSelectedClasse(), 'name');
            let classe = _.first($scope.selectedClasseSorted);
            $scope.lightboxPeriode.periodes = _.map(classe.periodes.all, (periode) => {
                return _.pick(periode, 'timestamp_dt', 'timestamp_fn', 'date_fin_saisie');
            });
            $scope.lightboxPeriode.typePeriode = classe.periodes.all.length;
            $scope.diffPeriodesError = !$scope.checkDiffPeriodsClasse($scope.selectedClasseSorted);
        };

        $scope.checkDiffPeriodsClasse = function (selectedClasseSorted) {
            for(let c in selectedClasseSorted){
                $scope.periodesTemp = _.map(selectedClasseSorted[c].periodes.all, (periode) => {
                    return _.pick(periode, 'timestamp_dt', 'timestamp_fn', 'date_fin_saisie');
                });
                if(!_.isEqual($scope.periodesTemp, $scope.lightboxPeriode.periodes))
                    return false;
            }
            return true;
        }

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
            $scope.lightboxPeriode.periodes = periodes;
            $scope.checkError();
        };

        $scope.savePeriode = async (periodes) => {


            if(!$scope.lightboxPeriode.error.errorFn && !$scope.lightboxPeriode.error.errorFnS) {
                try {
                    await $scope.structure.savePeriodes(_.pluck($scope.getSelectedClasse(), 'id'), periodes);
                    $scope.lightboxPeriode.show = false;
                    $scope.switchSelectedClasse($scope.getSelectedClasse(), false);
                    utils.safeApply($scope);

                    await $scope.structure.getPeriodes();
                } catch (err) {
                    console.log(err);
                    $scope.lightboxPeriode.error.error = true;
                }
            }

            utils.safeApply($scope);
        };

        $scope.closeLightboxPeriode = () => {
            $scope.lightboxPeriode = $scope.resetLightboxPeriode();
        };

        $scope.getCurrentPeriode = (classe): Periode => {
            let now = moment();
            let res;
            _.each(classe.periodes.all, (periode) => {
                if (moment(periode.timestamp_dt).isSameOrBefore(now, 'day' ) && moment(periode.timestamp_fn).isSameOrAfter(now,'day')) {
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
            let target = $('#' + idElement );
            /* le sélecteur $(html, body) permet de corriger un bug sur chrome
            et safari (webkit) */
            $('html, body')
            // on arrête toutes les animations en cours
                .stop()
                /* on fait maintenant l'animation vers le haut (scrollTop) vers
                 notre ancre target */
                .animate({scrollTop: $(target).offset().top}, 1000 );
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



        $scope.formatDatePeriode = (pODate) => {
            return getFormatedDate(pODate, 'DD MMMM YYYY');
        };

        route({
            accueil: async function (params) {
                /**
                 * Loading modules to check rights later
                 * @returns {Promise<void>}
                 */
                await Utils.loadModule('edt');
                await Utils.loadModule('competences');
                await Utils.loadModule('presences');

                moment.locale('fr');
                let openTemplate = () => {
                    template.open('main', '../templates/viescolaire/vsco_acu_personnel');
                    template.open('lightboxContainerCreateMotif', '../templates/viescolaire/display_creation_motif');
                    template.open('lightboxPeriode', '../templates/viescolaire/lightbox_param_periode');
                    // LightBox paramétrage d'items
                    template.open('lightboxContainerCreateItem',
                        '../../../competences/public/template/personnels/param_items/display_creation_item');
                    utils.safeApply($scope);
                };
                if ( $scope.structure === undefined ) {
                    console.log('Structure is undefined');
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
                            openTemplate();
                        });
                    });
                }
                else {
                    console.log('Structure is defined !');
                    $scope.structures = vieScolaire.structures;
                   openTemplate();
                }
            }
        });


    }
]);