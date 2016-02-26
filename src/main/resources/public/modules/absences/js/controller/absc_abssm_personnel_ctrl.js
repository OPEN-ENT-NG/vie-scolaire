/**
 * Created by ledunoiss on 19/02/2016.
 */
function AbscAbssmPersonnelController($scope, $rootScope, model, template, route, date){
    template.open('AbscFiltres', '../modules/absences/template/absc_personnel_filtres');
    $scope.psDisplayReponsables = true;
    model.evenements.sync($scope.periode.debut, $scope.periode.fin);
    model.evenements.on('sync', function(){
        $scope.sansMotifs = $scope.evenements.where({fk_type_evt_id : 1, fk_motif_id : null});
        $scope.defaultMotif = $scope.motifs.first();
        $scope.safeApply();
    });

    $scope.loadData = function(){
        if(($scope.periode.fin.getTime() - $scope.periode.debut.getTime()) > 0) {
            model.evenements.sync($scope.periode.debut, $scope.periode.fin);
        }
    };

    $scope.getJourDate = function(date){
        return moment(date).format('DD/MM/YYYY');
    };
    $scope.updateEvtMotif = function(evt){
        evt.update();
    };

    $scope.initEvenement = function(event){
        if(event.fk_motif_id !== null){
            event.motif = model.motifs.findWhere({motif_id : event.fk_motif_id});
        }else{
            event.motif = $scope.defaultMotif;
        }
    }
}