/*
 * Copyright (c) Région Hauts-de-France, Département de la Seine-et-Marne, Région Nouvelle Aquitaine, Mairie de Paris, CGI, 2016.
 * This file is part of OPEN ENT NG. OPEN ENT NG is a versatile ENT Project based on the JVM and ENT Core Project.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 * For the sake of explanation, any module that communicate over native
 * Web protocols, such as HTTP, with OPEN ENT NG is outside the scope of this
 * license and could be license under its own terms. This is merely considered
 * normal use of OPEN ENT NG, and does not fall under the heading of "covered work".
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import { idiom as lang, template, ng, moment, _, $ } from 'entcore';
import {vieScolaire} from "../models/vsco_personnel_mdl";
import {Motif} from "../models/personnel/Motif";
import {Categorie} from "../models/personnel/Categorie";
import {MotifAppel} from "../models/personnel/MotifAppel";
import {CategorieAppel} from "../models/personnel/CategorieAppel";
import * as utils from '../../utils/functions/safeApply';
import {getFormatedDate} from "../../utils/functions/formatDate";
import {Periode} from "../models/common/Periode";
import {TypePeriode} from "../models/common/TypePeriode";
import {Utils} from "../utils/Utils";

declare let window: any;

export let viescolaireController = ng.controller('ViescolaireController', [
    '$scope', 'route', 'model', '$location', '$anchorScroll', '$sce',
    async function ($scope, route, model, $location, $anchorScroll, $sce) {
        console.log('viescolaireController');

        async function enableModule (module: string): Promise<void> {
            try {
                await Behaviours.load(module);
                await model.me.workflow.load([module]);
                await lang.addBundlePromise(`/${module}/i18n`);
            } catch (err) {
                console.error(`Failed to enable module ${module}`);
                throw err;
            }
        }

        /**
         * Check if the modules are installed and if the current user can access them
         * @returns {Promise<void>}
         */
        async function loadAndCheckModulesAccess() {
            const promises = [];
            for (let service in window.services) {
                if (window.services[service]) promises.push(enableModule(service).catch(e => e));
            }

            //Do not use Promise.allSettled due to lack of implementation
            await Promise.all(promises);

            $scope.moduleCompetenceIsInstalled = Utils.moduleCompetenceIsInstalled();
            $scope.modulePresenceIsInstalled = Utils.modulePresenceIsInstalled();
            $scope.moduleEdtIsInstalled = Utils.moduleEdtIsInstalled();
            $scope.canAccessCompetences = Utils.canAccessCompetences();
            $scope.canAccessPresences = Utils.canAccessPresences();
            $scope.canAccessEdt = Utils.canAccessEdt();
            $scope.canAccessDiary = Utils.canAccessDiary();
            //
            let modulesAccess = {
                moduleCompetenceIsInstalled: $scope.moduleCompetenceIsInstalled,
                modulePresenceIsInstalled: $scope.modulePresenceIsInstalled,
                moduleEdtIsInstalled: $scope.moduleEdtIsInstalled,
                canAccessPresences: $scope.canAccessPresences,
                canAccessCompetences: $scope.canAccessCompetences,
                canAccessEdt: $scope.canAccessEdt,
                canAccessDiary: $scope.canAccessDiary
            };
            console.log('ModulesAccess', modulesAccess);
        };

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
                    error: false,
                    errorDateConseilBeforeFnS: false

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

        $scope.getImportOrdreTypePeriode = (periode: TypePeriode) => {
            if (periode !== undefined) {
                return _.findWhere($scope.structure.typePeriodes.all, {id: periode.id});
            }
        };

        $scope.displayPeriode = function (periode) {
            return $scope.getI18n("viescolaire.periode." +
                $scope.getOrdreTypePeriode(periode).type) + " " +
                $scope.getOrdreTypePeriode(periode).ordre;
        };

        $scope.displayImportPeriode = function (periode) {
            return $scope.getI18n("viescolaire.periode." +
                $scope.getImportOrdreTypePeriode(periode).type) + " " +
                $scope.getImportOrdreTypePeriode(periode).ordre;
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
                var periodeTmp = _.pick(periode, 'timestamp_dt', 'timestamp_fn', 'date_fin_saisie','date_conseil_classe','publication_bulletin');

                // conversion en date pour les date-picker
                periodeTmp.timestamp_dt = new Date(periodeTmp.timestamp_dt);
                periodeTmp.timestamp_fn = new Date(periodeTmp.timestamp_fn);
                periodeTmp.date_fin_saisie= new Date(periodeTmp.date_fin_saisie);
                periodeTmp.date_conseil_classe= new Date(periodeTmp.date_conseil_classe);
                return periodeTmp;
            });

            $scope.lightboxPeriode.typePeriode = classe.periodes.all.length;
            $scope.diffPeriodesError = !$scope.checkDiffPeriodsClasse($scope.selectedClasseSorted);
        };

        $scope.checkDiffPeriodsClasse = function (selectedClasseSorted) {

            let periodesLightBoxCompare = _.map($scope.lightboxPeriode.periodes ,(periode) =>{
                return _.omit(periode, 'date_conseil_classe','publication_bulletin');}
                );
            for(let c in selectedClasseSorted){
                $scope.periodesTemp = _.map(selectedClasseSorted[c].periodes.all, (periode) => {
                    var periodeTmp = _.pick(periode, 'timestamp_dt', 'timestamp_fn', 'date_fin_saisie');

                    // conversion en date pour les date-picker
                    periodeTmp.timestamp_dt = new Date(periodeTmp.timestamp_dt);
                    periodeTmp.timestamp_fn = new Date(periodeTmp.timestamp_fn);
                    periodeTmp.date_fin_saisie= new Date(periodeTmp.date_fin_saisie);
                    return periodeTmp;
                });
                if(!_.isEqual($scope.periodesTemp, periodesLightBoxCompare))
                    return false;
            }
            return true;
        };

        $scope.buildPeriodes = (typePeriode) => {
            let periodes = [];
            for (let i = 0; i < typePeriode; i++) {
                let periode = new Periode();
                periode.timestamp_dt = new Date();
                periode.timestamp_fn = new Date();
                periode.date_fin_saisie = new Date();
                periode.id_etablissement = $scope.structure.id;
                periode.date_conseil_classe = new Date();
                periode.publication_bulletin = false;
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

                moment.locale('fr');
                let openTemplate = () => {
                    template.open('main', '../templates/viescolaire/vsco_acu_personnel');
                    template.open('lightboxContainerCreateMotif',
                        '../../../presences/public/template/lightbox/display_creation_motif');
                    template.open('lightboxPeriode', '../templates/viescolaire/lightbox_param_periode');
                    utils.safeApply($scope);
                };
                if ( $scope.structure === undefined ) {
                    console.log('Structure is undefined');
                    vieScolaire.structures.sync().then(() => {
                        $scope.structures = vieScolaire.structures;
                        vieScolaire.structure.sync().then(() => {
                            $scope.structure = vieScolaire.structure;
                            if ($scope.structure.cycles !== undefined && $scope.structure.cycles.length > 0) {
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