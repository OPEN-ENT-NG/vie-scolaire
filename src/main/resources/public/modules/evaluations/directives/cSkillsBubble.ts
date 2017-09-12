/**
 * Created by ledunoiss on 09/11/2016.
 */

import {ng, appPrefix} from 'entcore/entcore';
import * as utils from '../utils/teacher';

/**
 * Directive d'affichage de la boule des compétences
 * @param color {string}: Contient le code couleur de la boulle.
 * @param text {string}: Contient le text contenu dans la boulle.
 * @param classes {string}: Contient l'ensemble des classes css supplémentaires à appliquer à la boulle.
 * @param selectCond {boolean}: Contient la condition pour marquer une boule comme sélectionnée
 *                                          (rajouter la classe selected).
 */
export let cSkillsBubble = ng.directive('cSkillsBubble', function () {
    return {
        restrict : 'E',
        scope : {
            color : '=',
            text : '=',
            classes : '=?',
            selectCond : '=?'
        },
        templateUrl : "/"+appPrefix+"/public/components/cSkillsBubble.html",
        controller : ['$scope', function ($scope) {

            if($scope.classes === undefined){
                $scope.classes = ' ';
            }
            if ($scope.selectCond === undefined) {
                $scope.selected = false;
            }
            $scope.$watch('color', function (newValue, oldValue) {
                if (newValue !== oldValue) {
                    utils.safeApply($scope);
                }
            }, true);
        }]
    };
});