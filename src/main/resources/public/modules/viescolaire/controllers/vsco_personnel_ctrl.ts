import {notify, idiom as lang, template, routes, model, ng } from '../../entcore/entcore';

let moment = require('moment');
let _ = require('underscore');

export let viescolaireController = ng.controller('ViescolaireController', [
    '$scope', 'route', 'model',
    function ($scope, route, model) {
        $scope.template = template;

        $scope.safeApply = function(fn) {
            let phase = this.$root.$$phase;
            if(phase == '$apply' || phase == '$digest') {
                if(fn && (typeof(fn) === 'function')) {
                    fn();
                }
            } else {
                this.$apply(fn);
            }
        };

        route({
            accueil: function (params) {
                template.open('main', '../templates/viescolaire/vsco_acu_personnel');
            }
        });
    }
]);