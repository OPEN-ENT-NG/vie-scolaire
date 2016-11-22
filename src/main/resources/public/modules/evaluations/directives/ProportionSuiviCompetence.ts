/**
 * Created by ledunoiss on 09/11/2016.
 */

import {ng} from 'entcore/entcore';
import * as utils from '../utils/teacher';

export let proportionSuiviCompetence = ng.directive('proportionSuiviCompetence', function () {
    return {
        restrict : 'E',
        scope : {
            evaluations : '=',
            filter : '=',
            user : '='
        },
        template : '<div class="inline-block suivi-proportion" ng-repeat="prop in proportion" ' +
        'ng-class="{green : prop.eval === 3, yellow : prop.eval === 2, orange: prop.eval === 1, red : prop.eval === 0, grey : prop.eval === -1}" ' +
        'ng-style="{\'width\': prop.percent + \'%\'}" ' +
        'tooltip="[[prop.nb]] [[translate(\'evaluations\')]]"></div>',
        controller : ['$scope', function ($scope) {
            $scope.$watch('filter', function (newValue, oldValue) {
                if (newValue !== oldValue) {
                    $scope.calculProportion();
                }
            }, true);

            $scope.translate = function (key) {
                return utils.translate(key);
            };

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
                    for (var i = 0; i < $scope.proportion.length; i++) {
                        var nb = _.where($scope.competencesEvaluations, {evaluation : parseInt($scope.proportion[i].eval)});
                        $scope.proportion[i].percent = (nb.length/$scope.competencesEvaluations.length)*100;
                        $scope.proportion[i].nb = nb.length;
                    }
                }
            };

            $scope.calculProportion();
        }]
    };
});