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

    $scope.getJourDate = function(evt){
        return moment(evt.cours_timestamp_dt).format('DD/MM/YYYY')+' '+moment(evt.cours_timestamp_dt).format('HH:mm')+' - '+moment(evt.cours_timestamp_fn).format('HH:mm');
      //  return moment(date).format('DD/MM/YYYY');
    };

    $scope.getEnseignantNom = function(evt){
        var e = model.enseignants.findWhere({personnel_id : evt.personnel_id});
        if(e !== undefined) return (e.personnel_nom+' '+ e.personnel_prenom) ;
    };

    $scope.updateEvtMotif = function(evt){
        evt.update(function(res){
            if(res !== undefined){
                evt.fk_motif_id = res[0].fk_motif_id;
            }
        });
    };

    var initAllEvenement = function(){
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
            return (event.fk_type_evt_id === 1 && event.fk_motif_id === 8);
        }else{
            return true;
        }
    };

    $scope.absencesFilter = $scope.absencesFilterFunction;
}