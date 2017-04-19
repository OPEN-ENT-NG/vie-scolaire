import { ng } from 'entcore/entcore';

export let offline = ng.directive('offline', () => {
    return {
        restrict : 'E',
        transclude : true,
        controller : ['$scope', '$rootScope', '$window', ($scope, $rootScope, $window) => {
            $rootScope.online = navigator.onLine;
            $window.addEventListener('offline', () => {
                $rootScope.$apply(() => {
                    $rootScope.online = false;
                    alert('OFFLINE');
                }, false);
            });
            $window.addEventListener('online', () => {
                $rootScope.$apply(() => {
                    $rootScope.online = true;
                    alert('ONLINE');
                }, false);
            }, false);
        }]
    };
});