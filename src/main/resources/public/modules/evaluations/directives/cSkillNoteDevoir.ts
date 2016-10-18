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
            currentDevoir   : '='
        },
        template : '<span ng-click="switchColor()" ng-mouseover="detailCompetence(competence.nom)"  ng-mouseleave="saveCompetence()" ng-init="init()"  class="rounded" ng-class="{grey : competence.evaluation == -1, red : competence.evaluation == 0, orange : competence.evaluation == 1, yellow : competence.evaluation == 2, green : competence.evaluation == 3}"></span>',
        controller : ['$scope', function($scope){
            $scope.color = -1;
            $scope.modified = false;
            $scope.compteur = 0;
            $scope.switchColor = function(){
                if($scope.competence.evaluation === -1){
                    $scope.competence.evaluation = 3;
                }else{
                    $scope.competence.evaluation = $scope.competence.evaluation -1;
                }
                $scope.$emit('majHeaderColumn', $scope.competence);
                $scope.modified = true;
            };

            $scope.detailCompetence = function(competenceNom) {
                var e = $("#competence-detail");
                e.html('<a class="resume-competence" tooltip="'+competenceNom +'">'+ competenceNom +'</a>');
                $compile(e.contents())($scope);
            };

            $scope.saveCompetence = function(){
                if($scope.modified === true){
                    // var competenceToSave = new CompetenceNote($scope.competence);
                    $scope.competence.save(function(id){
                        $scope.competence.id = id;
                    });
                    $scope.modified = false;
                }
            };
        }]
    }
});