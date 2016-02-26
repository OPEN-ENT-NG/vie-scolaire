/**
 * Created by ledunoiss on 18/02/2016.
 */
function VscoAcuPersonnelController($scope, $rootScope, model, template, route, date){
    $scope.widget = model.widget;

    $scope.formatDate = function(pODateDebut, pODateFin){
        return (moment(pODateDebut).format('DD/MM/YYYY')+" "+moment(pODateDebut).format('HH:MM')+"-"+moment(pODateFin).format('HH:MM'))
    };
}