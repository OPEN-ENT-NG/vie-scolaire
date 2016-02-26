/**
 * Created by ledunoiss on 19/02/2016.
 */
function AbscAbssmPersonnelController($scope, $rootScope, model, template, route, date){
    template.open('AbscFiltres', '../modules/absences/template/absc_personnel_filtres');
    $scope.psDisplayReponsables = true;
    $scope.pOFilterAbsences = { //Objet permettant le filtre des appels oubliés / non oubliés
        sansmotifs : true
    };
    model.evenements.sync($scope.periode.debut, $scope.periode.fin);

    /**
     * A la synchronisation des évènements, on récupères toutes les absences et le motif par défaut
     */
    model.evenements.on('sync', function(){
        model.evenements.synced = true;
        $scope.absences = $scope.evenements.where({fk_type_evt_id : 1});
        $scope.defaultMotif = $scope.motifs.first();
        initAllEvenement();
        $scope.safeApply();
    });

    model.motifs.on('sync', function(){
        model.motifs.synced = true;
        initAllEvenement();
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

    var initAllEvenement = function(){
        console.log(model.evenements);
        console.log(model.motifs);
        model.evenements.each(function(event){
            if(event.fk_type_evt_id === 1){
                $scope.initEvenement(event);
            }
        });
        $scope.safeApply();
    };

    $scope.initEvenement = function(event){
        if(event.fk_motif_id !== null){
            event.motif = model.motifs.findWhere({motif_id : event.fk_motif_id});
        }else{
            event.motif = $scope.defaultMotif;
        }
        $scope.safeApply();
    };

    $scope.applyAbsencesFilter = function(){
        $scope.absencesFilter = $scope.pOFilterAbsences.sansmotifs ? $scope.absencesFilterFunction : null;
    };

    $scope.absencesFilterFunction = function(event){
        if($scope.pOFilterAbsences.sansmotifs){
            return (event.fk_type_evt_id === 1 && event.fk_motif_id === null);
        }else{
            return true;
        }
    };

    $scope.absencesFilter = $scope.absencesFilterFunction;
}