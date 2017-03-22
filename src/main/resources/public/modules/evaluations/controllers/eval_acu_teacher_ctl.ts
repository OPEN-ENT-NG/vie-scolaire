import { model, notify, http, IModel, Model, Collection, BaseModel, idiom as lang, ng, template } from 'entcore/entcore';
import {Classe, Devoir, Devoirs, DevoirsCollection, Eleve, Enseignement, Evaluation, Evaluations, Competence, CompetenceNote, evaluations, Matiere, Periode, ReleveNote, Type, SousMatiere} from '../models/eval_teacher_mdl';
import * as utils from '../utils/teacher';

let moment = require('moment');
declare let _:any;

export let evalAcuTeacherController = ng.controller('EvalAcuTeacherController', [
    '$scope', 'route', 'model',
    function ($scope, route, model) {

        // Méthode d'initialisation ou de réinitialisation du Controler : notamment lors du changement d'établissement
        $scope.initControler = function () {
            $scope.evaluations = evaluations;
            $scope.search = {
                matiere: '*',
                periode : undefined,
                classe : '*',
                sousmatiere : '*',
                type : '*',
                idEleve : '*',
                name : ''
            };

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


            /**
             * Retourne la période courante
             * @returns {Promise<T>} Promesse retournant l'identifiant de la période courante
             */
            var setCurrentPeriode = function () : Promise<any> {
                return new Promise((resolve, reject) => {
                    var formatStr = "DD/MM/YYYY";
                    var momentCurrDate = moment(moment().format(formatStr), formatStr);
                    $scope.currentPeriodeId = -1;
                    for (var i = 0; i < evaluations.periodes.all.length; i++) {
                        var momentCurrPeriodeDebut = moment(moment(evaluations.periodes.all[i].timestamp_dt).format(formatStr), formatStr);
                        var momentCurrPeriodeFin = moment(moment(evaluations.periodes.all[i].timestamp_fn).format(formatStr), formatStr);
                        if(momentCurrPeriodeDebut.diff(momentCurrDate) <= 0 && momentCurrDate.diff(momentCurrPeriodeFin) <= 0) {
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

            $scope.devoirs= [];

            $scope.showAutocomplete= false;
            $scope.charts = {
                uncomplete : model.me.classes[0]
            };
            if (evaluations.synchronized.classes !== 0) {
                evaluations.classes.on('classes-sync', () => {
                    $scope.evaluations.devoirs.getPercentDone().then(() => {
                        $scope.initCharts();
                    });
                });
            } else {
                $scope.evaluations.devoirs.getPercentDone().then(() => {
                    $scope.initCharts();
                });
            }

            for(let i=0; i< $scope.evaluations.devoirs.all.length; i++){
                if($scope.evaluations.devoirs.all[i].is_evaluated && $scope.evaluations.devoirs.all[i].percent != 100){
                    $scope.devoirs.push($scope.evaluations.devoirs.all[i]);
                    utils.safeApply($scope);
                }
            }
        };

        // Initialisation du Controler
        $scope.initControler();

        $scope.getDateFormated = function (date) {
            return utils.getFormatedDate(date, "DD/MM/YYYY");
        };


        /**
         * Retourne un tableau contenant les informations des devoirs pour une classe donnée et un critère donné
         * @param idClasse identifiant de la classe
         * @param information critère
         * @returns {any[]} tableau contenant les informations
         */
        $scope.getDevoirsInformations = function (idClasse : string, information : string) : any[] {
            if (!evaluations.devoirs.percentDone) return;
            let devoirs = _.where(evaluations.devoirs.all, {id_groupe : idClasse});
            devoirs = _.filter(devoirs, devoir => (devoir.percent !== undefined && devoir.percent !== 100));
            return _.pluck(devoirs, information);
        };


        /**
         * Initialise les graphiques
         */
        $scope.initCharts = function () {
            $scope.chartOptions = {

                classes : {},

                options : {
                    tooltips: {
                        callbacks: {
                            label: function(tooltipItems, data) {
                                return tooltipItems.yLabel+"%";
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
                            display:false,
                        }]
                    }
                },
                colors : ['#4bafd5', '#46bfaf', '#ecbe30', '#FF8500', '#e13a3a', '#b930a2', '#763294', '#1a22a2']
            };
            let selectedClasse = false ;
            evaluations.devoirs.getPercentDone().then(()=>{
                $scope.classes.all = _.sortBy($scope.classes.all, 'name');
                for (let i = 0; i < $scope.classes.all.length; i++) {
                    $scope.chartOptions.classes[$scope.classes.all[i].id] = {
                        names : $scope.getDevoirsInformations($scope.classes.all[i].id, 'name'),
                        percents : $scope.getDevoirsInformations($scope.classes.all[i].id, 'percent'),
                        id :  $scope.getDevoirsInformations($scope.classes.all[i].id, 'id')
                    };
                    if($scope.chartOptions.classes[$scope.classes.all[i].id].percents.length > 0 && selectedClasse === false ){
                        $scope.charts.uncomplete = $scope.classes.all[i].id;
                        selectedClasse = true;
                    }

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
            let idOfpath = {idEleve : Eleve.id, idClasse: Eleve.classEleve.id};
            $scope.goTo(path,idOfpath);
        };

        /**
        * Changement établissemnt : réinitial
        * @param Eleve
        */
        $scope.changeEtablissement = () => {
            evaluations.sync().then(()=>{
                $scope.initControler();
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
                // } else {
                //     $scope.releveNote = rn;
                //     utils.safeApply($scope);
                // }

                $scope.openedStudentInfo = false;
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