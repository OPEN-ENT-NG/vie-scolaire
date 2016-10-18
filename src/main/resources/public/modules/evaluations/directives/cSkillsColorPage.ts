/**
 * Created by ledunoiss on 21/09/2016.
 */
import {ng, appPrefix} from 'entcore/entcore';

export let cSkillsColorPage = ng.directive("cSkillsColorPage", function(){
    return {
        restrict : 'E',
        scope : {
            devoir : '='
        },
        templateUrl: "/"+appPrefix+"/public/components/cSkillsColorPage.html",
        controller : ['$scope', function($scope){
            $scope.selectColor = function(evaluation){
                var text = "Cette action va initialiser l'ensemble des compétences à la valeur sélectionnée.\n\n Souhaitez vous continuer ?\n";
                if(confirm(text) === true){
                    var _datas = [];
                    for (var i = 0; i < $scope.devoir.eleves.all.length; i++) {
                        var _eval = $scope.devoir.eleves.all[i].evaluation;
                        for (var j = 0; j < _eval.competenceNotes.all.length; j++) {
                            _eval.competenceNotes.all[j].evaluation = evaluation;
                            _datas.push(_eval.competenceNotes.all[j]);
                        }
                    }
                    $scope.devoir.saveCompetencesNotes(_datas);
                    for (var g = 0; g < $scope.devoir.competences.all.length; g++) {
                        $scope.devoir.competences.all[g].evaluation = evaluation;
                    }
                }
            };
        }]
    };
})