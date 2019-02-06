import {ng, idiom as lang,notify} from "entcore";
import * as utils from "../../utils/functions/safeApply";
import {PeriodeAnnee} from "../models/common/PeriodeAnnee";
import {NotificationToast} from "../models/common/NotificationToast";

export const periodeAnneeController = ng.controller('periodeAnneeController', [
    '$scope',  'route', 'model',
    function ($scope) {
        console.log('periodeAnneeController');
        $scope.notifications = [];

        $scope.periodeAnnee = new PeriodeAnnee($scope.structure.id);

        $scope.periodeAnnee.sync().then((res) => {
            utils.safeApply($scope);
        });

        $scope.toastHttpCall = (response) => {
            if (response.succeed) {
                $scope.notifications.push(new NotificationToast(lang.translate(response.toastMessage), 'confirm'));
            } else {
                $scope.notifications.push(new NotificationToast(lang.translate(response.toastMessage), 'error'));
            }
            utils.safeApply($scope);
            return response;
        };

        $scope.createPeriodeAnnee = async () => {
            $scope.periodeAnnee.setIsOpening(true);
            $scope.toastHttpCall(await $scope.periodeAnnee.save());
        }
    }
]);