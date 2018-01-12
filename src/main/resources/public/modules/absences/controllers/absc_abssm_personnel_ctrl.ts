import { template,  ng } from 'entcore';
import { Evenement } from '../models/absc_personnel_mdl';
import * as utils from '../utils/personnel';


let moment = require('moment');
declare let _: any;

export let abscAbssmPersonnelController = ng.controller('AbscAbssmPersonnelController', [
    '$scope', 'route', '$rootScope', '$location',
    function ($scope, route, $rootScope, $location) {
        template.open('AbscFiltres', '../templates/absences/absc_personnel_filtres');

        $scope.eleveEvts = [];

        let syncAbs = async (reload) => {
            $scope.psDisplayResponsables = false;
            $scope.pOFilterAbsences = { // Objet permettant le filtre des appels oubliés / non oubliés
                sansmotifs: true,
                limitTo: 15
            };
            $scope.pOSelectedEvent = null;
            $scope.structure.isWidget = false;
            $scope.synchronized = {
                absences: false,
                motifs: false,
            };
            $scope.eleveEvts = [];

            if (reload) {
                $scope.structure.eleves.sync().then(function () {
                    $scope.structure.evenements.sync($scope.periode.debut, $scope.periode.fin);
                });
            }
            else {
                $scope.structure.evenements.sync($scope.periode.debut, $scope.periode.fin);
            }

            $scope.structure.evenements.on('sync', async () => {
                initAllEvenement();
                utils.safeApply($scope);
            });

            // /**
            //  * A la synchronisation des évènements, on récupères toutes les absences et le motif par défaut
            //  */
            // $scope.structure.evenements.on('sync', function () {
            //     $scope.structure.absences = $scope.structure.evenements;
            //     $scope.defaultMotif = $scope.structure.motifs.first();
            //     $scope.synchronized.absences = true;
            //     callInitEvent();
            //     utils.safeApply($scope);
            // });
            //
            // $scope.structure.motifs.on('sync', function () {
            //     $scope.synchronized.motifs = true;
            //     callInitEvent();
            //     utils.safeApply($scope);
            // });
        };



        // synchronisation des absences et des motifs lors du changement de structure
        $scope.$on('reloadAbsences', function () {
            syncAbs(true);
        });

        $scope.getJourDate = function(evt) {
            return moment(evt.timestamp_dt).format('DD/MM/YYYY') + ' ' + moment(evt.timestamp_dt).format('HH:mm') + ' - ' + moment(evt.timestamp_fn).format('HH:mm');
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

        $scope.initEvenement = function (event) {
            event.motif = $scope.structure.motifs.findWhere({id : event.id_motif});
            utils.safeApply($scope);
        };

        $scope.absencesFilterFunction = function (eleve) {
            if ($scope.pOFilterAbsences.sansmotifs) {
                let t = _.where(eleve.evenements, {id_type : 1});
                if (t.length > 0) {
                    return _.where(t, {id_motif : null}).length !== 0;
                }
                return false;
            } else {
                return true;
            }
        };

        $scope.absencesNonJustifieesFilter = function (evt) {
            if ($scope.pOFilterAbsences.sansmotifs) {
                return (evt.id_type === 1 && evt.id_motif == null);
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
            }
            utils.safeApply($scope);
        };

        $scope.setEvtMotifEleve = function (eleve) {
            let t = _.where(eleve.evenements, {id_type : 1});
            if (t.length > 0) {
                _.each(t, function (evt) {
                    if(eleve.motif == null){
                        evt.motif = {id: null};
                        evt.id_motif = null;
                    }else{
                        evt.motif = eleve.motif;
                        evt.id_motif = eleve.motif.id;
                    }
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
                        return e.id_motif == null;
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

        $scope.abssmPersonnelFilter = (eleve) => {
            let isCorrect = true;
            _.each(eleve.evenements, (evts) => {
                isCorrect = isCorrect && $scope.personnelFilter(evts);
            });
            return isCorrect;
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

        // let callInitEvent = () => {
        //     if ($scope.synchronized.absences && $scope.synchronized.motifs) {
        //         initAllEvenement();
        //     }
        // };

        let initAllEvenement = () => {
            // On clear la liste des evenements.
            $scope.eleveEvts = [];

            let evtByDate = _.groupBy($scope.structure.evenements.all, (evt) => {
                return moment(evt.timestamp_dt).format('DD/MM/YYYY');
            });

            let eleveIsValid = (eleve) => {
                return eleve.firstName && eleve.lastName && !_.isEmpty(eleve.evenements);
            };

            _.each(evtByDate, (evtDate, date) => {

                let eleves = _.groupBy(evtDate, 'id_eleve');

                _.each(eleves, (evts, idEleve) => {
                    let _eleve = _.findWhere($scope.structure.eleves.all, {id: idEleve});

                    _.each(evts, (evt) => {
                        let enseignant = $scope.structure.enseignants.findWhere({id: evt.id_personnel});
                        if (enseignant !== undefined) {
                            evt.enseignant = enseignant.lastName + ' ' + enseignant.firstName;
                        }

                        let matiere = _.findWhere($scope.structure.matieres.all, {id: evt.id_matiere});
                        if (matiere !== undefined) {
                            evt.matiere = matiere.name;
                        }

                        evt.periode = moment(evt.timestamp_dt).format('HH:mm') + ' - ' + moment(evt.timestamp_fn).format('HH:mm');

                        if (evt.id_type === 1) {
                            $scope.initEvenement(evt);
                        }
                    });

                    let poEleve = {
                        firstName: _eleve.firstName,
                        lastName: _eleve.lastName,
                        id: _eleve.id,
                        cours_date: date,
                        evenements: evts,
                        displayed: false,
                    };

                    if (eleveIsValid(poEleve)) {
                        $scope.eleveEvts.push(poEleve);
                    }
                });
            });
            $scope.$emit('AbsencesLoaded');
            utils.safeApply($scope);
        };

        syncAbs(false);
        utils.safeApply($scope);
    }
]);