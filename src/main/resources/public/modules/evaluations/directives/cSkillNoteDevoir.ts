/**
 * Created by ledunoiss on 21/09/2016.
 */
import {ng} from 'entcore/entcore';

export let $ = require('jquery');

export let cSkillNoteDevoir = ng.directive('cSkillNoteDevoir', function($compile){
    return {
        restrict : 'E',
        scope : {
            competence : '=',
            nbEleve : '=',
            nbCompetencesDevoir : '=',
            currentDevoir   : '=',
            disabled : '=?',
            focus : '=',
            blur : '=',
            indexRow:'=',
            indexColumn:'=',
            eleve:'=',
            getEleveInfo:'='
        },
        template : '<span autofocus ng-if="indexRow === 0 && indexColumn ===0 && !currentDevoir.is_evaluated" ng-click="switchColor()" tabindex="0" ng-focus="focus(competence.id_competence, true);getEleveInfo(eleve);" ng-blur="blur(competence.id_competence, false); saveCompetence()" ng-keydown="keyColor($event)"  ng-mouseleave="saveCompetence()" ng-blur="saveCompetence()" ng-init="init()"  class="competence-eval rounded" ng-class="{grey : competence.evaluation == -1, red : competence.evaluation == 0, orange : competence.evaluation == 1, yellow : competence.evaluation == 2, green : competence.evaluation == 3}"></span> <span ng-if="indexRow !== 0 || indexColumn !==0 || currentDevoir.is_evaluated" ng-click="switchColor()" tabindex="0" ng-focus="focus(competence.id_competence, true);getEleveInfo(eleve);" ng-blur="blur(competence.id_competence, false); saveCompetence()" ng-keydown="keyColor($event)"  ng-mouseleave="saveCompetence()" ng-blur="saveCompetence()" ng-init="init()"  class="competence-eval rounded" ng-class="{grey : competence.evaluation == -1, red : competence.evaluation == 0, orange : competence.evaluation == 1, yellow : competence.evaluation == 2, green : competence.evaluation == 3}"></span>',
        controller : ['$scope', function($scope){
            $scope.color = -1;
            $scope.modified = false;
            $scope.compteur = 0;
            $scope.switchColor = function(){
                if (!$scope.disabled) {
                    if ($scope.competence.evaluation === -1) {
                        $scope.competence.evaluation = 3;
                    } else {
                        $scope.competence.evaluation = $scope.competence.evaluation - 1;
                    }
                    $scope.$emit('majHeaderColumn', $scope.competence);
                    $scope.modified = $scope.competence.oldValeur !== $scope.competence.evaluation;
                }
            };

            $scope.keys = {
                numbers : {zero : 96, one : 97, two : 98, three : 99, four : 100},
                shiftNumbers : {zero : 48, one : 49, two : 50, three : 51, four : 52}
            };

            $scope.init = function () {
                $scope.disabled = $scope.disabled !== undefined ? $scope.disabled : false;
                $scope.competence.oldValeur = $scope.competence.evaluation;
            };

            $scope.keyColor = function ($event) {
                if (!$scope.disabled) {
                    var key = $event.keyCode | $event.which;

                    switch (key) {
                        case $scope.keys.numbers.zero :
                        case $scope.keys.shiftNumbers.zero : {
                            $scope.competence.evaluation = -1;
                        }
                            break;
                        case $scope.keys.numbers.one :
                        case $scope.keys.shiftNumbers.one : {
                            $scope.competence.evaluation = 0;
                        }
                            break;
                        case $scope.keys.numbers.two :
                        case $scope.keys.shiftNumbers.two : {
                            $scope.competence.evaluation = 1;
                        }
                            break;
                        case $scope.keys.numbers.three :
                        case $scope.keys.shiftNumbers.three: {
                            $scope.competence.evaluation = 2;
                        }
                            break;
                        case $scope.keys.numbers.four :
                        case $scope.keys.shiftNumbers.four : {
                            $scope.competence.evaluation = 3;
                        }
                            break;
                    }
                    $scope.$emit('majHeaderColumn', $scope.competence);
                    $scope.modified = $scope.competence.oldValeur !== $scope.competence.evaluation;
                }
            };

            $scope.saveCompetence = function(){
                if (!$scope.disabled) {
                    if ($scope.modified === true) {
                        $scope.competence.save();
                        $scope.modified = false;
                    }
                }
            };
        }]
    }
});