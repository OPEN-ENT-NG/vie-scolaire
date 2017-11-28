/**
 * Created by rollinq on 21/08/2017.
 */
import {ng, appPrefix} from 'entcore/entcore';

export let cRoundAvatar = ng.directive("cRoundAvatar", [function () {
    return {
        templateUrl: "/" + appPrefix + "/public/components/cRoundAvatar.html",
        restrict: "E",
        scope: {
            eleve: "=eleve"
        }
    };
}]);