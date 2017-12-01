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

export let evaluationsController = ng.controller('EvaluationsController', [
    '$scope', 'route', '$rootScope', '$location','$filter', '$sce', '$compile', '$timeout','$route',
    function ($scope, route, $rootScope, $location, $filter, $sce, $compile, $timeout,$route) {
        route({
            accueil : async function (params) {
                await $rootScope.init();
                template.open('header', '../templates/evaluations/parent_enfant/accueil/eval_parent_selectEnfants');
                template.open('menu', '../templates/evaluations/parent_enfant/accueil/eval_parent_menu');
                template.open('main', '../templates/evaluations/parent_enfant/accueil/eval_parent_acu');
                utils.safeApply($scope);
            },
            displayReleveNotes : async function(params) {
                await $rootScope.init();
                template.close('main');
                template.close('menu');
                template.open('header', '../templates/evaluations/parent_enfant/accueil/eval_parent_selectEnfants');
                template.open('main', '../templates/evaluations/parent_enfant/releve/eval_parent_dispreleve');
                utils.safeApply($scope);
            },
            listDevoirs : async function (params) {
                await $rootScope.init();
                template.close('main');
                template.close('menu');
                template.open('header', '../templates/evaluations/parent_enfant/accueil/eval_parent_selectEnfants');
                utils.safeApply($scope);
            },
            displayBilanDeCompetence : async function (params) {
                await $rootScope.init();
                template.close('main');
                template.close('menu');
                template.open('header', '../templates/evaluations/parent_enfant/accueil/eval_parent_selectEnfants');
                utils.safeApply($scope);
            }
        });

        /**
         *
         * @returns {Promise<void>}
         */
        $rootScope.init = async function () {
            let initialise = ()=> {
                if (model.me.type === 'ELEVE') {
                    // $scope.periodes = evaluations.periodes;
                    $rootScope.eleve = evaluations.eleve;
                    $rootScope.selectedEleve = $rootScope.eleve;
                    $rootScope.devoirs = evaluations.devoirs;
                    $rootScope.matieres = evaluations.matieres;
                }
                else if (model.me.type === 'PERSRELELEVE') {
                    $rootScope.eleves = evaluations.eleves.all;
                    $rootScope.chooseChild (evaluations.eleve);
                }
            };
            if ($rootScope.eleve === undefined) {
                await evaluations.sync();
                initialise();
            }
            else {
                initialise();
            }
        };

        $rootScope.getI18nPeriode = (periode) => {
            let result;
            if (periode.id === null ) {
                result = lang.translate("viescolaire.utils.annee");
            }
            else {
                let type_periode = _.findWhere(evaluations.eleve.classe.typePeriodes.all, {id: periode.id_type});

                result = type_periode ?
                    lang.translate("viescolaire.periode." + type_periode.type) + " " + type_periode.ordre
                    : lang.translate("viescolaire.utils.periodeError");
            }
            return result;
        };



        $rootScope.getFormatedDate = function(date) {
            return moment(date).format("DD/MM/YYYY");
        };

        $rootScope.noteMatiereEleve = function(idMatiere) {
            return $scope.dataReleve.devoirs.findWhere({ id_matiere : idMatiere });
        };

        // Fonction de sélection d'un enfant par le parent
        $rootScope.chooseChild = async function(eleve) {
            if(eleve.classe ===  undefined){
                eleve.classe = new Classe({id: eleve.idClasse});
                await eleve.classe.sync();
            }
            evaluations.eleve = eleve;
            $rootScope.eleve = evaluations.eleve;
            $rootScope.selectedEleve = $rootScope.eleve;
            $rootScope.setCurrentPeriode();
            await evaluations.devoirs.sync(eleve.idStructure,eleve.id, eleve.idClasse);
            $rootScope.devoirs = evaluations.devoirs;
            $rootScope.matieres = evaluations.matieres;
            $rootScope.enseignants = evaluations.enseignants;

            $rootScope.$broadcast('loadEleve');
            utils.safeApply($scope);
        };


        /**
         * Charge la liste des periodes dans $scope.periodes et détermine la période en cours et positionne
         * son id dans $scope.currentPeriodeId
         */
        $rootScope.setCurrentPeriode = function() {
            // récupération des périodes et filtre sur celle en cours
            let periodes;
            if (model.me.type === 'PERSRELELEVE') {
                periodes = $rootScope.eleve.classe.periodes;
            }else {
                periodes = evaluations.eleve.classe.periodes;
            }
            periodes.sync().then(() => {
                let formatStr = "DD/MM/YYYY";
                let momentCurrDate = moment(moment().format(formatStr), formatStr);
                let foundPeriode = false;
                $rootScope.currentPeriodeId = null;

                for (let i = 0; i < periodes.all.length && !foundPeriode; i++) {
                    let momentCurrPeriodeDebut = moment(moment(periodes.all[i].timestamp_dt).format(formatStr),
                        formatStr);
                    let momentCurrPeriodeFin = moment(moment(periodes.all[i].timestamp_fn).format(formatStr),
                        formatStr);

                    if ( momentCurrPeriodeDebut.diff(momentCurrDate) <= 0
                        && momentCurrDate.diff(momentCurrPeriodeFin) <= 0) {
                        foundPeriode = true;
                        $rootScope.periode = periodes.findWhere({id : periodes.all[i].id});
                        evaluations.periode = $rootScope.periode;
                        $rootScope.$broadcast('loadPeriode');
                        $rootScope.currentPeriodeId = $rootScope.periode.id;
                    }
                }
                utils.safeApply($scope);

            });
        };

        $rootScope.me = {
            type : model.me.type
        };
        $rootScope.isCurrentPeriode = function(periode) {
            return (periode.id === $rootScope.currentPeriodeId);
        };
    }
]);