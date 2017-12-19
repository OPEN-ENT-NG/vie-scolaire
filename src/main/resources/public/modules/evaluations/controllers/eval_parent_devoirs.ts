/*
 * Copyright (c) Région Hauts-de-France, Département 77, CGI, 2016.
 *
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
 *
 */

/**
 * Created by anabah on 29/11/2017.
 */

import {ng, idiom as lang} from 'entcore/entcore';
import {evaluations} from '../models/eval_parent_mdl';
import * as utils from '../utils/parent';
import {SuiviCompetence, Structure} from "../models/teacher/eval_teacher_mdl";

declare let _: any;
declare let window: any;
declare let $: any;

export let listController = ng.controller('ListController', [
    '$scope','$rootScope','$location','$filter',
    async  function ($scope, $rootScope, $location, $filter) {

        // Initialisation des variables
        $scope.initListDevoirs = async function () {
            $scope.openedDevoir = -1;
            $scope.devoirs =  evaluations.devoirs;
            $scope.search = {
                eleve: evaluations.eleve,
                periode: evaluations.periode,
                classe : null,
                matiere: null,
                enseignant: null,
                sousmatiere: null,
                type: null,
                name: ""
            };
            $scope.checkHaveResult = function () {
                let custom = $filter('customSearchFilters');
                let filter = $filter('filter');
                let res =  custom(evaluations.devoirs.all, $scope.search);
                res = filter(res, $scope.search.name);

                return (res.length > 0);
            };
            $scope.matieres = evaluations.matieres;
            $scope.enseignants = evaluations.enseignants;
            $scope.translate = lang.translate;
            if($location.path().split('/')[2] !== "list") {
                let devoirId = $location.path().split('/')[2];
                $rootScope.currentDevoir = _.findWhere(evaluations.devoirs.all, {id: parseInt(devoirId)});
                if ($rootScope.currentDevoir !== undefined) {
                    await evaluations.domaines.sync(evaluations.eleve.classe, evaluations.eleve,
                        $rootScope.currentDevoir.competences);
                    await $rootScope.currentDevoir.getAppreciation(evaluations.eleve.id);

                    $scope.suiviCompetence = {
                        domaines: evaluations.domaines
                    };
                    utils.safeApply($scope);
                }
                else {
                    if(evaluations.devoirs.all.length > 0 ) {
                        $scope.goToDevoir('#/devoir/'+ evaluations.devoirs.all[0].id);
                    }
                    else {
                        $scope.goToDevoir('#/');
                    }
                }
            }
            utils.safeApply($scope);
        };
        await $rootScope.init();
        // Au changement de la période courante par le parent
        $scope.$on('loadPeriode', async function() {
            await $scope.initListDevoirs();
            utils.safeApply($scope);
        });
        $scope.goToDevoir = function(url) {
            window.location.hash=url;
        };

        /**
         * Ouvre le détail du devoir correspondant à l'index passé en paramètre
         * @param index index du devoir
         * @param bool état du détail
         */
        $scope.expand = function (index, bool) {
            if ($scope.openedDevoir !== index) {
                $scope.openedDevoir = index;
            } else {
                if (bool === true) {
                    $scope.openedDevoir = -1;
                }
            }
        };

        $scope.FilterNotEvaluated = function (maCompetence) {
            var _t = maCompetence.competencesEvaluations;
            var max = _.max(_t, function (evaluation) {
                return evaluation.evaluation;
            });
            if (typeof max === 'object') {
                return true;
            }
            else {
                return false;
            }
        };
        $scope.FilterNotEvaluatedDomaine = function (monDomaineCompetence) {
            if (monDomaineCompetence.domaines.all.length > 0 ) {
                for (let i = 0; i < monDomaineCompetence.domaines.all.length; i++) {
                    if($scope.FilterNotEvaluated(monDomaineCompetence.domaines.all[i])){
                        return true;
                    }
                }
            }
            else {
                for (let i = 0; i < monDomaineCompetence.competences.all.length; i++) {
                    let maCompetence = monDomaineCompetence.competences.all[i];
                    if ($scope.FilterNotEvaluated(maCompetence)) {
                        return true;
                    }
                }
            }
            return false;
        };
        $scope.incrementDevoir = function (num) {
            let index = _.findIndex(evaluations.devoirs.all, {id: $rootScope.currentDevoir.id});
            if (index !== -1 && (index + parseInt(num)) >= 0
                && (index + parseInt(num)) < evaluations.devoirs.all.length) {
                let target = evaluations.devoirs.all[index + parseInt(num)];
                $scope.goToDevoir('#/devoir/' +target.id);
                utils.safeApply($scope);
            }
        };
    }
]);