import {TimeSlot, TimeSlots} from "../models/common/TimeSlots";
import {NotificationToast} from "../models/common/NotificationToast";
import * as utils from "../../utils/functions/safeApply";
import {idiom as lang, ng, _, moment} from "entcore";
import {AxiosError, AxiosPromise, AxiosResponse} from "axios";

export const timeSlotsController = ng.controller('timeSlotsController', [
    '$scope',  'route', 'model',
    function ($scope) {
        console.log('timeSlotsController');
        $scope.notifications = [];
        $scope.timeSlots = new TimeSlots($scope.structure.id);
        $scope.timeSlot = new TimeSlot();
        $scope.endTimes = [];

        const initEndTimes = (): void => {
            $scope.endTimes = [];
            $scope.timeSlot.slots.forEach(item => {
                $scope.endTimes.push(item.endHour);
            });
        };

        const formatTime = (time: string):string => {
            return moment(time, 'HH:mm').format('kk:mm');
        };

        const syncTimeslots = (): void => {
            $scope.timeSlots.syncAll().then(() => {
                for (let i = 0; i < $scope.timeSlots.all.length; i ++) {
                    $scope.timeSlots.all[i].endOfHalfDay = formatTime($scope.timeSlots.all[i].endOfHalfDay);
                    if ($scope.timeSlots.all[i].default) {
                        $scope.timeSlot = $scope.timeSlots.all[i];
                        initEndTimes();
                    }
                }
                utils.safeApply($scope);
            });
        };

        syncTimeslots();

        $scope.toastHttpCall = (response) => {
            if (response.succeed) {
                $scope.notifications.push(new NotificationToast(lang.translate(response.toastMessage), 'confirm'));
            } else {
                $scope.notifications.push(new NotificationToast(lang.translate(response.toastMessage), 'error'));
            }
            utils.safeApply($scope);
            return response;
        };

        $scope.hasDefaultTimeSlot = (): boolean => {
            return $scope.timeSlots.all.filter(item => item.default).length > 0;
        };

        $scope.saveTimeProfil = async (): Promise<void> => {
            $scope.toastHttpCall(await $scope.timeSlot.save());
            syncTimeslots();
        };

        $scope.updateEndOfHalfDay = async (): Promise<void> => {
            $scope.toastHttpCall(await $scope.timeSlot.saveEndHalfDay());
        };

        $scope.$watch(() => $scope.structure, (newValue, oldValue) => {
            if (newValue !== oldValue) {
                $scope.timeSlots = new TimeSlots($scope.structure.id);
                syncTimeslots();
            }
        });
    }
]);