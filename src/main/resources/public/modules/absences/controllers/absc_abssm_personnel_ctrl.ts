import {template,  ng } from 'entcore/entcore';
import {presences,  Evenement} from '../models/absc_personnel_mdl';
import * as utils from '../utils/personnel';


let moment = require('moment');
declare let _: any;

export let abscAbssmPersonnelController = ng.controller('AbscAbssmPersonnelController', [
    '$scope', 'route', '$rootScope', '$location',
    function ($scope, route, $rootScope, $location) {
        template.open('AbscFiltres', '../templates/absences/absc_personnel_filtres');
        $scope.psDisplayReponsables = true;
        $scope.pOFilterAbsences = { // Objet permettant le filtre des appels oubliés / non oubliés
            sansmotifs : true,
            limitTo : 15
        };
        presences.structure.evenements.sync($scope.periode.debut, $scope.periode.fin);

        /**
         * A la synchronisation des évènements, on récupères toutes les absences et le motif par défaut
         */
        presences.structure.evenements.on('sync', function() {
            // presences.structure.evenements.synced = true;
            $scope.absences = $scope.evenements;
            $scope.defaultMotif = $scope.motifs.first();
            initAllEvenement();
            utils.safeApply($scope);
        });

        presences.structure.motifs.on('sync', function() {
            // presences.structure.motifs.synced = true;
            initAllEvenement();
            utils.safeApply($scope);
        });

        $scope.loadData = function() {
            if (($scope.periode.fin.getTime() - $scope.periode.debut.getTime()) > 0) {
                presences.structure.evenements.sync($scope.periode.debut, $scope.periode.fin);
            }
        };

        $scope.getJourDate = function(evt) {
            return moment(evt.timestamp_dt).format('DD/MM/YYYY') + ' ' + moment(evt.timestamp_dt).format('HH:mm') + ' - ' + moment(evt.timestamp_fn).format('HH:mm');
        };

        $scope.getPeriodeCours = function(evt) {
            return moment(evt.timestamp_dt).format('HH:mm') + ' - ' + moment(evt.timestamp_fn).format('HH:mm');
        };

        $scope.getEnseignantNom = function(evt) {
            let e = presences.structure.enseignants.findWhere({id : evt.personnel_id});
            if (e !== undefined) { return (e.nom + ' ' + e.prenom); }
        };

        $scope.updateEvtMotif = function(evt) {
            let e = new Evenement(evt);
            e.update().then((res) => {
                if (res !== undefined) {
                    evt.fk_motif_id = res[0].fk_motif_id;
                }
            });
        };

        let initAllEvenement = function () {
            _.each(presences.structure.evenements, function (e) {
                _.each(e.evenements, function(evt) {
                    if (evt.fk_type_evt_id === 1) {
                        $scope.initEvenement(evt);
                    }
                });
            });
            utils.safeApply($scope);
        };

        $scope.initEvenement = function (event) {
            if (event.fk_motif_id !== null) {
                event.motif = presences.structure.motifs.findWhere({id : event.fk_motif_id});
            } else {
                event.motif = $scope.defaultMotif;
            }
            utils.safeApply($scope);
        };

        $scope.absencesFilterFunction = function (eleve) {
            if ($scope.pOFilterAbsences.sansmotifs) {
                let t = _.where(eleve.evenements, {fk_type_evt_id : 1});
                if (t.length > 0) {
                    return (_.where(t, {fk_motif_id : 8})).length !== 0 || (_.where(t, {fk_motif_id : 2})).length !== 0;
                }
                return false;
            } else {
                return true;
            }
        };

        $scope.absencesNonJustifieesFilter = function (evt) {
            if ($scope.pOFilterAbsences.sansmotifs) {
                return (evt.fk_type_evt_id === 1 && (evt.fk_motif_id === 8 || evt.fk_motif_id === 2));
            } else {
                return true;
            }
        };

        $scope.initList = function (eleve) {
            $scope.$on('closeList', function(event, args) {
                if (args.id !== eleve.id) {
                    eleve.displayed = false;
                    utils.safeApply($scope);
                }
            });
        };

        $scope.displayList = function (eleve) {
            eleve.displayed = !eleve.displayed;
            if (eleve.displayed) { $scope.$broadcast('closeList', eleve); }
            utils.safeApply($scope);
        };

        $scope.setEvtMotifEleve = function (eleve) {
            let t = _.where(eleve.evenements, {fk_type_evt_id : 1});
            if (t.length > 0) {
                _.each(t, function (evt) {
                    evt.motif = eleve.motif;
                    let e = new Evenement(evt);
                    e.update();
                });
            }
        };

        $scope.initEleveSelect = function (eleve) {
            let t = _.where(eleve.evenements, {fk_type_evt_id: 1});
            if (t.length > 0) {
                let a = t;
                if ($scope.pOFilterAbsences.sansmotifs) {
                    a = _.filter(t, function (e) {
                        return e.fk_motif_id === 2 || e.fk_motif_id === 8;
                    });
                }
                let m = a[0].fk_motif_id;
                if (_.every(a, function (evt) {
                        return evt.fk_motif_id === m;
                    })) {
                    eleve.motif = presences.structure.motifs.findWhere({motif_id: m});
                    utils.safeApply($scope);
                } else {
                    eleve.motif = undefined;
                }
            }
        };

        $scope.absencesFilter = function(eleve) {
            let result = true;
            if (typeof $scope.absencesFilterFunction === 'function') {
                result = result && $scope.absencesFilterFunction(eleve);
            }
            let found = $scope.pORecherche.nom === undefined ;
            if ($scope.pORecherche.nom !== undefined) {
                for (let prop in eleve) {
                    if (!eleve.hasOwnProperty(prop)) { continue; }
                    found = found || $scope.poRecherche.nom.toLowerCase().indexOf(eleve[prop].toLowerCase()) !== -1;
                }
            }
            return result && found;
        };
    }
]);