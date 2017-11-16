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

import { model, ng, template } from 'entcore/entcore';
import {evaluations} from '../models/eval_parent_mdl';
import * as utils from '../utils/parent';
let moment = require('moment');

declare let _: any;

export let evaluationsController = ng.controller('EvaluationsController', [
    '$scope', 'route', '$rootScope', '$location',
    function ($scope, route, $rootScope, $location) {
        route({
            displayReleveNotes : function(params) {
                if (model.me.type === 'ELEVE') {
                    $scope.periodes = evaluations.periodes;
                    $scope.searchReleve.eleve = model.me;
                    $scope.getPeriodes();
                }else if (model.me.type === 'PERSRELELEVE') {
                    evaluations.eleves.sync().then(() => {
                        $scope.eleves = evaluations.eleves;
                        if (evaluations.eleves.all.length > 1) {
                            template.open('lightboxContainer', '../templates/evaluations/parent_enfant/releve/eval_parent_dispenfants');
                            $scope.showNoteLightBox.bool = true;
                        }else {
                            $scope.searchReleve.eleve = evaluations.eleves.all[0];
                            $scope.getPeriodes();
                            $scope.getMatieres();
                        }
                    });
                }
                template.open('main', '../templates/evaluations/parent_enfant/releve/eval_parent_dispreleve');
                utils.safeApply($scope);
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

        $scope.classes = evaluations.classes;
        $scope.me = model.me;

        $scope.$on('syncPeriodes', function() {
            setCurrentPeriode(model.me.structures[0]);
            utils.safeApply($scope);
        });

        $scope.getPeriodes = function () {
            if ($scope.searchReleve.eleve !== null) {
                // TODO Recuperer l'etablissement courant et non le 1er etab dans la liste
                setCurrentPeriode($scope.searchReleve.eleve.structures[0]);
            }
        };


        $scope.getFormatedDate = function(date) {
            return moment(date).format("DD/MM/YYYY");
        };

        $scope.noteMatiereEleve = function(idMatiere) {
            return $scope.dataReleve.devoirs.findWhere({ id_matiere : idMatiere });
        };

        $scope.chooseChild = function(idEleve) {
            $scope.searchReleve.eleve = evaluations.eleves.findWhere({id : idEleve});
            $scope.getPeriodes();
            $scope.showNoteLightBox.bool = false;
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
                if (model.me.type === 'PERSRELELEVE') {
                    let eleve = evaluations.eleves.findWhere({id : $scope.searchReleve.eleve.id});
                    periode = eleve.periodes.findWhere({id : $scope.searchReleve.periode.id});
                    periode.devoirs.sync($scope.searchReleve.eleve.structures[0], $scope.searchReleve.eleve.id,
                        evaluations.classes).then( () => {
                        $scope.dataReleve = periode;
                        $scope.matieres = periode.matieres;
                        utils.safeApply($scope);
                        $scope.calculMoyenneMatieres();
                    });
                }else {
                    periode = evaluations.periodes.findWhere({id : $scope.searchReleve.periode.id});
                    periode.devoirs.sync($scope.searchReleve.eleve.structures[0],
                        $scope.searchReleve.eleve.userId, evaluations.classes).then( () => {
                        $scope.matieres = periode.matieres;
                        $scope.dataReleve = periode;
                        utils.safeApply($scope);
                        $scope.calculMoyenneMatieres();
                    });
                }
            }
        };

        /**
         * Charge la liste des periodes dans $scope.periodes et détermine la période en cours et positionne
         * son id dans $scope.currentPeriodeId
         * @param idEtablissement identifant de l'établissement concerné
         */
        let setCurrentPeriode = function(idEtablissement) {
            // récupération des périodes et filtre sur celle en cours
            let periodes;
            if (model.me.type === 'PERSRELELEVE') {
                periodes = $scope.searchReleve.eleve.periodes;
                periodes.sync(idEtablissement);
            }else {
                periodes = evaluations.periodes;
                periodes.sync(idEtablissement);
            }
            periodes.on('sync', function() {
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
            return (periode.id === $scope.currentPeriodeId);
        };

        // Impression du releve de l'eleve
        $scope.getReleve = function() {
            if (model.me.type === 'ELEVE') {
                $scope.searchReleve.periode.getReleve($scope.searchReleve.periode.id, $scope.searchReleve.eleve.userId);
            } else {
                $scope.searchReleve.periode.getReleve($scope.searchReleve.periode.id, $scope.searchReleve.eleve.id);
            }
        };
    }
]);