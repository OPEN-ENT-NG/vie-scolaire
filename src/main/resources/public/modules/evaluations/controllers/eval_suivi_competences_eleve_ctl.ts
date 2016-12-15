/**
 * Created by ledunoiss on 27/10/2016.
 */

import {ng, template } from 'entcore/entcore';
import {SuiviCompetence, Domaine} from '../models/eval_teacher_mdl';
import * as utils from '../utils/teacher';

declare let _:any;

export let evalSuiviCompetenceEleveCtl = ng.controller('EvalSuiviCompetenceEleveCtl', [
    '$scope', 'route', '$rootScope', '$location', '$filter', '$templateCache',
    function ($scope, route, $rootScope, $location, $filter, $templateCache) {
        template.open('container', '../templates/layouts/2_10_layout');
        template.open('left-side', '../templates/evaluations/enseignants/suivi_competences_eleve/left_side');
        template.open('content', '../templates/evaluations/enseignants/suivi_competences_eleve/content');
        template.open('suivi-competence-content', '../templates/evaluations/enseignants/suivi_competences_eleve/content_vue_suivi_eleve');
        $scope.search.eleve = "";
        delete $scope.informations.eleve;
        $scope.opened.detailCompetenceSuivi = false;
        $scope.suiviCompetence = {};

        $scope.suiviFilter = {
            mine : 'true'
        };


       $scope.idCycle = 1;

        /**
         * Créer une suivi de compétence
         */
        $scope.selectEleve = function () {
            $scope.informations.eleve = $scope.search.eleve;
            if ($scope.informations.eleve !== null && $scope.search.eleve !== "") {
                $scope.suiviCompetence = new SuiviCompetence($scope.search.eleve, $scope.search.periode);
                $scope.suiviCompetence.sync().then(() => {
                    $scope.suiviCompetence.domaines.sync($scope.idCycle);


                    setTimeout(function() {
                        $scope.suiviCompetence.setMoyenneCompetences($scope.suiviFilter.mine);
                    },400);

                    $scope.informations.eleve.suiviCompetences.push($scope.suiviCompetence);
                    if ($scope.opened.detailCompetenceSuivi) {
                        $scope.detailCompetence = $scope.suiviCompetence.findCompetence($scope.detailCompetence.id);
                        if (!$scope.detailCompetence) $scope.backToSuivi();
                    }

                    $scope.template.close('suivi-competence-content');
                    utils.safeApply($scope);
                    $scope.template.open('suivi-competence-content', '../templates/evaluations/enseignants/suivi_competences_eleve/content_vue_suivi_eleve');
                    utils.safeApply($scope);
                });
            }
        };

        /**
         * Filtre permettant de retourner l'évaluation maximum en fonction du paramètre de recherche "Mes Evaluations"
         * @param listeEvaluations Tableau d'évaluations de compétences
         * @returns {(evaluation:any)=>(boolean|boolean)} Retourne true si la compétence courante est la plus haute du tableau listeEvaluations
         */
        $scope.isMaxEvaluation = function (listeEvaluations) {
            return function (evaluation) {
                var _t = listeEvaluations;
                if ($scope.suiviFilter.mine === 'true' || $scope.suiviFilter.mine === true) {
                    _t = _.filter(listeEvaluations, function (competence) {
                        return competence.owner !== undefined && competence.owner === $scope.me.userId;
                    });
                }
                var max = _.max(_t, function (competence) {
                    return competence.evaluation;
                });
                if (typeof max === 'object') {
                    return evaluation.id_competences_notes === max.id_competences_notes;
                } else {
                    return false;
                }
            };
        };

        /**
         * Retourne si l'utilisateur n'est pas le propriétaire de compétences
         * @param listeEvaluations Tableau d'évaluations de compétences
         * @returns {boolean} Retourne true si l'utilisateur n'est pas le propriétaire
         */
        $scope.notEvalutationOwner = function (listeEvaluations) {
            if ($scope.suiviFilter.mine === 'false' || $scope.suiviFilter.mine === false) {
                return false;
            }
            var _t = _.filter(listeEvaluations, function (competence) {
                return competence.owner === undefined || competence.owner === $scope.me.userId;
            });
            return _t.length === 0;
        };


        /*
         Listener sur le template suivi-competence-detail permettant la transition entre la vue détail
         et la vue globale
         */
        template.watch("suivi-competence-detail", function () {
            if (!$scope.opened.detailCompetenceSuivi) {
                $scope.opened.detailCompetenceSuivi = true;
            }
        });

        /**
         * Lance la séquence d'ouverture du détail d'une compétence permettant d'accéder à la vue liste ou graph
         * @param competence Compétence à ouvrir
         */
        $scope.openDetailCompetence = function (competence) {
            $scope.detailCompetence = competence;
            template.open("suivi-competence-detail", "../templates/evaluations/enseignants/suivi_competences_eleve/detail_vue_tableau");
        };

        /**
         * Lance la séquence de retour à la vue globale du suivi de compétence
         */
        $scope.backToSuivi = function () {
            template.close("suivi-competence-detail");
            $scope.opened.detailCompetenceSuivi = false;
            $scope.detailCompetence = null;
        };

        /**
         * Retourne si l'utilisateur est le propriétaire de l'évaluation
         * @param evaluation Evaluation à afficher
         * @returns {boolean} Retourne true si l'utilisateur est le propriétaire de l'évaluation
         */
        $scope.filterOwnerSuivi = function (evaluation) {
            if ($scope.suiviFilter.mine === 'false' || $scope.suiviFilter.mine === false) {
                return true;
            }
            return evaluation.owner === $scope.me.userId;
        };

        /**
         * Recherche l'index de l'objet dans le tableau
         * @param array tableau d'objets
         * @param obj objet
         * @returns {number} index de l'objet
         */
        var searchIndex = function (array, obj) {
            return _.indexOf(array, obj);
        };

        /**
         * Remplace l'élève recherché par le nouveau suite à l'incrémentation de l'index
         * @param num pas d'incrémentation. Peut être positif ou négatif
         */
        $scope.incrementEleve = function (num) {
            var index = searchIndex($scope.search.classe.eleves.all, $scope.search.eleve);
            if (index !== -1 && (index + parseInt(num)) >= 0
                && (index + parseInt(num)) < $scope.search.classe.eleves.all.length) {
                $scope.search.eleve = $scope.search.classe.eleves.all[index + parseInt(num)];
                $scope.selectEleve();
                utils.safeApply($scope);
            }
        };
    }
]);
