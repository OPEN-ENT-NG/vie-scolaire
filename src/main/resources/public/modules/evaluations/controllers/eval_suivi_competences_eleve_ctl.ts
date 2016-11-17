/**
 * Created by ledunoiss on 27/10/2016.
 */

import {ng, template } from 'entcore/entcore';
import {SuiviCompetence, CompetenceNote} from '../models/eval_teacher_mdl';
import * as utils from '../utils/teacher';

declare let _:any;

export let evalSuiviCompetenceEleveCtl = ng.controller('EvalSuiviCompetenceEleveCtl', [
    '$scope', 'route', '$rootScope', '$location', '$filter',
    function ($scope, route, $rootScope, $location, $filter) {
        template.open('container', '../templates/layouts/2_10_layout');
        template.open('left-side', '../templates/evaluations/enseignants/suivi_competences_eleve/left_side');
        template.open('content', '../templates/evaluations/enseignants/suivi_competences_eleve/content');
        $scope.search.eleve = "";
        delete $scope.informations.eleve;
        $scope.opened.detailCompetenceSuivi = false;
        $scope.suiviCompetence = {};

        $scope.suiviFilter = {
            mine : 'true'
        };


        /**
         * Créer une suivi de compétence
         */
        $scope.selectEleve = function () {
            //Sélection de l'élève en cours.
            $scope.informations.eleve = $scope.search.eleve;
            if ($scope.informations.eleve !== null && $scope.search.eleve !== "") {
                //On regarde si l'utilisateur a déjà un suivi de compétence. Si oui, on le recupère. Si non, on le génère.
                var s = $scope.informations.eleve.suiviCompetences.findWhere({periode : $scope.search.periode});
                if (s === undefined) {
                    $scope.suiviCompetence = new SuiviCompetence($scope.enseignements, $scope.search.eleve, $scope.search.periode);
                    $scope.suiviCompetence.sync().then(() => {
                        $scope.informations.eleve.suiviCompetences.push($scope.suiviCompetence);
                        utils.safeApply($scope, null);
                    });
                } else {
                    $scope.suiviCompetence = s;
                    utils.safeApply($scope, null);
                }
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
                        return competence.owner === undefined || competence.owner === $scope.me.userId;
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
         * Retourne la propriété de l'évaluation
         * @param evaluation Evaluation à afficher
         * @returns {boolean} Retourne true si l'utilisateur est le propriétaire de l'évaluation
         */
        $scope.filterOwnerSuivi = function (evaluation) {
            if ($scope.suiviFilter.mine === 'false' || $scope.suiviFilter.mine === false) {
                return true;
            }
            return evaluation.owner === $scope.me.userId;
        }
    }
]);
