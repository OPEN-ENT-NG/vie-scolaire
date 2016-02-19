/**
 * Created by ledunoiss on 19/02/2016.
 */
function AbscFiltresPersonnelController($scope, $rootScope, model, template, route, date){
    $scope.switchAll = function(oListe, b){
        oListe.each(function(o){
            o.selected = b;
        });
    };
}