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
        $scope.suiviCompetence = {};

        $scope.suiviFilter = {
            mine : 'true'
        };

        $scope.suivi = {};

        $scope.selectEleve = function () {
            $scope.informations.eleve = $scope.search.eleve;
            if ($scope.informations.eleve !== null && $scope.search.eleve !== "") {
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

        $scope.isMaxEvaluation = function (evaluation, listeEvaluations) {
            var _t = listeEvaluations;
            if ($scope.suiviFilter.mine === 'true' || $scope.suiviFilter.mine === true) {
                _t = _.filter(listeEvaluations, function (competence) {
                    return competence.owner === undefined || competence.owner === $scope.me.userId;
                });
            }
            var max = _.max(_t, function (competence) {
                return competence.evaluation;
            });
            if (max !== 'Object') {
                return _.isEqual(evaluation, max)
            } else {
                return false;
            }

        };

        $scope.notEvalutationOwner = function (listeEvaluations) {
            if ($scope.suiviFilter.mine === 'false' || $scope.suiviFilter.mine === false) {
                return false;
            }
            var _t = _.filter(listeEvaluations, function (competence) {
                return competence.owner === undefined || competence.owner === $scope.me.userId;
            });
            return _t.length === 0;
        };
    }
]);
