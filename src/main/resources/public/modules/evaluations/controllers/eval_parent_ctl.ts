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
 * Created by ledunoiss on 08/08/2016.
 */

import { model, ng, idiom as lang, template } from 'entcore/entcore';
import {evaluations} from '../models/parent/eval_parent_mdl';
import * as utils from '../utils/parent';
let moment = require('moment');

declare let _: any;

export let evaluationsController = ng.controller('EvaluationsController', [
    '$scope', 'route', '$rootScope', '$location',
    function ($scope, route, $rootScope, $location) {
        route({
            displayReleveNotes : function(params) {
                if (model.me.type === 'ELEVE') {
                    // $scope.periodes = evaluations.periodes;
                    $scope.searchReleve.eleve = model.me;
                    $scope.getPeriodes();
                }else if (model.me.type === 'PERSRELELEVE') {
                    evaluations.sync().then(() => {
                        $scope.eleves = evaluations.eleves;
                        if (evaluations.eleves.all.length > 1) {
                            template.open('lightboxContainer',
                                '../templates/evaluations/parent_enfant/releve/eval_parent_dispenfants');
                            $scope.showNoteLightBox = {bool : true};
                            utils.safeApply($scope);
                        }else {
                            $scope.searchReleve.eleve = evaluations.eleves.all[0];
                            // $scope.getPeriodes();
                            // $scope.getMatieres();
                        }
                    });
                }
                template.open('main', '../templates/evaluations/parent_enfant/releve/eval_parent_dispreleve');
                utils.safeApply($scope);
            }
        });

        $scope.$watch("searchReleve.eleve", async (newVal) => {
            if (newVal !== null) {
                await newVal.periodes.sync();
                evaluations.periodes = newVal.periodes;
            }
        });

        // Initialisation des variables
        $scope.searchReleve = {
            eleve : null,
            periode : null
        };

        $scope.showNoteLightBox = {
            bool : false
        };

        // $scope.classes = evaluations.classes;
        // $scope.me = model.me;

        $scope.getI18nPeriode = (periode) => {
            let type_periode = _.findWhere(evaluations.periodes.all, {id: periode.id_type});
            let result = type_periode ?
                lang.translate("viescolaire.periode." + type_periode.type) + " " + type_periode.ordre
                : lang.translate("viescolaire.utils.periodeError");
            return result;
        };

        $scope.getPeriodes = function () {
            if ($scope.searchReleve.eleve !== null) {
                setCurrentPeriode();
            }
        };


        $scope.getFormatedDate = function(date) {
            return moment(date).format("DD/MM/YYYY");
        };

        $scope.noteMatiereEleve = function(idMatiere) {
            return $scope.dataReleve.devoirs.findWhere({ id_matiere : idMatiere });
        };

        $scope.chooseChild = function(idEleve) {
            $scope.searchReleve.eleve = _.findWhere(evaluations.eleves.all, {id : idEleve});
            // $scope.getPeriodes();
            $scope.showNoteLightBox.bool = false;
            utils.safeApply($scope);
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
                        id_eleve = $scope.searchReleve.eleve.userId;
                    }
                    matiere.getMoyenne(id_eleve, devoirsMatieres).then(() => {
                        utils.safeApply($scope);
                    });
                }
            });
        };

        $scope.loadReleveNote = function() {
            let periode;
            if ($scope.searchReleve.periode !== null) {
                let eleve;
                if (model.me.type === 'PERSRELELEVE') {
                    eleve = evaluations.eleves.findWhere({id : $scope.searchReleve.eleve.id});
                }else {
                    eleve = $scope.searchReleve.eleve;
                }

                periode = _.findWhere(eleve.periodes.all, {id_type: $scope.searchReleve.periode.id_type});
                periode = periode ? periode : _.findWhere(eleve.periodes.all, {id: 0});
                periode.devoirs.sync($scope.searchReleve.eleve.idStructure,
                    $scope.searchReleve.eleve.userId).then(() => {
                    $scope.matieres = periode.matieres;
                    $scope.dataReleve = periode;
                    $scope.calculMoyenneMatieres();
                    utils.safeApply($scope);
                });

            }
        };

        /**
         * Charge la liste des periodes dans $scope.periodes et détermine la période en cours et positionne
         * son id dans $scope.currentPeriodeId
         */
        let setCurrentPeriode = function() {
            // récupération des périodes et filtre sur celle en cours
            let periodes;
            if (model.me.type === 'PERSRELELEVE') {
                periodes = $scope.searchReleve.eleve.periodes;
            }else {
                periodes = evaluations.periodes;
            }
            periodes.sync().then(() => {
                let formatStr = "DD/MM/YYYY";
                let momentCurrDate = moment(moment().format(formatStr), formatStr);
                let foundPeriode = false;
                $scope.currentPeriodeId = -1;

                for (let i = 0; i < periodes.all.length && !foundPeriode; i++) {
                    let momentCurrPeriodeDebut = moment(moment(periodes.all[i].timestamp_dt).format(formatStr),
                        formatStr);
                    let momentCurrPeriodeFin = moment(moment(periodes.all[i].timestamp_fn).format(formatStr),
                        formatStr);

                    if ( momentCurrPeriodeDebut.diff(momentCurrDate) <= 0
                        && momentCurrDate.diff(momentCurrPeriodeFin) <= 0) {
                        foundPeriode = true;
                        $scope.searchReleve.periode = periodes.findWhere({id : periodes.all[i].id});
                        $scope.loadReleveNote();
                    }
                }
                utils.safeApply($scope);
            });
        };

        $scope.isCurrentPeriode = function(periode) {
            return (periode.id_type === $scope.currentPeriodeId);
        };

        // Impression du releve de l'eleve
        $scope.getReleve = function() {
            if (model.me.type === 'ELEVE') {
                $scope.searchReleve.periode.getReleve($scope.searchReleve.periode.id_type,
                    $scope.searchReleve.eleve.userId);
            } else {
                $scope.searchReleve.periode.getReleve($scope.searchReleve.periode.id_type,
                    $scope.searchReleve.eleve.id);
            }
        };
    }
]);