/**
 * Created by ledunoiss on 21/09/2016.
 */
import {ng, appPrefix, idiom as lang} from 'entcore/entcore';

export let cFilAriane = ng.directive("cFilAriane", ["$location", "route", "$rootScope", "$route", function($location, routes, $rootScope, $route){
    return {
        templateUrl: "/"+appPrefix+"/public/components/ariane.html",
        restrict : "E",
        link : function($scope, element, attrs){
            $scope.ariane = [];
            $scope.ariane.push({stateName : appPrefix+".title", url : $route.routes.null.redirectTo });
            $rootScope.$on("$routeChangeSuccess", function($currentRoute, $previousRoute, $location){
                if($route.current.originalPath === $route.routes.null.redirectTo || $route.current.action === undefined){
                    $scope.ariane.splice(1, $scope.ariane.length-1);
                }else{
                    var o = _.findWhere($scope.ariane, {url : $route.current.originalPath});
                    if(o!== undefined){
                        var i = $scope.ariane.indexOf(o);
                    }else{
                        i=-1;
                    }
                    if(i !== -1){
                        $scope.ariane.splice(i+1, $scope.ariane.length-1);
                    }else{
                        var state = {
                            stateName : "ariane."+appPrefix+"."+$route.current.action,
                            url : ""
                        };
                        if(getSize($route.current.params) > 0){
                            state.url = $route.current.originalPath.replace($route.current.regexp.exec($route.current.originalPath)[1], $route.current.params[$route.current.regexp.exec($route.current.originalPath)[1].substring(1)]);
                        }else{
                            state.url = $route.current.originalPath;
                        }
                        $scope.ariane.push(state);
                    }
                }
            });

            $scope.isLast = function(state){
                return $scope.ariane.indexOf(state)+1 === $scope.ariane.length;
            };

            $scope.getI18nValue = function(i18nKey){
                return lang.translate(i18nKey);
            };

            var getSize = function(obj) {
                var size = 0, key;
                for (key in obj) {
                    if (obj.hasOwnProperty(key)) size++;
                }
                return size;
            };
        }

    };
}]);