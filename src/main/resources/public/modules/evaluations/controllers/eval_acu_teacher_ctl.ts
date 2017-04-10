import { model, notify, http, IModel, Model, Collection, BaseModel, idiom as lang, ng, template } from 'entcore/entcore';
import {Classe, Devoir, Devoirs, DevoirsCollection, Eleve, Enseignement, Evaluation, Evaluations, Competence, CompetenceNote, evaluations, Matiere, Periode, ReleveNote, Type, SousMatiere} from '../models/eval_teacher_mdl';
import * as utils from '../utils/teacher';

let moment = require('moment');
declare let _:any;

export let evalAcuTeacherController = ng.controller('EvalAcuTeacherController', [
    '$scope', 'route', 'model',
    function ($scope, route, model) {
        // Méthode d'initialisation ou de réinitialisation du Controler : notamment lors du changement d'établissement
        $scope.initControler = function (isChangementEtablissement) {
            $scope.evaluations = evaluations;
            $scope.search = {
                matiere: '*',
                periode: undefined,
                classe: '*',
                sousmatiere: '*',
                type: '*',
                idEleve: '*',
                name: ''
            };
            $scope.chartOptions = {
                classes: {},
                options: {
                    tooltips: {
                        callbacks: {
                            label: function (tooltipItems, data) {
                                return tooltipItems.yLabel + "%";
                            }
                        }
                    },
                    scales: {
                        yAxes: [{
                            ticks: {
                                size: 0,
                                max: 100,
                                min: 0,
                                stepSize: 20,
                            },
                        }],
                        xAxes: [{
                            display: false,
                        }]
                    }
                },
                colors: ['#4bafd5', '#46bfaf', '#ecbe30', '#FF8500', '#e13a3a', '#b930a2', '#763294', '#1a22a2']
            };
            $scope.showAutocomplete = false;
            $scope.devoirsNotDone = [];
            $scope.devoirsClasses = [];

            evaluations.periodes.on('sync', function () {
                setCurrentPeriode().then((defaultPeriode) => {
                    $scope.search.periode = (defaultPeriode !== -1) ? defaultPeriode : '*';
                    utils.safeApply($scope);
                });
            });

            $scope.periodes = evaluations.periodes;
            // Récupération des structures
            $scope.structures = evaluations.structures;
            $scope.periodes.sync();


            // Changement établissement
            if (isChangementEtablissement === true) {
                $scope.classes = evaluations.classes;
            }

            /**
             * Retourne la période courante
             * @returns {Promise<T>} Promesse retournant l'identifiant de la période courante
             */
            var setCurrentPeriode = function (): Promise<any> {
                return new Promise((resolve, reject) => {
                    var formatStr = "DD/MM/YYYY";
                    var momentCurrDate = moment(moment().format(formatStr), formatStr);
                    $scope.currentPeriodeId = -1;
                    for (var i = 0; i < evaluations.periodes.all.length; i++) {
                        var momentCurrPeriodeDebut = moment(moment(evaluations.periodes.all[i].timestamp_dt).format(formatStr), formatStr);
                        var momentCurrPeriodeFin = moment(moment(evaluations.periodes.all[i].timestamp_fn).format(formatStr), formatStr);
                        if (momentCurrPeriodeDebut.diff(momentCurrDate) <= 0 && momentCurrDate.diff(momentCurrPeriodeFin) <= 0) {
                            $scope.currentPeriodeId = evaluations.periodes.all[i].id;
                            if (resolve && typeof (resolve) === 'function') {
                                resolve(evaluations.periodes.all[i]);
                            }
                        }
                    }
                    if (resolve && typeof (resolve) === 'function') {
                        resolve($scope.currentPeriodeId);
                    }
                });
            };

            $scope.getDevoirsNotDone = function (idDevoirs?) {
                return new Promise((resolve, reject) => {
                    if (!idDevoirs) {
                        idDevoirs = _.pluck(_.where(evaluations.devoirs.all, {is_evaluated: true}), 'id');
                    }
                    evaluations.devoirs.getPercentDone(idDevoirs).then(() => {
                        resolve(_.filter(evaluations.devoirs.all, (devoir) => {
                            return (devoir.percent < 100 && _.contains(idDevoirs, devoir.id));
                        }));
                    });
                });
            };

            $scope.initChartListNotDone = function () {
                $scope.getDevoirsNotDone().then((devoirs) => {
                    $scope.devoirsNotDone = devoirs;
                    $scope.devoirsClasses = _.filter(evaluations.classes.all, (classe) => {
                        return (_.find(evaluations.devoirs.all, {'id_groupe' : classe.id, is_evaluated : true}));
                    })
                    $scope.chartOptions.selectedClasse = _.first(_.sortBy(_.filter(evaluations.classes.all, (classe) => {
                        return _.contains(_.pluck(evaluations.devoirs.all, 'id_groupe'), classe.id);
                    }), 'name')).id;
                    $scope.loadChart($scope.chartOptions.selectedClasse);
                });
            }

            if (!evaluations.synchronized.devoirs) {
                evaluations.devoirs.on('sync', () => {
                    $scope.initChartListNotDone()
                });
            } else{
                $scope.initChartListNotDone();
            }
        };

        // Initialisation du Controler
        if(evaluations.structure !== undefined){
            $scope.initControler(false);
        }else{
            console.log("Aucun établissement actif pour l'utilisateur");
        }

        $scope.getDateFormated = function (date) {
            return utils.getFormatedDate(date, "DD/MM/YYYY");
        };

        $scope.loadChart = function (idClasse) {
            let idDevoirs = _.pluck(_.where(evaluations.devoirs.all, {id_groupe: idClasse}), 'id');
            $scope.getDevoirsNotDone(idDevoirs).then((devoirs) => {
                if (devoirs) {
                    $scope.chartOptions.classes[idClasse] = {
                        names: _.pluck(devoirs, 'name'),
                        percents: _.pluck(devoirs, 'percent'),
                        id: _.pluck(devoirs, 'id')
                    };
                }

                utils.safeApply($scope);
            });
            utils.safeApply($scope);
        };

        /**
         * ouvrir le suivi d'un eleve (utilisé dans la barre de recherche)
         * @param Eleve
         */
        $scope.openSuiviEleve = (Eleve) => {
            let path = '/competences/eleve';
            let idOfpath = {idEleve : Eleve.id, idClasse: Eleve.idClasse};
            $scope.goTo(path,idOfpath);
        };

        /**
        * Changement établissemnt : réinitial
        * @param Eleve
        */
        $scope.changeEtablissementAccueil = () => {
            evaluations.sync().then(()=>{
                $scope.initControler(true);
            });
        };

        /**
         * ouvrir la page de création devoir
         */
        $scope.openCreateEval = () =>{
            let path = '/devoir/create';
            $scope.goTo(path);
        };
        $scope.FilterGroupEmpty = (item) => {
            let nameofclasse = $scope.getClasseData(item.id_groupe, 'name');
            if( item.id_groupe !== '' && nameofclasse !== undefined && nameofclasse !== ''){
                return item;
            }
        };


        /**
         * Séquence de récupération d'un relevé de note
         */
        $scope.getReleve = function () {
            if($scope.search.periode !== undefined && $scope.search.periode !== '*') {
                var p = {
                    idPeriode : parseInt($scope.search.periode.id)
                };

                if(evaluations.synchronized.classes !== 0) {
                    evaluations.classes.on('classes-sync', function () {
                        var releve = new ReleveNote(p);
                        evaluations.releveNotes.push(releve);
                        $scope.releveNote = releve;
                        $scope.releveNote.sync().then(() => {
                            $scope.releveNote.synchronized.releve = true;
                            $scope.releveNote.calculStatsDevoirs().then(() => {
                                $scope.releveNote.calculMoyennesEleves().then(() => {
                                    utils.safeApply($scope);
                                });
                                utils.safeApply($scope);
                            });
                            utils.safeApply($scope);
                        });
                    });
                    return;
                }
                var releve = new ReleveNote(p);
                evaluations.releveNotes.push(releve);
                $scope.releveNote = releve;
                $scope.releveNote.sync().then(() => {
                    $scope.releveNote.synchronized.releve = true;
                    $scope.releveNote.calculStatsDevoirs().then(() => {
                        $scope.releveNote.calculMoyennesEleves().then(() => {
                            utils.safeApply($scope);
                        });
                        utils.safeApply($scope);
                    });
                    utils.safeApply($scope);
                });

                $scope.openedStudentInfo = false;
            }
        };

        /**
         * Séquence de récupération d'un relevé de note
         */
        $scope.getReleve = function () {
            if($scope.search.periode !== undefined && $scope.search.periode !== '*') {
                var p = {
                    idPeriode : parseInt($scope.search.periode.id)
                };

                if(evaluations.synchronized.classes !== 0) {
                    evaluations.classes.on('classes-sync', function () {
                        var releve = new ReleveNote(p);
                        evaluations.releveNotes.push(releve);
                        $scope.releveNote = releve;
                        $scope.releveNote.sync().then(() => {
                            $scope.releveNote.synchronized.releve = true;
                            $scope.releveNote.calculStatsDevoirs().then(() => {
                                $scope.releveNote.calculMoyennesEleves().then(() => {
                                    utils.safeApply($scope);
                                });
                                utils.safeApply($scope);
                            });
                            utils.safeApply($scope);
                        });
                    });
                    return;
                }
                var releve = new ReleveNote(p);
                evaluations.releveNotes.push(releve);
                $scope.releveNote = releve;
                $scope.releveNote.sync().then(() => {
                    $scope.releveNote.synchronized.releve = true;
                    $scope.releveNote.calculStatsDevoirs().then(() => {
                        $scope.releveNote.calculMoyennesEleves().then(() => {
                            utils.safeApply($scope);
                        });
                        utils.safeApply($scope);
                    });
                    utils.safeApply($scope);
                });
                // } else {
                //     $scope.releveNote = rn;
                //     utils.safeApply($scope);
                // }

                $scope.openedStudentInfo = false;
            }
        };
        /**
         * Return la periode scolaire courante
         * @returns {any}
         */
        $scope.periodeParDefault = function () {
            let PeriodeParD = new Date().toISOString();
            let PeriodeSet = false;
            //let  PeriodeParD = new Date().getFullYear() +"-"+ new Date().getMonth() +1 +"-" +new Date().getDate();

            for (let i = 0; i < $scope.periodes.all.length; i++) {
                if (PeriodeParD >= $scope.periodes.all[i].timestamp_dt && PeriodeParD <= $scope.periodes.all[i].timestamp_fn) {
                    PeriodeSet = true;
                    return $scope.periodes.all[i];
                }
            }
            if (PeriodeSet === false) {
                return $scope.textPeriode;
            }
        };

        $scope.filterValidClasse = () => {
            return (item) => {
                return $scope.isValidClasse(item.id_groupe);
            }
        };

        $scope.SaisieNote = (points, evt) =>{
            if(points.length>0 && points !== undefined ){
                let path = '/devoir/'+ $scope.chartOptions.classes[$scope.charts.uncomplete].id[points[0]._index];
                $scope.goTo(path);
            }

        }
    }
]);