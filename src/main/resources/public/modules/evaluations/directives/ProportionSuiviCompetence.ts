/**
 * Created by ledunoiss on 09/11/2016.
 */

import {ng} from 'entcore/entcore';
import * as utils from '../utils/teacher';

/**
 * Directive de proportions de compétences
 */
export let proportionSuiviCompetence = ng.directive('proportionSuiviCompetence', function () {
    return {
        restrict : 'E',
        scope : {
            evaluations : '=',
            filter : '=',
            user : '=',
            isClasse : '='
        },
        template : '<div class="inline-block suivi-proportion" ng-repeat="prop in proportion" ' +
        'ng-class="{green : prop.eval === 3, yellow : prop.eval === 2, orange: prop.eval === 1, red : prop.eval === 0, grey : prop.eval === -1}" ' +
        'ng-style="{\'width\': prop.percent + \'%\'}" ' +
        'tooltip="[[prop.percent]] [[translate(\'%\')]]"></div>',
        controller : ['$scope', function ($scope) {

            $scope.isClasse = $scope.isClasse !== undefined ? $scope.isClasse : false;

            /**
             * Listener sur la variable filter. Si modification de la variable, recalcule des proportions
             */
            $scope.$watch('filter', function (newValue, oldValue) {
                if (newValue !== oldValue) {
                    $scope.calculProportion();
                }
            }, true);

            /**
             * Retourne la valeur d'une clé i18n passées en paramètres
             * @param key clé i18n
             * @returns {String} valeur i18n
             */
            $scope.translate = function (key) {
                return utils.translate(key);
            };

            /**
             * Calcul la proportion d'évaluations pour une compétence
             */
            $scope.calculProportion = function () {
                $scope.competencesEvaluations = $scope.evaluations;
                $scope.proportion = [];
                for (var i = -1; i < 4; i++) {
                    $scope.proportion.push({
                        eval : i,
                        percent : 0,
                        nb : 0
                    });
                }
                if ($scope.filter.mine === 'true' || $scope.filter.mine === true) {
                    $scope.competencesEvaluations = _.filter($scope.evaluations, function (evaluation) {
                        return evaluation.owner === $scope.user.userId;
                    });
                }
                if ($scope.competencesEvaluations.length > 0 && !_.every($scope.competencesEvaluations, function (competence) { return competence.evaluation === -1})) {
                    var nbEleves = 0;
                    if ($scope.isClasse == true) {
                        var elevesMap = {};
                        for (var i = 0; i < $scope.competencesEvaluations.length; i++) {
                            if (!elevesMap.hasOwnProperty($scope.competencesEvaluations[i].id_eleve)) {
                                elevesMap[$scope.competencesEvaluations[i].id_eleve] = $scope.competencesEvaluations[i];
                                $scope.proportion[($scope.competencesEvaluations[i].evaluation) + 1].nb++;
                                nbEleves++;
                            } else if (parseInt(elevesMap[$scope.competencesEvaluations[i].id_eleve].evaluation) < parseInt($scope.competencesEvaluations[i].evaluation)) {
                                $scope.proportion[(elevesMap[$scope.competencesEvaluations[i].id_eleve].evaluation) + 1].nb--;
                                elevesMap[$scope.competencesEvaluations[i].id_eleve] = $scope.competencesEvaluations[i];
                                $scope.proportion[parseInt($scope.competencesEvaluations[i].evaluation) + 1].nb++;
                            }
                        }
                    }
                    for (var i = 0; i < $scope.proportion.length; i++) {
                        if ($scope.isClasse == true) {
                            var nb = $scope.proportion[i].nb;
                            $scope.proportion[i].percent = (nb / nbEleves) * 100;
                            $scope.proportion[i].nb = nb;
                        } else {
                            var nb = _.where($scope.competencesEvaluations, {evaluation : parseInt($scope.proportion[i].eval)});
                            $scope.proportion[i].percent = (nb.length / $scope.competencesEvaluations.length) * 100;
                            $scope.proportion[i].nb = nb.length;
                        }
                    }
                }
            };

            $scope.calculProportion();
        }]
    };
});