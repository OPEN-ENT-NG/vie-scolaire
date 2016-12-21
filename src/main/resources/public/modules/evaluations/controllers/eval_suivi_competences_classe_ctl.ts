/**
 * Created by ledunoiss on 27/10/2016.
 */

import {ng, template, model } from 'entcore/entcore';
import {SuiviCompetenceClasse, CompetenceNote} from '../models/eval_teacher_mdl';
import * as utils from '../utils/teacher';

declare let _:any;

export let evalSuiviCompetenceClasseCtl = ng.controller('EvalSuiviCompetenceClasseCtl', [
    '$scope', 'route', '$rootScope', '$location', '$filter', '$route',
    function ($scope, route, $rootScope, $location, $filter, $route) {
        template.open('container', '../templates/layouts/2_10_layout');
        template.open('left-side', '../templates/evaluations/enseignants/suivi_competences_eleve/left_side');
        template.open('content', '../templates/evaluations/enseignants/suivi_competences_classe/content');
        template.close('suivi-competence-detail');
        template.close('suivi-competence-content');
        delete $scope.informations.eleve;
        delete $scope.informations.classe;
        $scope.opened.detailCompetenceSuivi = false;
        $scope.suiviCompetence = {};
        $scope.mapEleves = {};
        $scope.route = $route;
        $scope.search.classe = "";
        $scope.suiviFilter = {
            mine : 'false'
        };


        $scope.idCycle = 1;


        /**
         * Créer une suivi de compétence
         */

        $scope.selectSuivi = function () {
            $scope.informations.classe = $scope.search.classe;
            if ($scope.informations.classe !== null && $scope.informations.classe !== "") {
                $scope.suiviCompetence = new SuiviCompetenceClasse($scope.search.classe, $scope.search.periode);
                $scope.suiviCompetence.sync().then(() => {
                    $scope.suiviCompetence.domaines.sync($scope.idCycle);
                    if ($scope.opened.detailCompetenceSuivi) {
                        $scope.detailCompetence = $scope.suiviCompetence.findCompetence($scope.detailCompetence.id);
                        if (!$scope.detailCompetence) $scope.backToSuivi();
                    }

                    $scope.informations.classe.suiviCompetenceClasses.push($scope.suiviCompetence);
                    if (!template.isEmpty('suivi-competence-content')) template.close('suivi-competence-content');
                    utils.safeApply($scope);
                    template.open('suivi-competence-content', '../templates/evaluations/enseignants/suivi_competences_classe/content_vue_suivi_classe');
                    utils.safeApply($scope);
                });
            }
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

        /**
         * Filtre les évaluations pour le suivi classe
         * @param listeEvaluations liste des évaluations
         */
        $scope.filterEvaluation = function (listeEvaluations) {
            if ($scope.suiviFilter.mine === "false") {
                var evals = [];
                for (var i = 0; i < $scope.informations.classe.eleves.all.length; i++) {
                    var t = _.where(listeEvaluations, {id_eleve : $scope.informations.classe.eleves.all[i].id});
                    if (t.length > 0) {
                        evals.push(_.max(t, function (evaluation) { return evaluation.evaluation }));
                    } else {
                        evals.push(new CompetenceNote({evaluation : -1, id_eleve : $scope.informations.classe.eleves.all[i].id}));
                    }
                }
                return evals;
            }
            return listeEvaluations;
        };


        /**
         * Listener sur le template suivi-competence-detail permettant la transition entre la vue détail
         * et la vue globale
         */
        template.watch("suivi-competence-detail", function () {
            if (!$scope.opened.detailCompetenceSuivi) {
                $scope.opened.detailCompetenceSuivi = true;
            }
        });

        /**
         * Lance la séquence d'ouverture du détail d'une compétence
         * @param competence Compétence à ouvrir
         */
        $scope.openDetailCompetence = function (competence) {
            $scope.detailCompetence = competence;
            template.open("suivi-competence-detail", "../templates/evaluations/enseignants/suivi_competences_classe/detail_vue_classe");
        };

        /**
         * Retourne la classe en fonction de l'évaluation obtenue pour la compétence donnée
         * @param eleveId identifiant de l'élève
         * @returns {String} Nom de la classe
         */
        $scope.getEvaluationResult = function (eleveId) {
            var evaluations = $scope.suiviFilter.mine == 'true'
                ? _.where($scope.detailCompetence.competencesEvaluations, {id_eleve : eleveId, owner : model.me.userId})
                : _.where($scope.detailCompetence.competencesEvaluations, {id_eleve : eleveId});
            if (evaluations.length > 0) {
                var evaluation = _.max(evaluations, function (evaluation) { return evaluation.evaluation; });
                switch (evaluation.evaluation) {
                    case 0 : return "border-red";
                    case 1 : return "border-orange";
                    case 2 : return "border-yellow";
                    case 3 : return "border-green";
                    default : return "border-grey";
                }
            }
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
        $scope.incrementClasse = function (num) {
            var index = searchIndex($scope.classes.all, $scope.search.classe);
            if (index !== -1 && (index + parseInt(num)) >= 0
                && (index + parseInt(num)) < $scope.classes.all.length) {
                $scope.search.classe = $scope.classes.all[index + parseInt(num)];
                $scope.selectSuivi('/competences/classe');
                utils.safeApply($scope);
            }
        };
    }
]);
