import {ng, template} from "entcore/entcore";
import * as utils from "../utils/parent";

export let abscParentController = ng.controller('AbscParentController', [
    '$scope', 'route', 'model', '$rootScope', '$location', '$route',
    function ($scope, route, model, $rootScope, $location, $route) {
        route({
            AbsencesParentAccueil : function(params) {
                if (model.me.type === 'ELEVE' || model.me.type === 'PERSRELELEVE') {
                    template.open('main', '../templates/absences/parent_enfant/accueil/main');
                    utils.safeApply($scope);
                }
            }
        });
        $scope.template = template;
        $scope.showAbsLightBox = {
            bool : false
        };
}]);