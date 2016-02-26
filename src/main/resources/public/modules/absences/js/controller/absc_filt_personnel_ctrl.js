/**
 * Created by ledunoiss on 19/02/2016.
 */
function AbscFiltresPersonnelController($scope, $rootScope, model, template, route, date){
    $scope.pOFilterCtrl = {
        enseignants : false,
        classes : false,
        responsables : false
    };

    $scope.switchAll = function(oListe, b){
        oListe.each(function(o){
            o.selected = b;
        });
    };
}