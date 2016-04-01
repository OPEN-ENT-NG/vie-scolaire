/**
 * Created by ledunoiss on 19/02/2016.
 */
function AbscAbssmPersonnelController($scope, $rootScope, model, template, route, date){
    template.open('AbscFiltres', '../modules/absences/template/absc_personnel_filtres');
    $scope.psDisplayReponsables = true;
    $scope.pOFilterAbsences = { //Objet permettant le filtre des appels oubliés / non oubliés
        sansmotifs : true,
        limitTo : 15
    };
    model.evenements.sync($scope.periode.debut, $scope.periode.fin);

    /**
     * A la synchronisation des évènements, on récupères toutes les absences et le motif par défaut
     */
    model.evenements.on('sync', function(){
        model.evenements.synced = true;
        // $scope.absences = $scope.evenements.where({fk_type_evt_id : 1});
        $scope.absences = $scope.evenements;
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
    };

    $scope.getPeriodeCours = function(evt){
        return moment(evt.cours_timestamp_dt)  .format('HH:mm')+' - '+moment(evt.cours_timestamp_fn).format('HH:mm');
    };

    $scope.getEnseignantNom = function(evt){
        var e = model.enseignants.findWhere({personnel_id : evt.personnel_id});
        if(e !== undefined) return (e.personnel_nom+' '+ e.personnel_prenom) ;
    };

    $scope.updateEvtMotif = function(evt){
        var e = new Evenement(evt);
        e.update(function(res){
            if(res !== undefined){
                evt.fk_motif_id = res[0].fk_motif_id;
            }
        });
    };

    var initAllEvenement = function(){
        model.evenements.each(function(e){
            _.each(e.evenements, function(evt){
                if(evt.fk_type_evt_id === 1){
                    $scope.initEvenement(evt);
                }
            });
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

    // $scope.applyAbsencesFilter = function(){
    //     $scope.absencesFilter = $scope.pOFilterAbsences.sansmotifs ? $scope.absencesFilterFunction : null;
    // };

    $scope.absencesFilterFunction = function(eleve){
        // if($scope.pOFilterAbsences.sansmotifs){
        //     return (event.fk_type_evt_id === 1 && (event.fk_motif_id === 8 || event.fk_motif_id === 2));
        // }else{
        //     return true;
        // }
        if($scope.pOFilterAbsences.sansmotifs){
            var t = _.where(eleve.evenements, {fk_type_evt_id : 1});
            if(t.length > 0){
                return (_.where(t, {fk_motif_id : 8})).length !== 0 || (_.where(t, {fk_motif_id : 2})).length !== 0;
            }
            return false;
        }else{
            return true;
        }
        // return (_.findWhere(eleve.evenements, {fk_type_evt_id : 1}) !== undefined && $scope.pOFilterAbsences.sansmotifs)
    };

    $scope.absencesNonJustifieesFilter = function(evt){
        if($scope.pOFilterAbsences.sansmotifs){
            return (evt.fk_type_evt_id === 1 && (evt.fk_motif_id === 8 || evt.fk_motif_id === 2));
        }else{
            return true;
        }
    };

    $scope.initList =function(eleve){
      $scope.$on('closeList', function(event, args){
          if(args.eleve_id !== eleve.eleve_id){
              eleve.displayed = false;
              $scope.safeApply();
          }
      });
    };

    $scope.displayList = function(eleve){
        eleve.displayed = !eleve.displayed;
        if(eleve.displayed) $scope.$broadcast('closeList', eleve);
        $scope.safeApply();
    };

    $scope.setEvtMotifEleve = function(eleve){
        var t = _.where(eleve.evenements, {fk_type_evt_id : 1});
        if (t.length > 0) {
            _.each(t, function (evt) {
                evt.motif = eleve.motif;
                var e = new Evenement(evt);
                e.update();
            });
        }
    };

    $scope.initEleveSelect = function(eleve){
        var t = _.where(eleve.evenements, {fk_type_evt_id: 1});
        if (t.length > 0) {
            var a = t;
            if ($scope.pOFilterAbsences.sansmotifs) {
                a = _.filter(t, function (e) {
                    return e.fk_motif_id === 2 || e.fk_motif_id === 8
                });
            }
            var m = a[0].fk_motif_id;
            if (_.every(a, function (evt) {
                    return evt.fk_motif_id === m;
                })) {
                eleve.motif = model.motifs.findWhere({motif_id: m});
                $scope.safeApply();
            }else{
                eleve.motif = undefined;
            }
        }
    };

    $scope.absencesFilter = function(eleve){
        var result = true;
        if(typeof $scope.absencesFilterFunction === 'function'){
            result = result && $scope.absencesFilterFunction(eleve);
        }
        var found = $scope.pORecherche.nom === undefined ;
        if($scope.pORecherche.nom !== undefined){
            for (var prop in eleve) {
                found = found || $scope.poRecherche.nom.toLowerCase().indexOf(item[prop].toLowerCase()) !== -1
            }
        }
        return result && found;
    };

    // $scope.absencesFilter = $scope.absencesFilterFunction;
}

// var found = false;
// for(var prop in item){
//     found = found || $scope.poRecherche.nom.toLowerCase().indexOf(item[prop].toLowerCase()) !== -1
// }