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

import { model, ng, idiom as lang, template } from 'entcore/entcore';
import {evaluations} from '../models/eval_parent_mdl';
import * as utils from '../utils/parent';
import {Classe} from "../models/parent_eleve/Classe";
let moment = require('moment');

declare let _: any;

export let releveController = ng.controller('ReleveController', [
    '$scope','$rootScope', '$location',
    function ($scope, $rootScope, $location) {
        // Initialisation des variables
        $scope.dataReleve = {
            devoirs : $rootScope.devoirs
        };
        $scope.searchReleve = {
            eleve: $rootScope.eleve,
            periode: $rootScope.periode,
            enseignants: $rootScope.enseignants
        };
        $scope.me = {
            type: model.me.type
        }

        // Au changement d'enfant par le pare
        $scope.$on('loadReleve', async function () {
            $scope.matieres = evaluations.matieres;
            $scope.dataReleve.devoirs = evaluations.devoirs;
            $scope.searchReleve.eleve = evaluations.eleve;
            $scope.searchReleve.periode = $rootScope.periode;
            $scope.searchReleve.enseignants = evaluations.enseignants;
            await $scope.loadReleveNote();
            utils.safeApply($scope);
        });




        $scope.initData = function (devoirs, eleve, periode) {

        };

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

        $scope.loadReleveNote = async function() {
            let periode;
            if ($scope.searchReleve.eleve.classe === undefined) {
                $scope.searchReleve.eleve.classe = new Classe({id: $scope.searchReleve.eleve.idClasse});
                await $scope.searchReleve.eleve.classe.sync();
            }
            if ($scope.searchReleve.eleve.classe.periode !== null) {
                let eleve = $scope.searchReleve.eleve;
                periode = _.findWhere(eleve.classe.periodes.all,
                    {id: $scope.searchReleve.periode.id});
                periode = periode ? periode : _.findWhere(eleve.classe.periodes.all, {id: null});
                $scope.dataReleve.devoirs = evaluations.devoirs.findWhere({id_periode: periode.id});
            }
            $scope.calculMoyenneMatieres();
            utils.safeApply($scope);
        };


        // Impression du releve de l'eleve
        $scope.getReleve = function() {
            if (model.me.type === 'ELEVE') {
                $scope.searchReleve.eleve.classe.periode.getReleve($scope.searchReleve.eleve.classe.periode.id_type,
                    $scope.searchReleve.eleve.id);
            } else {
                $scope.searchReleve.periode.getReleve($scope.searchReleve.periode.id_type,
                    $scope.searchReleve.eleve.id);
            }
        };
    }
]);