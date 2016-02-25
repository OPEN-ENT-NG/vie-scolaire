/**
 * Created by ledunoiss on 18/02/2016.
 */
function VscoAppoPersonnelController($scope, $rootScope, model, template, route, date){
    template.open('AbscFiltres', '../modules/absences/template/absc_personnel_filtres');
    $scope.pOFilterAppel = { //Objet permettant le filtre des appels oubliés / non oubliés
        noneffectues : false
    };
    $scope.appelFilter = null;
    $scope.selectedAppels = [];
    $scope.periode.fin = new Date();
    model.appels.sync($scope.periode.debut, $scope.periode.fin);

    //$scope.appels.on('sync', function(){
    //    $scope.nonEffectues = model.appels.filter(function(appel){return appel.fk_etat_appel_id !== 3});
    //});


    $scope.formatDate = function(pODateDebut, pODateFin){
        return (moment(pODateDebut).format('DD/MM/YYYY')+" "+moment(pODateDebut).format('HH:MM')+"-"+moment(pODateFin).format('HH:MM'))
    };

    $scope.appelFilterFunction = function(appel){
        if($scope.pOFilterAppel.noneffectues){
            return (appel.fk_etat_appel_id !== 3);
        }else{
            return true;
        }
    }

    $scope.applyAppelFilter = function(){
        $scope.appelFilter = $scope.pOFilterAppel.noneffectues ? $scope.appelFilterFunction : null;
    }

    $scope.checkAppel = function(appel){
        var index = _.indexOf($scope.selectedAppels, appel);
        if( index === -1){
            $scope.selectedAppels.push(appel);
        }else{
            $scope.selectedAppels.splice(index, 1);
        }
    };

}