/**
 * Created by ledunoiss on 21/09/2016.
 */
import {ng, appPrefix} from 'entcore/entcore';

export let cSkillsColorColumn = ng.directive("cSkillsColorColumn", function(){
    return {
        restrict : 'E',
        scope : {
            devoir : '='
        },
        templateUrl: "/"+appPrefix+"/public/components/cSkillsColorColumn.html",
        controller : ['$scope', function($scope){
            $scope.compteur = 0;

            $scope.selectCompetences = function(competenceHeader){
                _.each($scope.eleves, function (eleve) {
                    var competencesNotesEleve = eleve.competencesNotesEleve;
                    var competenceEleve = _.findWhere(competencesNotesEleve, {id_competence: competenceHeader.id_competence});
                    competenceEleve.evaluation = competenceHeader.evaluation;
                });
                $scope.safeApply();
            };

            $scope.safeApply = function (fn) {
                var phase = this.$root.$$phase;
                if(phase === '$apply' || phase === '$digest') {
                    if(fn && (typeof(fn) === 'function')) fn();
                } else this.$apply(fn);
            };

            $scope.saveCompetences = function(competenceHeader){
                if(competenceHeader.modified) {
                    var _data = [];
                    for (var i = 0; i < $scope.devoir.eleves.all.length; i++) {
                        var competence = $scope.devoir.eleves.all[i].evaluation.competenceNotes.findWhere({id_competence: competenceHeader.id_competence});
                        if (competence !== undefined) {
                            competence.evaluation = competenceHeader.evaluation;
                            _data.push(competence);
                        }
                    }
                    $scope.devoir.saveCompetencesNotes(_data);
                    competenceHeader.modified = false;
                }
            };

            $scope.init = function(competenceHeader){
                $scope.$on('initHeaderColumn', function () {
                    competenceHeader.evaluation = -1;
                    competenceHeader.modified = false;
                    $scope.majHeaderColor(competenceHeader);
                })
            };

            $scope.switchColor = function(competenceHeader){
                if(competenceHeader.evaluation === -1){
                    competenceHeader.evaluation = 3;
                }else{
                    competenceHeader.evaluation = competenceHeader.evaluation -1;
                }
                competenceHeader.modified = true;
                $scope.selectCompetences(competenceHeader);
            };

            $scope.$on('changeHeaderColumn', function(event, competence){
                var competenceHeader = $scope.devoir.competences.findWhere({id_competence : competence.id_competence});
                $scope.majHeaderColor(competenceHeader);
            });

            $scope.majHeaderColor = function(competenceHeader) {
                // recuperation de la competence pour chaque eleve
                var allCompetencesElevesColumn = [];
                _.each($scope.devoir.eleves.all, function (eleve) {
                    if (eleve.evaluation.competenceNotes !== undefined && eleve.evaluation.competenceNotes.all.length > 0) {
                        var competenceEleve = eleve.evaluation.competenceNotes.findWhere({id_competence: competenceHeader.id_competence});
                        allCompetencesElevesColumn.push(competenceEleve);
                    }
                });


                if(allCompetencesElevesColumn !== undefined && allCompetencesElevesColumn.length > 0) {
                    // si toutes les competences ont la même note on colore evaluation de la même couleur
                    if (_.every(allCompetencesElevesColumn, function (competence) {
                            return (competence.evaluation === allCompetencesElevesColumn[0].evaluation);
                        })) {
                        competenceHeader.evaluation = allCompetencesElevesColumn[0].evaluation;
                    } else {
                        competenceHeader.evaluation = -1;
                    }
                }
                $scope.safeApply();
            };
        }]
    };
})