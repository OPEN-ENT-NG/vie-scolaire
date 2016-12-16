/**
 * Created by ledunoiss on 21/09/2016.
 */
import {ng, appPrefix} from 'entcore/entcore';

export let cSkillsColorPage = ng.directive("cSkillsColorPage", function(){
    return {
        restrict : 'E',
        scope : {
            devoir : '=',
            selectedEleves : '=',
            selectedCompetences : '='
        },
        templateUrl: "/"+appPrefix+"/public/components/cSkillsColorPage.html",
        controller : ['$scope', function($scope){
            $scope.selectColor = function(evaluation){
                var text = "Cette action va initialiser l'ensemble des compétences à la valeur sélectionnée.\n\n Souhaitez vous continuer ?\n";
                if(confirm(text) === true){
                    var _datas = [];
                    var _range = $scope.selectedEleves.length > 0 ? $scope.selectedEleves
                        : $scope.devoir.eleves.all;
                    // Boucle sur les élèves
                    for (var i = 0; i < _range.length; i++) {
                        // On récupère l'évaluation de l'élève sur le devoir
                        var _eval = _range[i].evaluation;
                        if ($scope.selectedCompetences.length > 0) {
                            // Si on a des compétences de sélectionnées, on les récupère
                            var ids = [];
                            for (var g = 0; g < $scope.selectedCompetences.length; g++) {
                                ids.push($scope.selectedCompetences[g].id_competence);
                            }
                            // Pour chaque compétences Notes de l'élève
                            for (var j = 0; j < _eval.competenceNotes.all.length; j++) {
                                if (ids.indexOf(_eval.competenceNotes.all[j].id_competence) !== -1) {
                                    // Si la compétence est sélectionnée, on l'ajoute
                                    _eval.competenceNotes.all[j].evaluation = evaluation;
                                    _datas.push(_eval.competenceNotes.all[j]);
                                }
                            }
                        } else {
                            for (var j = 0; j < _eval.competenceNotes.all.length; j++) {
                                _eval.competenceNotes.all[j].evaluation = evaluation;
                                _datas.push(_eval.competenceNotes.all[j]);
                            }
                        }
                    }
                    _.map($scope.selectedCompetences, function (comp) {
                        $scope.$emit('majHeaderColumn', comp);
                        comp.selected = false;
                    });
                    $scope.selectedCompetences = [];
                    _.map($scope.selectedEleves, function (eleve) {
                        eleve.selected = false;
                    });
                    $scope.selectedEleves = [];
                    $scope.devoir.saveCompetencesNotes(_datas);
                }
            };
        }]
    };
})