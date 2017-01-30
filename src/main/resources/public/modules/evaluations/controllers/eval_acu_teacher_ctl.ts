import { model, notify, http, IModel, Model, Collection, BaseModel, idiom as lang, ng, template } from 'entcore/entcore';
import {Classe, Devoir, Devoirs, DevoirsCollection, Eleve, Enseignement, Evaluation, Evaluations, Competence, CompetenceNote, evaluations, Matiere, Periode, ReleveNote, Structure, Type, SousMatiere} from '../models/eval_teacher_mdl';
import * as utils from '../utils/teacher';

let moment = require('moment');
declare let _:any;

export let evalAcuTeacherController = ng.controller('EvalAcuTeacherController', [
    '$scope', 'route', 'model',
    function ($scope, route, model) {
        $scope.evaluations = evaluations;
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
            if($scope.evaluations.devoirs.all[i].percent != 100){
                $scope.devoirs.push($scope.evaluations.devoirs.all[i]);
                utils.safeApply($scope);
            }
        }
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
            let devoirs = _.where($scope.devoirs, {id_classe : idClasse});
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

            for (let i = 0; i < $scope.classes.all.length; i++) {
                $scope.chartOptions.classes[$scope.classes.all[i].id] = {
                    names : $scope.getDevoirsInformations($scope.classes.all[i].id, 'name'),
                    percents : $scope.getDevoirsInformations($scope.classes.all[i].id, 'percent'),
                    // id :  $scope.getDevoirsInformations($scope.classes.all[i].id, 'id')
                }
            }
            $scope.$apply();
        };

        $scope.searchStudent = (student) => (student.firstName.toUpperCase().indexOf($scope.search.eleveName.toUpperCase()) !== -1
        || student.lastName.toUpperCase().indexOf($scope.search.eleveName.toUpperCase()) !== -1);

        $scope.openSuiviEleve = (Eleve) => {
            let path = '/competences/eleve';
            let idOfpath = {idEleve : Eleve.id, idClasse: Eleve.classEleve.id};
            $scope.goTo(path,idOfpath);
        };

        $scope.openCreateEval = () =>{
            let path = '/devoir/create';
            $scope.goTo(path);
        }

    }
]);