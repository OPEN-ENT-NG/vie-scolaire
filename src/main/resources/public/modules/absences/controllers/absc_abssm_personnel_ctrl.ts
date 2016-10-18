/**
 * Created by ledunoiss on 19/02/2016.
 */
import {notify, idiom as lang, template, routes, model, ng } from 'entcore/entcore';
import {vieScolaire, Appel, Classe, Eleve, Enseignant, Evenement, Justificatif, Matiere, Motif, Responsable, VieScolaire} from '../models/absc_personnel_mdl';

let moment = require('moment');
declare let _: any;

export let abscAbssmPersonnelController = ng.controller('AbscAbssmPersonnelController', [
    '$scope', 'route', '$rootScope', '$location',
    function ($scope, route, $rootScope, $location) {
        template.open('AbscFiltres', '../templates/absences/absc_personnel_filtres');
        $scope.psDisplayReponsables = true;
        $scope.pOFilterAbsences = { //Objet permettant le filtre des appels oubliés / non oubliés
            sansmotifs : true,
            limitTo : 15
        };
        vieScolaire.evenements.sync($scope.periode.debut, $scope.periode.fin);

        /**
         * A la synchronisation des évènements, on récupères toutes les absences et le motif par défaut
         */
        vieScolaire.evenements.on('sync', function(){
            vieScolaire.evenements.synced = true;
            $scope.absences = $scope.evenements;
            $scope.defaultMotif = $scope.motifs.first();
            initAllEvenement();
            $scope.safeApply();
        });

        vieScolaire.motifs.on('sync', function(){
            vieScolaire.motifs.synced = true;
            initAllEvenement();
            $scope.safeApply();
        });

        $scope.loadData = function(){
            if(($scope.periode.fin.getTime() - $scope.periode.debut.getTime()) > 0) {
                vieScolaire.evenements.sync($scope.periode.debut, $scope.periode.fin);
            }
        };

        $scope.getJourDate = function(evt){
            return moment(evt.timestamp_dt).format('DD/MM/YYYY')+' '+moment(evt.timestamp_dt).format('HH:mm')+' - '+moment(evt.timestamp_fn).format('HH:mm');
        };

        $scope.getPeriodeCours = function(evt){
            return moment(evt.timestamp_dt).format('HH:mm')+' - '+moment(evt.timestamp_fn).format('HH:mm');
        };

        $scope.getEnseignantNom = function(evt){
            var e = vieScolaire.enseignants.findWhere({id : evt.personnel_id});
            if(e !== undefined) return (e.nom+' '+ e.prenom) ;
        };

        $scope.updateEvtMotif = function(evt){
            var e = new Evenement(evt);
            e.update().then((res) => {
                if(res !== undefined){
                    evt.fk_motif_id = res[0].fk_motif_id;
                }
            })
        };

        var initAllEvenement = function(){
            _.each(vieScolaire.evenements, function(e){
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
                event.motif = vieScolaire.motifs.findWhere({id : event.fk_motif_id});
            }else{
                event.motif = $scope.defaultMotif;
            }
            $scope.safeApply();
        };

        $scope.absencesFilterFunction = function(eleve){
            if($scope.pOFilterAbsences.sansmotifs){
                var t = _.where(eleve.evenements, {fk_type_evt_id : 1});
                if(t.length > 0){
                    return (_.where(t, {fk_motif_id : 8})).length !== 0 || (_.where(t, {fk_motif_id : 2})).length !== 0;
                }
                return false;
            }else{
                return true;
            }
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
                if(args.id !== eleve.id){
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
                    eleve.motif = vieScolaire.motifs.findWhere({motif_id: m});
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
                    found = found || $scope.poRecherche.nom.toLowerCase().indexOf(eleve[prop].toLowerCase()) !== -1
                }
            }
            return result && found;
        };
    }
]);