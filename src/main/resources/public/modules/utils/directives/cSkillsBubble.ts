/**
 * Created by ledunoiss on 09/11/2016.
 */

//FIXME CETTE DIRECTIVE DOIT SE TROUVER DANS LE MODULE COMPETENCES. LA SUPPRIMER QUAND UN SNIPPET SERA UTILISE

import { ng } from 'entcore';
import * as utils from '../functions/safeApply';

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
            text : '=?',
            classes : '=?',
            selectCond : '=?',
            onClick: '=?'
        },
        templateUrl : "/competences/public/template/directives/cSkillsBubble.html",
        controller : ['$scope', function ($scope) {
            $scope.activeClick = false;
            if($scope.classes === undefined){
                $scope.classes = ' ';
            }
            if ($scope.selectCond === undefined) {
                $scope.selected = false;
            }
            if ($scope.onClick !== undefined) {
                $scope.activeClick = true;
            }
            $scope.text = $scope.text || '';
            $scope.$watch('color', function (newValue, oldValue) {
                if (newValue !== oldValue) {
                    utils.safeApply($scope);
                }
            }, true);
        }]
    };
});