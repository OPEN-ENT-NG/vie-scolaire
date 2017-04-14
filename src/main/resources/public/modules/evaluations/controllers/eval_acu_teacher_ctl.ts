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
                $scope.setCurrentPeriode().then((defaultPeriode) => {
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
                        return _.some(devoirs, (devoir) => {
                            return devoir.id_groupe == classe.id;
                        });
                    });
                    $scope.chartOptions.selectedClasse = _.first(_.sortBy($scope.devoirsClasses, 'name')).id;
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

        $scope.loadChart = function (idClasse) {
            let idDevoirs = _.pluck(_.where(evaluations.devoirs.all, {id_groupe: idClasse}), 'id');
            $scope.getDevoirsNotDone(idDevoirs).then((devoirs) => {
                if (devoirs) {
                    $scope.chartOptions.classes[idClasse] = {
                        names: _.pluck(devoirs, 'name'),
                        percents: _.pluck(devoirs, 'percent'),
                        id: _.pluck(devoirs, 'id')
                    };
                } else {
                    $scope.chartOptions.classes[idClasse] = {
                        names: [],
                        percents: [],
                        id: []
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
        //permet de basculer sur l' écran de saisie de note en cliquant sur le diagramme
        $scope.SaisieNote = (points, evt) =>{
            if(points.length>0 && points !== undefined ){
                let path = '/devoir/'+
                    $scope.chartOptions.classes[$scope.chartOptions.selectedClasse].id[points[0]._index];
                $scope.goTo(path);
            }

        }
    }
]);