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

declare let _: any;
declare let window: any;

export let listController = ng.controller('ListController', [
    '$scope','$rootScope','$location','$filter',
    async  function ($scope, $rootScope, $location, $filter) {

        /**
         * Retourne le libelle de la matière correspondant à l'identifiant passé en paramètre
         * @param idMatiere identifiant de la matière
         * @returns {any} libelle de la matière
         */
        $scope.getLibelleMatiere = function (idMatiere) {
            if (idMatiere === undefined || idMatiere == null || idMatiere === "") return "";
            let matiere = _.findWhere($scope.matieres.all, {id: idMatiere});
            if (matiere !== undefined && matiere.hasOwnProperty('name')) {
                return matiere.name;
            } else {
                return '';
            }
        };

        $scope.getTeacherDisplayName = function (owner) {
            if (owner === undefined || owner === null || owner === "") return "";
            let ensenseignant = _.findWhere(evaluations.enseignants.all, {id: owner});
            if (ensenseignant !== undefined && ensenseignant.hasOwnProperty('name')) {
                return ensenseignant.firstName[0] + '.' + ensenseignant.name;
            } else {
                return '';
            }
        };

        /**
         * Format la date passée en paramètre
         * @param date Date à formatter
         * @returns {any|string} date formattée
         */
        $scope.getDateFormated = function (date) {
            return utils.getFormatedDate(date, "DD/MM/YYYY");
        };
        // Initialisation des variables
        $scope.initListDevoirs = function () {
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
            $scope.matieres = evaluations.matieres;
            $scope.enseignants = evaluations.enseignants;
            $scope.translate = lang.translate;
            utils.safeApply($scope);
        };
        await $rootScope.init();
        $scope.initListDevoirs();
        // Au changement de la période par le parent
        $scope.$on('loadPeriode', async function() {
            $scope.initListDevoirs();
            utils.safeApply($scope);
        });
        $scope.goToDevoir = function(url) {
            window.location.hash=url;
        };
        $scope.checkHaveResult = function () {
            let custom = $filter('customSearchFilters');
            let filter = $filter('filter');
            let res =  custom(evaluations.devoirs.all, $scope.search);
            res = filter(res, $scope.search.name);

            return (res.length > 0);
        };
    }
]);