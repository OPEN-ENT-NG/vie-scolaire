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

import { model, ng, idiom as lang,} from 'entcore/entcore';
import {evaluations} from '../models/eval_parent_mdl';
import * as utils from '../utils/parent';

let moment = require('moment');

declare let _: any;

export let releveController = ng.controller('ReleveController', [
    '$scope','$rootScope', '$location',
    async  function ($scope, $rootScope, $location) {

        // Au changement d'enfant par le parent
        $scope.$on('loadEleve', async function () {
            $scope.calculMoyenneMatieres();
            utils.safeApply($scope);
        });

        // Au changement de la période par le parent
        $scope.$on('loadPeriode', async function() {
            $scope.searchReleve.periode = evaluations.periode;
            await $scope.loadReleveNote();
            utils.safeApply($scope);
        });

        /**
         * Calcul la moyenne pour chaque matière
         * contenue dans $scope.matieres
         */
        $scope.calculMoyenneMatieres = function() {
            if ($scope.dataReleve === undefined) {
                return ;
            }

            $scope.matieres.forEach((matiere) => {
                let devoirsMatieres = $scope.dataReleve.devoirs.where({id_matiere : matiere.id});

                if (devoirsMatieres !== undefined && matiere !== undefined) {
                    let id_eleve;
                    if (model.me.type === 'PERSRELELEVE') {
                        id_eleve = $scope.searchReleve.eleve.id;
                    } else {
                        id_eleve = $scope.eleve.id;
                    }
                    matiere.getMoyenne(id_eleve, devoirsMatieres).then(() => {
                        utils.safeApply($scope);
                    });
                }
            });
        };
        /**
         * chargement d'un
         * @returns {Promise<void>}
         */
        $scope.loadReleveNote = async function() {
            let eleve = $scope.searchReleve.eleve;
            let idPeriode = undefined;
            if ($scope.searchReleve.periode !== null && $scope.searchReleve.periode.id !== null) {
                idPeriode = $scope.searchReleve.periode.id_type;
            }
            await evaluations.devoirs.sync(eleve.idStructure,eleve.id, eleve.idClasse, idPeriode);
            $scope.dataReleve = {
                devoirs : evaluations.devoirs
            };
            $scope.matieres = evaluations.matieres;
            $scope.calculMoyenneMatieres();
            utils.safeApply($scope);
        };


        // Impression du releve de l'eleve
        $scope.getReleve = function() {
            if (model.me.type === 'ELEVE') {
                evaluations.getReleve($scope.searchReleve.eleve.classe.periode.id_type,
                    $scope.searchReleve.eleve.id);
            } else {
                evaluations.getReleve($scope.searchReleve.periode.id_type,
                    $scope.searchReleve.eleve.id);
            }
        };

        // Initialisation des variables du relevé
        $scope.initReleve = function () {
            $scope.dataReleve = {
                devoirs : evaluations.devoirs
            };
            $scope.searchReleve = {
                eleve: evaluations.eleve,
                periode: evaluations.periode,
                enseignants: evaluations.enseignants
            };
            $scope.me = {
                type: model.me.type
            };
            $scope.matieres = evaluations.matieres;
            $scope.translate = lang.translate;
            $scope.calculMoyenneMatieres();
            utils.safeApply($scope);
        };

        await $rootScope.init();
        $scope.initReleve();
    }
]);