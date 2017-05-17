import {notify, idiom as lang, template, routes, model, ng } from 'entcore/entcore';
import {vieScolaire} from '../models/vsco_personnel_mdl';

let moment = require('moment');
declare let _: any;

export let viescolaireController = ng.controller('ViescolaireController', [
    '$scope', 'route', 'model', '$location',
    function ($scope, route, model, $location) {
        $scope.template = template;

        $scope.safeApply = function(fn) {
            let phase = this.$root.$$phase;
            if (phase === '$apply' || phase === '$digest') {
                if (fn && (typeof(fn) === 'function')) {
                    fn();
                }
            } else {
                this.$apply(fn);
            }
        };
        $scope.lang = lang;

        $scope.hasParam = function (param) {
            return Object.prototype.hasOwnProperty.call($location.search(), param);
        };

        $scope.findParam = function (key) {
            if ($scope.hasParam(key)) {
                return ($location.search())[key];
            } else {
                return false;
            }
        };

        route({
            accueil: function (params) {
                $scope.structures = vieScolaire.structures;
                $scope.structure = vieScolaire.structure;
                template.open('main', '../templates/viescolaire/vsco_acu_personnel');
                $scope.safeApply($scope);
            }
        });
    }
]);