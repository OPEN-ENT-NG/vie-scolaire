import {ng, idiom as lang, notify, moment} from "entcore";
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
            if ($scope.periodeAnnee.id) {
                $scope.periodeAnnee.setIsExist(true);
            }
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

        $scope.isValidForm = function () {
            if ($scope.periodeAnnee) {
                return moment($scope.periodeAnnee.end_date).diff(moment($scope.periodeAnnee.start_date)) >= 0
                    && moment().isBefore($scope.periodeAnnee.end_date);
            }
        };

        $scope.createPeriodeAnnee = async () => {
            $scope.periodeAnnee.setIsOpening(true);
            $scope.toastHttpCall(await $scope.periodeAnnee.save());
        }
    }
]);