import {TimeSlots} from "../models/common/TimeSlots";
import {NotificationToast} from "../models/common/NotificationToast";
import * as utils from "../../utils/functions/safeApply";
import {idiom as lang, ng, _} from "entcore";

export const timeSlotsController = ng.controller('timeSlotsController', [
    '$scope',  'route', 'model',
    function ($scope) {
        console.log('timeSlotsController');
        $scope.notifications = [];
        $scope.timeSlots = new TimeSlots($scope.structure.id);
        $scope.timeSlot = undefined;

       $scope.timeSlots.syncAll().then(() => {
            for (let i = 0; i < $scope.timeSlots.all.length; i ++) {
                if ($scope.timeSlots.all[i].default) {
                    $scope.timeSlot = $scope.timeSlots.all[i];
                }
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

        $scope.saveTimeProfil = async () => {
            $scope.toastHttpCall(await $scope.timeSlot.save());
        }
    }
]);