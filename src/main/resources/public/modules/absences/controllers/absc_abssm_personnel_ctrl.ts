import {template,  ng } from 'entcore/entcore';
import {presences,  Evenement} from '../models/absc_personnel_mdl';
import * as utils from '../utils/personnel';


let moment = require('moment');
declare let _: any;

export let abscAbssmPersonnelController = ng.controller('AbscAbssmPersonnelController', [
    '$scope', 'route', '$rootScope', '$location',
    function ($scope, route, $rootScope, $location) {
        template.open('AbscFiltres', '../templates/absences/absc_personnel_filtres');
        let syncAbs = (reload) => {
            $scope.psDisplayResponsables = false;
            $scope.pOFilterAbsences = { // Objet permettant le filtre des appels oubliés / non oubliés
                sansmotifs: true,
                limitTo: 15
            };
            $scope.pOSelectedEvent = null;
            $scope.structure.isWidget = false;
            if (reload) {
                $scope.structure.eleves.sync().then(() => {
                    $scope.structure.evenements.sync($scope.periode.debut, $scope.periode.fin);
                });
            }
            else {
                $scope.structure.evenements.sync($scope.periode.debut, $scope.periode.fin);
            }

            /**
             * A la synchronisation des évènements, on récupères toutes les absences et le motif par défaut
             */
            $scope.structure.evenements.on('sync', function () {
                // $scope.structure.evenements.synced = true;
                $scope.structure.absences = $scope.structure.evenements;
                $scope.defaultMotif = $scope.structure.motifs.first();
                $scope.synchronized.absences = true;
                initAllEvenement();
                utils.safeApply($scope);
            });

            $scope.structure.motifs.on('sync', function () {
                // $scope.structure.motifs.synced = true;
                $scope.synchronized.motifs = true;
                initAllEvenement();
                utils.safeApply($scope);
            });
        };
        syncAbs(false);

        // synchronisation des absences et des motifs lors du changement de structure
        $scope.$on('reloadAbsences', function () {
            syncAbs(true);
        });



        $scope.getJourDate = function(evt) {
            return moment(evt.timestamp_dt).format('DD/MM/YYYY') + ' ' + moment(evt.timestamp_dt).format('HH:mm') + ' - ' + moment(evt.timestamp_fn).format('HH:mm');
        };

        $scope.getPeriodeCours = function(evt) {
            return moment(evt.timestamp_dt).format('HH:mm') + ' - ' + moment(evt.timestamp_fn).format('HH:mm');
        };

        $scope.getEnseignantNom = function(evt) {
            let e = $scope.structure.enseignants.findWhere({id : evt.id_personnel});
            if (e !== undefined) { return (e.lastName + ' ' + e.firstName); }
        };

        $scope.updateEvtMotif = function(evt) {
            let e = new Evenement(evt);
            e.id_motif = evt.motif.id;
            e.update().then((res) => {
                if (res !== undefined) {
                    evt.id_motif = res[0].id_motif;
                    evt.motif = res[0];
                }
            });
        };

        let initAllEvenement = function () {
            if ($scope.synchronized.absences && $scope.synchronized.motifs ) {
                _.each($scope.structure.evenements, function (e) {
                    _.each(e.evenements, function(evt) {
                        if (evt.id_type === 1) {
                            $scope.initEvenement(evt);
                        }
                    });
                });
                $scope.$emit('AbsencesLoaded');
                utils.safeApply($scope);
            }
        };

        $scope.initEvenement = function (event) {
            if (event.id_motif !== null) {
                event.motif = $scope.structure.motifs.findWhere({id : event.id_motif});
            } else {
                event.motif = $scope.defaultMotif;
            }
            utils.safeApply($scope);
        };

        $scope.absencesFilterFunction = function (eleve) {
            if ($scope.pOFilterAbsences.sansmotifs) {
                let t = _.where(eleve.evenements, {id_type : 1});
                if (t.length > 0) {
                    return (_.where(t, {id_motif : 8})).length !== 0 || (_.where(t, {id_motif : 2})).length !== 0;
                }
                return false;
            } else {
                return true;
            }
        };

        $scope.absencesNonJustifieesFilter = function (evt) {
            if ($scope.pOFilterAbsences.sansmotifs) {
                return (evt.id_type === 1 && (evt.id_motif === 8 || evt.id_motif === 2));
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
            if (eleve.displayed) {
                $scope.$broadcast('closeList', eleve);
                $scope.pOSelectedEvent = eleve;
                $scope.psDisplayResponsables = true;
            }
            utils.safeApply($scope);
        };

        $scope.setEvtMotifEleve = function (eleve) {
            let t = _.where(eleve.evenements, {id_type : 1});
            if (t.length > 0) {
                _.each(t, function (evt) {
                    evt.motif = eleve.motif;
                    evt.id_motif = eleve.motif.id;
                    let e = new Evenement(evt);
                    e.update();
                });
            }
        };

        $scope.initEleveSelect = function (eleve) {
            let t = _.where(eleve.evenements, {id_type: 1});
            if (t.length > 0) {
                let a = t;
                if ($scope.pOFilterAbsences.sansmotifs) {
                    a = _.filter(t, function (e) {
                        return e.id_motif === 2 || e.id_motif === 8;
                    });
                }
                let m = a[0].id_motif;
                if (_.every(a, function (evt) {
                        return evt.id_motif === m;
                    })) {
                    eleve.motif = $scope.structure.motifs.findWhere({id: m});
                    eleve.id_motif = eleve.motif.id;
                    utils.safeApply($scope);
                } else {
                    eleve.motif = undefined;
                    eleve.id_motif = undefined;
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