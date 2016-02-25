/**
 * Created by ledunoiss on 18/02/2016.
 */
function VscoAppoPersonnelController($scope, $rootScope, model, template, route, date){
    template.open('AbscFiltres', '../modules/absences/template/absc_personnel_filtres');
    $scope.periode.fin = new Date();
    model.appels.sync($scope.periode.debut, $scope.periode.fin);

    $scope.appels.on('sync', function(){
        $scope.nonEffectues = model.appels.filter(function(appel){return appel.fk_etat_appel_id !== 3});
    });

    $scope.loadData = function(){
        if(($scope.periode.fin.getTime() - $scope.periode.debut.getTime()) > 0) {
            model.appels.sync($scope.periode.debut, $scope.periode.fin);
        }
    };

    $scope.formatDate = function(pODateDebut, pODateFin){
        return (moment(pODateDebut).format('DD/MM/YYYY')+" "+moment(pODateDebut).format('HH:MM')+"-"+moment(pODateFin).format('HH:MM'))
    };

}