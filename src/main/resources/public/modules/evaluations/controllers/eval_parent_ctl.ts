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
import {Defaultcolors} from "../models/eval_niveau_comp";
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
                template.open('main', '../templates/evaluations/parent_enfant/liste_devoirs/display_devoirs_structure');
                template.open('evaluations', '../templates/evaluations/parent_enfant/liste_devoirs/list_view');
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
            let initialise = async () => {
                if (model.me.type === 'ELEVE') {
                    $rootScope.eleve = evaluations.eleve;
                }
                else if (model.me.type === 'PERSRELELEVE') {
                    $rootScope.eleves = evaluations.eleves.all;
                }
                await $rootScope.chooseChild (evaluations.eleve);
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
            evaluations.eleve = eleve;
            $rootScope.eleve = evaluations.eleve;
            $rootScope.selectedEleve = $rootScope.eleve;
            eleve.classe = new Classe({id: eleve.idClasse});
            await eleve.classe.sync();
            await evaluations.devoirs.sync( eleve.idStructure, eleve.id, undefined );
            $rootScope.devoirs = evaluations.devoirs;
            $rootScope.matieres = evaluations.matieres;
            $rootScope.enseignants = evaluations.enseignants;
            $rootScope.setCurrentPeriode();
            await $rootScope.updateNiveau(evaluations.usePerso);
            $scope.update = false;
            utils.safeApply($scope);
        };


        /**
         * Charge la liste des periodes dans $scope.periodes et détermine la période en cours et positionne
         * son id dans $scope.currentPeriodeId
         */
        $rootScope.setCurrentPeriode = function() {
            // récupération des périodes et filtre sur celle en cours
            let periodes = evaluations.eleve.classe.periodes;
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
                        $rootScope.currentPeriodeId = $rootScope.periode.id;
                    }

                }
                $rootScope.$broadcast('loadPeriode');
                utils.safeApply($scope);

            });
        };
        $rootScope.template = template;
        $rootScope.me = {
            type : model.me.type
        };
        $rootScope.suiviFilter= {
            mine : false
        };
        $rootScope.isCurrentPeriode = function(periode) {
            return (periode.id === $rootScope.currentPeriodeId);
        };
        /**
         * Retourne le libelle de la matière correspondant à l'identifiant passé en paramètre
         * @param idMatiere identifiant de la matière
         * @returns {any} libelle de la matière
         */
        $rootScope.getLibelleMatiere = function (idMatiere) {
            if (idMatiere === undefined || idMatiere == null || idMatiere === "") return "";
            let matiere = _.findWhere($rootScope.matieres.all, {id: idMatiere});
            if (matiere !== undefined && matiere.hasOwnProperty('name')) {
                return matiere.name;
            } else {
                return '';
            }
        };

        $rootScope.getTeacherDisplayName = function (owner) {
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
        $rootScope.getDateFormated = function (date) {
            return utils.getFormatedDate(date, "DD/MM");
        };

        $rootScope.saveTheme = function () {
            $rootScope.chooseTheme();
        };

        $rootScope.updateColorAndLetterForSkills = async function () {
            $rootScope.niveauCompetences = evaluations.niveauCompetences;
            $rootScope.arrayCompetences = _.groupBy(evaluations.niveauCompetences.all,
                {id_cycle : evaluations.eleve.classe.id_cycle}).true;
            $rootScope.structure = {
                usePerso: evaluations.usePerso
            };
            // chargement dynamique des couleurs du niveau de compétences
            // et de la valeur max (maxOrdre)
            $rootScope.mapCouleurs = {"-1": Defaultcolors.unevaluated};
            $rootScope.mapLettres = {"-1": " "};
            _.forEach($rootScope.arrayCompetences, function (niv) {
                $rootScope.mapCouleurs[niv.ordre - 1] = niv.couleur;
                $rootScope.mapLettres[niv.ordre - 1] = niv.lettre;
            });
            utils.safeApply($rootScope);
        };
        $rootScope.updateColorArray = async function () {
            evaluations.arrayCompetences =
                _.groupBy(evaluations.niveauCompetences.all,{id_cycle : evaluations.eleve.classe.id_cycle}).true;
        };
        $rootScope.updateNiveau =  async function (usePerso) {
            if (usePerso === 'true') {
                evaluations.usePerso = 'true';
                evaluations.niveauCompetences.sync(false).then(async () => {
                    if ($scope.update){
                        await $rootScope.syncColorAndLetter();

                    }
                    else {
                        evaluations.niveauCompetences.first().markUser().then(async () => {
                            await $rootScope.syncColorAndLetter();
                        });
                    }
                });

            }
            else if (usePerso === 'false') {
                evaluations.usePerso = 'false';
                evaluations.niveauCompetences.sync(true).then( async () => {
                    if($rootScope.update) {
                        await $rootScope.syncColorAndLetter();
                    }
                    else {
                        evaluations.niveauCompetences.first().unMarkUser().then(async () => {
                            await $rootScope.syncColorAndLetter();
                        });
                    }
                });
            }
        };

        $rootScope.syncColorAndLetter = async function () {
            await $rootScope.updateColorArray();
            $rootScope.updateColorAndLetterForSkills();
            utils.safeApply($rootScope);
        };
        $rootScope.initLimit = function () {
            $rootScope.limits = [5,10,15,20];
            $rootScope.limitSelected = $rootScope.limits[0];
        };

        $rootScope.getLibelleLimit = function (limit) {
            return limit +" " + lang.translate('last');
        };

        $rootScope.update = true;

    }
]);